package com.levin.commons.dao.annotation;


import java.lang.annotation.*;

/**
 * <p>Ignore class.</p>
 *
 * @author llw
 * @version 2.0.0
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Ignore {

    /**
     * 描述信息
     *
     * @return
     */
    String value() default "";


    /**
     * 忽略的动作，默认忽略全部
     *
     * @return
     * @since 2.0
     */
    Action action() default Action.ALL;


    enum Action {
        ALL,
        // 2.0 还不支持选择性忽略
//        SELECT,
//        UPDATE,
//        DELETE
    }

}
