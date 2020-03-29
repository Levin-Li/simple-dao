package com.levin.commons.dao;


import java.util.Map;

/**
 * 简单条件构建器
 *
 * @param <T>
 * @since 1.1.6
 */
public interface SimpleStatBuilder<T extends SimpleStatBuilder> {

    T count(String expr, String alias);

    T avg(String expr, String alias, Map<String, Object>... paramValues);

    T sum(String expr, String alias, Map<String, Object>... paramValues);

    T max(String expr, String alias, Map<String, Object>... paramValues);

    T min(String expr, String alias, Map<String, Object>... paramValues);

    /**
     *
     * <p>
     * 通过模拟 GroupBy 注解的方式增加 groupBy 语句
     *
     *
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T groupByAsAnno(String expr, String alias, Map<String, Object>... paramValues);

}
