package com.levin.commons.dao;

import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.order.OrderBy;

import java.util.List;

/**
 * 查询接口
 */
public interface SelectDao<T> extends ConditionBuilder<SelectDao<T>>, SimpleStatBuilder<SelectDao<T>> {

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
     * 设置的分页
     *
     * @param pageIndex 第几页，从1开始
     * @param pageSize  分页大小
     * @return
     * @see #limit(int, int)
     */
    @Override
    SelectDao<T> page(int pageIndex, int pageSize);


    /**
     * 设置的分页
     *
     * @return
     * @see #limit(int, int)
     */
    @Override
    SelectDao<T> page(Paging paging);

    ////////////////////////////////////////////////////////////////////////////

    /**
     * 增加select 列表达式，列可以设置参数
     *
     * @param expr
     * @param paramValues
     * @return
     */

    SelectDao<T> select(String expr, Object... paramValues);

    /**
     * 增加要选择的列表达式
     *
     * @param isAppend    是否增加，主要用于方便链式调用
     * @param expr        eg  " a.name , b.name "
     * @param paramValues 参数列表
     * @return
     */
    SelectDao<T> select(Boolean isAppend, String expr, Object... paramValues);


    /**
     * 增加要选择的列，会自动尝试加上别名
     *
     * @param columnNames 单个列名，eg. name , age
     * @return
     */
    SelectDao<T> select(String... columnNames);
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 在有必要的情况下，增加连接查询条件
     * <p/>
     * 关联 (join)
     * JPQL 仍然支持和 SQL 中类似的关联语法：
     * left out join/left join
     * inner join
     * left join fetch/inner join fetch
     * <p/>
     * <p/>
     * left out join/left join 等， 都是允许符合条件的右边表达式中的 Entiies 为空（ 需要显式使用 left join/left outer join 的情况会比较少。 ）
     * 例：
     * // 获取 26 岁人的订单 , 不管 Order 中是否有 OrderItem
     * select o from Order o left join o.orderItems where o.ower.age=26 order by o.orderid
     * <p/>
     * <p/>
     * inner join 要求右边的表达式必须返回 Entities 。
     * 例：
     * // 获取 26 岁人的订单 ,Order 中必须要有 OrderItem
     * select o from Order o inner join o.orderItems where o.ower.age=26 order by o.orderid
     * <p/>
     * <p/>
     * ！！重要知识点 ： 在默认的查询中， Entity 中的集合属性默认不会被关联，集合属性默认是延迟加载 ( lazy-load ) 。那么， left fetch/left out fetch/inner join fetch 提供了一种灵活的查询加载方式来提高查询的性能。
     * 例：
     * private String QueryInnerJoinLazyLoad(){
     * // 默认不关联集合属性变量 (orderItems) 对应的表
     * Query find = em.createQuery("select o from Order o inner join o.orderItems where o.ower.age=26 order by o.orderid");
     * List result = find.getResultList();
     * if (result!=null && result.size()>0){
     * // 这时获得 Order 实体中 orderItems( 集合属性变量 ) 为空
     * Order order = (Order) result.get(0);
     * // 当需要时， EJB3 Runtime 才会执行一条 SQL 语句来加载属于当前 Order 的
     * //OrderItems
     * Set<OrderItem> list = order.getOrderItems ();
     * Iterator<OrderItem> iterator = list.iterator();
     * if (iterator.hasNext()){
     * OrderItem orderItem =iterator.next();
     * System.out.println (" 订购产品名： "+ orderItem.getProductname());
     * }
     * }
     * <p/>
     * <p/>
     * 为了避免 N+1 的性能问题，我们可以利用 join fetch 一次过用一条 SQL 语句把 Order 的所有信息查询出来
     * select o from Order o inner join fetch o.orderItems where o.ower.age=26 order by o.orderid
     *
     * @param joinStatements inner join 和left join 表达式
     */
    SelectDao<T> join(String... joinStatements);

    /**
     * 增加连接语句
     *
     * @param isAppend
     * @param joinStatements
     * @return
     */
    SelectDao<T> join(Boolean isAppend, String... joinStatements);


    /**
     * 笛卡儿积
     *
     * @param isAppend
     * @param targetClass
     * @param targetAlias
     * @return
     */
    SelectDao<T> join(Boolean isAppend, Class targetClass, String targetAlias);


    /**
     * 笛卡儿积
     *
     * @param isAppend
     * @param joinOptions
     * @return
     */
    SelectDao<T> join(Boolean isAppend, SimpleJoinOption... joinOptions);

    /**
     * join
     *
     * @param isAppend
     * @param joinOptions
     * @return
     */
    SelectDao<T> join(Boolean isAppend, JoinOption... joinOptions);
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 增加要抓取的集合
     * <p>
     * 连接抓取
     *
     * @param setAttrs
     * @return
     */
    SelectDao<T> joinFetch(String... setAttrs);

    /**
     * @param isAppend
     * @param setAttrs
     * @return
     */
    SelectDao<T> joinFetch(Boolean isAppend, String... setAttrs);

    /**
     * 增加要抓取的集合
     *
     * @param joinType
     * @param setAttrs
     * @return
     */
    SelectDao<T> joinFetch(Fetch.JoinType joinType, String... setAttrs);

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * SQL语句执行过程：
     * FROM子句
     * WHERE子句
     * GROUP BY子句  不能使用别名
     * HAVING子句
     * SELECT子句
     * ORDER BY子句
     * <p>
     * 增加 group by 字段列表
     *
     * @param columnNames 字段列表，会尝试自动增加别名
     * @return
     */
    SelectDao<T> groupBy(String... columnNames);

    /**
     * 增加 group by 语句和参数
     *
     * @param expr        表达式
     * @param paramValues 参数
     * @return
     */
    SelectDao<T> groupBy(String expr, Object... paramValues);

    /**
     * 增加 group by 语句和参数
     *
     * @param expr        表达式
     * @param paramValues 参数
     * @return
     */
    SelectDao<T> groupBy(Boolean isAppend, String expr, Object... paramValues);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Having与Where的区别
     * where 子句的作用是在对查询结果进行分组前，将不符合where条件的行去掉，即在分组之前过滤数据，where条件中不能包含聚组函数，使用where条件过滤出特定的行。
     * having 子句的作用是筛选满足条件的组，即在分组之后过滤数据，条件中经常包含聚组函数，使用having 条件过滤出特定的组，也可以使用多个分组标准进行分组。
     * 示例1:
     * <p/>
     * select 类别, sum(数量) as 数量之和 from A
     * group by 类别
     * having sum(数量) > 18
     * 示例2：Having和Where的联合使用方法
     * <p/>
     * select 类别, SUM(数量)from A
     * where 数量 gt;8
     * group by 类别
     * having SUM(数量) > 10
     *
     * @param havingStatement
     * @param paramValues
     * @return
     */
    SelectDao<T> having(String havingStatement, Object... paramValues);

    /**
     * 增加 having 字句
     *
     * @param isAppend
     * @param havingStatement
     * @param paramValues
     * @return
     */
    SelectDao<T> having(Boolean isAppend, String havingStatement, Object... paramValues);
    /////////////////////////////////////// 排序支持 ///////////////////////////////////////////////////////////////////////////

    /**
     * 增加排序字段
     *
     * @param columnNames 例：  "name desc" , "createTime desc"
     * @return
     */
    SelectDao<T> orderBy(String... columnNames);

    /**
     * 增加排序字段
     *
     * @param isAppend
     * @param columnNames 例：  "name desc" , "createTime desc"
     * @return
     */
    SelectDao<T> orderBy(Boolean isAppend, String... columnNames);

    /**
     * 增加排序字段
     *
     * @param type        如果不填写，默认为 Desc
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    SelectDao<T> orderBy(OrderBy.Type type, String... columnNames);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 记录总数
     * COUNT() ，返回类型为 Long ，注意 count(*) 语法在 hibernate 中可用，但在 toplink 其它产品中并不可用
     * 目前该方法有一个bug，如果查询本身已经是一个统计语句或是分组统计语句，可能将导致错误
     * 遇到这种情况，建议手动编写统计，以确保正确
     *
     * @return long
     */
    long count();

///////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E> targetType 要求的结果类型
     * @return
     */
    <E> List<E> find(Class<E> targetType);

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E>              targetType 要求的结果类型
     * @param maxCopyDeep      -1，表示不限层级
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     * @return
     */
    <E> List<E> find(Class<E> targetType, int maxCopyDeep, String... ignoreProperties);

    /**
     * 获取结果集，并转换成指定的对对象
     *
     * @param
     * @return
     */
    <I, E> List<E> find(Converter<I, E> converter);


    /**
     * 获取结果集
     *
     * @param <E>
     * @return
     */
    <E> List<E> find();

    //////////////////////////////////////////////

    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E> targetType 要求的结果类型
     * @return
     */
    <E> E findOne(Class<E> targetType);


    /**
     * 获取结果集，并转换成指定的对对象
     * 数据转换采用spring智能转换器
     *
     * @param <E>              targetType 要求的结果类型
     * @param maxCopyDeep      -1，表示不限层级
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     * @return
     */
    <E> E findOne(Class<E> targetType, int maxCopyDeep, String... ignoreProperties);

    /**
     * 获取结果集，并转换成指定的对对象
     *
     * @param
     * @return
     */

    <I, E> E findOne(Converter<I, E> converter);


    /**
     * 获取一个结果
     * 如果没有数据，可能返回null
     *
     * @param <E>
     * @return
     */
    <E> E findOne();
    ////////////////////////////////////////////////////////////////////////////

    /**
     * 获取主表的查询别名
     *
     * @return alias
     */
    String getAlias();

    /**
     * 智能属性拷贝
     *
     * @param source
     * @param target
     * @param ignoreProperties 忽略目标对象的属性
     *                         a.b.c.name* *号表示忽略以什么开头的属性
     *                         a.b.c.{*}    大括号表示忽略所有的复杂类型属性
     *                         a.b.c.{com.User}    大括号表示忽略User类型属性
     * @return
     */
    <E extends Object> E copyProperties(Object source, E target, String... ignoreProperties);

}
