package com.chleon.telematics;

import android.util.Log;

public class MyLog {

    private final static String TAG = "DataCollection";

    public static void d(String tag, String log) {
        Log.d(TAG, buildMessage(log));
    }

    public static void i(String tag, String log) {
        Log.i(TAG, buildMessage(log));
    }

    public static void w(String tag, String log) {
        Log.w(TAG, buildMessage(log));
    }

    public static void w(String tag, String log, Throwable e) {
        Log.w(TAG, buildMessage(log), e);
    }

    public static void e(String tag, String log) {
        Log.e(TAG, buildMessage(log));
    }

    public static void e(String tag, String log, Throwable e) {
        Log.e(TAG, buildMessage(log), e);
    }


    /**
     * 构建当前的log日志信息
     *
     * @param rawMessage 当前的输出信息
     * @return 输出构建后的信息
     */
    private static String buildMessage(String rawMessage) {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String fullClassName = caller.getClassName();
        int lineNumber = caller.getLineNumber();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        StringBuffer sb = new StringBuffer();
        sb.append(className).append(".")
                .append(caller.getMethodName()).append("()")
                .append("<").append(lineNumber).append(">:")
                .append(rawMessage);
        return sb.toString();
    }
}
