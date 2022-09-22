package com.tzx

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 校验发布maven仓库配置
 */
class PublishMavenConfigCheckTask extends DefaultTask {

    /**
     * 被TaskAction注解标记的，执行与gradle执行阶段的代码
     */
    @TaskAction
    void doAction() {
        def publishingConfig = project.extensions.publishMavenConfig
        if (!publishingConfig.groupId ||
                !publishingConfig.artifactId ||
                !publishingConfig.version) {
            throw new GradleException('publish info groupId,artifactId,version is empty~!')
        }
        if (!publishingConfig.repoUrl) {
            throw new GradleException('publish release repoUrl isn\'t match url~!~!')
        }
        println "PublishMavenConfigCheckTask run success~!"
    }
}