package com.levin.commons.dao.support;

import org.hibernate.dialect.PostgreSQL10Dialect;

import java.sql.Types;

/**
 * 为了兼容JPA @Lob 注解
 *
 *
 *   使用案例
 *   jpa:
 *     show-sql: false
 *     generate-ddl: true
 *     database: POSTGRESQL     # DB2, DERBY,  H2,  HANA,  HSQL,  INFORMIX,   MYSQL,  ORACLE,  POSTGRESQL, SQL_SERVER,   SYBASE;
 *     database-platform: com.levin.commons.dao.support.LobPostgreSQL10Dialect
 *
 *
 */
public class LobPostgreSQL10Dialect
        extends PostgreSQL10Dialect {

    public LobPostgreSQL10Dialect() {
        super();
        registerColumnType(Types.CLOB, "text");
        registerColumnType(Types.BLOB, "bytea");
    }
}
