package com.levin.commons.dao.codegen.db;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

@Data
@Accessors(chain = true)
@ToString
public class DbConfig {

    private DbType dbType;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * schema(PGSQL专用)
     */
    private String schemaName;

    /**
     * 数据库host
     */
    private String host;

    /**
     * 数据库端口
     */
    private Integer port;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    private String jdbcUrl;


    public String getDriverClass() {

        if (dbType == null) {
            throw new RuntimeException("不支持数据库类型" + this.dbType + "，请在DbType.java中配置");
        }

        return dbType.getDriverClass();
    }

    public DbConfig setJdbcUrl(String jdbcUrl) {

        if (StringUtils.hasText(jdbcUrl)) {
            this.dbType = DbType.guess(jdbcUrl);
        }

        this.jdbcUrl = jdbcUrl;

        return this;
    }

    public String getJdbcUrl() {

        if (StringUtils.hasText(this.jdbcUrl)) {
            return this.jdbcUrl;
        }

        if (dbType == null) {
            throw new RuntimeException("不支持数据库类型" + this.dbType + "，请在DbType.java中配置");
        }

        String jdbcUrl = dbType.getJdbcUrl();

        return String.format(jdbcUrl, host, port, dbName);
    }

}
