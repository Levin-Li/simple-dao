package com.levin.commons.dao.codegen.db.dm;

import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.SQLService;
import com.levin.commons.dao.codegen.db.TableSelector;

public class DmService implements SQLService {

	@Override
	public TableSelector getTableSelector(DbConfig generatorConfig) {
		return new DmTableSelector(new DmColumnSelector(generatorConfig), generatorConfig);
	}

}
