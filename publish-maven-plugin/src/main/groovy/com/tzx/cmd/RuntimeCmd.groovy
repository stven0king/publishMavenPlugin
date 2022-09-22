package com.tzx.cmd

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: 执行git命令操作
 */
class RuntimeCmd {
    /**
     * 运行shell并获得结果，注意：如果sh中含有awk,一定要按new String[]{"/bin/sh","-c",shStr}写,才可以获得流
     *
     * @param shStr 需要执行的shell
     * @return
     */
    static CmdResult runShell(String shStr, String dir) {
        List<String> success = new ArrayList<String>();
        List<String> error = new ArrayList<String>();
        String line;
        int resultCode;
        try {
            String[] cmd = ["/bin/sh", "-c", shStr]
            Process process = Runtime.getRuntime().exec(cmd, null, new File(dir))
            InputStreamReader inputError = new InputStreamReader(process.getErrorStream());
            InputStreamReader inputResult = new InputStreamReader(process.getInputStream());
            LineNumberReader readerError = new LineNumberReader(inputError);
            LineNumberReader readerResult = new LineNumberReader(inputResult);
            resultCode = process.waitFor();
            while ((line = readerResult.readLine()) != null) {
                success.add(line);
            }
            while ((line = readerError.readLine()) != null) {
                error.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
        return new CmdResult(success, error, resultCode);
    }
}