package com.levin.commons.dao.codegen.db.dm;

import com.levin.commons.dao.codegen.db.ColumnSelector;
import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.TableDefinition;
import com.levin.commons.dao.codegen.db.TableSelector;

import java.util.Map;

import static com.levin.commons.dao.codegen.db.util.FieldUtil.convertString;

/**
 * 查询mysql数据库表
 */
public class DmTableSelector extends TableSelector {

	public DmTableSelector(ColumnSelector columnSelector,
						   DbConfig dataBaseConfig) {
		super(columnSelector, dataBaseConfig);
	}

	/**
	 * SELECT a.TABLE_NAME,b.COMMENTS
	 * FROM ALL_TABLES a,USER_TAB_COMMENTS b
	 * WHERE a.TABLE_NAME=b.TABLE_NAME
	 * AND a.OWNER='SYSTEM'
	 * @param generatorConfig generatorConfig
	 * @return
	 */
	@Override
	protected String getShowTablesSQL(DbConfig generatorConfig) {
		String owner = generatorConfig.getSchemaName().toUpperCase();
		StringBuilder sb = new StringBuilder("");
		sb.append("SELECT a.TABLE_NAME AS NAME,b.COMMENTS FROM USER_TABLES a left join USER_TAB_COMMENTS b on a.TABLE_NAME = b.TABLE_NAME ");
		sb.append(" WHERE 1=1 ");
		if(this.getSchTableNames() != null && this.getSchTableNames().size() > 0) {
			StringBuilder tables = new StringBuilder();
			for (String table : this.getSchTableNames()) {
				tables.append(",'").append(table).append("'");
			}
			sb.append(" AND a.TABLE_NAME IN (" + tables.substring(1) + ")");
		}
		return sb.toString();
	}

	@Override
	protected TableDefinition buildTableDefinition(Map<String, Object> tableMap) {
		TableDefinition tableDefinition = new TableDefinition();
		tableDefinition.setTableName(convertString(tableMap.get("NAME")));
		tableDefinition.setComment(convertString(tableMap.get("COMMENTS")));
		return tableDefinition;
	}

}
