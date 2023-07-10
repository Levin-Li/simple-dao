package com.levin.commons.dao.support;

import org.hibernate.dialect.PostgreSQL10Dialect;

import java.sql.Types;

/**
 * 为了兼容JPA @Lob 注解
 */
public class LobPostgreSQL10Dialect
        extends PostgreSQL10Dialect {

    public LobPostgreSQL10Dialect() {
        super();
        registerColumnType(Types.CLOB, "text");
        registerColumnType(Types.BLOB, "bytea");
    }
}
