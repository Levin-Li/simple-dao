package com.levin.commons.dao;

/**
 * 属性拷贝器
 */
@FunctionalInterface
public interface DeepCopier {

    /**
     * 深度属性拷贝器
     *
     * @param source           拷贝源对象
     * @param target           实体 或  Class
     * @param deep             拷贝深度，建议不要超过3级
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     *                         spel:...
     * @return
     */
    <T> T copy(Object source, T target, int deep, String... ignoreProperties);


    /**
     * 深度属性拷贝
     *
     * @param source
     * @param target
     * @param ignoreProperties
     * @param <T>
     * @return
     */
    default <T> T copy(Object source, T target, String... ignoreProperties) {
        return copy(source, target, -1, ignoreProperties);
    }
}
