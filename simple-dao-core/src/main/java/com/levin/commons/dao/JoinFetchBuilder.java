package com.levin.commons.dao;


import com.levin.commons.dao.annotation.misc.Fetch;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * JPA特有的连接抓取语句构建
 */
public interface JoinFetchBuilder<T extends JoinFetchBuilder<T, DOMAIN>, DOMAIN> {

    /**
     * 增加要抓取的集合
     * <p>
     * 连接抓取
     *
     * @param setAttrs
     * @return
     */
    default T joinFetch(String... setAttrs) {
        return joinFetch(true, setAttrs);
    }

    default T joinFetch(PFunction<DOMAIN, ?>... attrReadFunctions) {
        return joinFetch(true, null, attrReadFunctions);
    }

    /**
     * 连接抓取
     *
     * @param isAppend
     * @param setAttrs
     * @return
     */
    default T joinFetch(Boolean isAppend, String... setAttrs) {
        return joinFetch(isAppend, null, setAttrs);
    }

    default T joinFetch(Boolean isAppend, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return joinFetch(isAppend, null, attrReadFunctions);
    }

    /**
     * 增加要抓取的集合
     *
     * @param joinType
     * @param setAttrs
     * @return
     */
    T joinFetch(Boolean isAppend, Fetch.JoinType joinType, String... setAttrs);

    default T joinFetch(Boolean isAppend, Fetch.JoinType joinType, PFunction<DOMAIN, ?>... attrReadFunctions) {
        return joinFetch(isAppend, joinType, Stream.of(attrReadFunctions).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

}
