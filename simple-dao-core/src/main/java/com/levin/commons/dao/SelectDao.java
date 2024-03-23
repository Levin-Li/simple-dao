package com.levin.commons.dao;

import com.levin.commons.dao.support.DefaultPagingData;

import java.util.List;

/**
 * 查询接口
 */
public interface SelectDao<T> extends
        ConditionBuilder<SelectDao<T>, T>,
        SelectBuilder<SelectDao<T>, T>,
        OrderByBuilder<SelectDao<T>, T>,
        GroupByBuilder<SelectDao<T>, T>,
        HavingBuilder<SelectDao<T>, T>,
        JoinBuilder<SelectDao<T>, T>,
        JoinFetchBuilder<SelectDao<T>, T>,
        SimpleStatBuilder<SelectDao<T>, T> {

    /**
     * 是否有统计的列
     *
     * @return
     */
    boolean hasStatColumns();

    /**
     * 是否有选择的列
     * <p>
     * 2018.3.30 增加
     *
     * @return
     */
    boolean hasSelectColumns();

    /**
     * 设置 having 子句 group by 子句 和 order by 子句 是否使用统计别名
     *
     * @return
     */
    SelectDao<T> useStatAliasForHavingGroupByOrderBy(boolean useStatAlias);

    /**
     * 记录总数
     * COUNT() ，返回类型为 Long ，注意 count(*) 语法在 hibernate 中可用，但在 toplink 其它产品中并不可用
     * 目前该方法有一个bug，如果查询本身已经是一个统计语句或是分组统计语句，可能将导致错误
     * 遇到这种情况，建议手动编写统计，以确保正确
     *
     * @return long
     */
    long count();

    /**
     * 分页查询
     *
     * @param resultType
     * @param paging
     * @param <E>
     * @return
     */
    default <E> PagingData<E> findPaging(Class<? extends E> resultType, Paging paging) {
        return new DefaultPagingData<E>()
                .setItems(paging.isRequireResultList() ? (List<E>) find(resultType) : null)
                .setTotals(paging.isRequireTotals() ? count() : -1L)
                .setPageIndex(paging.getPageIndex())
                .setPageSize(paging.getPageSize())
                ;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取结果集
     *
     * @param <E>
     * @return
     */
    default <E> List<E> find() {
        return find((Class<E>) null);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E> resultType 要求的结果类型
     * @return
     */
    default <E> List<E> find(Class<E> resultType) {
        return find(resultType, 3);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E>              resultType 要求的结果类型
     * @param maxCopyDeep      -1，表示不限层级
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     * @return
     */
    <E> List<E> find(Class<E> resultType, int maxCopyDeep, String... ignoreProperties);

    /**
     * 获取结果集，并转换成指定的对对象
     *
     * @param
     * @return
     */
    <I, E> List<E> find(Converter<I, E> converter);

    //////////////////////////////////////////////

    /**
     * 获取一个结果
     * 如果没有数据，可能返回null
     *
     * @param <E>
     * @return
     */
    default <E> E findOne() {
        return findOne(false);
    }

    /**
     * 获取一个结果
     * 如果没有数据，可能返回null
     *
     * @param <E>
     * @return
     */
    default <E> E findUnique() {
        return findOne(true);
    }

    /**
     * 获取一个结果
     *
     * @param isExpectUnique 是否预期唯一，如果true，但是查询结果不唯一将抛出异常，
     * @param <E>
     * @return
     */
    default <E> E findOne(boolean isExpectUnique) {
        return findOne(isExpectUnique, (Class<E>) null);
    }


    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E> targetType 要求的结果类型
     * @return
     */
    default <E> E findOne(Class<E> resultType) {
        return findOne(false, resultType);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E> targetType 要求的结果类型
     * @return
     */
    default <E> E findUnique(Class<E> resultType) {
        return findOne(true, resultType);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E> resultType 要求的结果类型
     * @return
     */
    default <E> E findOne(boolean isExpectUnique, Class<E> resultType) {
        return findOne(isExpectUnique, resultType, 3);
    }


    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E>              resultType 要求的结果类型
     * @param maxCopyDeep      -1，表示不限层级
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     * @return
     */
    default <E> E findOne(Class<E> resultType, int maxCopyDeep, String... ignoreProperties) {
        return findOne(false, resultType, maxCopyDeep, ignoreProperties);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E>              resultType 要求的结果类型
     * @param maxCopyDeep      -1，表示不限层级
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     * @return
     */
    <E> E findOne(boolean isExpectUnique, Class<E> resultType, int maxCopyDeep, String... ignoreProperties);

    /**
     * 获取结果集，并转换成指定的对对象
     *
     * @param
     * @return
     */

    default <I, E> E findOne(Converter<I, E> converter) {
        return findOne(false, converter);
    }

    /**
     * 获取结果集，并转换成指定的对对象
     *
     * @param isExpectUnique
     * @param converter
     * @param <I>
     * @param <E>
     * @return
     */
    default <I, E> E findOne(boolean isExpectUnique, Converter<I, E> converter) {

        if (converter == null) {
            throw new IllegalArgumentException("converter is null");
        }

        Object data = findOne(isExpectUnique);

        return data != null ? converter.convert((I) data) : null;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * 获取主表的查询别名
     *
     * @return alias
     */
    String getAlias();

}
