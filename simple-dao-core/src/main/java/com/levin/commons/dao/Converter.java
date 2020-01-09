package com.levin.commons.dao;

/**
 * 结果结果转换器
 *
 * @param <I> 查询结果
 * @param <O> 转换后的结果
 */
public interface Converter<I, O> {

    O convert(I data);

}
