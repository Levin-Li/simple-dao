package com.levin.commons.dao;

import org.springframework.transaction.annotation.Transactional;

public interface UpdateDao<T>
        extends ConditionBuilder<UpdateDao<T>> {

    /**
     * 设置更新的列
     * 将清除以前的设置参数
     *
     * @param columns
     * @param paramValues 参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理
     *                    是Map时，会当成命名参数进行处理，当Map中的key是Int时，会当成位置参数来使用
     * @return
     */
    UpdateDao<T> setColumns(String columns, Object... paramValues);

    /**
     * 增加更新的列
     *
     * @param columns
     * @param paramValues 参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理
     *                    是Map时，会当成命名参数进行处理，当Map中的key是Int时，会当成位置参数来使用
     * @return
     */
    UpdateDao<T> appendColumns(String columns, Object... paramValues);


    /**
     * 增加更新的列
     *
     * @param isAppend
     * @param columns
     * @param paramValues
     * @return
     */
    UpdateDao<T> appendColumns(Boolean isAppend, String columns, Object... paramValues);


    /**
     * 增加单个需要更新的属性
     *
     * @param entityAttrName 需要更新的属性名
     * @param paramValue     属性值
     * @return
     */

    UpdateDao<T> set(String entityAttrName, Object paramValue);

    /**
     * 增加单个需要更新的属性
     *
     * @param isAppend       为了保持链式调用，增加参数支持
     * @param entityAttrName
     * @param paramValue
     * @return
     */
    UpdateDao<T> set(Boolean isAppend, String entityAttrName, Object paramValue);

    /**
     * 增加单个需要更新的属性
     *
     * @param entityAttrName 需要更新的属性名
     * @param paramValue     属性值
     * @return
     */

    UpdateDao<T> appendColumn(String entityAttrName, Object paramValue);


    /**
     * 增加单个需要更新的属性
     *
     * @param isAppend       为了保持链式调用，增加参数支持
     * @param entityAttrName
     * @param paramValue
     * @return
     */
    UpdateDao<T> appendColumn(Boolean isAppend, String entityAttrName, Object paramValue);

    /**
     * 增加需要更新的属性
     * <p/>
     * dto 所有的属性值不为null的都会被做为更新字段
     *
     * @param dto
     * @return
     * @Deprecated 建议使用注解 UpdateColumn
     * @see com.levin.commons.dao.annotation.update.UpdateColumn
     */
    @Deprecated
    UpdateDao<T> appendColumnByDTO(Object dto, String... ignoreAttrs);

    /**
     * 增加需要更新的属性和值
     * <p/>
     * dto 所有的属性值不为null的都会被做为更新字段
     *
     * @param dto
     * @return
     * @Deprecated 建议使用注解 UpdateColumn
     * @see com.levin.commons.dao.annotation.update.UpdateColumn
     */
    @Deprecated
    UpdateDao<T> appendColumnByDTO(boolean ignoreNullValueColumn, Object dto, String... ignoreAttrs);


    /**
     * 是否有要更新的列
     * <p>
     * 2018.3.30 增加
     *
     * @return
     */
    boolean hasColumnsForUpdate();

    /**
     * 执行更新动作，并返回受影响的记录数
     *
     * @return
     */
    @Transactional
    int update();

}
