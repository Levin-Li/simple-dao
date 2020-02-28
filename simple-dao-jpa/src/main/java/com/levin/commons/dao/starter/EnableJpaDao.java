package com.levin.commons.dao.starter;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(JpaDaoAutoConfiguration.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableJpaDao {

}
