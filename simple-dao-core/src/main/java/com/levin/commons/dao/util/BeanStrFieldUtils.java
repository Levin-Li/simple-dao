package com.levin.commons.dao.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.levin.commons.dao.TargetOption;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串属性工具类
 */
public abstract class BeanStrFieldUtils {

    /**
     * 去除字符串属性的头尾空字符
     *
     * @param bean
     * @param ignoreFields
     * @param <T>
     * @return
     */
    public static <T> T trimStrFields(T bean, String... ignoreFields) {
        return BeanUtil.trimStrFields(bean, ignoreFields);
    }

    /**
     * 自动填充字符串属性
     *
     * @param bean
     * @param fillFunc     如果为空，则填充空字符串
     * @param ignoreFields
     * @param <T>
     * @return
     */
    public static <T> T fillNullStrFields(T bean, Function<String, String> fillFunc, String... ignoreFields) {
        return BeanUtil.edit(bean, (field) -> {
            if (ignoreFields != null && ArrayUtil.containsIgnoreCase(ignoreFields, field.getName())) {
                // 不处理忽略的Fields
                return field;
            }
            if (String.class.equals(field.getType())) {
                // 只有String的Field才处理
                fillNullValue(bean, fillFunc, field);
            }
            return field;
        });
    }

    public static <T> T fillNullStrFields(T bean, String... ignoreFields) {
        return fillNullStrFields(bean, null, ignoreFields);
    }

    public static <T> T fillNullStrFieldsByEntityClass(T bean) {
        return fillNullStrFields(bean, (Class<?>) null);
    }

    /**
     * 根据实体类的定义，自动填充字符串属性
     *
     * @param bean
     * @param entityClass 目标实体类
     * @param <T>
     * @return
     */
    public static <T> T fillNullStrFields(T bean, Class<?> entityClass) {
        return fillNullStrFields(bean, entityClass, null);
    }

    /**
     * 根据实体类的定义，自动填充字符串属性
     *
     * @param bean
     * @param entityClass 目标实体类
     * @param fillFunc    如果为空，则填充空字符串
     * @param <T>
     * @return
     */
    public static <T> T fillNullStrFields(T bean, Class<?> entityClass, Function<String, String> fillFunc) {

        if (null == bean) {
            return null;
        }

        // 尝试取出目标实体类
        if (entityClass == null) {
            TargetOption targetOption = AnnotatedElementUtils.findMergedAnnotation(entityClass, TargetOption.class);
            // 取出目标实体类
            entityClass = targetOption != null ? targetOption.entityClass() : null;
        }

        Assert.isTrue(entityClass != null
                && entityClass != Void.class
                && entityClass != void.class, "entityClass must not be set");

        //从 entityClass 中找出所有的 有标记 @Column(nullable = false) 的字段名字
        List<String> fieldNames = Stream.of(ReflectUtil.getFields(entityClass))
                .filter(field -> field.isAnnotationPresent(Column.class))
                .filter(field -> !field.getAnnotation(Column.class).nullable())
                .map(Field::getName)
                .collect(Collectors.toList());

        return BeanUtil.edit(bean, (field) -> {

            if (String.class.equals(field.getType())
                    && fieldNames.contains(field.getName())) {

                fillNullValue(bean, fillFunc, field);
            }
            return field;
        });
    }


    private static <T> void fillNullValue(T bean, Function<String, String> fillFunc, Field field) {
        final String val = (String) ReflectUtil.getFieldValue(bean, field);

        if (null == val) {
            final String newVal = fillFunc == null ? StrUtil.EMPTY : fillFunc.apply(val);
            if (null != newVal) {
                // 填充新值
                ReflectUtil.setFieldValue(bean, field, newVal);
            }
        }
    }

}
