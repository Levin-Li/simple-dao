package com.levin.commons.dao;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 分组语句构建
 */
public interface GroupByBuilder<T extends GroupByBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 增加分组字段
     *
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    default T groupBy(String... columnNames) {
        return groupBy(true, columnNames);
    }

    default T groupBy(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return groupBy(true, lambdaMethodAttrs);
    }

    /**
     * 增加分组字段
     * <p>
     * 如果不填写，默认为 Desc
     *
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    T groupBy(Boolean isAppend, String... columnNames);

    /**
     * 增加分组字段
     *
     * @param isAppend
     * @param lambdaMethodAttrs
     * @return
     */
    default T groupBy(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {

        //快速返回，优化性能
        return (Boolean.TRUE.equals(isAppend)) ? groupBy(isAppend, Stream.of(lambdaMethodAttrs).filter(Objects::nonNull).map(LambdaMethodAttr::getAttrName).toArray(String[]::new)) : (T) this;
    }


    /**
     * 增加分组表达式，可设置参数
     *
     * @param statement
     * @param paramValues
     * @return
     */
    default T groupByStatement(String statement, Object... paramValues) {
        return groupByStatement(true, statement, paramValues);
    }

    /**
     * 增加分组表达式，可设置参数
     *
     * @param isAppend
     * @param statement
     * @param paramValues
     * @return
     */
    T groupByStatement(Boolean isAppend, String statement, Object... paramValues);
}
