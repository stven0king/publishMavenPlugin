package com.hrg

import com.hrg.cmd.CmdGit
import com.hrg.git.GitTagCheckUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 展示远程aar的信息
 */
class ShowSdkRemoteInfoTask extends DefaultTask {
    /**
     * 被TaskAction注解标记的，执行与gradle执行阶段的代码
     */
    @TaskAction
    void doAction() {
        println("\n\n")
        PublishMavenConfig publishMavenConfig = project.extensions.getByName("publishMavenConfig")
        GitTagCheckUtils.showAarRemoteInfo(publishMavenConfig, project.projectDir.absolutePath)
        println("\n\n")
    }
}