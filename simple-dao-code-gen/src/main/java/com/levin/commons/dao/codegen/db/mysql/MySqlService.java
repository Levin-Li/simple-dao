package com.levin.commons.dao.codegen.db.mysql;

import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.SQLService;
import com.levin.commons.dao.codegen.db.TableSelector;

public class MySqlService implements SQLService {

	@Override
	public TableSelector getTableSelector(DbConfig generatorConfig) {
		return new MySqlTableSelector(new MySqlColumnSelector(generatorConfig), generatorConfig);
	}

}
