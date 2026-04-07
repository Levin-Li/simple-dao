package com.levin.commons.dao;


/**
 * 表达式和参数对
 *
 * @author lilw
 */
public interface ExprAndParamPair {

    /**
     * 获取表达式
     *
     * @return
     */
    String getExpr();

    /**
     * 获取参数
     *
     * @return
     */
    <P> P getParam();

}
