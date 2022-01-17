package com.levin.commons.dao;

import com.levin.commons.service.support.ValueHolder;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;

/**
 * 属性处理器
 */
@FunctionalInterface
public interface AttrHandler<T, V, CTX> {

    /**
     * 处理单个属性，然后返回处理后的值
     * <p>
     * 返回结果中如果有值则覆盖原值
     *
     * @param isInput  true 为为输入，false为输出
     * @param rootBean 属性所在的对象，可空
     * @param field    属性对应的字段定义，可空
     * @param name     属性名称
     * @param value    属性值，可空
     * @param contexts 上下文，可空
     * @return
     */
    ValueHolder<? extends V> handle(boolean isInput, T rootBean, Field field, @NotNull String name, V value, CTX... contexts);
}
