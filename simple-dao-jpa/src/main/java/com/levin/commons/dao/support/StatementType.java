package com.levin.commons.dao.support;

/**
 * 语句类型
 */
public enum StatementType {

    Where,
    Select,
    Update,
    Stat, //统计语句，包括GroupBy ,Avg等
    OrderBy,
    GroupBy,
    Logic

}
