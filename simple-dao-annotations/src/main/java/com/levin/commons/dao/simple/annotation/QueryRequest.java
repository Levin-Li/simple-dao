package com.levin.commons.dao.simple.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 * 实体查询支持注解
 *
 * @author llw
 * @version 2.0.0
 */
public @interface QueryRequest {
    /**
     * 查找的字段
     * <p/>
     * new com.test.ResultObj(g.name,g.id)
     *
     * @return select 之后的语句 如：new com.test.ResultObj(g.name,g.id)
     */
    String selectStatement() default "";

    /**
     * 查询的目标语句，可能是一张表，或是多张表
     *
     * @return
     */
    String fromStatement() default "";

    /**
     * join 表达式
     * <p/>
     * 如果抓取子集合 left join fetch g.custmers
     *
     * @return
     */
    String joinStatement() default "";


    /**
     * 自动抓取的集合属性
     *
     * @return
     */
    String[] joinFetchSetAttrs() default {};

    /**
     * 查询结果类
     * <p/>
     * 默认是从方法返回值中获取
     *
     * @return
     */
    Class resultClass() default Void.class;

    /**
     * 转换器类名
     * <p/>
     * 转换器优先
     * <p/>
     *
     * @return 转换器类
     */
    Class resultConverterClass() default Void.class;


    /**
     * 固定条件
     * 如 enable = true
     *
     * @return
     */
    String fixedCondition() default "";

    /**
     * 查询时默认排序
     * 针对查询有效
     * <p/>
     * 如果 orderCode desc,name desc
     *
     * @return
     */
    String defaultOrderBy() default "";

}
