package com.levin.commons.dao;

import javax.validation.constraints.NotNull;

public interface DaoFactory {


    /**
     * @return
     */
    MiniDao getDao();

    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 原生查询
     *
     * @param <T>
     * @return
     */
    <T> SelectDao<T> selectFrom(boolean nativeQL, @NotNull String fromStatement);

    /**
     * 原生查询
     *
     * @param tableName 表名
     * @param alias     表别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     * @param <T>
     * @return
     */
    <T> SelectDao<T> selectFrom(@NotNull String tableName, String... alias);

    /**
     * 创建一个指定类型的查询对象dao
     *
     * @param clazz 实体类，不允许为null
     * @param alias 实体类别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     * @param <T>
     * @return
     * @throws IllegalArgumentException 如果别名多于一个将会抛出异常
     */
    <T> SelectDao<T> selectFrom(@NotNull Class<T> clazz, String... alias);


    /**
     * 创建一个指定类型的更新dao
     *
     * @param clazz 实体类，不允许为null
     * @param alias 实体类别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     * @param <T>
     * @return
     * @throws IllegalArgumentException 如果别名多于一个将会抛出异常
     */
    <T> UpdateDao<T> updateTo(@NotNull Class<T> clazz, String... alias);

    /**
     * 创建一个指定类型的删除dao
     *
     * @param clazz 实体类，不允许为null
     * @param alias 实体类别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     * @param <T>
     * @return
     * @throws IllegalArgumentException 如果别名多于一个将会抛出异常
     */
    <T> DeleteDao<T> deleteFrom(@NotNull Class<T> clazz, String... alias);

    ///////////////////////////////////////////////////////////////////////////////////////


    /**
     * 使用表名创建一个更新的dao
     *
     * @param tableName 表名，不允许为null
     * @param alias     别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     * @param <T>
     * @return
     * @throws IllegalArgumentException 如果别名多于一个将会抛出异常
     */
    <T> UpdateDao<T> updateTo(@NotNull String tableName, String... alias);

    /**
     * 使用表名创建一个删除的dao
     * <p/>
     * alias别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     *
     * @param tableName 表名，不允许为null
     * @param alias     为了接口使用更方便，使用可变参，但只获取第一个别名
     * @param <T>
     * @return
     */
    <T> DeleteDao<T> deleteFrom(@NotNull String tableName, String... alias);

    ////////////////////////////////////////////////////////////////////////////////////////////

}
