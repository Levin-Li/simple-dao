package com.levin.commons.dao;


/**
 * 简单条件构建器2
 * <p>
 * <p>
 * SimpleConditionBuilder2
 */
@Deprecated
interface SimpleConditionBuilder2<T> {

    /**
     * 增加单个参数及表达式
     * eg：appendWhereEquals("name","echo") 表示 and name = 'echo'
     *
     * @param entityAttrName 如 name
     * @param paramValue     如果值为null ，将不加入查询条件
     * @return
     */
    T appendWhereEquals(String entityAttrName, Object paramValue);


    /**
     * field between ? and ? and ?
     * or
     * field >= ?
     *
     * @return
     */
    T appendBetween(String entityAttrName, Object... paramValues);

    /**
     * field in (?...)
     *
     * @return
     */
    T appendWhereIn(String entityAttrName, Object... paramValues);


    /**
     * field in (?...)
     *
     * @return
     */
    T appendWhereNotIn(String entityAttrName, Object... paramValues);


    /**
     * like %keyword%
     *
     * @return
     */
    T appendWhereContains(String entityAttrName, String keyword);


    /**
     * like keyword%
     *
     * @return
     */
    T appendWhereStartsWith(String entityAttrName, String keyword);


    /**
     * like %keyword
     *
     * @return
     */
    T appendWhereEndsWith(String entityAttrName, String keyword);

}
