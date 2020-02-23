package com.levin.commons.dao;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MiniDao {


    /**
     * 是否 JPA Dao
     *
     * @return
     */
    boolean isJpa();


    /**
     * 是否是实体类型
     * <p>
     * 对于 jpa 来说 entity 类型是有 @Entity 注解
     *
     * @param type
     * @return
     */
    boolean isEntityType(Class type);


    /**
     * 获取参数的开始位置
     *
     * @return
     */
    int getParamStartIndex();


    /**
     * 创建对象
     * 如果对象有ID标识，将会抛出异常
     *
     * @param entity
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
