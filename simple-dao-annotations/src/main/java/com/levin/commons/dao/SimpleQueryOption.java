package com.levin.commons.dao;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
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
