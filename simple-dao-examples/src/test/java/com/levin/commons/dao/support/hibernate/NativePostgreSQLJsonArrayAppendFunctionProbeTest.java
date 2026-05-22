package com.levin.commons.dao.support.hibernate;

import org.hibernate.type.spi.TypeConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativePostgreSQLJsonArrayAppendFunctionProbeTest {

    @Test
    void shouldDetectHibernate727RootPathRenderingBug() {
        assertTrue(NativePostgreSQLJsonArrayAppendFunctionProbe.isRootPathRenderingBroken(new TypeConfiguration()));
    }

    @Test
    void shouldNotRequireOverrideWhenRootPathArrayIsWellFormed() {
        assertFalse(NativePostgreSQLJsonArrayAppendFunctionProbe.isRenderingBroken(
                "jsonb_set_lax(t.d,t.p,(t.d)#>t.p||to_jsonb(?),false,'return_target')"
                        + " from (values(doc,array[]::text[])) t(d,p)"
        ));
    }
}
