package com.levin.commons.dao;

/**
 *
 * @author llw
 */
public enum FlushMode {

    /**
     * 自动模式，会在必要的时候自动 flush，发送SQL语句
     */
    AUTO,

    /**
     * 立刻 发送 SQL 语句，不会进行批处理优化
     */
    IMMEDIATE,

}
