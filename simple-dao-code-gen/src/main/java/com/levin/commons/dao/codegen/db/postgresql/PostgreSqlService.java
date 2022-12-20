package com.levin.commons.dao.codegen.db.postgresql;

import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.SQLService;
import com.levin.commons.dao.codegen.db.TableSelector;

/**
 * @author tanghc
 */
public class PostgreSqlService implements SQLService {
    @Override
    public TableSelector getTableSelector(DbConfig generatorConfig) {
        return new PostgreSqlTableSelector(new PostgreSqlColumnSelector(generatorConfig), generatorConfig);
    }

}
