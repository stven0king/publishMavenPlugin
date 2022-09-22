package com.tzx;

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 发布需要的数据模型
 */
class PublishMavenConfig {
    static final String SNAPSHOT_SUFFIX =  "SNAPSHOT"
    /**
     * 是否为快照版本
     */
    Boolean isSnapshot = false
    /**
     * 是否同时发布源码
     */
    Boolean publishSource = true
    /**
     * 发布正式版本时是否记录md文档
     */
    Boolean publishDocument = true
    /**
     * 发布快照时是否记录md文档
     */
    Boolean publishSnapshotDocument = false

    /**
     * 是否检测依赖项有快照
     */
    Boolean checkDependenceSnapShot = true;

    String groupId = ""
    String artifactId = ""
    String version = ""

    String pomName = ""
    String pomDescription = ""
    String pomUrl = ""

    String repoUrl = ""
    String repoName = ""
    String repoPassword = ""

    /**
     * 插件自动写入发布版本记录文件名
     */
    String recordFileName = ""
    /**
     * 当前sdk版本的描述
     */
    String versionDescription = ""
    /**
     * 当前sdk对应的app版本号
     */
    String appVersion = ""
    /**
     * tag名称
     */
    private String aarTagName = ""

    String getAarPublishVersion() {
        return isSnapshot ? "${version}-${SNAPSHOT_SUFFIX}" : version
    }

    String getAarPublishTagName() {
        if (aarTagName == "") {
            if (isSnapshot) {
                def date = new Date()
                def year = date.getYear() + 1900
                def month = date.getMonth() + 1
                def day = date.getDate()
                def hour = date.getHours()
                def minute = date.getMinutes()
                def second = date.getSeconds()
                def time = "${year}${month}${day}${hour}${minute}${second}"
                aarTagName = "aar_${version}_${SNAPSHOT_SUFFIX}_${time}"
            } else {
                aarTagName = "aar_${version}"
            }
        }
        return aarTagName;
    }
}
