package com.levin.commons.dao.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


/**
 * 异常处理工具类
 * 模块名称：
 * 功能说明：
 */

public abstract class ExceptionUtils {

    /**
     * 返回调用这个方法的堆栈追踪信息
     *
     * @return
     */
    public static StackTraceElement getInvokeThisMethodStackTrace() {
        return new Exception().getStackTrace()[1];
    }

    /**
     * 返回调用这个方法的堆栈追踪信息
     *
     * @return
     */
    public static String getInvokeMethodName() {
        return new Exception().getStackTrace()[1].getMethodName();
    }

    public static String getPrintInfo(Throwable e) {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream(256);
        e.printStackTrace(new PrintStream(tempOut));
        return new String(tempOut.toByteArray());
    }

    /**
     * 获取异常的全部消息
     *
     * @param e
     * @param delim
     * @param excludeTypes 忽略的异常信息
     * @return
     */
    public static String getAllCauseInfo(Throwable e, String delim, Class<? extends Throwable>... excludeTypes) {

        StringBuilder sb = new StringBuilder();

        if (delim == null)
            delim = "";

        while (e != null) {

            boolean isAppend = true;

            if (excludeTypes != null) {
                for (Class<? extends Throwable> exType : excludeTypes) {
                    if (exType.isInstance(e)) {
                        isAppend = false;
                        break;
                    }
                }
            }

            if (isAppend)
                sb.append("[" + e + "]");

            e = e.getCause();

            if (isAppend && e != null)
                sb.append(delim);
        }

        return sb.toString();
    }

    /**
     * 获取根异常的消息
     *
     * @param ex
     * @return
     */
    public static Throwable getRootCause(Throwable ex) {

        Throwable result = ex;

        while (ex != null) {
            result = ex;
            ex = ex.getCause();
        }

        return result;
    }


    public static <EX extends Throwable> EX getCauseByStartsWith(Throwable ex, String... startsWithClassNames) {
        while (ex != null) {
            for (String startsWithClassName : startsWithClassNames) {
                if (ex.getClass().getName().startsWith(startsWithClassName))
                    return (EX) ex;
            }
            ex = ex.getCause();
        }
        return null;
    }


    public static <EX extends Throwable> EX getCauseByTypes(Throwable e, Class<? extends Throwable>... exTypes) {
        while (e != null) {
            for (Class exType : exTypes) {
                if (exType.isInstance(e))
                    return (EX) e;
            }
            e = e.getCause();
        }
        return null;
    }

    /**
     * 获取根异常的消息
     *
     * @param e
     * @return
     */
    public static String getRootCauseInfo(Throwable e) {
        return getRootCause(e).getLocalizedMessage();
    }


}
