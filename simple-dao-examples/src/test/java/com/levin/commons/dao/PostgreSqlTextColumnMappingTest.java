package com.levin.commons.dao;

import com.levin.commons.dao.domain.OperationLog;
import com.levin.commons.dao.domain.Task;
import com.levin.commons.dao.domain.TestOrg;
import com.levin.commons.dao.domain.TestRole;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PostgreSqlTextColumnMappingTest {

    @Test
    void exampleTextFieldsShouldNotUsePostgreSqlLargeObjects() throws Exception {
        assertTextColumn(OperationLog.class, "logText");
        assertTextColumn(Task.class, "actions");
        assertTextColumn(TestOrg.class, "extInfo");
        assertTextColumn(TestRole.class, "assignedOrgIdList");
        assertTextColumn(TestRole.class, "permissionList");
    }

    private static void assertTextColumn(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        assertFalse(field.isAnnotationPresent(Lob.class), field::toString);
        assertEquals("text", field.getAnnotation(Column.class).columnDefinition(), field::toString);
    }
}
