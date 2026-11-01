package com.levin.commons.dao.support;

import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DetachedLogicGroupEndTest {
    static class Row { String orgId; String name; String state; String userId; }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class DetachedOnly {
        @OR @Eq("name") String first = "first";
        @Eq("state") String second = "second";
        @END(containCurrentField = false) @Eq(value = "orgId", group = "visible") String detached = "detached";
        @Eq("userId") String after = "after";
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class DetachedAndLocal {
        @OR @Eq("name") String first = "first";
        @Eq("state") String second = "second";
        @END(containCurrentField = false)
        @Eq(value = "orgId", group = "visible") @Eq("userId") String mixed = "mixed";
    }

    @Test void endingWithDetachedOnlyPreservesPreviousOrMembers() {
        SelectDaoImpl<Row> query = select();
        query.appendByQueryObj(new DetachedOnly());
        String sql = query.genFinalStatement();
        assertTrue(sql.matches("(?s).*\\(\\s*r\\.name\\s*=.*?\\s+OR\\s+r\\.state\\s*=.*?\\).*"), sql);
        assertEquals(List.of("detached", "first", "second", "after"), strings(query));
    }

    @Test void endingWithDetachedAndLocalMovesOnlyCurrentLocalCondition() {
        SelectDaoImpl<Row> query = select();
        query.appendByQueryObj(new DetachedAndLocal());
        String sql = query.genFinalStatement();
        assertTrue(sql.matches("(?s).*\\(\\s*r\\.name\\s*=.*?\\s+OR\\s+r\\.state\\s*=.*?\\)\\s+AND\\s+r\\.userId\\s*=.*"), sql);
        assertEquals(List.of("mixed", "first", "second", "mixed"), strings(query));
    }

    private static List<?> strings(SelectDaoImpl<Row> query) {
        return query.genFinalParamList().stream().filter(String.class::isInstance).toList();
    }

    private static SelectDaoImpl<Row> select() {
        MiniDao mini = (MiniDao) Proxy.newProxyInstance(DetachedLogicGroupEndTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getParamPlaceholder")) return ":?";
                    if (method.getName().equals("getSafeModeMaxLimit")) return 10;
                    if (method.getName().equals("getNamingStrategy")) return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
        return new SelectDaoImpl<>(mini, false, Row.class, "r");
    }
}
