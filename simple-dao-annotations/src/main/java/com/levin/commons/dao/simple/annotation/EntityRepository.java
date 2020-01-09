package com.levin.commons.dao.simple.annotation;

import java.lang.annotation.*;

/**
 * 实体 DAO 接口注解
 * <p/>
 * 主要用于表示这个接口是一个自动实现的DAO接口
 * <p/>
 * 注解接口不可继承
 *
 * @author llw
 * @version 2.0.0
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Inherited
public @interface EntityRepository {

    /**
     * 名称
     *
     * @return
     */
    String value() default "";

}
