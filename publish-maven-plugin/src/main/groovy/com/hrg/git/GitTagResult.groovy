package com.hrg.git

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: TAG对于的信息
 */
class GitTagResult {
    static String _REGEX = '^(\\S*)\\s*\\[info\\]:(.*)\\[branch\\]:(.*)\\[publisher\\]:(.*)$'
    /**
     * tag名称
     */
    String name
    /**
     * tag描述
     */
    String info
    /**
     * tag对应的分支
     */
    String branch
    /**
     * tag的发布者
     */
    String publisher
}