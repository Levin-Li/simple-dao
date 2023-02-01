package com.levin.commons.dao.codegen.db.oracle;

import com.levin.commons.dao.codegen.db.ColumnDefinition;
import com.levin.commons.dao.codegen.db.ColumnSelector;
import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.TypeFormatter;
import com.levin.commons.dao.codegen.db.util.FieldUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * oracle表信息查询
 */
public class OracleColumnSelector extends ColumnSelector {

	private static final TypeFormatter TYPE_FORMATTER = new OracleTypeFormatter();

//	private static final String COLUMN_SQL = "select "
//			+ " utc.column_name as FIELD,utc.data_type TYPE, utc.data_scale SCALE, utc.data_length MAXLENGTH, "
//			+ " 		CASE utc.nullable WHEN 'N' THEN '否' ELSE '是' END 可空, "
//			+ " utc.data_default 默认值,ucc.comments COMMENTS,UTC.table_name 表名, "
//			+ " CASE UTC.COLUMN_NAME "
//			+ " WHEN (select "
//			+ " 		col.column_name "
//			+ " 		from "
//			+ " 		user_constraints con,user_cons_columns col "
//			+ " 		where "
//			+ " 	con.constraint_name=col.constraint_name and con.constraint_type='P' "
//			+ " 		and col.table_name='%s')   THEN 'true' ELSE 'false' END AS KEY "
//			+ " 		from "
//			+ " user_tab_columns utc,user_col_comments ucc "
//			+ " 		where "
//			+ " utc.table_name = ucc.table_name "
//			+ " and utc.column_name = ucc.column_name "
//			+ " and utc.table_name = '%s' "
//			+ " order by "
//			+ " column_id ";

	private static final String COLUMN_SQL = " SELECT " +
			" atc.COLUMN_NAME FIELD, atc.DATA_TYPE TYPE, atc.DATA_SCALE SCALE, atc.DATA_LENGTH MAXLENGTH, " +
			" atc.NULLABLE NULLABLE , " +
			" atc.DATA_DEFAULT 默认值, acc.COMMENTS COMMENTS, atc.TABLE_NAME 表名, " +
			" CASE atc.COLUMN_NAME " +
			" WHEN " +
			" ( SELECT col.column_name FROM all_constraints con " +
			"	LEFT JOIN all_cons_columns col ON con.table_name = col.table_name " +
			"	AND con.OWNER = col.OWNER AND con.CONSTRAINT_NAME = col.CONSTRAINT_NAME " +
			"   WHERE con.constraint_type = 'P' " +
			"	AND col.table_name = '%s' AND con.OWNER = '%s' AND col.column_name = atc.COLUMN_NAME ) " +
			" THEN 'true' ELSE 'false' END AS KEY " +
			" FROM ALL_TAB_COLUMNS atc " +
			" LEFT JOIN ALL_COL_COMMENTS acc " +
			" ON acc.TABLE_NAME = atc.TABLE_NAME AND acc.COLUMN_NAME = atc.COLUMN_NAME " +
			" AND acc.OWNER = atc.OWNER " +
			" WHERE atc.TABLE_NAME = '%s' AND atc.OWNER = '%s' " +
			" ORDER BY atc.COLUMN_ID ";

	public OracleColumnSelector(DbConfig generatorConfig) {
		super(generatorConfig);
	}

	@Override
	protected String getColumnInfoSQL(String tableName) {
		String owner = this.getGeneratorConfig().getSchemaName();
		return String.format(COLUMN_SQL, tableName, owner, tableName, owner);
	}

	@Override
	protected ColumnDefinition buildColumnDefinition(Map<String, Object> rowMap){
		Set<String> columnSet = rowMap.keySet();

		for (String columnInfo : columnSet) {
			rowMap.put(columnInfo.toUpperCase(), rowMap.get(columnInfo));
		}

		ColumnDefinition columnDefinition = new ColumnDefinition();

		columnDefinition.setColumnName(FieldUtil.convertString(rowMap.get("FIELD")));

		columnDefinition.setIsIdentity(false);

		boolean isPk = "true".equalsIgnoreCase(FieldUtil.convertString(rowMap.get("KEY")));
		columnDefinition.setIsPk(isPk);

		String type = FieldUtil.convertString(rowMap.get("TYPE"));
		// 如果是number
		if (StringUtils.containsIgnoreCase(type, "number")) {
			// 有精度则为decimal，否则是int
			Object scaleCol = rowMap.get("SCALE");
			if (scaleCol == null) {
				scaleCol = 0;
			}
			String scale = String.valueOf(scaleCol);
			type = "0".equals(scale) ? "int" : "decimal";
		}

		columnDefinition.setType(TYPE_FORMATTER.format(type))
				.setColumnType(type)
				.setTypeFormatter(TYPE_FORMATTER);

		columnDefinition.setComment(FieldUtil.convertString(rowMap.get("COMMENTS")));

		String maxLength = FieldUtil.convertString(rowMap.get("MAXLENGTH"));
		columnDefinition.setMaxLength(new Integer(StringUtils.isEmpty(maxLength) ? "0" : maxLength));

		String scale = FieldUtil.convertString(rowMap.get("SCALE"));
		columnDefinition.setScale(new Integer(StringUtils.isEmpty(scale) ? "0" : scale));

		String isNullable = FieldUtil.convertString(rowMap.get("NULLABLE"));
		columnDefinition.setIsNullable(!"N".equalsIgnoreCase(isNullable));

		return columnDefinition;
	}


}
