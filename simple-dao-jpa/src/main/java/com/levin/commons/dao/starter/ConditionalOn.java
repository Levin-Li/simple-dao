package com.levin.commons.dao.starter;


import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(SimpleCondition.class)
public @interface ConditionalOn {

    enum Type {
        OnBean,
        OnMissingBean,
        OnClass,
        OnMissingClass,
        OnProperty,
        OnExpr,
    }

    /**
     * 条件类型
     *
     * @return
     */
    Type type();

    /**
     * 条件值
     *
     * @return
     */
    String value();

}
