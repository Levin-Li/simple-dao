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

class SingleUpdatePrecheckTest {

    @Test
    void singleUpdateShouldSelectAtMostTwoRowsBeforeUpdating() {
        AtomicInteger precheckLimit = new AtomicInteger();
        AtomicReference<String> precheckStatement = new AtomicReference<>();
        AtomicInteger updateCalls = new AtomicInteger();
        AtomicInteger updateLimit = new AtomicInteger();
        UpdateDaoImpl<TestEntity> updateDao = new UpdateDaoImpl<>(stubDao(precheckLimit, precheckStatement, updateCalls, updateLimit), false,
                TestEntity.class, "e");

        boolean updated = updateDao
                .set(true, false, false, "value", "after")
                .where("e.id = 1")
                .disableSafeMode()
                .singleUpdate();

        assertTrue(updated);
        assertEquals(2, precheckLimit.get());
        assertTrue(precheckStatement.get().startsWith(" Select 1 From "), precheckStatement::get);
        assertTrue(updateLimit.get() <= 0, () -> "UPDATE 不应再携带 MaxResults，实际值=" + updateLimit.get());
        assertEquals(1, updateCalls.get());
    }

    private static MiniDao stubDao(AtomicInteger precheckLimit, AtomicReference<String> precheckStatement,
                                   AtomicInteger updateCalls, AtomicInteger updateLimit) {
        return (MiniDao) Proxy.newProxyInstance(SingleUpdatePrecheckTest.class.getClassLoader(),
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
                    if ("find".equals(method.getName())) {
                        precheckLimit.set((Integer) args[3]);
                        precheckStatement.set((String) args[4]);
                        return Collections.singletonList(1);
                    }
                    if ("update".equals(method.getName())) {
                        updateCalls.incrementAndGet();
                        updateLimit.set((Integer) args[2]);
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
        String value;
    }
}
