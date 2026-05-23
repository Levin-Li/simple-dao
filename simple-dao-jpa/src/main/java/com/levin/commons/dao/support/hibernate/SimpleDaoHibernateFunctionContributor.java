package com.levin.commons.dao.support.hibernate;

import org.hibernate.Version;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.spi.TypeConfiguration;
import org.jboss.logging.Logger;

/**
 * Simple DAO Hibernate function overrides.
 */
public class SimpleDaoHibernateFunctionContributor implements FunctionContributor {

    private static final Logger log = Logger.getLogger(SimpleDaoHibernateFunctionContributor.class);

    private static final int USER_DEFINED_FUNCTION_ORDINAL = 1001;

    private static volatile Boolean postgreSQLJsonArrayAppendOverrideRequired;
    private static volatile Boolean postgreSQLJsonArrayInsertOverrideRequired;

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        if (!(functionContributions.getDialect() instanceof PostgreSQLDialect)) {
            return;
        }

        contributePostgreSQLFunctions(functionContributions);
    }

    static void contributePostgreSQLFunctions(FunctionContributions functionContributions) {
        TypeConfiguration typeConfiguration = functionContributions.getTypeConfiguration();
        if (requiresPostgreSQLJsonArrayAppendOverride(typeConfiguration)) {
            functionContributions.getFunctionRegistry().register(
                    "json_array_append",
                    new SimpleDaoPostgreSQLJsonArrayAppendFunction(typeConfiguration)
            );
        }
        if (requiresPostgreSQLJsonArrayInsertOverride(typeConfiguration)) {
            functionContributions.getFunctionRegistry().register(
                    "json_array_insert",
                    new SimpleDaoPostgreSQLJsonArrayInsertFunction(typeConfiguration)
            );
        }
    }

    public static boolean requiresPostgreSQLJsonArrayAppendOverride() {
        return requiresPostgreSQLJsonArrayAppendOverride(new TypeConfiguration());
    }

    public static boolean requiresPostgreSQLJsonFunctionOverride() {
        TypeConfiguration typeConfiguration = new TypeConfiguration();
        return requiresPostgreSQLJsonArrayAppendOverride(typeConfiguration)
                || requiresPostgreSQLJsonArrayInsertOverride(typeConfiguration);
    }

    static boolean requiresPostgreSQLJsonArrayAppendOverride(TypeConfiguration typeConfiguration) {
        Boolean required = postgreSQLJsonArrayAppendOverrideRequired;
        if (required != null) {
            return required;
        }

        synchronized (SimpleDaoHibernateFunctionContributor.class) {
            required = postgreSQLJsonArrayAppendOverrideRequired;
            if (required == null) {
                required = NativePostgreSQLJsonArrayAppendFunctionProbe.isRootPathRenderingBroken(typeConfiguration);
                postgreSQLJsonArrayAppendOverrideRequired = required;
                logPostgreSQLJsonArrayAppendProbeResult(required);
            }
        }
        return required;
    }

    static boolean requiresPostgreSQLJsonArrayInsertOverride(TypeConfiguration typeConfiguration) {
        Boolean required = postgreSQLJsonArrayInsertOverrideRequired;
        if (required != null) {
            return required;
        }

        synchronized (SimpleDaoHibernateFunctionContributor.class) {
            required = postgreSQLJsonArrayInsertOverrideRequired;
            if (required == null) {
                required = NativePostgreSQLJsonArrayInsertFunctionProbe.isCoalesceRenderingBroken(typeConfiguration);
                postgreSQLJsonArrayInsertOverrideRequired = required;
                logPostgreSQLJsonArrayInsertProbeResult(required);
            }
        }
        return required;
    }

    private static void logPostgreSQLJsonArrayAppendProbeResult(boolean overrideRequired) {
        String hibernateVersion = getHibernateVersion();
        if (overrideRequired) {
            log.infof(
                    "Hibernate ORM %s native PostgreSQL json_array_append root-path rendering still needs the Simple DAO workaround.",
                    hibernateVersion
            );
        } else {
            log.infof(
                    "Hibernate ORM %s has fixed native PostgreSQL json_array_append root-path rendering; Simple DAO workaround disabled.",
                    hibernateVersion
            );
        }
    }

    private static void logPostgreSQLJsonArrayInsertProbeResult(boolean overrideRequired) {
        String hibernateVersion = getHibernateVersion();
        if (overrideRequired) {
            log.infof(
                    "Hibernate ORM %s native PostgreSQL json_array_insert coalesce rendering still needs the Simple DAO workaround.",
                    hibernateVersion
            );
        } else {
            log.infof(
                    "Hibernate ORM %s has fixed native PostgreSQL json_array_insert coalesce rendering; Simple DAO workaround disabled.",
                    hibernateVersion
            );
        }
    }

    static String getHibernateVersion() {
        return Version.getVersionString();
    }

    @Override
    public int ordinal() {
        return USER_DEFINED_FUNCTION_ORDINAL;
    }
}
