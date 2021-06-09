package com.levin.commons.dao;

import javax.validation.constraints.NotNull;

public interface DaoFactory {

    /**
     *
     * 获取 Dao
     * @return
     */
    //  MiniDao getDao();

    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 通过查询对象创建 DAO
     *
     * @param daoClass
     * @param queryObjs
     * @param <DAO>
     * @return 返回 SelectDao or UpdateDao or DeleteDao
     */
    <DAO extends ConditionBuilder> DAO newDao(Class<DAO> daoClass, Object... queryObjs);


    /**
     * 从查询对象构造SelectDao
     *
     * @param <T>
     * @return
     */
    <T> SelectDao<T> forSelect(Object... queryObjs);

    /**
     * 从查询对象构造UpdateDao
     *
     * @param <T>
     * @return
     */
    <T> UpdateDao<T> forUpdate(Object... queryObjs);

    /**
     * 从查询对象构造UpdateDao
     *
     * @param <T>
     * @return
     */
    <T> DeleteDao<T> forDelete(Object... queryObjs);
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 原生查询
     *
     * @param nativeQL      是否原生查询
     * @param fromStatement
     * @param <T>
     * @return
     */
    @Deprecated
    <T> SelectDao<T> selectFrom(boolean nativeQL, @NotNull String fromStatement);

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
     * 原生查询
     *
     * @param clazz
     * @param alias
     * @param <T>
     * @return
     */
    <T> SelectDao<T> selectByNative(@NotNull Class<T> clazz, String... alias);

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
     * 创建一个指定类型的更新dao
     *
     * @param clazz 实体类，不允许为null
     * @param alias 实体类别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     * @param <T>
     * @return
     * @throws IllegalArgumentException 如果别名多于一个将会抛出异常
     */
    <T> UpdateDao<T> updateByNative(@NotNull Class<T> clazz, String... alias);

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

    /**
     * @param clazz
     * @param alias
     * @param <T>
     * @return
     */
    <T> DeleteDao<T> deleteByNative(@NotNull Class<T> clazz, String... alias);

    ///////////////////////////////////////////////////////////////////////////////////////

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
     * 使用表名创建一个更新的dao
     * 默认为原生查询
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
     * 默认为原生查询
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
