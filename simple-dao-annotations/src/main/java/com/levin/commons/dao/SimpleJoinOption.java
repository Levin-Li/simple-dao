package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 笛卡儿积
 *
 * @author llw
 * @version 2.0.0
 */

@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SimpleJoinOption {

    /**
     * 实体类
     * <p>
     * 对应表名
     *
     * @return
     */
    Class entityClass();


    /**
     * 别名，必须，并且别名，不允许重名
     * <p>
     * 自己的别名
     *
     * @return
     */
    String alias();

}
