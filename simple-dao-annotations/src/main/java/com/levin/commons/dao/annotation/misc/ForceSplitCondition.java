package com.levin.commons.dao.annotation.misc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仅适用于集合或数组参数；在生成查询条件时，强制将其按元素拆解为多个独立条件。
 * <p>
 * 此注解在条件参数处理中的优先级最高，会先于原子类型标记和操作符自身的集合参数规则生效。
 * <p>
 * 标量字段不应声明此注解，代码生成器也不会为标量字段生成此注解。
 * <p>
 * 此注解仅影响当前字段的查询条件参数处理，不改变实体字段的持久化映射，也不影响更新时的整体 JSON 绑定。
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ForceSplitCondition {

    /**
     * 描述信息。
     */
    String desc() default "强制拆解集合查询条件";
}
