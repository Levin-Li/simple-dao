package com.levin.commons.dao.codegen.db;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class TableSelector {

    private ColumnSelector columnSelector;
    private DbConfig generatorConfig;
    private List<String> schTableNames;

    public TableSelector(ColumnSelector columnSelector, DbConfig generatorConfig) {
        this.generatorConfig = generatorConfig;
        this.columnSelector = columnSelector;
    }

    /**
     * 查询数据库表的SQL
     * 1.如果是oracle的話則應該傳入用戶名，oracle是根據用戶去管理數據的
     * 2.mysql的话是传入数据库名，mysql和sqlserver是根据数据库去管理的
     * @param generatorConfig 配置
     * @return 返回获取表信息的sql
     */
    protected abstract String getShowTablesSQL(DbConfig generatorConfig);

    protected abstract TableDefinition buildTableDefinition(Map<String, Object> tableMap);

    public List<TableDefinition> getTableDefinitions() {
        List<Map<String, Object>> resultList = SqlHelper.runSql(getGeneratorConfig(), getShowTablesSQL(generatorConfig));
        List<TableDefinition> tablesList = new ArrayList<TableDefinition>(resultList.size());

        for (Map<String, Object> rowMap : resultList) {
            TableDefinition tableDefinition = this.buildTableDefinition(rowMap);
            String tableName = tableDefinition.getTableName();
            List<ColumnDefinition> columnDefinitions = columnSelector.getColumnDefinitions(tableName);
            tableDefinition.setColumnDefinitions(buildRealColumnDefinitions(columnDefinitions, JavaColumnDefinition::new));
            tableDefinition.setCsharpColumnDefinitions(buildRealColumnDefinitions(columnDefinitions, CsharpColumnDefinition::new));
            tablesList.add(tableDefinition);
        }

        return tablesList;
    }

    private <T> List<T> buildRealColumnDefinitions(List<ColumnDefinition> columnDefinitions, Supplier<T> supplier) {
        return columnDefinitions.stream()
                .map(columnDefinition -> {
                    T t = supplier.get();
                    BeanUtils.copyProperties(columnDefinition, t);
                    return t;
                })
                .collect(Collectors.toList());
    }

    public List<TableDefinition> getSimpleTableDefinitions() {
        List<Map<String, Object>> resultList = SqlHelper.runSql(getGeneratorConfig(), getShowTablesSQL(generatorConfig));
        List<TableDefinition> tablesList = new ArrayList<TableDefinition>(resultList.size());

        for (Map<String, Object> rowMap : resultList) {
            tablesList.add(this.buildTableDefinition(rowMap));
        }

        return tablesList;
    }

    public List<String> wrapTableNames() {
        List<String> schTableNames = this.getSchTableNames();
        if (CollectionUtils.isEmpty(schTableNames)) {
            return Collections.emptyList();
        }
        return schTableNames.stream()
                .map(this::wrapValue)
                .collect(Collectors.toList());
    }

    protected String wrapValue(String tableName) {
        return String.format("'%s'", tableName);
    }

    public DbConfig getGeneratorConfig() {
        return generatorConfig;
    }

    public void setGeneratorConfig(DbConfig generatorConfig) {
        this.generatorConfig = generatorConfig;
    }

    public ColumnSelector getColumnSelector() {
        return columnSelector;
    }

    public void setColumnSelector(ColumnSelector columnSelector) {
        this.columnSelector = columnSelector;
    }

    public List<String> getSchTableNames() {
        return schTableNames;
    }

    public void setSchTableNames(List<String> schTableNames) {
        this.schTableNames = schTableNames;
    }

}
