package com.levin.commons.plugins;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PluginContext {


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

}
