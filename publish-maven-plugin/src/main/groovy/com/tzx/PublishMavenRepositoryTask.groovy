package com.tzx

import com.hrg.utils.ColorPrint
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 校验发布mavenRepository任务
 */
class PublishMavenRepositoryTask extends DefaultTask {

    /**
     * 被TaskAction注解标记的，执行与gradle执行阶段的代码
     */
    @TaskAction
    void doAction() {
        println("\n")
        PublishMavenConfig publishMavenConfig = project.extensions.getByName("publishMavenConfig")
        String group = publishMavenConfig.groupId.replace("\\", "/")
        String url = "${publishMavenConfig.repoUrl}${group}/${publishMavenConfig.artifactId}"
        ColorPrint.log url
        ColorPrint.log "\n已经发布到远程仓库~！"
        println("\n")
    }
}