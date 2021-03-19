package com.levin.commons.dao;

/**
 * 表名或是字段名，命名策略
 */
public interface PhysicalNamingStrategy {

    PhysicalNamingStrategy DEFAULT_PHYSICAL_NAMING_STRATEGY = new PhysicalNamingStrategy() {
    };

    default String toPhysicalCatalogName(String name, Object jdbcEnvironment) {
        return name;
    }

    default String toPhysicalSchemaName(String name, Object jdbcEnvironment) {
        return name;
    }

    default String toPhysicalTableName(String name, Object jdbcEnvironment) {
        return name;
    }

    default String toPhysicalSequenceName(String name, Object jdbcEnvironment) {
        return name;
    }

    default String toPhysicalColumnName(String name, Object jdbcEnvironment) {
        return name;
    }

}
