package com.levin.commons.dao.annotation;

import java.lang.annotation.*;

/** 条件成立时生成无参数、恒为假的 {@code 1=2} 谓词。 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DaoAlwaysFalse {

    /** 条件所属的 DAO 逻辑组；空值表示不指定组。 */
    String group() default "";
    String condition() default "";

    boolean require() default false;
}
