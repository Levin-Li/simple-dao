package com.levin.commons.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 显式声明实体字段按 JSON 字符串数组执行全部成员强制范围。 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MandatoryJsonStringArray {
}
