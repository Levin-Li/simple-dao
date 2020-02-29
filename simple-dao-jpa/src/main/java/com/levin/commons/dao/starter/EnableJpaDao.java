package com.levin.commons.dao.starter;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(JpaDaoConfiguration.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableJpaDao {

}
