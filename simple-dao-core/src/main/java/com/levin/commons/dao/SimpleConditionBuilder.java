package com.levin.commons.dao;


/**
 * 简单条件构建器
 *
 * @param <T>
 * @since 1.1.6
 */
public interface SimpleConditionBuilder<T> {

    /**
     * 禁用空值过滤
     * <p>
     * 默认是禁用空值过滤
     * <p>
     * 禁用后，新加入的查询条件会被强制加入
     *
     * @return
     */
    T disableEmptyValueFilter();


    /**
     * 允许空值过滤
     * <p>
     * 允许空值过滤后，新加入的条件将会过滤空值
     *
     * @return
     */
    T enableEmptyValueFilter();

    /**
     * is null
     *
     * @param entityAttrName 如 name
     * @return
     */
    T isNull(String entityAttrName);


    /**
     * is not null
     *
     * @param entityAttrName 如 name
     * @return
     */
    T isNotNull(String entityAttrName);


    /**
     * xx is null or xx = paramValue
     *
     * @param entityAttrName 如 name
     * @return
     */
    T isNullOrEq(String entityAttrName, Object paramValue);

    /**
     * =
     * eg：appendWhereEquals("name","echo") 表示 and name = 'echo'
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T eq(String entityAttrName, Object paramValue);


    /**
     * !=
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T notEq(String entityAttrName, Object paramValue);

    /**
     * > 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gt(String entityAttrName, Object paramValue);


    /**
     * < 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lt(String entityAttrName, Object paramValue);


    /**
     * >= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T gte(String entityAttrName, Object paramValue);


    /**
     * <= 操作
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T lte(String entityAttrName, Object paramValue);

    /**
     * field between ? and ? and ?
     * or
     * field >= ?
     *
     * @return
     */
    T between(String entityAttrName, Object... paramValues);

    /**
     * field in (?...)
     *
     * @return
     */
    T in(String entityAttrName, Object... paramValues);


    /**
     * field not in (?...)
     *
     * @return
     */
    T notIn(String entityAttrName, Object... paramValues);


    /**
     * exists 操作
     * 查询对象或是字符串表达式
     *
     * @return
     */
    T exists(Object exprOrQueryObj, Object... paramValues);

    /**
     * exists 操作
     * <p>
     * 查询对象或是字符串表达式
     *
     * @return
     */
    T notExists(Object exprOrQueryObj, Object... paramValues);


    /**
     * like %keyword%
     *
     * @return
     */
    T contains(String entityAttrName, String keyword);


    /**
     * like keyword%
     *
     * @return
     */
    T startsWith(String entityAttrName, String keyword);


    /**
     * like %keyword
     *
     * @return
     */
    T endsWith(String entityAttrName, String keyword);

}
