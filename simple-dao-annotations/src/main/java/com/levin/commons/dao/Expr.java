package com.levin.commons.dao;

public interface Expr<L, R> {

    /**
     * 生成表达式
     *
     * @param l 左操作数
     * @param r 右操作数
     * @return
     */
    String gen(L l, R r);


}
