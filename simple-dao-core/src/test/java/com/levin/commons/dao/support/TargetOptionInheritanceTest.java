package com.levin.commons.dao.support;

import com.levin.commons.dao.TargetOption;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TargetOptionInheritanceTest {

    @Test
    void subclassShouldInheritTargetOptionWhenItDoesNotDeclareOne() {
        TargetOption targetOption = AnnotatedElementUtils.findMergedAnnotation(InheritedTargetOption.class, TargetOption.class);

        assertEquals(BaseTarget.class, targetOption.entityClass());
        assertEquals("base", targetOption.alias());
    }

    @Test
    void subclassTargetOptionShouldOverrideInheritedDefinition() {
        TargetOption targetOption = AnnotatedElementUtils.findMergedAnnotation(OverriddenTargetOption.class, TargetOption.class);

        assertEquals(SubTarget.class, targetOption.entityClass());
        assertEquals("sub", targetOption.alias());
    }

    @TargetOption(entityClass = BaseTarget.class, alias = "base")
    static class BaseTargetOption {
    }

    static class InheritedTargetOption extends BaseTargetOption {
    }

    @TargetOption(entityClass = SubTarget.class, alias = "sub")
    static class OverriddenTargetOption extends BaseTargetOption {
    }

    static class BaseTarget {
    }

    static class SubTarget {
    }
}
