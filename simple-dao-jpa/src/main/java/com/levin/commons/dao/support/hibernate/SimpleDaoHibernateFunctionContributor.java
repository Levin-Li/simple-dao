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

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        if (!(functionContributions.getDialect() instanceof PostgreSQLDialect)) {
            return;
        }

        contributePostgreSQLFunctions(functionContributions);
    }

    static void contributePostgreSQLFunctions(FunctionContributions functionContributions) {
        if (!requiresPostgreSQLJsonArrayAppendOverride(functionContributions.getTypeConfiguration())) {
            return;
        }

        functionContributions.getFunctionRegistry().register(
                "json_array_append",
                new SimpleDaoPostgreSQLJsonArrayAppendFunction(functionContributions.getTypeConfiguration())
        );
    }

    public static boolean requiresPostgreSQLJsonArrayAppendOverride() {
        return requiresPostgreSQLJsonArrayAppendOverride(new TypeConfiguration());
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

    static String getHibernateVersion() {
        return Version.getVersionString();
    }

    @Override
    public int ordinal() {
        return USER_DEFINED_FUNCTION_ORDINAL;
    }
}
