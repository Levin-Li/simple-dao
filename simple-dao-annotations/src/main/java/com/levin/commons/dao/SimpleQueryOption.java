package com.levin.commons.dao;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 默认的查询选项
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class SimpleQueryOption implements QueryOption {

    boolean Native;

    Class<?> entityClass;

    String entityName;

    Class<?> resultClass;

    String alias;

    JoinOption[] joinOptions;

    SimpleJoinOption[] simpleJoinOptions;

}
