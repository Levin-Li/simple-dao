package com.levin.commons.dao;


import java.util.Map;

/**
 * 简单统计构建器
 * <p>
 * <p>
 * 使用例子：
 * List<Map> g = dao.selectFrom(Group.class, "g")
 * .join("left join " + User.class.getName() + " u on g.id = u.group.id")
 * .join("left join " + Task.class.getName() + " t on u.id = t.user.id")
 * .count("1", "cnt")
 * .avg("t.score + ${v}", "ts", MapUtils.put("v", (Object) 5L).build())
 * .avg("u.score", "us")
 * .avg("g.score", "gs")
 * .sum("t.score", "ts2")
 * //                .where("u.name = :?","sss")
 * .groupByAndSelect(E_Group.name, "groupName")
 * //                .groupBy("g.name")
 * .orderBy("ts2")
 * .find(Map.class);
 *
 * @param <T>
 * @since 1.1.6
 */
public interface SimpleStatBuilder<T extends SimpleStatBuilder> {

    /**
     * count 函数
     *
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T count(String expr, String alias, Map<String, Object>... paramValues);

    /**
     * avg 函数
     *
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T avg(String expr, String alias, Map<String, Object>... paramValues);

    /**
     * sum 函数
     *
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T sum(String expr, String alias, Map<String, Object>... paramValues);

    /**
     * max 函数
     *
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T max(String expr, String alias, Map<String, Object>... paramValues);

    /**
     * min 函数
     *
     * @param expr
     * @param alias
     * @param paramValues
     * @return
     */
    T min(String expr, String alias, Map<String, Object>... paramValues);

    /**
     * group by 字句
     *
     * <p>
     * 通过模拟 GroupBy 注解的方式增加 groupBy 语句
     *
     * @param expr
     * @param alias
     * @param paramValues 支持表达式中使用参数
     * @return
     */
    T groupByAndSelect(String expr, String alias, Map<String, Object>... paramValues);

}
