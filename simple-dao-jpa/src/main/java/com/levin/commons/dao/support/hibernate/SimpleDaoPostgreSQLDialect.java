package com.levin.commons.dao.support.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.PostgreSQLDriverKind;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * PostgreSQL dialect shim for Simple DAO Hibernate function fixes.
 */
public class SimpleDaoPostgreSQLDialect extends PostgreSQLDialect {

    public SimpleDaoPostgreSQLDialect() {
        super();
    }

    public SimpleDaoPostgreSQLDialect(DialectResolutionInfo info) {
        super(info);
    }

    public SimpleDaoPostgreSQLDialect(DatabaseVersion version) {
        super(version);
    }

    public SimpleDaoPostgreSQLDialect(DatabaseVersion version, PostgreSQLDriverKind driverKind) {
        super(version, driverKind);
    }

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);
        SimpleDaoHibernateFunctionContributor.contributePostgreSQLFunctions(functionContributions);
    }
}
