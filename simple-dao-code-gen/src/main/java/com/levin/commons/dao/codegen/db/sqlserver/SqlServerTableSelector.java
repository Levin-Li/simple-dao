package com.levin.commons.dao.codegen.db.sqlserver;

import com.levin.commons.dao.codegen.db.ColumnSelector;
import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.TableDefinition;
import com.levin.commons.dao.codegen.db.TableSelector;

import java.util.Map;

import static com.levin.commons.dao.codegen.db.util.FieldUtil.convertString;

public class SqlServerTableSelector extends TableSelector {

	public SqlServerTableSelector(ColumnSelector columnSelector,
                                  DbConfig dataBaseConfig) {
		super(columnSelector, dataBaseConfig);
	}

	@Override
	protected String getShowTablesSQL(DbConfig generatorConfig2) {
		return "SELECT SS.name + '.' + t.name AS table_name " +
				",CONVERT(varchar(50),ISNULL(ext.value, '')) as comment " +
				"FROM sysobjects t " +
				"INNER JOIN sys.objects SO ON t.name = SO.name " +
				"INNER JOIN sys.schemas  SS ON SO.schema_id = SS.schema_id " +
				"LEFT JOIN sys.extended_properties ext ON ext.major_id = SO.object_id and ext.minor_id=0 " +
				"WHERE t.xtype='u' " +
				this.buildTableSchWhere() +
				" ORDER BY SS.name ASC,t.name ASC";
	}

	// and ( (t.name = 'bar' and SS.name = 'front') or (t.name = 'adjustBatch' and SS.name = 'account') )
	private String buildTableSchWhere() {
		if(this.getSchTableNames() != null && this.getSchTableNames().size() > 0) {
			int i = 0;
			StringBuilder tables = new StringBuilder(" and ( ");
			for (String table : this.getSchTableNames()) {
				String[] tableArr = table.split("\\.");
				if(i > 0) {
					tables.append(" or ");
				}
				tables.append("(SS.name='").append(tableArr[0]).append("' and t.name='").append(tableArr[1]).append("') ");
				i++;
			}
			tables.append(" )");
			return tables.toString();
		}
		return "";
	}

	// {TABLE_NAME=account.adjustBatch, COMMENT=}
	@Override
	protected TableDefinition buildTableDefinition(Map<String, Object> tableMap) {
		TableDefinition tableDefinition = new TableDefinition();
		tableDefinition.setTableName(convertString(tableMap.get("TABLE_NAME")));
		tableDefinition.setComment(convertString(tableMap.get("COMMENT")));
		return tableDefinition;
	}

}
