package com.levin.commons.dao.codegen.db.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 字段类型转换
 */
public class SqlTypeUtil {
	private static final Map<String, String> javaTypeMap = new HashMap<>();
	private static final Map<String, String> javaBoxTypeMap = new HashMap<>();
	private static final Map<String, String> mybatisTypeMap = new HashMap<>();

	static {
		javaTypeMap.put("bigint", "long");
		javaTypeMap.put("binary", "byte[]");
		javaTypeMap.put("bit", "boolean");
		javaTypeMap.put("boolean", "boolean");
		javaTypeMap.put("blob", "byte[]");
		javaTypeMap.put("char", "String");
		javaTypeMap.put("date", "Date");
		javaTypeMap.put("datetime", "Date");
		javaTypeMap.put("decimal", "BigDecimal");
		javaTypeMap.put("double", "double");
		javaTypeMap.put("float", "float");
		javaTypeMap.put("int", "int");
		javaTypeMap.put("integer", "int");
		javaTypeMap.put("image", "byte[]");
		javaTypeMap.put("money", "BigDecimal");
		javaTypeMap.put("nchar", "String");
		javaTypeMap.put("ntext", "byte");
		javaTypeMap.put("numeric", "BigDecimal");
		javaTypeMap.put("nvarchar", "String");
		javaTypeMap.put("real", "float");
		javaTypeMap.put("smalldatetime", "Date");
		javaTypeMap.put("smallint", "int");
		javaTypeMap.put("smallmoney", "BigDecimal");
		javaTypeMap.put("sql_variant", "String");
		javaTypeMap.put("text", "String");
		javaTypeMap.put("tinyint", "byte");
		javaTypeMap.put("timestamp", "Date");
		javaTypeMap.put("uniqueidentifier", "String");
		javaTypeMap.put("varbinary", "byte[]");
		javaTypeMap.put("varchar", "String");


		javaBoxTypeMap.put("bigint", "Long");
		javaBoxTypeMap.put("binary", "Byte[]");
		javaBoxTypeMap.put("bit", "Boolean");
		javaBoxTypeMap.put("bool", "Boolean");
		javaBoxTypeMap.put("boolean", "Boolean");
		javaBoxTypeMap.put("blob", "Byte[]");
		javaBoxTypeMap.put("char", "String");
		javaBoxTypeMap.put("date", "Date");
		javaBoxTypeMap.put("datetime", "Date");
		javaBoxTypeMap.put("decimal", "BigDecimal");
		javaBoxTypeMap.put("double", "Double");
		javaBoxTypeMap.put("float", "Float");
		javaBoxTypeMap.put("int", "Integer");
		javaBoxTypeMap.put("integer", "Integer");
		javaBoxTypeMap.put("image", "Byte[]");
		javaBoxTypeMap.put("money", "BigDecimal");
		javaBoxTypeMap.put("nchar", "String");
		javaBoxTypeMap.put("ntext", "String");
		javaBoxTypeMap.put("numeric", "BigDecimal");
		javaBoxTypeMap.put("nvarchar", "String");
		javaBoxTypeMap.put("real", "Float");
		javaBoxTypeMap.put("smalldatetime", "Date");
		javaBoxTypeMap.put("smallint", "Integer");
		javaBoxTypeMap.put("smallmoney", "BigDecimal");
		javaBoxTypeMap.put("sql_variant", "String");
		javaBoxTypeMap.put("text", "String");
		javaBoxTypeMap.put("tinyint", "Byte");
		javaBoxTypeMap.put("timestamp", "Date");
		javaBoxTypeMap.put("uniqueidentifier", "String");
		javaBoxTypeMap.put("varbinary", "Byte[]");
		javaBoxTypeMap.put("varchar", "String");


		mybatisTypeMap.put("bigint", "BIGINT");
		mybatisTypeMap.put("binary", "BLOB");
		mybatisTypeMap.put("bit", "BOOLEAN");
		mybatisTypeMap.put("boolean", "BOOLEAN");
		mybatisTypeMap.put("blob", "BLOB");
		mybatisTypeMap.put("char", "CHAR");
		mybatisTypeMap.put("date", "TIMESTAMP");
		mybatisTypeMap.put("datetime", "TIMESTAMP");
		mybatisTypeMap.put("decimal", "DECIMAL");
		mybatisTypeMap.put("double", "DOUBLE");
		mybatisTypeMap.put("float", "FLOAT");
		mybatisTypeMap.put("int", "INTEGER");
		mybatisTypeMap.put("integer", "INTEGER");
		mybatisTypeMap.put("image", "BLOB");
		mybatisTypeMap.put("money", "DECIMAL");
		mybatisTypeMap.put("nchar", "NCHAR");
		mybatisTypeMap.put("ntext", "VARCHAR");
		mybatisTypeMap.put("numeric", "DECIMAL");
		mybatisTypeMap.put("nvarchar", "NVARCHAR");
		mybatisTypeMap.put("real", "FLOAT");
		mybatisTypeMap.put("smalldatetime", "TIMESTAMP");
		mybatisTypeMap.put("smallint", "INTEGER");
		mybatisTypeMap.put("smallmoney", "DECIMAL");
		mybatisTypeMap.put("sql_variant", "VARCHAR");
		mybatisTypeMap.put("text", "VARCHAR");
		mybatisTypeMap.put("tinyint", "TINYINT");
		mybatisTypeMap.put("timestamp", "TIMESTAMP");
		mybatisTypeMap.put("uniqueidentifier", "VARCHAR");
		mybatisTypeMap.put("varbinary", "BLOB");
		mybatisTypeMap.put("varchar", "VARCHAR");
	}

	/**
	 * 将sql字段类型转换为java字段类型
	 * @param sqlType 数据库字段类型
	 * @return 找不到类型默认返回String
	 */
	public static String convertToJavaType(String sqlType){
		String javaType = javaTypeMap.get(sqlType);
		return javaType == null ? "String" : javaType;
	}

	/**
	 * 将sql字段类型转换为java装箱字段类型
	 * @param sqlType 数据库字段类型
	 * @return 找不到类型默认返回String
	 */
	public static String convertToJavaBoxType(String sqlType){
		String javaType = javaBoxTypeMap.get(sqlType);
		return javaType == null ? "String" : javaType;
	}

	/**
	 * 将sql字段类型转换为mybatis的jdbcType
	 * @param sqlType 数据库字段类型
	 * @return 找不到类型默认返回VARCHAR
	 */
	public static String convertToMyBatisJdbcType(String sqlType){
		String javaType = mybatisTypeMap.get(sqlType);
		return javaType == null ? "VARCHAR" : javaType;
	}

}
