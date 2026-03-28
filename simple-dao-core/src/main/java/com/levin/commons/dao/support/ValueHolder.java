package com.levin.commons.dao.support;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Supplier;


/**
 * @author lilw
 */
@Data
@Accessors(chain = true)
public class ValueHolder<T> implements Supplier<T> {

    public final Object root;

    public String name;

    public T value;

    public ValueHolder(Object root, String name, T value) {
        this.root = root;
        this.name = name;
        this.value = value;
    }

    public ValueHolder(Object root, T value) {
        this.root = root;
        this.value = value;
    }


    public ValueHolder(T value) {
        this.value = value;
        this.root = null;
    }

    public ValueHolder() {
        this.root = null;
    }

    @Override
    public T get() {
        return value;
    }

}
