package com.levin.commons.dao.codegen;

import com.levin.commons.dao.EntityOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlyInnerAccessControllerGenerationTest {

    @Test
    void defaultEntityShouldGenerateControllers() {
        assertTrue(ServiceModelCodeGenerator.shouldGenerateController(DefaultAccessEntity.class));
    }

    @Test
    void innerAccessOnlyEntityShouldNotGenerateControllers() {
        assertFalse(ServiceModelCodeGenerator.shouldGenerateController(InternalOnlyEntity.class));
    }

    static class DefaultAccessEntity {
    }

    @EntityOption(innerAccessOnly = true)
    static class InternalOnlyEntity {
    }
}
