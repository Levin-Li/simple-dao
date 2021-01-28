package com.levin.commons.dao;


/**
 * 简单条件构建器
 *
 * @param <T>
 * @since 1.1.6
 */
public interface SimpleStatBuilder<T extends SimpleStatBuilder> {

    /**
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T count(String expr, String alias, Object... paramValues);

    /**
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T avg(String expr, String alias, Object... paramValues);

    /**
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T sum(String expr, String alias, Object... paramValues);

    /**
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T max(String expr, String alias, Object... paramValues);

    /**
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T min(String expr, String alias, Object... paramValues);

    /**
     * <p>
     * 通过模拟 GroupBy 注解的方式增加 groupBy 语句
     *
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T groupByAndSelect(String expr, String alias, Object... paramValues);

}
