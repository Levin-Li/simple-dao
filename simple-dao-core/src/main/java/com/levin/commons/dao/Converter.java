package com.levin.commons.dao;

import java.util.function.Function;

/**
 * 结果结果转换器
 *
 * @param <I> 查询结果
 * @param <O> 转换后的结果
 */
@FunctionalInterface
public interface Converter<I, O> extends Function<I, O> {

    O convert(I data);

    @Override
    default O apply(I data) {
        return convert(data);
    }

}
