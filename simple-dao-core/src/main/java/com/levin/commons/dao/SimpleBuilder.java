package com.levin.commons.dao;


/**
 * 简单条件构建器
 *
 * @param <T>
 * @since 1.1.6
 */
public interface SimpleBuilder<T> extends SimpleConditionBuilder<T> {

    /**
     * 属性名称转换器
     *
     * @param converter
     * @return
     */
    T useNameConverter(Converter<String, String> converter);

    /**
     * 新的条件构建器
     *
     * @return
     */

    SimpleBuilder<T> newBuilder();

    /**
     * @return
     */
    T and(SimpleBuilder<T> builder);


    /**
     * @return
     */
    T or(SimpleBuilder<T> builder);


    /**
     * @return
     */
    T clear();


}
