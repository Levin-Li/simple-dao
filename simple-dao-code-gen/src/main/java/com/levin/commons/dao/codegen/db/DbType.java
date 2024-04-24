package com.levin.commons.dao.codegen.db;

import org.springframework.util.Assert;

import java.util.stream.Stream;

/**
 * @author tanghc
 */
public enum DbType {

    MYSQL(1,
            "MySQL",
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
    ),
    ORACLE(2,
            "Oracle",
            "oracle.jdbc.driver.OracleDriver",
            "jdbc:oracle:thin:@%s:%s%s"),

    SQL_SERVER(3,
            "SQL Server",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jdbc:sqlserver://%s:%s;DatabaseName=%s"),

    POSTGRE_SQL(4,
            "PostgreSQL",
            "org.postgresql.Driver",
            "jdbc:postgresql://%s:%s/%s"),

    DM(5,
            "DM",
            "dm.jdbc.driver.DmDriver",
            "jdbc:dm://%s:%s/%s"),

    ;

    private final int type;
    private final String displayName;
    private final String driverClass;
    private final String jdbcUrl;

    DbType(int type, String displayName, String driverClass, String jdbcUrl) {
        this.type = type;
        this.displayName = displayName;
        this.driverClass = driverClass;
        this.jdbcUrl = jdbcUrl;
    }

    public static DbType of(int type) {
        for (DbType value : DbType.values()) {
            if (value.type == type) {
                return value;
            }
        }
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public int getType() {
        return type;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public static DbType guess(String jdbcUrl) {

        Assert.hasText(jdbcUrl, "jdbcUrl is blank");

        int index = jdbcUrl.indexOf(":");

        Assert.isTrue(index > -1, "incorrect jdbcUrl " + jdbcUrl);

        index = jdbcUrl.indexOf(":", index + 1);

        Assert.isTrue(index > -1, "incorrect jdbcUrl " + jdbcUrl);

        String prefix = jdbcUrl.substring(0, index + 1);

        return Stream.of(values()).filter(dbType -> dbType.jdbcUrl.startsWith(prefix)).findFirst().orElse(null);

    }

    public static void main(String[] args) {

        System.out.println(guess("jdbc:postgresql://host:54321/dbName"));

    }
}
