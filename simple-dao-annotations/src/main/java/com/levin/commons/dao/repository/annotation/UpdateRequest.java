package com.levin.commons.dao.repository.annotation;

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
     *  如： name = ''
     *
     *
     * @return
     */
    String updateStatement() default "";

    /**
     * 固定where条件
     * 如 enable = true
     *
     * @return
     */
    String fixedCondition() default "";

}
