package com.levin.commons.dao.support;

import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.exception.StatementBuildException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DetachedLogicGroupTest {
    static class Record { String orgId; String userId; String state; String name; }

    @DaoLogicGroup(name = "first", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "second", logicOp = DaoLogicGroup.LogicOp.AND, minConditions = 1, unless = "1=3")
    static class DisabledLocalOr {
        @OR(autoClose = true, condition = "false")
        @Eq(value = "orgId", group = "first")
        @Eq(value = "userId", group = "second")
        @Eq(value = "state", condition = "true")
        String value = "v";
        @Eq("name") String name = "after";
    }

    @DaoLogicGroup(name = "first", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=2")
    static class EnabledLocalOr {
        @OR(autoClose = true)
        @Eq(value = "orgId", group = "first")
        @Eq(value = "userId")
        @IsNull(value = "state")
        String value = "v";
    }

    @DaoLogicGroup(name = "first", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=2")
    static class DisabledOuterOr {
        @OR(condition = "false") @Eq("name") String start = "ignored-start";
        @Eq(value = "orgId", group = "first") @Eq("state") String member = "detached";
        @END @Eq("userId") String end = "ignored-end";
        @Eq("name") String after = "after";
    }

    @DaoLogicGroup(name = "first", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=2")
    static class DisabledLocalAndAndCondition {
        @AND(autoClose = true, condition = "false")
        @Eq(value = "orgId", group = "first", condition = "false")
        @Eq(value = "userId", group = "first", condition = "true")
        @Eq("state") String value = "v";
    }

    @DaoLogicGroup(name = "parent", logicOp = DaoLogicGroup.LogicOp.OR, condition = "false", minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "child", parent = "parent", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=3")
    static class DisabledParent {
        @OR(autoClose = true, condition = "false")
        @Eq(value = "orgId", group = "child")
        @Eq("state") String value = "v";
        @Eq("name") String name = "after";
    }

    @DaoLogicGroup(name = "first", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=2")
    static class DisabledMember {
        @OR(autoClose = true, condition = "false")
        @Eq(value = "orgId", group = "first", condition = "false")
        @Eq("state") String value = "v";
    }

    static class MissingGroup {
        @OR(autoClose = true, condition = "false")
        @Eq(value = "orgId", group = "missing", condition = "false")
        @Eq("state") String value = "v";
    }

    @DaoLogicGroup(name = "first", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=2")
    static class DetachedAlwaysFalse {
        @OR(autoClose = true, condition = "false")
        @DaoAlwaysFalse(group = "first") @Eq("state") String value = "v";
    }

    @Test void disabledLocalOrStillRoutesEachGroupedConditionIndependently() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new DisabledLocalOr());
        List<?> before = strings(query);
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.orgId ="), sql);
        assertTrue(sql.contains("r.userId ="), sql);
        assertFalse(sql.contains("r.state"), sql);
        assertTrue(sql.indexOf("r.orgId") < sql.indexOf("r.userId"), sql);
        assertTrue(sql.indexOf("r.userId") < sql.indexOf("r.name"), sql);
        assertEquals(List.of("v", "v", "after"), before);
        assertEquals(before, strings(query));
    }

    @Test void enabledLocalOrRetainsOnlyUngroupedConditions() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new EnabledLocalOr());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.orgId ="), sql);
        assertTrue(sql.contains("r.userId ="), sql);
        assertTrue(sql.contains("r.state IS NULL"), sql);
        assertTrue(sql.contains(" OR "), sql);
        assertTrue(sql.indexOf("r.orgId") < sql.indexOf("r.userId"), sql);
        assertEquals(List.of("v", "v"), strings(query));
    }

    @Test void disabledOuterOrDoesNotSuppressDetachedConditionsAndIsClosedCorrectly() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new DisabledOuterOr());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.orgId ="), sql);
        assertFalse(sql.contains("r.userId"), sql);
        assertFalse(sql.contains("r.state"), sql);
        assertEquals(List.of("detached", "after"), strings(query));
    }

    @Test void detachedConditionStillEvaluatesItsOwnConditionForAnd() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new DisabledLocalAndAndCondition());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.userId ="), sql);
        assertFalse(sql.contains("r.orgId"), sql);
        assertFalse(sql.contains("r.state"), sql);
        assertEquals(List.of("v"), strings(query));
    }

    @Test void disabledParentStillSuppressesDetachedChildAndItsFallback() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new DisabledParent());
        String sql = query.genFinalStatement();
        assertFalse(sql.contains("r.orgId"), sql);
        assertFalse(sql.contains("r.state"), sql);
        assertFalse(sql.contains("1=2"), sql);
        assertFalse(sql.contains("1=3"), sql);
        assertEquals(List.of("after"), strings(query));
    }

    @Test void inactiveDetachedMemberDoesNotSatisfyMinimum() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new DisabledMember());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("1=2"), sql);
        assertFalse(sql.contains("r.orgId"), sql);
        assertEquals(List.of(), strings(query));
    }

    @Test void missingGroupStillFailsEvenWhenFieldAndMemberAreDisabled() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new MissingGroup());
        assertThrows(StatementBuildException.class, query::genFinalStatement);
    }

    @Test void detachedAlwaysFalseIsSubjectToMinimumCount() {
        SelectDaoImpl<Record> query = select();
        query.appendByQueryObj(new DetachedAlwaysFalse());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("1=2"), sql);
        assertFalse(sql.contains("r.state"), sql);
        assertEquals(List.of(), strings(query));
    }

    private static List<?> strings(SelectDaoImpl<Record> query) {
        return query.genFinalParamList().stream().filter(String.class::isInstance).toList();
    }

    private static SelectDaoImpl<Record> select() {
        MiniDao mini = (MiniDao) Proxy.newProxyInstance(DetachedLogicGroupTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getParamPlaceholder")) return ":?";
                    if (method.getName().equals("getSafeModeMaxLimit")) return 10;
                    if (method.getName().equals("getNamingStrategy"))
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
        return new SelectDaoImpl<>(mini, false, Record.class, "r");
    }
}
