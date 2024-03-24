package com.levin.commons.dao;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 选择语句构建
 */
public interface SelectBuilder<T extends SelectBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 增加选择字段
     *
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    default T select(String... columnNames) {
        return select(true, columnNames);
    }

    default T select(PFunction<DOMAIN, ?>... attrGetFunctions) {
        return select(true, attrGetFunctions);
    }

    /**
     * 增加选择字段
     *
     * @param isAppend
     * @param attrGetFunctions
     * @return
     */
    default T select(Boolean isAppend, PFunction<DOMAIN, ?>... attrGetFunctions) {

        //快速返回，优化性能
        if (!Boolean.TRUE.equals(isAppend)) {
            return (T) this;
        }

        return select(isAppend, Stream.of(attrGetFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

    /**
     * 增加选择字段
     * <p>
     *
     * @param columnNames 例：  "name" , "createTime"
     * @return
     */
    T select(Boolean isAppend, String... columnNames);


    /**
     * 增加选择表达式，可设置参数
     *
     * @param statement
     * @param paramValues
     * @return
     */
    default T selectByStatement(String statement, Object... paramValues) {
        return selectByStatement(true, statement, paramValues);
    }

    /**
     * 增加选择表达式，可设置参数
     *
     * @param isAppend
     * @param statement
     * @param paramValues
     * @return
     */
    T selectByStatement(Boolean isAppend, String statement, Object... paramValues);

}
