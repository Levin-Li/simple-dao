package com.levin.commons.dao;


import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.service.support.SimpleVariableInjector;
import com.levin.commons.service.support.VariableInjector;
import org.springframework.util.Assert;

import java.util.*;

/**
 * DAO环境上下文
 * <p/>
 * 有全局上下文和线程级别上下文
 *
 * @author lilw
 */

public abstract class DaoContext {

    private static final VariableInjector defaultVariableInjector = SimpleVariableInjector.defaultSimpleVariableInjector;

    private static final String VARIABLE_INJECTOR_KEY = VariableInjector.class.getName() + defaultVariableInjector.hashCode();

    public static final ContextHolder<String, Object> globalContext = ContextHolder.buildContext(true);

    public static final ContextHolder<String, Object> threadContext = ContextHolder.buildThreadContext(true);

    //////////////////////////////////////////////////////////////////////////////////////
    public static Map<String, Object> getGlobalContext() {
        return globalContext.getAll(true);
    }

    public static Map<String, Object> getThreadContext() {
        return threadContext.getAll(true);
    }

    private static final String AUTO_FLUSH_AND_CLEAR_CACHE = DaoContext.class.getName() + "#AUTO_FLUSH_AND_CLEAR_CACHE";

    /**
     * 获取变量注入器
     *
     * @return
     */
    public static VariableInjector getVariableInjector() {

        //1、获取当前线程
        VariableInjector variableInjector = threadContext.get(VARIABLE_INJECTOR_KEY);

        if (variableInjector == null) {
            //2、获取全局
            variableInjector = globalContext.get(VARIABLE_INJECTOR_KEY);
        }

        if (variableInjector == null) {
            //3、获取默认
            variableInjector = defaultVariableInjector;
        }

        return variableInjector;
    }

    public static VariableInjector setCurrentThreadVariableInjector(VariableInjector variableInjector) {
        return threadContext.put(VARIABLE_INJECTOR_KEY, variableInjector);
    }

    public static VariableInjector setGlobalVariableInjector(VariableInjector variableInjector) {
        return globalContext.put(VARIABLE_INJECTOR_KEY, variableInjector);
    }

    /**
     * 从变量来源注入变量到目标变量中
     *
     * @param targetBean
     * @param varSourceBeans 变量来源，注意顺序
     * @return
     */
    public static List<String> injectVars(Object targetBean, Object... varSourceBeans) {

        if (targetBean == null) {
            return Collections.emptyList();
        }

        Assert.notNull(varSourceBeans, "varSourceBeans is null");

        List<Object> contexts = new ArrayList<>(varSourceBeans.length + 2);

        contexts.addAll(Arrays.asList(varSourceBeans));

        //加上线程
        contexts.add(getThreadContext());

        //加上全局
        contexts.add(getGlobalContext());

        return getVariableInjector().inject(targetBean, contexts);
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

    /**
     * 设置全局默认值
     *
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
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

}
