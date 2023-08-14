package com.levin.commons.dao.annotation.misc;

import com.levin.commons.annotation.GenNameConstant;
import com.levin.commons.dao.annotation.C;

import java.lang.annotation.*;

/**
 * SQL case
 * <p>
 * Case表达式是SQL标准（SQL92发行版）的一部分，并已在Oracle Database、SQL Server、 MySQL、 PostgreSQL、 IBM UDB和其他数据库服务器中实现；
 * <p>
 * CASE可用于允许有效表达式的任何语句或子句中。
 * 例如，可以在SELECT、UPDATE、DELETE和SET等语句中使用CASE，
 * 也可以在SELECT\U list、in、WHERE、ORDER BY和HAVING等子句中使用CASE。
 *
 * <p>
 * 1、简单 CASE
 * 2、Case 搜索函数
 * <p>
 * <p>
 * 简单 CASE 如下：
 * CASE sex
 * WHEN '1' THEN '男'
 * ELSE '其他'
 * END
 * <p>
 * 统计，选择常用
 *
 * @author llw
 */

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@GenNameConstant
public @interface Case {

    /**
     * Case 选项
     */
    @Target({ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @GenNameConstant
    @interface When {
        /**
         * when 表达式
         * <p>
         * 或
         *
         * @return
         */
        String whenExpr();

        /**
         * then 新值
         *
         * @return
         */
        String thenExpr();

        /**
         * 描述信息
         *
         * @return
         */
        String desc() default "";

    }

    /**
     * 字段名/列名
     *
     * @return
     */
    String column() default C.ORIGIN_EXPR;

    /**
     * 选项
     *
     * @return
     */
    When[] whenOptions();


    /**
     * else 表达式
     *
     * @return
     */
    String elseExpr();

    /**
     * 条件
     * SPEL 表达式
     *
     * @return
     */
    String condition() default "";

    /**
     * 描述信息
     *
     * @return
     */
    String desc() default "";

}
