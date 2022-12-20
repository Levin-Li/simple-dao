package com.levin.commons.dao.codegen.db.sqlserver;

import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.SQLService;
import com.levin.commons.dao.codegen.db.TableSelector;

public class SqlServerService implements SQLService {

	@Override
	public TableSelector getTableSelector(DbConfig generatorConfig) {
		return new SqlServerTableSelector(new SqlServerColumnSelector(generatorConfig), generatorConfig);
	}

}
