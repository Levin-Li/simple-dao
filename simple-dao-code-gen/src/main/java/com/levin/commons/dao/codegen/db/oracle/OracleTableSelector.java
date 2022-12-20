package com.levin.commons.dao.codegen.db.oracle;

import com.levin.commons.dao.codegen.db.ColumnSelector;
import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.TableDefinition;
import com.levin.commons.dao.codegen.db.TableSelector;

import java.util.Map;

import static com.levin.commons.dao.codegen.db.util.FieldUtil.convertString;

/**
 * 查询oracle数据库表
 */
public class OracleTableSelector extends TableSelector {

	public OracleTableSelector(ColumnSelector columnSelector,
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
	protected String getShowTablesSQL(DbConfig generatorConfig2) {
		StringBuilder sb = new StringBuilder("");
		sb.append(" SELECT a.TABLE_NAME as NAME,b.COMMENTS" +
				"  FROM ALL_TABLES a,USER_TAB_COMMENTS b" +
				"  WHERE a.TABLE_NAME=b.TABLE_NAME");
		sb.append(" AND 1=1 ");
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
