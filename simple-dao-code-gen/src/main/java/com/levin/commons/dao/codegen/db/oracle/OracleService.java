package com.levin.commons.dao.codegen.db.oracle;

import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.SQLService;
import com.levin.commons.dao.codegen.db.TableSelector;

public class OracleService implements SQLService {

	@Override
	public TableSelector getTableSelector(DbConfig generatorConfig) {
		return new OracleTableSelector(new OracleColumnSelector(generatorConfig), generatorConfig);
	}

}
