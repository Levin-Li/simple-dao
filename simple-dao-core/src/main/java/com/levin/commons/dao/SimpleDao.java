package com.levin.commons.dao;

import com.levin.commons.dao.support.PagingData;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.BiConsumer;

public interface SimpleDao extends MiniDao, DaoFactory {

    /**
     * 更新或是创建
     *
     * @param entity
     * @return
     */
    @Transactional
    default <E> E save(Object entity) {
        return save(entity, false);
    }

    /**
     * 更新或是创建
     *
     * @param entity
     * @param <E>
     * @return
     */
    @Transactional
    <E> E save(Object entity, boolean isCheckUnionValue);

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
     * 更新一条记录
     * <p>
     * 如果更新的记录数为多条，将抛出异常并回滚
     *
     * @param queryObjs
     * @return 如果被更新的记录数为 0，则返回false, 被更新的记录数为 1 返回TRUE，如果被更新的记录数大于1则抛出异常
     */
    @Transactional
    boolean singleUpdateByQueryObj(Object... queryObjs);

    /**
     * 通过查询对象删除
     *
     * @return
     */
    @Transactional
    int deleteByQueryObj(Object... queryObjs);


    /**
     * 删除一条记录
     * <p>
     * 如果删除的记录数为多条，将抛出异常并回滚
     *
     * @param queryObjs
     * @return 如果被删除的记录数为 0，则返回false, 被删除的记录数为 1 返回TRUE，如果被删除的记录数大于1则抛出异常
     */
    @Transactional
    boolean singleDeleteByQueryObj(Object... queryObjs);
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

    /**
     * 通过查询对象查询唯一约束的对象ID
     *
     * @param queryObj
     * @param entityClass
     * @param <ID>
     * @return
     */
    <ID> ID findUniqueEntityId(@NotNull Object queryObj, @Nullable Class<?> entityClass, BiConsumer<ID, String> onFind);
    /////////////////////////////////////////////////////////////////

    /**
     * 统计数量
     *
     * @param queryObjs 统计语句
     * @return
     */
    long countByQueryObj(Object... queryObjs);

    /**
     * 查询并返回结果
     * <p>
     * 当有多个查询对象时，返回值以第一个@TargetOption的注解为准
     *
     * @param queryObjs
     * @param <E>
     * @return
     */
    <E> List<E> findByQueryObj(Object... queryObjs);

    /**
     * 查找分页数据
     * <p>
     *
     * @param queryObjs
     * @param <E>
     * @return
     */
    default <E> PagingData<E> findPagingDataByQueryObj(Object... queryObjs) {
        return findPageByQueryObj(PagingData.class, queryObjs);
    }

    /**
     * 查询分页数据
     * <p>
     * 参考注解类PageOption {@link com.levin.commons.dao.PageOption}
     * <p>
     * 参考 PagingData  {@link com.levin.commons.dao.support.PagingData}
     *
     * @param pagingHolderInstanceOrClass 分页结果存放对象，分页对象必须使用 PageOption 进行注解
     * @param queryObjs                   查询对象
     *                                    如果查询对象中没有分页设置，默认 new SimplePaging {@link com.levin.commons.dao.support.SimplePaging}
     * @param <P>
     * @return 返回分页对象
     * @since 2.2.27 新增方法
     */
    <P> P findPageByQueryObj(Object pagingHolderInstanceOrClass, Object... queryObjs);

    /**
     * 查询并统计行数
     *
     * @param queryObjs
     * @param <E>
     * @return
     */
    <E> RS<E> findTotalsAndResultList(Object... queryObjs);

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
