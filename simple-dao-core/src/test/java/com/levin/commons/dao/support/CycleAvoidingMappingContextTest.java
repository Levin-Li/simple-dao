package com.levin.commons.dao.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CycleAvoidingMappingContextTest {

    @Test
    void shouldCacheBySourceIdentityAndTargetTypeUntilCleared() {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        Object firstSource = new String("same");
        Object secondSource = new String("same");
        Object firstTarget = new Object();
        String secondTarget = "target";

        context.storeMappedInstance(firstSource, firstTarget, Object.class);
        context.storeMappedInstance(firstSource, secondTarget, String.class);

        assertSame(firstTarget, context.getMappedInstance(firstSource, Object.class));
        assertSame(secondTarget, context.getMappedInstance(firstSource, String.class));
        assertNull(context.getMappedInstance(secondSource, Object.class));

        context.clear();
        assertNull(context.getMappedInstance(firstSource, Object.class));
        assertNull(context.getMappedInstance(firstSource, String.class));
    }

    @Test
    void legacyUtilTypeShouldRemainUsable() {
        assertTrue(new com.levin.commons.dao.util.CycleAvoidingMappingContext()
                instanceof CycleAvoidingMappingContext);
    }
}
