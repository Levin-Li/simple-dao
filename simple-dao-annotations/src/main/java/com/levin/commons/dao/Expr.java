package com.levin.commons.dao;


/**
 * 表达式生成接口
 *
 * @param <L> 左操作数
 * @param <R> 右操作数
 */
@FunctionalInterface
public interface Expr<L, R> {

    /**
     * 生成表达式
     *
     * @param l 左操作数
     * @param r 右操作数
     * @return 返回的表达式
     */
    String gen(L l, R r);

}
