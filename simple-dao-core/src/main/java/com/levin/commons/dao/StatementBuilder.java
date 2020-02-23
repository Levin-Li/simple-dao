package com.levin.commons.dao;

import java.util.List;

/**
 *
 *
 *
 */
public interface StatementBuilder {


    /**
     * 生成最终的语句
     *
     * @return
     */
    String genFinalStatement();

    /**
     * 生成最终的参数列表
     *
     * @return
     */
    List genFinalParamList();

}
