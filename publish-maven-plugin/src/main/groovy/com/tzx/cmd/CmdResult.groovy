package com.tzx.cmd

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: Runtime执行的结果
 */
class CmdResult {
    int resultCode = -1
    CmdResult(List<String> success, List<String> error, int resultCode) {
        this.success = success
        this.error = error
        this.resultCode = resultCode
    }
    List<String> success = new ArrayList<String>()
    List<String> error = new ArrayList<String>()
    Boolean isSuccess() {
        return resultCode == 0
    }
}