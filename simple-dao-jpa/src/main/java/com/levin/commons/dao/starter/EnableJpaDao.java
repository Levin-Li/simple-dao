package com.levin.commons.dao.starter;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JpaDaoConfiguration.class)
public @interface EnableJpaDao {

    /**
     * bean 的名字
     *
     * @return
     */
//    String value() default "";

}
