package com.levin.commons.dao.support.hibernate;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

/**
 * Lets Hibernate auto-detection pick Simple DAO's PostgreSQL dialect shim.
 */
public class SimpleDaoDialectResolver implements DialectResolver {

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        if (info != null
                && "PostgreSQL".equalsIgnoreCase(info.getDatabaseName())
                && SimpleDaoHibernateFunctionContributor.requiresPostgreSQLJsonFunctionOverride()) {
            return new SimpleDaoPostgreSQLDialect(info);
        }
        return null;
    }
}
