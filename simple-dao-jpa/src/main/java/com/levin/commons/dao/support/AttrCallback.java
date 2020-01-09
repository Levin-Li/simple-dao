package com.levin.commons.dao.support;

import java.lang.annotation.Annotation;

/**
 *
 */

public interface AttrCallback {

    /**
     * 类的属性处理回调
     *
     * @param bean
     * @param fieldOrMethod
     * @param name
     * @param varAnnotations
     * @param attrType
     * @param value
     * @return 返回false表示不继续处理
     */
    boolean onAction(Object bean, Object fieldOrMethod, String name, Annotation[] varAnnotations, Class<?> attrType, Object value);

}
