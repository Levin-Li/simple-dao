package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 查询结果类
 *
 * @author llw
 * @version 2.0.0
 */

@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface QueryResultOption {

    /**
     * 预期的结果集数据
     * -1 标识没有预期值
     *
     * @return
     */
    int expectResultSetCount() default -1;

    /**
     * 查询期望的查询结果类
     * 只针对查询有效
     * <p>
     * 结果类上默认不支持where条件
     *
     * @return
     */
    Class<?> resultClass() default Void.class;

}
