package com.levin.commons.dao;


import com.levin.commons.service.support.ContextHolder;

import java.util.Map;

/**
 * DAO环境上下文
 * <p/>
 * 有全局上下文和线程级别上下文
 *
 * @author lilw
 */

public abstract class DaoContext {


    public static final ContextHolder<String, Object> globalContext = ContextHolder.buildContext(true);

    public static final ContextHolder<String, Object> threadContext = ContextHolder.buildThreadContext(true);


    //////////////////////////////////////////////////////////////////////////////////////
    public static Map<String, Object> getGlobalContext() {
        return globalContext.getAll(true);
    }

    public static Map<String, Object> getThreadContext() {
        return threadContext.getAll(true);
    }

    /**
     * 先从线程变量取，没有在取全局，没有再默认
     *
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    public static <T> T getValue(String key, T defaultValue) {

        T value = threadContext.get(key);

        return value != null ? value : globalContext.getOrDefault(key, defaultValue);
    }

    public static <T> T setGlobalValue(String key, T defaultValue) {
        return globalContext.put(key, defaultValue);
    }

    /**
     * 是否自动清除
     *
     * @param defaultValue
     * @return
     */
    public static boolean isAutoFlushAndClearBeforeQuery(boolean defaultValue) {
        return Boolean.TRUE.equals(getValue(AUTO_FLUSH_AND_CLEAR_CACHE, defaultValue));
    }

    /**
     * 设置全局或是当前线程自动清除
     *
     * @param isGlobalEffect
     * @param autoFlushAndClear
     * @return
     */
    public static boolean setAutoFlushAndClearBeforeQuery(boolean isGlobalEffect, boolean autoFlushAndClear) {
        return Boolean.TRUE.equals(isGlobalEffect ?
                globalContext.put(AUTO_FLUSH_AND_CLEAR_CACHE, autoFlushAndClear)
                : threadContext.put(AUTO_FLUSH_AND_CLEAR_CACHE, autoFlushAndClear));
    }


    private static final String AUTO_FLUSH_AND_CLEAR_CACHE = DaoContext.class.getName() + ".AUTO_FLUSH_AND_CLEAR_CACHE";

}
