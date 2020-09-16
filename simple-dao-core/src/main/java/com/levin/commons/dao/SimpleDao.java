package com.levin.commons.dao;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;

public interface SimpleDao extends MiniDao, DaoFactory {

    /**
     * 更新或是删除
     *
     * @param entity
     * @return
     */
    @Transactional
    Object save(Object entity);

    /**
     * 删除一个实体对象
     *
     * @param entity
     */
    @Transactional
    void delete(Object entity);

    /**
     * 删除指定id的对象
     * 这个过程是先加载对象，然后再删除对象
     *
     * @param entityClass
     */
    @Transactional
    boolean deleteById(Class entityClass, Object id);

    /**
     * 更新
     * 查询参数可以是数组，也可以是Map会进行自动识别
     *
     * @param statement   更新或是删除语句
     * @param paramValues 参数可紧一个数组,或是Map，或是List，或是具体的参数值，会对参数进行递归处理
     * @return
     */
    @Transactional
    int update(String statement, Object... paramValues);

    /**
     * 更新
     * 查询参数可以是数组，也可以是Map会进行自动识别
     *
     * @param statement   更新或是删除语句
     * @param paramValues 参数可紧一个数组,或是Map，或是List，或是具体的参数值，会对参数进行递归处理
     * @return
     */
    @Transactional
    int update(boolean isNative, String statement, Object... paramValues);

    /**
     * 通过查询对象更新
     *
     * @return
     */
    @Transactional
    int updateByQueryObj(Object... queryObjs);

    /**
     * 通过查询对象删除
     *
     * @return
     */
    @Transactional
    int deleteByQueryObj(Object... queryObjs);

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 通过主键查找
     *
     * @param entityClass
     * @param id
     * @param <T>
     * @return
     */
    <T> T find(Class<T> entityClass, Object id);

    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 查询
     * 查询参数可以是数组，也可以是Map会进行自动识别
     *
     * @param statement
     * @param paramValues 数组中的元素可以是map，数组，或是list,或值对象
     * @param <T>
     * @return
     */
    <T> List<T> find(String statement, Object... paramValues);

    /**
     * 分页查询
     * 查询参数可以是数组，也可以是Map会进行自动识别
     *
     * @param start       要返回的结果集的开始位置 position，从0开始
     * @param count       要返回的记录数
     * @param statement
     * @param paramValues 数组中的元素可以是map，数组，或是list,或值对象
     * @param <T>
     * @return
     */
    <T> List<T> find(int start, int count, String statement, Object... paramValues);

    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取实体ID
     *
     * @param entity
     * @param <ID>
     * @return
     */
    <ID> ID getEntityId(Object entity);

    /**
     * 获取ID属性
     *
     * @param entity
     * @return
     */
    String getEntityIdAttrName(Object entity);

    ///////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 通过查询对象创建 DAO
     *
     * @param daoClass
     * @param queryObjs
     * @param <DAO>
     * @return 返回 SelectDao or UpdateDao or DeleteDao
     */
    <DAO extends ConditionBuilder> DAO newDao(Class<DAO> daoClass, Object... queryObjs);

    /////////////////////////////////////////////////////////////////

    /**
     * 统计数量
     *
     * @param queryObjs 统计语句
     * @return
     */
    long countByQueryObj(Object... queryObjs);

    /**
     * @param queryObjs
     * @param <E>
     * @return
     */
    <E> List<E> findByQueryObj(Object... queryObjs);

    /**
     * @param queryObjs
     * @param <E>
     * @return
     */
    <E> E findOneByQueryObj(Object... queryObjs);

    /**
     * @param resultType
     * @param queryObjs
     * @param <E>
     * @return
     */
    <E> List<E> findByQueryObj(Class<E> resultType, Object... queryObjs);


    /**
     * @param resultType
     * @param queryObjs
     * @param <E>
     * @return
     */
    <E> E findOneByQueryObj(Class<E> resultType, Object... queryObjs);


    /**
     * 智能属性拷贝，使用Spring转换器
     *
     * @param source
     * @param target
     * @param ignoreProperties a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     * @return
     */
    @Deprecated
    <T> T copyProperties(Object source, T target, String... ignoreProperties);

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
    <T> T copyProperties(Object source, Object target, int deep, String... ignoreProperties);

    /**
     * 深度拷贝器
     *
     * @return
     */
    DeepCopier getDeepCopier();


    /**
     * 获取事务管理器
     *
     * @return
     */
    PlatformTransactionManager getTransactionManager();


    /**
     * jsr Validator
     *
     * @return
     */
    Validator getValidator();

}