package com.levin.commons.dao.codegen.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 表信息查询
 */
public abstract class ColumnSelector {

	private DbConfig generatorConfig;

	public ColumnSelector(DbConfig generatorConfig){
		this.generatorConfig = generatorConfig;
	}

	/**
	 * 返回查询表字段信息的SQL语句,不同的数据查询表信息不一样
	 * 如mysql是DESC tableName
	 * @return
	 */
	protected abstract String getColumnInfoSQL(String tableName);

	/**
	 * 构建列信息
	 * @param rowMap
	 * @return
	 */
	protected abstract ColumnDefinition buildColumnDefinition(Map<String, Object> rowMap);

	public List<ColumnDefinition> getColumnDefinitions(String tableName) {
		List<Map<String, Object>> resultList = SqlHelper.runSql(this.getGeneratorConfig(), getColumnInfoSQL(tableName));

		List<ColumnDefinition> columnDefinitionList = new ArrayList<ColumnDefinition>(resultList.size());
		// 构建columnDefinition
		for (Map<String, Object> rowMap : resultList) {
			columnDefinitionList.add(buildColumnDefinition(rowMap));
		}

		return columnDefinitionList;
	}

	public DbConfig getGeneratorConfig() {
		return generatorConfig;
	}

	public void setGeneratorConfig(DbConfig generatorConfig) {
		this.generatorConfig = generatorConfig;
	}

}
