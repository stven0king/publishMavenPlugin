package com.hrg

import com.android.build.gradle.AndroidConfig
import com.hrg.cmd.CmdGit
import com.hrg.utils.ColorPrint
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 发布maven仓库，版本校验和版本信息维护
 */
class PublishMaven implements Plugin<Project> {
    def publishConfig = "publishMavenConfig"
    def PUBLISH_MAVEN_GROUP = "Uploader"
    static String mavenLocalUrl = ""
    @Override
    void apply(Project project) {
        def publishingConfig = project.extensions.create(publishConfig, PublishMavenConfig)
        if (project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('dependencies publish maven plugin should library~!~!')
        }
        boolean isPlugin = true
        def androidSourcesJar
        if (project.plugins.hasPlugin("com.android.library")) {
            isPlugin = false
            // 获取 Android 配置信息（包括了应用和库）
            AndroidConfig androidConfig = project.getExtensions().getByType(AndroidConfig)
            def javaSourceFiles = androidConfig.sourceSets.getByName("main").java.srcDirs
            androidSourcesJar = project.tasks.create("androidSourcesJar", Jar.class, {configuration ->
                getArchiveClassifier().set("sources")
                from(javaSourceFiles)
            })
            androidSourcesJar.setGroup(PUBLISH_MAVEN_GROUP)
        }
        project.plugins.apply MavenPublishPlugin
        PublishingExtension publishing = project.extensions.getByType(PublishingExtension)
        project.afterEvaluate {
            publishing.publications({ publications ->
                publications.create(PUBLISH_MAVEN_GROUP, MavenPublication.class, { MavenPublication publication ->
                    publication.groupId = publishingConfig.groupId
                    publication.artifactId = publishingConfig.artifactId
                    publication.version = publishingConfig.getAarPublishVersion()
                    if (!isPlugin) {
                        project.afterEvaluate {
                            publication.artifact(project.tasks.getByName("bundleReleaseAar"))
                        }
                        if (publishingConfig.publishSource) {
                            publication.artifact(androidSourcesJar)
                        }
                        publication.pom.withXml {
                            def dependenciesNode = asNode().appendNode('dependencies')
                            def scopes = []
                            //compileOnly包含provided
                            if (project.configurations.hasProperty("compileOnly")) {
                                scopes.add(project.configurations.compileOnly)
                            }
                            //implementation包含api
                            if (project.configurations.hasProperty("implementation")) {
                                scopes.add(project.configurations.implementation)
                            }
                            //debugImplementation包含debugCompile
                            if (project.configurations.hasProperty("debugImplementation")) {
                                scopes.add(project.configurations.debugImplementation)
                            }
                            //releaseImplementation包含releaseCompile
                            if (project.configurations.hasProperty("releaseImplementation")) {
                                scopes.add(project.configurations.releaseImplementation)
                            }
                            scopes.each { scope ->
                                scope.allDependencies.each {
                                    //如果发现有工程源码依赖，那么打包失败
                                    if (it instanceof DefaultProjectDependency) {
                                        DefaultProjectDependency projectDependency = (DefaultProjectDependency)it
                                        Project dependencyProject = projectDependency.getDependencyProject()
                                        throw new IllegalStateException(String.format("Build for publishMaven must dependency archives(jar or aar), but has a Project:%s", dependencyProject.getName()))
                                    }
                                    //如果设置禁止依赖传递，那么该依赖不进行写入pom文件
                                    if (it instanceof ModuleDependency) {
                                        ModuleDependency moduleDependency = ((ModuleDependency) it)
                                        boolean isTransitive = moduleDependency.transitive
                                        if (!isTransitive) {
                                            println "<<<< not transitive dependency: [${it.group}, ${it.name}, ${it.version}]"
                                            return
                                        }
                                    }
                                    //如果checkDependenceSnapShot=true，需要校验依赖是否包含快照版本
                                    if (publishingConfig.checkDependenceSnapShot && it.version != null) {
                                        String version = it.version
                                        if (version.contains("SNAPSHOT")) {
                                            String groupId = it.group == null ? "" : it.group
                                            String artifactId = it.name == null ? "" : it.name
                                            throw new IllegalStateException(String.format("Build for publishMaven must dependency release archives(jar or aar), but has a SNAPSHOT:%s:%s:%s", groupId, artifactId, version))
                                        }
                                    }
                                    if (it.group == null || it.group == "unspecified"
                                            || it.version == null || it.version == 'unspecified'
                                            || it.name == null || it.name == 'unspecified') {
                                        println "<<< dependencies " + it.toString() + "  scope=" + scope.toString()
                                    } else {
                                        def dependencyNode = dependenciesNode.appendNode('dependency')
                                        dependencyNode.appendNode('groupId', it.group)
                                        dependencyNode.appendNode('artifactId', it.name)
                                        dependencyNode.appendNode('version', it.version)
                                        dependencyNode.appendNode('scope', scope.name)
                                    }
                                }
                            }
                        }
                    } else {
                        publication.from(project.components.find())
                    }
                    publication.pom {
                        mavenPom -> configPom(mavenPom, publishingConfig)
                    }
                })
            })
            publishing.repositories { artifactRepositories ->
                artifactRepositories.mavenLocal()
                mavenLocalUrl = artifactRepositories.mavenLocal().url.toString()
                artifactRepositories.maven { mavenArtifactRepository ->
                    mavenArtifactRepository.url = publishingConfig.repoUrl
                    mavenArtifactRepository.credentials {
                        username = publishingConfig.repoName
                        password = publishingConfig.repoPassword
                    }
                }
            }

            def showAarRemoteInfo = project.tasks.create("showSdkRemoteInfo", ShowSdkRemoteInfoTask)
            showAarRemoteInfo.setGroup(PUBLISH_MAVEN_GROUP)

            def aarCheck = project.tasks.create("sdkCheck", PublishSdkCheckTask)
            aarCheck.setGroup(PUBLISH_MAVEN_GROUP)

            def recordAarPublishInfo = project.tasks.create("recordSdkPublishInfo", RecordSdkPublishInfoTask)
            recordAarPublishInfo.setGroup(PUBLISH_MAVEN_GROUP)

            Task publishRepositoryTask = project.tasks.findByName("publish${PUBLISH_MAVEN_GROUP}PublicationToMavenRepository")
            publishRepositoryTask.dependsOn(aarCheck)

            Task publishLocalTask = project.tasks.findByName("publish${PUBLISH_MAVEN_GROUP}PublicationToMavenLocal")

            def publishMavenRepository = project.tasks.create("publishMavenRepository", PublishMavenRepositoryTask)
            publishMavenRepository.setGroup(PUBLISH_MAVEN_GROUP)
            publishMavenRepository.dependsOn(publishRepositoryTask)
            publishMavenRepository.doLast {
                recordAarPublishInfo.doAction()
            }

            def publishMavenLocal = project.tasks.create("publishMavenLocal", PublishMavenLocalTask)
            publishMavenLocal.setGroup(PUBLISH_MAVEN_GROUP)
            publishMavenLocal.dependsOn(publishLocalTask)

            def publishMavenConfigCheck = project.tasks.create("publishMavenConfigCheck", PublishMavenConfigCheckTask)
            publishMavenConfigCheck.setGroup(PUBLISH_MAVEN_GROUP)

            publishRepositoryTask.dependsOn(publishMavenConfigCheck)
            publishLocalTask.dependsOn(publishMavenConfigCheck)
        }
    }

    private static void configPom(MavenPom mavenPom, PublishMavenConfig config) {
        mavenPom.name = config.pomName
        mavenPom.description = config.pomDescription
        mavenPom.url = config.pomUrl
    }
}
