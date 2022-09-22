package com.hrg.utils

/**
 * Created by Tanzhenxing
 * Date: 2022/5/16 11:34
 * Description: TAG对于的信息
 */
class ColorPrint {
   static String ANSI_RESET = "\u001B[0m";
   static String ANSI_BLACK = "\u001B[30m";
   static String ANSI_RED = "\u001B[31m";
   static String ANSI_GREEN = "\u001B[32m";
   static String ANSI_YELLOW = "\u001B[33m";
   static String ANSI_BLUE = "\u001B[34m";
   static String ANSI_PURPLE = "\u001B[35m";
   static String ANSI_CYAN = "\u001B[36m";
   static String ANSI_WHITE = "\u001B[37m";

    static void log(String s) {
        println "\033[0;32m${s}\033[0m"
    }

    static void logErr(String s) {
        println "\033[0;31m${s}\033[0m"
    }
}