package com.levin.commons.dao.simple.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

/**
 * 更新请求
 *
 * @author llw
 * @version 2.0.0
 */
public @interface UpdateRequest {

    /**
     * 静态的更新的字段
     * 可用于时间更新等
     *
     * @return
     */
    String updateStatement() default "";

    /**
     * 固定条件
     * 如 enable = true
     *
     * @return
     */
    String fixedCondition() default "";

}
