package com.levin.commons.dao.util;

import com.levin.commons.dao.annotation.select.Select;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryAnnotationUtilSelectFieldTest {

    @Test
    void shouldStopRecursiveScanWhenTypesReferenceEachOther() {
        assertFalse(QueryAnnotationUtil.hasSelectStatementField(NoSelectA.class));
    }

    @Test
    void shouldContinueScanningAfterRecursiveBranch() {
        assertTrue(QueryAnnotationUtil.hasSelectStatementField(SelectA.class));
    }

    static class NoSelectA {
        NoSelectB b;
    }

    static class NoSelectB {
        NoSelectA a;
    }

    static class SelectA {
        SelectB b;
    }

    static class SelectB {
        SelectA a;

        @Select
        String name;
    }
}
