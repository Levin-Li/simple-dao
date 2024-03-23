package com.levin.commons.dao;


import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

public interface ConditionBuilder<T extends ConditionBuilder<T, DOMAIN>, DOMAIN>
        extends
        SafeController<T>,
        LogicOP<T>,
        SimpleConditionBuilder<T, DOMAIN>,
        StatementBuilder {

    /**
     * 设置上下文用于表达式求值
     *
     * @param context
     * @return this
     */
    T setContext(Map<String, Object> context);

    /**
     * 获取上下文
     *
     * @return
     */
    Map<String, Object> getContext();

    /**
     * 设置查询结果集的的范围
     * <p/>
     * <p/>
     * 目前只对查询有效，对于更新和删除语句无效
     *
     * @param rowStartPosition ，从0开始， -1表示不设置限制
     * @param rowCount         ,受影响的记录数，-1表示不设置限制
     * @return this
     * @see
     * @since 1.1.5
     */
    T limit(int rowStartPosition, int rowCount);

    /**
     * 允许自动追加 limit 语句
     * <p>
     * Jpa 对于更新或是删除语句并不会启用 limit ，该参数主要为了弥补这个问题
     * <p>
     * 默认是允许的
     * 该选项对不影响查询，只作用于更新或是删除
     * <p>
     * 这个方法用于更新或是删除时，限制记录数
     * <p>
     * 请注意，通常只对 Mysql 的 limit 语法兼容
     *
     * @return
     */
    T enableAutoAppendLimitStatement(boolean enable);

    /**
     * 禁用更新和删除的条件
     *
     * @return
     * @see EntityOption#updateCondition()
     * @see EntityOption#deleteCondition()
     */
    T disableOperationCondition();

    /**
     * 设置分页
     *
     * @param pageIndex 第几页，从1开始
     * @param pageSize  分页大小
     * @return
     * @see #limit(int, int)
     */
    T page(int pageIndex, int pageSize);

    /**
     * 设置分页
     *
     * @return
     * @see #limit(int, int)
     */
    T page(Paging paging);

    /**
     * 当前limit 是否处于安全范围
     *
     * @return
     */
    boolean isSafeLimit();

    /**
     * 禁用表名和字段名称转换
     * <p>
     * 当原生查询模式时，默认字段名称会自动装换，但也会影响性能
     * <p>
     * 如果你确认字段名称正确，建议你调用这个方法
     *
     * <p>
     * 禁用名称转换
     *
     * @return
     */
    T disableNameConvert();

    /**
     * 过滤操作已经被逻辑删除的数据
     * 默认被逻辑删除的数据不会被查询出来
     *
     * @return
     */
    T filterLogicDeletedData(boolean enable);

    /**
     * 增加where条件及参数
     *
     * @param expr
     * @param paramValues 参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理
     *                    是Map时，会当成命名参数进行处理，当Map中的key是Int时，会当成位置参数来使用
     * @return
     */
    default T where(String expr, Object... paramValues) {
        return this.where(true, expr, paramValues);
    }

    /**
     * 增加where条件及参数
     *
     * @param isAppend    方便保持链式调用，是否加入条件，条件当isAppend为true时，才会把条件和参数加入where
     * @param expr        如 name = ?，或name = :pName
     * @param paramValues 参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理
     *                    是Map时，会当成命名参数进行处理，当Map中的key是Int时，会当成位置参数来使用
     * @return this
     */
    T where(Boolean isAppend, String expr, Object... paramValues);

    /**
     * 把表达式加到最后
     * <p>
     * 比如 加 limits 的一些操作
     *
     * @param isAppend
     * @param expr
     * @param paramValues
     * @return
     */
    T appendToLast(Boolean isAppend, String expr, Object... paramValues);

    /**
     * 自动把对象的所有属性枚举出来
     * <p/>
     * <p/>
     * 支持使用条件Map做为查询条件
     * Map支持EL表达式，可参考appendWhereByEL方法
     *
     * @param queryObjs 可以值对象要求打上com.levin.commons.dao.annotation注解
     * @return
     */
    T appendByQueryObj(Object... queryObjs);

    /**
     * 通过方法增加查询条件
     *
     * @param method
     * @param args
     * @return
     */
    T appendByMethodParams(Object methodOwnerBean, Method method, Object... args);

    /**
     * 按文本表达式构建查询条件
     * <p>
     * <p/>
     * 如：属性名Q_Not_Like_name  值 llw，表f示会生成查询条件 name not like '%llw%'
     * 注意时间的文本表达式："2016/07/16 23:59:07"
     * <p/>
     * <p/>
     * param.put("Q_name", "llw");
     * param.put("nickName", "llw");
     * param.put("Q_Like_name", "llw");
     * param.put("Q_Gt_date1", new Date());
     * param.put("Q_Lt_date2", new Date());
     * param.put("Q_Gte_date3", new Date());
     * param.put("Q_Lte_date4", new Date());
     * param.put("Q_Not_gt_date5", new Date());
     * param.put("Q_NotLike_date6", new Date());
     * param.put("Q_NotEq_date7", new Date());
     * <p/>
     * param.put("Q_NotNull_date8","2016/07/16 23:59:07");
     * param.put("Q_NotLike_date9", new Date());
     * <p/>
     * <p/>
     * param.put("Q_NotLike_", "llw");
     * param.put("Q_name1", "llw");
     * param.put("Q_Not_Contains_name2", "llw");
     * param.put("Q_StartsWith_name3", "llw");
     * <p/>
     * param.put("Q_Not_EndsWith_name5", "llw");
     * param.put("name6", "llw");
     *
     * @param paramPrefix，Key前缀，例如：Q_
     * @param queryParams             map
     * @return
     */
    T appendByEL(String paramPrefix, Map<String, Object>... queryParams);

    /**
     * 通过注解的方式增加条件
     * <p>
     * 例如： table.xx is null or table.xx = aValue
     * <p>
     * 可以通过调用这个方法实现（true,"xx",aValue,OR.class,IsNull.class,Eq.class,END.class）
     *
     * @param isAppend  是否增加，方便保持链式调用
     * @param attrName  不能为空
     * @param attrValue
     * @param annoTypes
     * @return this
     */
    T appendByAnnotations(Boolean isAppend, @NotNull String attrName, Object attrValue, Class<? extends Annotation>... annoTypes);

}
