package com.levin.commons.dao;

/**
 * 属性拷贝器
 */
public interface DeepCopier {


    /**
     * 智能属性拷贝，使用Spring转换器
     *
     * @param source
     * @param target
     * @param deep             拷贝深度，建议不要超过3级
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     *                         spel:...
     * @return
     */
    <T> T copy(Object source, Object target, int deep, String... ignoreProperties);

}
