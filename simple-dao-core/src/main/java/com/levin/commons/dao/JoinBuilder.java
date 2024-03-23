package com.levin.commons.dao;

import com.levin.commons.dao.annotation.misc.Fetch;


/**
 * 连接语句构建
 */
public interface JoinBuilder<T extends JoinBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 增加连接语句
     * <p>
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
    default T join(String... joinStatements) {
        return join(true, joinStatements);
    }

    /**
     * 增加连接语句
     *
     * @param isAppend
     * @param joinStatements
     * @return
     */
    T join(Boolean isAppend, String... joinStatements);

    /**
     * 笛卡儿积
     * 自然连接
     *
     * @param isAppend
     * @param joinOptions
     * @return
     */
    T join(Boolean isAppend, SimpleJoinOption... joinOptions);

    /**
     * 增加连接
     *
     * @param isAppend
     * @param joinOptions
     * @return
     */
    T join(Boolean isAppend, JoinOption... joinOptions);


    /**
     * 左连接
     *
     * @param entityClass
     * @return
     */
    default T leftJoin(Class<?> entityClass) {
        return leftJoin(entityClass, "");
    }

    /**
     * 左连接
     * 默认连接主要的主键字段
     *
     * @param entityClass
     * @param alias
     * @return
     */
    default T leftJoin(Class<?> entityClass, String alias) {
        return leftJoin(entityClass, alias, "");
    }

    /**
     * 左连接
     * 默认连接主要的主键字段
     *
     * @param entityClass
     * @param alias
     * @param joinColumn
     * @return
     */
    default T leftJoin(Class<?> entityClass, String alias, String joinColumn) {
        return join(true, Fetch.JoinType.Left, entityClass, alias, joinColumn, "", "");
    }

    /**
     * 左连接
     *
     * @param entityClass
     * @param alias
     * @param joinColumn
     * @param joinTargetAlias
     * @param joinTargetColumn
     * @return
     */
    default T leftJoin(Class<?> entityClass, String alias, String joinColumn, String joinTargetAlias, String joinTargetColumn) {
        return join(true, Fetch.JoinType.Left, entityClass, alias, joinColumn, joinTargetAlias, joinTargetColumn);
    }

    /**
     * 增加连接
     *
     * @param isAppend
     * @param joinType         如果不填，默认为左连接
     * @param entityClass
     * @param alias
     * @param joinColumn
     * @param joinTargetAlias
     * @param joinTargetColumn
     * @return
     */
    T join(Boolean isAppend, Fetch.JoinType joinType, Class<?> entityClass, String alias, String joinColumn, String joinTargetAlias, String joinTargetColumn);

}
