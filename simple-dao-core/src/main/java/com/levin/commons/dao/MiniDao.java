package com.levin.commons.dao;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MiniDao {

    interface RS<T> {

        /**
         * 获取总记录数
         *
         * @return
         */
        long getTotals();

        /**
         * 获取分页结果
         *
         * @return
         */
        List<T> getResultList();

    }

    /**
     * 默认的占位符,挂号里面不能有空格
     */
    String DEFAULT_JDBC_PARAM_PLACEHOLDER = "?";

    /**
     * 安全模式下，结果集的最大记录数
     *
     * @return
     */
    default int getSafeModeMaxLimit() {
        return 2000;
    }

    /**
     * 是否 JPA Dao
     *
     * @return
     */
    default boolean isJpa() {
        return false;
    }


    /**
     * 获取表或是实体的主键名称
     *
     * @param tableOrEntityClass
     * @return
     */
    default String getPKName(Object tableOrEntityClass) {
        return null;
    }

    /**
     * 获取参数的占位符
     *
     * @param isNative 是否是原生查询
     * @return
     */
    default String getParamPlaceholder(boolean isNative) {
        return DEFAULT_JDBC_PARAM_PLACEHOLDER;
    }


    /**
     * 创建对象
     * 如果对象有ID标识，将会抛出异常
     *
     * @param entity 返回的对象已经托管
     * @return
     */
    @Transactional
    Object create(Object entity);

    /**
     * @param start
     * @param count
     * @param statement   更新或是删除语句
     * @param paramValues 参数可紧一个数组,或是Map，或是List，或是具体的参数值，会对参数进行递归处理
     * @return
     */
    @Transactional
    int update(boolean isNative, int start, int count, String statement, Object... paramValues);


    /**
     * 手动刷新事务，主要用于同个事务中先写后读的时候
     */
    default void flush() {
    }


    /**
     * @param isNative    是否是原生查询
     * @param resultClass 可以为null(结果集将返回对象数组)，或是java.util.Map 或是具体的类
     * @param start       从0开始
     * @param count
     * @param statement
     * @param paramValues 数组中的元素可以是map，数组，或是list,或值对象
     * @param <T>
     * @return
     */
    <T> List<T> find(boolean isNative, Class resultClass, int start, int count, String statement, Object... paramValues);
}
