package com.levin.commons.dao;

import com.levin.commons.dao.annotation.Eq;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 标记实体字段，供代码生成器生成逻辑组的查询条件。 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GenDaoLogicGroup {

    /** Trusted participant scope used by generated request and update guards. */
    enum ScopeRole { NONE, ORG, OWNER, USER, TENANT, PLATFORM_DOMAIN }

    /** 对应实体类上的 {@link DaoLogicGroup#name()}。 */
    String name();

    /** Explicit role; field names and injection variable names are not authority metadata. */
    ScopeRole scopeRole() default ScopeRole.NONE;

    Class<? extends Annotation> filterOp() default Eq.class;

    /** 生成字段的注入变量名；空值使用实体字段名。 */
    String injectVarName() default "";

    /** 传递给生成字段的 {@code @InjectVar.isOverride}。 */
    String injectOverride() default "true";

    /** 传递给生成字段的 {@code @InjectVar.isRequired}。 */
    String injectRequired() default "false";
}
