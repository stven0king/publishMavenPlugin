package com.tzx.git

import com.tzx.PublishMavenConfig
import com.tzx.cmd.CmdGit
import com.tzx.utils.ColorPrint

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: TAG对于的信息
 */
class GitTagCheckUtils {
    /**
     * 展示远程仓库aar的tag信息
     * @param publishMavenConfig maven仓库的配置信息
     * @param path 项目路径
     * @return true标识当前maven的配置对于的tag版本已经存在远端
     */
    static boolean showAarRemoteInfo(PublishMavenConfig publishMavenConfig, String path) {
        def result = CmdGit.getGitTags(path)
        String aarName = publishMavenConfig.getAarPublishTagName()
        def findSuccess = false
        if (result.size() > 0) {
            ColorPrint.log "===================最近构建信息======================"
            result.forEach() {
                ColorPrint.log "${it.name}  ${it.info}  ${it.branch}  ${it.publisher}"
                findSuccess = aarName == it.name
            }
            ColorPrint.log "====================================================="
            ColorPrint.log "===================最新AAR版本信息======================"
            ColorPrint.log "发布版本：${result[result.size() - 1].name}"
            ColorPrint.log "构建人：${result[result.size() - 1].publisher}"
            ColorPrint.log "构建分支：${result[result.size() - 1].branch}"
            ColorPrint.log "版本更新信息：${result[result.size() - 1].info}"
            ColorPrint.log "====================================================="
        }
        if (findSuccess) {
            ColorPrint.logErr "你要发布的版本${publishMavenConfig.getAarPublishVersion()}已经在远程仓库存在！！！，请检查代码同步或修改发布信息配置\""
        } else {
            ColorPrint.log "你要发布的版本${publishMavenConfig.getAarPublishVersion()}不在远程仓库，可以正常发布~！"
        }
        return findSuccess
    }

    /**
     * 校验aar的版本信息是否可以进行发布
     * 快照版本和本地仓库不需要检查远端的tag信息
     * @param projectPath 项目路径
     * @param aarVersion aar的版本号
     * @param aarInfo aar的版本描述
     */
    static void aarCheck(String projectPath, PublishMavenConfig config) {
        String aarVersion = config.version
        String aarInfo = config.versionDescription
        ColorPrint.logErr "-------------"
        def gitVersion = CmdGit.getGitVersion(projectPath)
        ColorPrint.logErr gitVersion
        def gitBranch = CmdGit.getBranch(projectPath)
        ColorPrint.logErr gitBranch
        ColorPrint.logErr projectPath
        def publisher = CmdGit.getPublisher(projectPath)
        if (!publisher) {
            throw new RuntimeException("git 仓库未设置用户Email，请设置！！！")
        }
        ColorPrint.logErr publisher
        //快照版本不需要检查远端的tag信息
        if (!config.isSnapshot) {
            long aarVersionNumber = aarVersionNUmber(aarVersion)
            if (aarVersionNumber < 0) {
                ColorPrint.logErr "aarVersion=${aarVersion} ，This is not a canonical name~！"
            }
            def result = CmdGit.getGitTags(projectPath)
            if (result.size() > 0) {
                ColorPrint.log "===================最近构建信息======================"
                result.forEach() {
                    if (it.name == config.getAarPublishTagName()) {
                        throw new RuntimeException("${config.getAarPublishTagName()} 已经存在远程仓库，请检查代码更新合并或者更新aar版本信息")
                    }
                    if (!it.name.contains(PublishMavenConfig.SNAPSHOT_SUFFIX)) {
                        long tmpAarVersionNumber = aarVersionNUmber(it.name)
                        if (tmpAarVersionNumber < 0) {
                            ColorPrint.logErr "aarVersion=${aarVersion} ，This is not a canonical name~！"
                        } else {
                            if (aarVersionNumber <= tmpAarVersionNumber) {
                                ColorPrint.logErr "aarVersionNumber=${aarVersionNumber} <= aarVersionNUmber(it.name)"
                                throw new RuntimeException("新创建的版本[${config.getAarPublishTagName()}]不能比已存在${it.name}的版本小，请检查!!!")
                            }
                        }
                    } else {
                        ColorPrint.logErr "aarVersionNumber=${aarVersionNumber}, aarVersionNUmber(it.name) is SNAPSHOT~!"
                    }
                }
            }
        }
        if (!aarInfo) {
            throw new RuntimeException("请在配置文件中填写 AAR 版本更新信息！！！")
        }
        def gitStatus = CmdGit.getGitStatus(projectPath)
        if (!checkGitStatus(gitStatus)) {
            throw new RuntimeException("请提交本地未提交的代码，保持代码和远程仓库一致！！！")
        }
        def branchShaCheck = CmdGit.checkBranchSha(projectPath, gitBranch)
        if (!branchShaCheck) {
            throw new RuntimeException("请同步本地仓库，保持代码和远程仓库一致！！！")
        }
        ColorPrint.log "all checked, begin upload aar"
    }

    /**
     * 判断当前的git状态是否和服务端同步
     * @param msgs git 命令结果
     * @return true标识和服务端同步
     */
    static Boolean checkGitStatus(List<String> msgs) {
        for (int i = 0; i < msgs.size(); i++) {
            String it = msgs.get(i)
            if (!it.trim()) {
                continue
            }
            if (it.contains("Changes to be committed")) {
                return false
            }
            if (it.contains("Changes not staged for commit")) {
                return false
            }
            if (it.contains("Your branch is ahead")) {
                return false
            }
            if (it.contains("Untracked files")) {
                return false
            }
        }
        return true
    }

    /**
     * aar_1.0.3或者1.0.3这样的版本号转化为数字版本号，用于比较版本号的大小
     * @param aarVersion 字符串类型的版本号
     * @return 数据化版本号，用于比较大小
     */
    static long aarVersionNUmber(String aarVersion) {
        try {
            //快照格式的版本不做处理
            if (aarVersion.contains(PublishMavenConfig.SNAPSHOT_SUFFIX)) {
                return 0
            }
            String version = aarVersion
            if (aarVersion.startsWith("aar_")) {
                version = aarVersion.substring("aar_".size(), aarVersion.size())
            }
            def versions = version.split("\\.")
            if (versions.length == 4) {
                return 10000 * parseInt(versions[0]) + 1000 * parseInt(versions[1]) + 100 * parseInt(versions[2]) + 10 * parseInt(versions[3]);
            }
            if (versions.length == 3) {
                return 10000 * parseInt(versions[0]) + 1000 * parseInt(versions[1]) + 100 * parseInt(versions[2]);
            }
            if (versions.length == 2) {
                return 10000 * parseInt(versions[0]) + 1000 * parseInt(versions[1]);
            }
        } catch (Exception e) {
            e.printStackTrace()
            return -1
        }
        return 0
    }

    static int parseInt(String s) {
        return Integer.parseInt(s)
    }
}