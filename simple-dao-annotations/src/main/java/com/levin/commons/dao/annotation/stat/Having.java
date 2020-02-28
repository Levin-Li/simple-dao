package com.levin.commons.dao.annotation.stat;

import java.lang.annotation.*;

/**
 * <p>Having class.</p>
 * <p>
 * Having 注解有最高的优先权
 *
 * @author llw
 * @version 2.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

//等于
public @interface Having {

    /**
     * @return
     */
    boolean require() default false;


    /**
     * 根据数据类型自动转换值
     *
     * @return
     */
    boolean isAutoConvertValue() default true;


    /**
     * 表达式，考虑支持Groovy和SpEL
     * <p/>
     * 当条件成立时，整个条件才会被加入
     * <p/>
     * Having做为附件处理方法以默认认为不符合条件，不做为主注解使用
     *
     * @return
     */
    String condition() default "";

}
