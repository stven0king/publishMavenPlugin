package com.tzx.cmd

import com.tzx.git.GitTagResult

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 执行git命令操作
 */
final class CmdGit extends RuntimeCmd{
    static final String GIT_FETCH_TAG = "git fetch --tags"
    static final String GIT_GET_TAGS = "git tag -n"
    static final String GIT_BRANCH = "git rev-parse --abbrev-ref HEAD"
    static final String GIT_PUBLISHER = "git config user.email"
    static final String GIT_VERSION = "git --version"
    static final String Git_REMOTE_AARS = "git ls-remote --tags -q |cut -d \"/\" -f 3 | grep aar_ | tr -d \"^{}\" | uniq"
    static final String GIT_STATUS = "git status"
    static final String GIT_REMOTE_SHA = "git rev-parse --short origin/"
    static final String GIT_LOCAL_SHA = "git rev-parse --short "
    static final String GIT_ADD_FILE = "git add "
    static final String GIT_COMMIT_MSG = "git commit -m "
    static final String GIT_PUSH_ORIGIN = "git push origin "
    static final String GIT_PUSH_TAG = "git tag -a %s -m %s"
    static List<GitTagResult> getGitTags(String path) {
        def gitFetch = runShell(GIT_FETCH_TAG, path)
        if (!gitFetch.isSuccess()) {
            throw new RuntimeException("${gitFetch.error}")
        }
        def gitTags = runShell(GIT_GET_TAGS, path)
        if (!gitTags.isSuccess()) {
            throw new RuntimeException("${gitTags.error}")
        }
        List<GitTagResult> result = new ArrayList<>()
        gitTags.success.forEach() {
            Pattern pattern = Pattern.compile(GitTagResult._REGEX)
            Matcher matcher = pattern.matcher(it)
            if (matcher.matches()) {
                GitTagResult tagResult = new GitTagResult()
                tagResult.name = matcher.group(1)
                tagResult.info = matcher.group(2)
                tagResult.branch = matcher.group(3)
                tagResult.publisher = matcher.group(4)
                result.add(tagResult)
            }
        }
        return result
    }

    /**
     * 获取当前项目的git分支
     * @param projectPath 项目地址
     * @return git分支名称
     */
    static String getBranch(String projectPath) {
        def result = runShell(GIT_BRANCH, projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
        if (result.success.size() == 1) {
            return result.success[0]
        }
        throw new RuntimeException("${result.success}")
    }

    /**
     * 获取当前项目的git发布人信息
     * @param projectPath 项目地址
     * @return git发布人信息
     */
    static String getPublisher(String projectPath) {
        def result = runShell(GIT_PUBLISHER, projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
        if (result.success.size() == 1) {
            return result.success[0]
        }
        throw new RuntimeException("${result.success}")
    }

    /**
     * 获取当前项目的git版本信息
     * @param projectPath 项目地址
     * @return git版本信息
     */
    static String getGitVersion(String projectPath) {
        def result = runShell(GIT_VERSION, projectPath)
            if (!result.isSuccess()) {
                throw new RuntimeException("${result.error}")
            }
            if (result.success.size() == 1) {
                return result.success[0]
        }
        throw new RuntimeException("${result.success}")
    }
    /**
     * 获取当前项目的git版本信息
     * @param projectPath 项目地址
     * @return git版本信息
     */
    static List<String> getGitStatus(String projectPath) {
        def result = runShell(GIT_STATUS, projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
        return result.success
    }

    static String checkBranchSha(String projectPath, String branch) {
        String localBranchShaCmd = "${GIT_LOCAL_SHA}${branch}"
        def result = runShell(localBranchShaCmd, projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
        if (result.success.size() != 1) {
            throw new RuntimeException("${result.success}")
        }
        String remoteBranchSha = "${GIT_REMOTE_SHA}${branch}"
        def result2 = runShell(remoteBranchSha, projectPath)
        if (!result2.isSuccess()) {
            throw new RuntimeException("${result2.error}")
        }
        if (result2.success.size() != 1) {
            throw new RuntimeException("${result2.success}")
        }
        return result.success[0] == result2.success[0]
    }

    static void gitAddFile(String projectPath, String fileName) {
        def result = runShell("${GIT_ADD_FILE}${fileName}", projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
    }

    static void gitCommitMessage(String projectPath, String msg) {
        def result = runShell("${GIT_COMMIT_MSG}${msg}", projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
    }

    static void gitPushOrigin(String projectPath, String tagOrBranch) {
        def result = runShell("${GIT_PUSH_ORIGIN}${tagOrBranch}", projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
    }

    static void gitPushTag(String projectPath, String tagName, String tagMessage) {
        def result = runShell(String.format(GIT_PUSH_TAG, tagName, tagMessage), projectPath)
        if (!result.isSuccess()) {
            throw new RuntimeException("${result.error}")
        }
    }
}