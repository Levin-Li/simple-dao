package com.levin.commons.dao.support;

import java.io.Serializable;

public class ValueHolder<T> implements Serializable {

    public final Object root;

    public T value;

    public boolean needValue = true;

    public ValueHolder(Object root, T value) {
        this.root = root;
        this.value = value;
    }

    public ValueHolder(T value) {
        this.value = value;
        this.root = null;
    }

    public boolean hasValue() {
        return value != null;
    }
}
