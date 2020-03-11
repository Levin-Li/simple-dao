package com.levin.commons.dao;


import java.util.Map;

/**
 * 简单条件构建器
 *
 * @param <T>
 * @since 1.1.6
 */
public interface SimpleStatBuilder<T extends SimpleStatBuilder> {

    T count(String expr);

    T avg(String expr, Map<String, Object>... paramValues);

    T sum(String expr, Map<String, Object>... paramValues);

    T max(String expr, Map<String, Object>... paramValues);

    T min(String expr, Map<String, Object>... paramValues);

    /**
     * 因为方法名称冲突，只好取 groupByAsAnno 的名字
     *
     * @param expr
     * @param paramValues
     * @return
     */
 //   T groupByAsAnno(String expr, Map<String, Object>... paramValues);

}
