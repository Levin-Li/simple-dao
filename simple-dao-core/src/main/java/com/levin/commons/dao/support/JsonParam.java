package com.levin.commons.dao.support;

import com.levin.commons.dao.PrimitiveValueWrapper;

/**
 * Marks a value that should be bound as a JSON parameter.
 */
public final class JsonParam<T> implements PrimitiveValueWrapper<T> {

    private final T value;

    private JsonParam(T value) {
        this.value = value;
    }

    public static <T> JsonParam<T> of(T value) {
        return new JsonParam<>(value);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
