package com.levin.commons.dao.codegen.db;

import com.levin.commons.dao.codegen.db.dm.DmService;
import com.levin.commons.dao.codegen.db.mysql.MySqlService;
import com.levin.commons.dao.codegen.db.oracle.OracleService;
import com.levin.commons.dao.codegen.db.postgresql.PostgreSqlService;
import com.levin.commons.dao.codegen.db.sqlserver.SqlServerService;

import java.util.HashMap;
import java.util.Map;

public class SQLServiceFactory {

    private static final Map<Integer, SQLService> SERVICE_CONFIG = new HashMap<>(16);

    static {
        SERVICE_CONFIG.put(DbType.MYSQL.getType(), new MySqlService());
        SERVICE_CONFIG.put(DbType.ORACLE.getType(), new OracleService());
        SERVICE_CONFIG.put(DbType.SQL_SERVER.getType(), new SqlServerService());
        SERVICE_CONFIG.put(DbType.POSTGRE_SQL.getType(), new PostgreSqlService());
        SERVICE_CONFIG.put(DbType.DM.getType(), new DmService());

    }

    public static SQLService build(DbConfig generatorConfig) {
        SQLService service = SERVICE_CONFIG.get(generatorConfig.getDbType().getType());
        if (service == null) {
            throw new RuntimeException("本系统暂不支持该数据源(" + generatorConfig.getDriverClass() + ")");
        }
        return service;
    }

}
