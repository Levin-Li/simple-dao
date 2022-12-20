package com.levin.commons.dao.codegen.db.sqlserver;

import com.levin.commons.dao.codegen.db.ColumnDefinition;
import com.levin.commons.dao.codegen.db.ColumnSelector;
import com.levin.commons.dao.codegen.db.DbConfig;
import com.levin.commons.dao.codegen.db.util.FieldUtil;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

public class SqlServerColumnSelector extends ColumnSelector {

	private static final SqlServerTypeFormatter TYPE_FORMATTER = new SqlServerTypeFormatter();

	// https://blog.csdn.net/qq_14880455/article/details/92842329
	private static String TABKE_DETAIL_SQL = new StringBuilder()
		.append("SELECT")
		.append("	 col.name AS column_name")
		.append("	, col.max_length AS MaxLength")  //sqlserver 字段长度
		.append("	, col.scale AS Scale")  //sqlserver 字段精度
		.append("	, col.is_nullable AS NULLABLE")  //1 =列可以为空。
		.append("	, bt.name AS type")
		.append("	, col.is_identity")
		.append("	, ext.value AS comment")
		.append("	,(")
		.append("		SELECT COUNT(1) FROM sys.indexes IDX ")
		.append("		INNER JOIN sys.index_columns IDXC ")
		.append("		ON IDX.[object_id]=IDXC.[object_id] ")
		.append("		AND IDX.index_id=IDXC.index_id ")
		.append("		LEFT JOIN sys.key_constraints KC ")
		.append("		ON IDX.[object_id]=KC.[parent_object_id] ")
		.append("		AND IDX.index_id=KC.unique_index_id ")
		.append("		INNER JOIN sys.objects O ")
		.append("		ON O.[object_id]=IDX.[object_id] ")
		.append("		WHERE O.[object_id]=col.[object_id] ")
		.append("		AND O.type='U' ")
		.append("		AND O.is_ms_shipped=0 ")
		.append("		AND IDX.is_primary_key=1 ")
		.append("		AND IDXC.Column_id=col.column_id ")
		.append("	) AS is_pk ")
		.append("FROM sys.columns col ")
		.append("LEFT OUTER JOIN sys.types bt on bt.user_type_id = col.system_type_id ")
		.append("LEFT JOIN sys.extended_properties ext ON ext.major_id = col.object_id AND ext.minor_id = col.column_id ")
		.append("WHERE col.object_id = object_id('%s') ")
		.append("ORDER BY col.column_id").toString();



	public SqlServerColumnSelector(DbConfig generatorConfig) {
		super(generatorConfig);
	}

	/*	SELECT
		 col.name AS column_name
		, bt.name AS type
		, col.is_identity
		, ext.value as comment
		,(
			SELECT COUNT(1) FROM sys.indexes IDX
			INNER JOIN sys.index_columns IDXC
			ON IDX.[object_id]=IDXC.[object_id]
			AND IDX.index_id=IDXC.index_id
			LEFT JOIN sys.key_constraints KC
			ON IDX.[object_id]=KC.[parent_object_id]
			AND IDX.index_id=KC.unique_index_id
			INNER JOIN sys.objects O
			ON O.[object_id]=IDX.[object_id]
			WHERE O.[object_id]=col.[object_id]
			AND O.type='U'
			AND O.is_ms_shipped=0
			AND IDX.is_primary_key=1
			AND IDXC.Column_id=col.column_id
		) AS is_pk
	FROM sys.columns col
	LEFT OUTER JOIN sys.types bt on bt.user_type_id = col.system_type_id
	LEFT JOIN sys.extended_properties ext ON ext.major_id = col.object_id AND ext.minor_id = col.column_id
	WHERE col.object_id = object_id('front.bar')
	ORDER BY col.column_id;
	*/
	@Override
	protected String getColumnInfoSQL(String tableName) {
		return String.format(TABKE_DETAIL_SQL, tableName);
	}


	/*
	 * rowMap:
	 * {COLUMN_NAME=barId, IS_IDENTITY=true, COMMENT=网吧ID, IS_PK=1, TYPE=int}
	 */
	@Override
	protected ColumnDefinition buildColumnDefinition(Map<String, Object> rowMap) {
		Set<String> columnSet = rowMap.keySet();

		for (String columnInfo : columnSet) {
			rowMap.put(columnInfo.toUpperCase(), rowMap.get(columnInfo));
		}

		ColumnDefinition columnDefinition = new ColumnDefinition();

		columnDefinition.setColumnName(FieldUtil.convertString(rowMap.get("COLUMN_NAME")));
		columnDefinition.setIsIdentity((Boolean)rowMap.get("IS_IDENTITY"));
		boolean isPk = (Integer)rowMap.get("IS_PK") == 1;
		columnDefinition.setIsPk(isPk);
		String type = FieldUtil.convertString( rowMap.get("TYPE"));
		columnDefinition.setType(TYPE_FORMATTER.format(type));

		columnDefinition.setComment(FieldUtil.convertString(rowMap.get("COMMENT")));

		//sqlserver 字段长度
		String maxLength = FieldUtil.convertString(rowMap.get("MAXLENGTH"));
		columnDefinition.setMaxLength(Integer.parseInt(StringUtils.isEmpty(maxLength) ? "0" : maxLength));
		//sqlserver 字段精度
		String scale = FieldUtil.convertString(rowMap.get("SCALE"));
		columnDefinition.setScale(Integer.parseInt(StringUtils.isEmpty(scale) ? "0" : scale));

		String isNullable = FieldUtil.convertString(rowMap.get("NULLABLE"));
		columnDefinition.setIsNullable("1".equalsIgnoreCase(isNullable));
		return columnDefinition;
	}

}
