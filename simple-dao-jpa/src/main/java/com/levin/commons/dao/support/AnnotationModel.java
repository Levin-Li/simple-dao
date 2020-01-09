package com.levin.commons.dao.support;


import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 *
 */
public class AnnotationModel
        implements Serializable, Comparable<AnnotationModel> {

    /**
     * 源注解
     */
    transient Annotation annotation;

    /**
     * 排序优先级
     * <p/>
     * 按数值从小到大排序
     *
     * @return
     */
    Integer order;

    /**
     * 查询字段名称，默认为字段的属性名称
     * 排序方式，可以用字段隔开
     *
     * @return
     */
    String value;

    /**
     *
     *
     */

    boolean require = false;


    /**
     *
     */
    boolean isAutoConvertValue = true;

    /**
     * 表达式，考虑支持Groovy和SpEL
     * <p/>
     * 当条件成立时，整个条件才会被加入
     *
     * @return
     */
    String condition;


    /**
     * 操作符
     *
     * @return
     */
    String op;


    /**
     * 专用于Having子句
     */
    String havingOp;

    /**
     * 对右操作数的包围前缀
     *
     * @return
     */
    String prefix;

    /**
     * 对右操作数的包围后缀
     *
     * @return
     */
    String suffix;

    /**
     * 子查询
     */
    String subQuery;

    /**
     * 描述信息
     *
     * @return
     */
    String desc;

    private AnnotationModel() {
    }

    public static AnnotationModel copy(Annotation annotation) {

        AnnotationModel model = new AnnotationModel();

        model.annotation = annotation;

        for (Field field : AnnotationModel.class.getDeclaredFields()) {

            if (field.getName().equals("annotation"))
                continue;

            field.setAccessible(true);

            try {
                field.set(model, annotation.annotationType().getDeclaredMethod(field.getName()).invoke(annotation));
            } catch (NoSuchMethodException e) {
                //故意忽略方法异常
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return model;
    }


    @Override
    public int compareTo(AnnotationModel o) {

        int l = order != null ? order : 0;

        int r = (o != null && o.order != null) ? o.order : 0;

        return l - r;

    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Integer getOrder() {
        return order;
    }

    public String getValue() {
        return value;
    }

    public boolean isRequire() {
        return require;
    }

    public void setRequire(boolean require) {
        this.require = require;
    }

    public String getCondition() {
        return condition;
    }

    public String getOp() {
        return op;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getHavingOp() {
        return havingOp;
    }

    public String getDesc() {
        return desc;
    }


    @Override
    public String toString() {
        return "AnnotationModel{" +
                "annotation=" + annotation +
                '}';
    }
}
