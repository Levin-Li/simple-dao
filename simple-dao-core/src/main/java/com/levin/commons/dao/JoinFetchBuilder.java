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

    default T joinFetch(LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return joinFetch(true, null, lambdaMethodAttrs);
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

    default T joinFetch(Boolean isAppend, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return joinFetch(isAppend, null, lambdaMethodAttrs);
    }

    /**
     * 增加要抓取的集合
     *
     * @param joinType
     * @param setAttrs
     * @return
     */
    T joinFetch(Boolean isAppend, Fetch.JoinType joinType, String... setAttrs);

    default T joinFetch(Boolean isAppend, Fetch.JoinType joinType, LambdaMethodAttr<DOMAIN, ?>... lambdaMethodAttrs) {
        return joinFetch(isAppend, joinType, Stream.of(lambdaMethodAttrs).filter(Objects::nonNull).map(LambdaMethodAttr::getAttrName).toArray(String[]::new));
    }

}
