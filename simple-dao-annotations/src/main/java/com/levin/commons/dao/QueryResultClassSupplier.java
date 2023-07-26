package com.levin.commons.dao;


import java.util.function.Supplier;

/**
 * 查询结果类
 */
@FunctionalInterface
public interface QueryResultClassSupplier extends Supplier<Class<?>> {

}
