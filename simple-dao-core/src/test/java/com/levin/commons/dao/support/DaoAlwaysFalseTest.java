package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.DaoAlwaysFalse;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.annotation.logic.OR;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaoAlwaysFalseTest {
    static class Record { String orgId; }
    static class Direct { @DaoAlwaysFalse Object ignored; }
    static class Fallback {
        @OR(autoClose = true) @In("orgId") @DaoAlwaysFalse(condition = C.VALUE_EMPTY)
        List<String> orgIds;
        Fallback(List<String> orgIds) { this.orgIds = orgIds; }
    }

    @Test
    void directFalseUsesNoEntityFieldOrParameter() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new Direct());
        assertTrue(query.genFinalStatement().contains("1=2"));
        assertEquals(0, query.genFinalParamList().stream().filter(String.class::isInstance).count());
    }

    @Test
    void emptyInListFallsBackToFalseThroughExistingOrGroup() {
        SelectDaoImpl<Record> empty = select();
        empty.appendByQueryObj(new Fallback(List.of()));
        assertTrue(empty.genFinalStatement().contains("1=2"));
        assertEquals(0, empty.genFinalParamList().stream().filter(String.class::isInstance).count());

        SelectDaoImpl<Record> present = select();
        present.appendByQueryObj(new Fallback(List.of("O1")));
        assertTrue(present.genFinalStatement().contains("r.orgId IN"));
        assertEquals(List.of("O1"), present.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    private static SelectDaoImpl<Record> select() {
        MiniDao dao = (MiniDao) Proxy.newProxyInstance(DaoAlwaysFalseTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getParamPlaceholder")) return ":?";
                    if (method.getName().equals("getSafeModeMaxLimit")) return 10;
                    if (method.getName().equals("getNamingStrategy"))
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
        return new SelectDaoImpl<>(dao, false, Record.class, "r");
    }
}
