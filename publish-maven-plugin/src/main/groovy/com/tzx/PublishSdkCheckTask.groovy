package com.tzx


import com.hrg.git.GitTagCheckUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 校验当前配置是否能够打AAR包
 */
class PublishSdkCheckTask extends DefaultTask {
    /**
     * 被TaskAction注解标记的，执行与gradle执行阶段的代码
     */
    @TaskAction
    void doAction() {
        println("\n\n")
        PublishMavenConfig publishMavenConfig = project.extensions.getByName("publishMavenConfig")
        GitTagCheckUtils.aarCheck(project.projectDir.absolutePath, publishMavenConfig)
        println("\n\n")
    }
}