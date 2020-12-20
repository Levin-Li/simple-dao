package com.levin.commons.dao.annotation;


import com.levin.commons.dao.Expr;
import lombok.Getter;

import java.util.Arrays;

import static com.levin.commons.dao.annotation.Op.OperandType.Field;
import static com.levin.commons.dao.annotation.Op.OperandType.Param;

/**
 * 可扩展操作枚举对象
 */
@Getter
public enum Op
        implements Expr<String, String> {

    //条件操作
    Eq("="), NotEq("!="),
    Gt(">"), Gte(">="),
    Lt("<"), Lte("<="),

    Between(),

    //null 判读
    IsNull("IS NULL", null),
    IsNotNull("IS NOT NULL", null),

    //模糊查找
    //1)%通配符
    //2)_通配符
    ////字符'%'（百分号）和'_'（下划线）
    Like("LIKE", "", ""),
    NotLike("NOT LIKE", "", ""),

    //% 通配符
    Contains("LIKE", "'%'||", "||'%'"),
    StartsWith("LIKE", "", "||'%'"),
    EndsWith("LIKE", "'%'||", ""),

    // 函数形式
//    Contains("LIKE", "CONCAT('%',CONCAT(", ",'%'))"),
//    StartsWith("LIKE", "CONCAT(", ",'%')"),
//    EndsWith("LIKE", "CONCAT('%',", ")"),

    //
    In("IN", "(", ")"), NotIn("NOT IN", "(", ")"),

    //存在比较
    Exists("EXISTS", "(", Param, true, ")"),
    NotExists("NOT EXISTS", "(", Param, true, ")"),

    Not("NOT", "(", Param, true, ")"),

    Select("", null, Field),
    GroupBy("", null, Field),
    OrderBy("", null, Field),

    //函数
    //Func("", null, Field),

    Avg(null, "(", Field, true, ")"),
    Sum(null, "(", Field, true, ")"),
    Count(null, "(", Field, true, ")"),
    Max(null, "(", Field, true, ")"),
    Min(null, "(", Field, true, ")"),


    //Having(),

    Update("=", false),

    Expr("", null, Param),

    None("");


    enum OperandType {
        //字段
        Field,

        //参数
        Param
    }


    static final String DELIMITER = " ";


    /**
     * 左操作数类型
     * 默认是字段
     * <p>
     * 可以为 null
     */
    OperandType leftOperandType = Field;


    /**
     * 右操作数类型
     * 默认是参数
     * <p>
     * 可以为 null
     */
    OperandType rightOperandType = Param;


    /**
     * 是否允许字段上的函数注解
     * <p>
     * 默认可以，查询和条件都可以跌加，但更新是不允许字段叠加
     */

    boolean allowFieldFunc = true;


    /**
     * 扩展参数值
     * <p>
     * 比如 IN， NOT IN，Between
     */

    boolean expandParamValue = false;


    /**
     * 参数之间的间隔符号
     */
    String paramDelimiter = ",";

    /**
     * 操作符
     *
     * @return
     */
    String operator = null;


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


    void init() {

        if (this.name().equals("In")
                || this.name().equals("NotIn")
                || this.name().equals("Between")) {

            this.expandParamValue = true;

            if (this.name().equals("Between")) {
                paramDelimiter = " AND ";
            }
        }

    }


    Op() {
        this.operator = this.name();
        init();
    }


    Op(String operator) {
        this.operator = operator;
        init();
    }


    Op(String operator, boolean allowFieldFunc) {
        this.operator = operator;
        this.allowFieldFunc = allowFieldFunc;
        init();
    }


    Op(String operator, OperandType rightOperandType) {
        this.operator = operator;
        this.rightOperandType = rightOperandType;
        init();
    }

    Op(String operator, OperandType leftOperandType, OperandType rightOperandType) {
        this.leftOperandType = leftOperandType;
        this.rightOperandType = rightOperandType;
        this.operator = operator;
        init();
    }

    /**
     * 简单操作符构造
     *
     * @param operator
     * @param prefix
     * @param suffix
     */
    Op(String operator, String prefix, String suffix) {
        this.operator = operator;
        this.prefix = prefix;
        this.suffix = suffix;
        init();
    }


    /**
     * 表达构造
     * <p>
     * 比如 Avg Sum
     *
     * @param prefix
     * @param suffix
     */
    Op(String operator, String prefix, OperandType operandType, boolean isROP, String suffix) {

        if (operandType == null) {
            throw new IllegalArgumentException("operandType is null");
        }

        this.leftOperandType = this.rightOperandType = null;

        if (isROP) {
            this.rightOperandType = operandType;
        } else {
            this.leftOperandType = operandType;
        }

        this.operator = operator;

        this.prefix = prefix;

        if (operator == null) {
            this.prefix = name() + this.prefix;
        }

        this.suffix = suffix;

        init();
    }


    Op(OperandType leftOperandType, String operator, String prefix, OperandType rightOperandType, String suffix) {
        this.leftOperandType = leftOperandType;
        this.rightOperandType = rightOperandType;
        this.operator = operator;
        this.prefix = prefix;
        this.suffix = suffix;
        init();
    }


    /**
     * 是否需要参数表达式
     *
     * @return
     */
    public boolean isNeedParamExpr() {
        return leftOperandType == Param || rightOperandType == Param;
    }

    /**
     * 是否需要字段表达式
     *
     * @return
     */
    public boolean isNeedFieldExpr() {
        return leftOperandType == Field || rightOperandType == Field;
    }

    /**
     * 生成表达式
     * <p>
     * 生成规则：  左操作数(可空)  + " " + 操作符(可空) + " " + 前缀(可空) + " " + 右操作数(可空) + " " + 后缀(可空);
     * <p>
     * 最后生成后去除头尾空格
     *
     * @param fieldExpr
     * @param paramExpr
     * @return expr
     */
    @Override
    public String gen(String fieldExpr, String paramExpr) {

        if (this == None) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        //1、左操作数
        if (OperandType.Field.equals(leftOperandType)) {
            sb.append(nullSafe(fieldExpr));
        } else if (Param.equals(leftOperandType)) {
            sb.append(nullSafe(paramExpr));
        }

        sb.append(DELIMITER);

        //2、操作符
        sb.append(nullSafe(operator));

        sb.append(DELIMITER);

        // 3
        sb.append(nullSafe(prefix));

        sb.append(DELIMITER);

        //4、右操作数
        if (Param.equals(rightOperandType)) {
            sb.append(nullSafe(paramExpr));
        } else if (OperandType.Field.equals(rightOperandType)) {
            sb.append(nullSafe(fieldExpr));
        }

        sb.append(DELIMITER);

        //5
        sb.append(nullSafe(suffix));

        //最后生成后去除头尾空格
        return sb.toString().trim();

    }


    static String nullSafe(String txt) {
        return txt != null ? txt : "";
    }


    public static void main(String[] args) {


        Arrays.asList("name", "age", "sex")
                .stream()
                .map(f ->
                        Arrays.stream(Op.values()).map(op -> op.gen(f, ":?")).reduce((p1, p2) -> p1 + " And " + p2).get()
                ).forEach(p -> System.out.println("*** 条件：" + p));


        Arrays.stream(Op.values())
                .forEach(op -> System.out.println(op + "  : " + op.gen("字段", "参数")));

    }

}
