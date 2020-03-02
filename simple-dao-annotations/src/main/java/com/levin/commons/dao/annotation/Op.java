package com.levin.commons.dao.annotation;


public enum Op {

    Eq("="), NotEq("!="),
    Gt(">"), Gte(">="),
    Lt("<"), Lte("<"),
    IsNull("IS NULL", false), IsNotNull("IS NOT NULL", false),
    Like("LIKE"), NotLike("NOT LIKE"),
    Contains("LIKE", "%", "%"),
    StartsWith("LIKE", "", "%"), EndsWith("LIKE", "%", ""),
    In("IN"), NotIN("NOT IN"),
    Exists("EXISTS"), NotExists("NOT EXISTS");

    /**
     *
     * 有具体的左操作数
     *
     * field + " " + op + " " + prefix + " " + 参数 or 子查询 + " " + suffix;
     *
     * 举例：  name Like ( ? )
     *        name = ( select n from subTable where id = ? )
     *        age >= func()
     *
     *
     */

    /**
     * 是否需要右操作数
     */
    boolean isRequireRightOperand = true;



    /**
     * 操作符
     *
     * @return
     */
    String op = "";

    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix = "";

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix = "";


    Op() {
        this.op = this.name();
    }

    Op(String op) {
        this.op = op;
    }

    Op(String op, boolean isRequireRightOperand) {
        this.op = op;
        this.isRequireRightOperand = isRequireRightOperand;
    }

    Op(String op, String prefix, String suffix) {
        this.op = op;
        this.prefix = prefix;
        this.suffix = suffix;
    }


}
