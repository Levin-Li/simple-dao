package com.levin.commons.dao.codegen.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

    public static Connection getConnection(DbConfig config) throws ClassNotFoundException, SQLException {
        Class.forName(config.getDriverClass());
        return DriverManager.getConnection(config.getJdbcUrl(),
                config.getUsername(), config.getPassword());
    }

    /**
     * 测试连接,返回错误信息,无返回内容说明连接成功
     *
     * @param generatorConfig 数据源配置
     * @return 返回错误信息, 无返回内容说明连接成功
     */
    public static String testConnection(DbConfig generatorConfig) {
        Connection con = null;
        String ret = null;
        try {
            con = DBConnect.getConnection(generatorConfig);
            // 不为空说明连接成功
            if (con == null) {
                ret = generatorConfig.getDbName() + "连接失败";
            }
        } catch (ClassNotFoundException e) {
            ret = generatorConfig.getDbName() + "连接失败" + "，"
                    + "找不到驱动" + generatorConfig.getDriverClass();
        } catch (SQLException e) {
            ret = generatorConfig.getDbName() + "连接失败" + "，"
                    + e.getMessage();
        } finally {
            if (con != null) {
                try {
                    con.close(); // 关闭连接,该连接无实际用处
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }
}
