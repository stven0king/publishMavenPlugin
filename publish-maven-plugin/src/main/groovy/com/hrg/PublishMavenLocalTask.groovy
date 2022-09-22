package com.hrg

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.hrg.utils.*

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 校验发布mavenLocal任务
 */
class PublishMavenLocalTask extends DefaultTask {

    /**
     * 被TaskAction注解标记的，执行与gradle执行阶段的代码
     */
    @TaskAction
    void doAction() {
        println("\n")
        PublishMavenConfig publishMavenConfig = project.extensions.getByName("publishMavenConfig")
        String group = publishMavenConfig.groupId.replace(".", "/")
        String url = "${PublishMaven.mavenLocalUrl}${group}/${publishMavenConfig.artifactId}"
        ColorPrint.log url
        ColorPrint.log "\n已经发布到本地仓库~！"
        println("\n")
    }
}