package com.tzx

import com.hrg.cmd.CmdGit
import com.hrg.utils.ColorPrint
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.text.SimpleDateFormat

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 记录发布aar的信息
 */
class RecordSdkPublishInfoTask extends DefaultTask {

    /**
     * 被TaskAction注解标记的，执行与gradle执行阶段的代码
     */
    @TaskAction
    void doAction() {
        println("\n")
        println("recordForAArPublish begin")
        PublishMavenConfig publishingConfig = project.extensions.getByName("publishMavenConfig")
        if ((publishingConfig.publishDocument && !publishingConfig.isSnapshot)
                || (publishingConfig.isSnapshot && publishingConfig.publishSnapshotDocument)) {
            //发版记录写入md文件
            try {
                recordMarkDownFile(publishingConfig, project.projectDir.absolutePath)
            } catch (Exception e) {
                throw new RuntimeException(e)
            }
            //提交md文件，push到远端
            CmdGit.gitAddFile(project.projectDir.absolutePath, publishingConfig.recordFileName)
            ColorPrint.log "git add ${publishingConfig.recordFileName} file~!"
            CmdGit.gitCommitMessage(project.projectDir.absolutePath, "\"MOD: update aar version record file\"")
            ColorPrint.log "git commit -m \"MOD: update aar version record file\""
            String branch = CmdGit.getBranch(project.projectDir.absolutePath)
            CmdGit.gitPushOrigin(project.projectDir.absolutePath, branch)
            ColorPrint.log "git push origin ${branch}"
            //创建对于的tag，push到远端
            String publisher = CmdGit.getPublisher(project.projectDir.absolutePath)
            String tagMsg = "\"[info]:${publishingConfig.versionDescription}[branch]:${branch}[publisher]:${publisher}\""
            CmdGit.gitPushTag(project.projectDir.absolutePath, publishingConfig.getAarPublishTagName(), tagMsg)
            ColorPrint.log "git tag -a ${publishingConfig.getAarPublishTagName()} -m ${tagMsg}"
            CmdGit.gitPushOrigin(project.projectDir.absolutePath, publishingConfig.getAarPublishTagName())
            ColorPrint.log "git push origin ${publishingConfig.getAarPublishTagName()}"
        }
        println("recordForAArPublish end")
        println("\n")
    }

    private void recordMarkDownFile(PublishMavenConfig config, String projectPath) {
        String aarVersion = config.version
        String appVersion = config.appVersion
        String aarInfo = config.versionDescription
        String aarName = config.artifactId
        String tagName = config.getAarPublishTagName()
        String recordFileName = config.recordFileName
        if (!recordFileName) {
            recordFileName = "README.md"
        }
        String aarBranch = CmdGit.getBranch(projectPath)
        String publisher = CmdGit.getPublisher(projectPath)
        if (!publisher) {
            throw new RuntimeException("git 仓库未设置用户Email，请设置！！！")
        }
        String publishTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())).format(new Date())
        File recordFile = new File(project.projectDir, recordFileName)
        if (recordFile.exists() && !recordFile.isFile()) {
            throw new RuntimeException("recordFile not a File!!! path -->" + recordFile.getAbsolutePath());
        }
        if (!recordFile.exists() || recordFile.size() == 0) {
            FileWriter writer = new FileWriter(recordFile);
            writer.write(String.format("# %s AAR版本更新记录", [aarName]));
            writer.write("\n\n");
            writer.write("> <font color=red>**本记录由脚本自动生成，请勿修改**\n");
            writer.write("\n");
            writer.flush();
            writer.close();
        }
        RandomAccessFileWrap accessFile = new RandomAccessFileWrap(recordFile, "rw");
        StringBuilder sb = new StringBuilder();
        sb.append("| AAR版本| ").append(aarVersion).append(" |").append("\n");
        sb.append("| ---- | ---- |").append("\n");
        sb.append("| 更新说明 | ").append(aarInfo).append(" |").append("\n");
        sb.append("| APP版本 | ").append(appVersion).append(" |").append("\n");
        sb.append("| 构建分支 | ").append(aarBranch).append(" |").append("\n");
        sb.append("| 构建tag | ").append(tagName).append(" |").append("\n");
        sb.append("| 发布人 | ").append(publisher).append(" |").append("\n");
        sb.append("| 发布时间 | ").append(publishTime).append(" |").append("\n");
        sb.append("\n");
        sb.append("-----------------------------------\n");
        sb.append("\n");
        accessFile.insertAfterLine(4, sb.toString());
        accessFile.close();
    }

    static class RandomAccessFileWrap extends RandomAccessFile {
        private final String lineSeparator = "\n";
        RandomAccessFileWrap(String name, String mode) throws FileNotFoundException {
            super(name, mode);
        }
        RandomAccessFileWrap(File file, String mode) throws FileNotFoundException {
            super(file, mode);
        }
        boolean seekLine(int lineNum) throws IOException {
            if (lineNum < 0) {
                throw new IOException("Negative seekLine lineNum must be not negative!!!");
            }
            seek(0L);
            boolean suc = true;
            for (int i = 0; i < lineNum; i++) {
                int lineCharCount = nextLine();
                if (lineCharCount == -1) {
                    suc = false;
                    break;
                }
            }
            return suc;
        }
        int nextLine() throws IOException {
            int c = -1;
            boolean eol = false;
            int count = 0;
            while (!eol) {
                long cur;
                switch (c = read()) {
                    case -1:
                    case 10:
                        eol = true;
                        continue;
                    case 13:
                        eol = true;
                        cur = getFilePointer();
                        if (read() != 10)
                            seek(cur);
                        continue;
                }
                count++;
            }
            if (c == -1 && count == 0) {
                return -1;
            }
            return count;
        }
        void newLine() throws IOException {
            write("\n".getBytes());
        }
        void writeLine(String line) throws IOException {
            write((line + "\n").getBytes());
        }
        void insertAfterLine(int lineNum, String content) throws IOException {
            if (!seekLine(lineNum)) {
                throw new IOException("Negative seekLine lineNum Failure!!!");
            }
            long insertAt = getFilePointer();
            insert(insertAt, content);
        }
        void insert(long insertAt, String content) throws IOException {
            File tempFile = File.createTempFile("_temp", null);
            tempFile.deleteOnExit();
            FileOutputStream tempOut = new FileOutputStream(tempFile);
            FileInputStream tempIn = new FileInputStream(tempFile);
            seek(insertAt);
            byte[] buf = new byte[1024];
            int readCount = 0;
            while ((readCount = read(buf)) > 0) {
                tempOut.write(buf, 0, readCount);
            }
            seek(insertAt);
            write(content.getBytes());
            while ((readCount = tempIn.read(buf)) > 0) {
                write(buf, 0, readCount);
            }
            tempIn.close();
            tempOut.close();
        }
    }
}