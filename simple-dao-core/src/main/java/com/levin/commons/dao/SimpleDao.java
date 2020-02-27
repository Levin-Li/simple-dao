
package com.levin.commons.dao;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface SimpleDao extends MiniDao {


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
    <T> T copyProperties(Object source, Object target, String... ignoreProperties);


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