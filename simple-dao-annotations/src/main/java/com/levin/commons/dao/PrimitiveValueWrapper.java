package com.levin.commons.dao;

import java.util.function.Supplier;


/**
 * @author lilw
 */ /*
 * 值包装器
 *
 */
@FunctionalInterface
public interface PrimitiveValueWrapper<T> extends Supplier<T> {

    static <T> PrimitiveValueWrapper<T> of(T value) {
        return new PrimitiveValueWrapper<>() {
            @Override
            public T get() {
                return value;
            }

            @Override
            public String toString() {
                return String.valueOf(get());
            }
        };
    }
}
