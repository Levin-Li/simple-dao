package com.levin.commons.dao;


/**
 * Having语句构建
 */
public interface HavingBuilder<T extends HavingBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 增加Having表达式，可设置参数
     *
     * @param statement
     * @param paramValues
     * @return
     */
    default T having(String statement, Object... paramValues) {
        return having(true, statement, paramValues);
    }

    /**
     * 增加Having表达式，可设置参数
     * <p>
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
     * @param isAppend
     * @param statement
     * @param paramValues
     * @return
     */
    T having(Boolean isAppend, String statement, Object... paramValues);
}
