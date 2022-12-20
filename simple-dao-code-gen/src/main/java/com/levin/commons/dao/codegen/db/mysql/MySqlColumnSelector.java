package com.levin.commons.dao.codegen.db.mysql;

import com.levin.commons.dao.codegen.db.ColumnDefinition;
import com.levin.commons.dao.codegen.db.ColumnSelector;
import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.TypeFormatter;
import com.levin.commons.dao.codegen.db.util.FieldUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * mysql表信息查询
 *
 */
public class MySqlColumnSelector extends ColumnSelector {

	private static final TypeFormatter TYPE_FORMATTER = new MySqlTypeFormatter();

	private static final String SHOW_SQL = " SELECT " +
			" COLUMN_NAME AS 'Field', " +
			" COLUMN_DEFAULT AS 'Default', " +
			" IS_NULLABLE AS 'Null', " +
			" DATA_TYPE AS 'DataType', " +
			" CASE DATA_TYPE " +
			"     WHEN 'int' THEN NUMERIC_PRECISION " +
			"     WHEN 'varchar' THEN CHARACTER_MAXIMUM_LENGTH " +
			" END AS 'MaxLength', " +
			" IFNULL(NUMERIC_SCALE,0) AS 'Scale', " +
			" COLUMN_TYPE AS 'Type', " +
			" COLUMN_KEY 'KEY', " +
			" EXTRA AS 'Extra', " +
			" COLUMN_COMMENT AS 'Comment' " +
			" FROM information_schema.`COLUMNS` " +
			" WHERE 1=1 AND TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' ";

	public MySqlColumnSelector(DbConfig generatorConfig) {
		super(generatorConfig);
	}

	/**
	 * SHOW FULL COLUMNS FROM 表名
	 */
	@Override
	protected String getColumnInfoSQL(String tableName) {
		return String.format(SHOW_SQL, getGeneratorConfig().getDbName(), tableName);
	}

	/*
	 * {FIELD=username, EXTRA=, COMMENT=用户名, COLLATION=utf8_general_ci, PRIVILEGES=select,insert,update,references, KEY=PRI, NULL=NO, DEFAULT=null, TYPE=varchar(20)}
	 */
	@Override
	protected ColumnDefinition buildColumnDefinition(Map<String, Object> rowMap){
		Set<String> columnSet = rowMap.keySet();

		for (String columnInfo : columnSet) {
			rowMap.put(columnInfo.toUpperCase(), rowMap.get(columnInfo));
		}

		ColumnDefinition columnDefinition = new ColumnDefinition();

		columnDefinition.setColumnName(FieldUtil.convertString(rowMap.get("FIELD")));

		boolean isIdentity = "auto_increment".equalsIgnoreCase(FieldUtil.convertString(rowMap.get("EXTRA")));
		columnDefinition.setIsIdentity(isIdentity);

		boolean isPk = "PRI".equalsIgnoreCase(FieldUtil.convertString(rowMap.get("KEY")));
		columnDefinition.setIsPk(isPk);

		String type = FieldUtil.convertString(rowMap.get("TYPE"));
		columnDefinition.setType(TYPE_FORMATTER.format(type));

		columnDefinition.setComment(FieldUtil.convertString(rowMap.get("COMMENT")));

		String maxLength = FieldUtil.convertString(rowMap.get("MAXLENGTH"));
		columnDefinition.setMaxLength(new Integer(StringUtils.isEmpty(maxLength) ? "0" : maxLength));

		String scale = FieldUtil.convertString(rowMap.get("SCALE"));
		columnDefinition.setScale(new Integer(StringUtils.isEmpty(scale) ? "0" : scale));

		String isNullable = FieldUtil.convertString(rowMap.get("NULL"));
		columnDefinition.setIsNullable("YES".equalsIgnoreCase(isNullable));

		return columnDefinition;
	}

}
