package com.levin.commons.dao;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO环境上下文
 * <p/>
 * 有全局上下文和线程级别上下文
 *
 * @author lilw
 */

public abstract class DaoContext {

    //
    public static final String ENABLE_EMPTY_STRING_QUERY = "enable_empty_string_query";

//    public static final String ENABLE_EMPTY_STRING_UPDATE = "enable_empty_string_update";
//
//    public static final String DAO_SAFE_MODE = "dao_safe_mode";


    private static final ThreadLocal<Map> threadContext = new ThreadLocal<>();

    //
    private static final Map<String, Object> globalContext = new ConcurrentHashMap<>();


    private static Map<String, Object> _getThreadContext() {

        Map context = threadContext.get();

        if (context == null) {
            context = new ConcurrentHashMap<>();
            threadContext.set(context);
        }

        return context;
    }


    public static <T> T getVar(String key, T defaultValue) {

        if (getThreadContext().containsKey(key)) {
            return getThreadVar(key);
        } else if (getGlobalContext().containsKey(key)) {
            return getGlobalVar(key);
        } else {
            return defaultValue;
        }

    }


    //////////////////////////////////////////////////////////////////////////////////////
    public static Map<String, Object> getGlobalContext() {
        return Collections.unmodifiableMap(globalContext);
    }

    public static void clearGlobalContext() {
        globalContext.clear();
    }

    public static <T> T getGlobalVar(String key) {
        return (T) globalContext.get(key);
    }

    public static <T> T removeGlobalVar(String key) {
        return (T) globalContext.remove(key);
    }

    public static <T> T setGlobalVar(String key, Object object) {
        return (T) globalContext.put(key, object);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static <T> T getThreadVar(String key) {
        return (T) _getThreadContext().get(key);
    }

    public static <T> T removeThreadVar(String key) {
        return (T) _getThreadContext().remove(key);
    }

    public static <T> T setThreadVar(String key, Object object) {
        return (T) _getThreadContext().put(key, object);
    }

    public static Map<String, Object> getThreadContext() {
        return Collections.unmodifiableMap(_getThreadContext());
    }

    public static void clearThreadContext() {
        _getThreadContext().clear();
    }

    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean isAutoFlush(boolean defaultValue) {
        return Boolean.TRUE.equals(getVar(UpdateDao.class.getName() + ".autoFlush", defaultValue));
    }

    public static boolean setAutoFlush(boolean isGlobalEffect, boolean isAutoFlush) {

        String key = UpdateDao.class.getName() + ".autoFlush";

        return Boolean.TRUE.equals(isGlobalEffect ? setGlobalVar(key, isAutoFlush) : setThreadVar(key, isAutoFlush));
    }
    
}
