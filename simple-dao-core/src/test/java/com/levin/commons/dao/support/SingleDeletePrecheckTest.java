package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleDeletePrecheckTest {

    @Test
    void singleDeleteShouldSelectAtMostTwoRowsBeforeDeleting() {
        AtomicInteger precheckLimit = new AtomicInteger();
        AtomicReference<String> precheckStatement = new AtomicReference<>();
        AtomicInteger deleteLimit = new AtomicInteger();
        AtomicInteger safeModeLimit = new AtomicInteger();
        DeleteDaoImpl<TestEntity> deleteDao = new DeleteDaoImpl<>(stubDao(precheckLimit, precheckStatement, deleteLimit, safeModeLimit), false,
                TestEntity.class, "e");

        boolean deleted = deleteDao
                .where("e.id = 1")
                .disableSafeMode()
                .singleDelete();

        assertTrue(deleted);
        assertEquals(2, precheckLimit.get());
        assertTrue(precheckStatement.get().startsWith(" Select 1 From "), precheckStatement::get);
        assertTrue(deleteLimit.get() <= 0, () -> "DELETE 不应携带 MaxResults，实际值=" + deleteLimit.get());
        assertTrue(safeModeLimit.get() <= 0, () -> "关闭安全模式时的限制值=" + safeModeLimit.get());
    }

    private static MiniDao stubDao(AtomicInteger precheckLimit, AtomicReference<String> precheckStatement,
                                   AtomicInteger deleteLimit, AtomicInteger safeModeLimit) {
        return (MiniDao) Proxy.newProxyInstance(SingleDeletePrecheckTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if ("getParamPlaceholder".equals(method.getName())) {
                        return ":?";
                    }
                    if ("getSafeModeMaxLimit".equals(method.getName())) {
                        return 20;
                    }
                    if ("getNamingStrategy".equals(method.getName())) {
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    }
                    if ("setCurrentThreadMaxLimit".equals(method.getName())) {
                        if (args[0] != null) {
                            safeModeLimit.set((Integer) args[0]);
                        }
                        return null;
                    }
                    if ("find".equals(method.getName())) {
                        precheckLimit.set((Integer) args[3]);
                        precheckStatement.set((String) args[4]);
                        return Collections.singletonList(1);
                    }
                    if ("update".equals(method.getName())) {
                        deleteLimit.set((Integer) args[2]);
                        return 1;
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    @Entity
    static class TestEntity {
    }
}
