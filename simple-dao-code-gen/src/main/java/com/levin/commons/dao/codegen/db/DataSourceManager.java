package com.levin.commons.dao.codegen.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanghc
 */
public class DataSourceManager {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);

    private static final Map<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>(16);
    private static final ThreadLocal<Connection> CONNECTION_LOCAL = new ThreadLocal<>();

    public static Connection getConnection(DbConfig generatorConfig) {

        Connection connection = CONNECTION_LOCAL.get();

        if (connection == null) {
            try {
                connection = getDataSource(generatorConfig).getConnection();
                CONNECTION_LOCAL.set(connection);
            } catch (SQLException e) {
                logger.error("获取Connection失败, jdbcUrl:{}", generatorConfig.getJdbcUrl(), e);
                throw new RuntimeException("获取Connection失败", e);
            }
        }

        return connection;

    }


    public static DataSource getDataSource(DbConfig generatorConfig) {

        String jdbcUrl = generatorConfig.getJdbcUrl() + ":" + generatorConfig.getUsername();

        DataSource dataSource = DATA_SOURCE_MAP.computeIfAbsent(jdbcUrl, key -> {
            Properties properties = new Properties();
            properties.put("driverClassName", generatorConfig.getDriverClass());
            properties.put("url", generatorConfig.getJdbcUrl());
            properties.put("username", generatorConfig.getUsername());
            properties.put("password", generatorConfig.getPassword());
            // 初始连接数
            properties.put("initialSize", 1);
            // 最大活跃数
            properties.put("maxTotal", 30);
            properties.put("minIdle", 5);
            properties.put("maxIdle", 10);
            // 最长等待时间(毫秒)
            properties.put("maxWaitMillis", 1000);
            // 程序中的连接不使用后是否被连接池回收
            properties.put("removeAbandonedOnMaintenance", true);
            properties.put("removeAbandonedOnBorrow", true);
            // 连接在所指定的秒数内未使用才会被删除(秒)
            properties.put("removeAbandonedTimeout", 5);

            return new DriverManagerDataSource(jdbcUrl,properties);

        });

        if (dataSource == null) {
            throw new RuntimeException("连接数据库失败");
        }

        return dataSource;
    }

    public static DataSource getDataSource_OLD(DbConfig generatorConfig) {

        String jdbcUrl = generatorConfig.getJdbcUrl() + ":" + generatorConfig.getUsername();

        DataSource dataSource = DATA_SOURCE_MAP.computeIfAbsent(jdbcUrl, key -> {
            Properties properties = new Properties();
            properties.put("driverClassName", generatorConfig.getDriverClass());
            properties.put("url", generatorConfig.getJdbcUrl());
            properties.put("username", generatorConfig.getUsername());
            properties.put("password", generatorConfig.getPassword());
            // 初始连接数
            properties.put("initialSize", 1);
            // 最大活跃数
            properties.put("maxTotal", 30);
            properties.put("minIdle", 5);
            properties.put("maxIdle", 10);
            // 最长等待时间(毫秒)
            properties.put("maxWaitMillis", 1000);
            // 程序中的连接不使用后是否被连接池回收
            properties.put("removeAbandonedOnMaintenance", true);
            properties.put("removeAbandonedOnBorrow", true);
            // 连接在所指定的秒数内未使用才会被删除(秒)
            properties.put("removeAbandonedTimeout", 5);
            try {
              //  return BasicDataSourceFactory.createDataSource(properties);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        if (dataSource == null) {
            throw new RuntimeException("连接数据库失败");
        }

        return dataSource;
    }

}
