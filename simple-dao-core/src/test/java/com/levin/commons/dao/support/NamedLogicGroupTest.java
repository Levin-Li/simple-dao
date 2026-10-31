package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.DaoContext;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.exception.StatementBuildException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedLogicGroupTest {

    static class Record {
        String salesOrgId;
        String financeOrgId;
        String salesUserId;
        String financeUserId;
        String state;
    }

    @EntityOption(logicalDeleteFieldName = "deleted", logicalDeleteValue = "true")
    static class SoftDeletedRecord extends Record {
        Boolean deleted;
    }

    static class ScopeReq {
        @OR(group = "visible") @Eq("salesOrgId") String sales = "S";
        @Eq("state") String state = "OPEN";
        @OR(group = "visible") @Eq("financeOrgId") String finance = "F";
    }

    static class ConflictingReq {
        @OR(group = "visible") @Eq("salesOrgId") String sales = "S";
        @AND(group = "visible") @Eq("financeOrgId") String finance = "F";
    }

    static class AndReq {
        @AND(group = "both") @Eq("salesOrgId") String sales = "S";
        @Eq("state") String state = "OPEN";
        @AND(group = "both") @Eq("financeOrgId") String finance = "F";
    }

    static class HavingReq {
        @OR(group = "visible") @Eq(value = "state", having = true) String state = "OPEN";
    }

    static class GeneratedScopeReq {
        @OR(group = "visible") @In("salesOrgId") @In("financeOrgId")
        List<String> allowedOrgIds = List.of("S1", "S2");
        @Eq("state") String state = "OPEN";
        @OR(group = "visible") @Eq("salesUserId") @Eq("financeUserId")
        String currentUserId = "U1";
    }

    static class FirstMember {
        @OR(group = "visible") @Eq("salesOrgId") String sales = "S";
    }

    static class LaterMember {
        @OR(group = "visible") @Eq("financeOrgId") String finance = "F";
    }

    @Test
    void joinsNonAdjacentFieldsAndKeepsParameterOrder() {
        SelectDaoImpl<Record> query = new SelectDaoImpl<>(mini(), false, Record.class, "r");
        query.appendByQueryObj(new ScopeReq());

        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.state ="), sql);
        assertTrue(sql.contains("(r.salesOrgId =  :? OR r.financeOrgId =  :?)"), sql);
        assertTrue(sql.indexOf("r.salesOrgId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("S", "F", "OPEN"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
        assertEquals(sql, query.genFinalStatement());
    }

    @Test
    void rejectsMixedOperatorsForOneName() {
        SelectDaoImpl<Record> query = new SelectDaoImpl<>(mini(), false, Record.class, "r");
        assertThrows(StatementBuildException.class, () -> query.appendByQueryObj(new ConflictingReq()));
    }

    @Test
    void joinsNamedAndGroupAtItsFirstNode() {
        SelectDaoImpl<Record> query = new SelectDaoImpl<>(mini(), false, Record.class, "r");
        query.appendByQueryObj(new AndReq());
        String sql = query.genFinalStatement();
        assertTrue(sql.indexOf("r.financeOrgId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("S", "F", "OPEN"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void rejectsNamedWhereGroupOnHavingCondition() {
        SelectDaoImpl<Record> query = new SelectDaoImpl<>(mini(), false, Record.class, "r");
        assertThrows(StatementBuildException.class, () -> query.appendByQueryObj(new HavingReq()));
    }

    @Test
    void generatedScopeKeepsSqlAndParametersAlignedWhenRequestedInEitherOrder() {
        SelectDaoImpl<Record> query = new SelectDaoImpl<>(mini(), false, Record.class, "r");
        query.appendByQueryObj(new GeneratedScopeReq());
        List<?> before = query.genFinalParamList();
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.salesOrgId IN"), sql);
        assertTrue(sql.contains("r.financeOrgId IN"), sql);
        assertTrue(sql.contains("r.salesUserId ="), sql);
        assertTrue(sql.contains("r.financeUserId ="), sql);
        assertTrue(sql.indexOf("r.financeUserId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("S1", "S2", "S1", "S2", "U1", "U1", "OPEN"),
                before.stream().filter(String.class::isInstance).toList());
        assertEquals(before, query.genFinalParamList());
    }

    @Test
    void logicDeleteAndNamedGroupParametersStayStableAcrossCallOrder() {
        Function<Field, Boolean> previous = DaoContext.setEntityClassFieldNullableTestFun(field -> true, false);
        try {
            SelectDaoImpl<SoftDeletedRecord> query = new SelectDaoImpl<>(mini(), false, SoftDeletedRecord.class, "r");
            query.appendByQueryObj(new ScopeReq());
            List<?> before = query.genFinalParamList();
            String firstSql = query.genFinalStatement();
            List<?> after = query.genFinalParamList();
            String secondSql = query.genFinalStatement();
            assertEquals(firstSql, secondSql);
            assertEquals(before, after);
            assertEquals(List.of(true, "S", "F", "OPEN"), after.stream()
                    .filter(value -> value instanceof Boolean || value instanceof String).toList());
        } finally {
            DaoContext.setEntityClassFieldNullableTestFun(previous, false);
        }
    }

    @Test
    void updateAndLogicalDeleteKeepSetValuesBeforeWhereValues() {
        Function<Field, Boolean> previous = DaoContext.setEntityClassFieldNullableTestFun(field -> true, false);
        try {
            UpdateDaoImpl<SoftDeletedRecord> update = new UpdateDaoImpl<>(mini(), false, SoftDeletedRecord.class, "r");
            update.set("state", "NEXT");
            update.appendByQueryObj(new ScopeReq());
            update.limit(0, 10);
            List<?> updateBefore = update.genFinalParamList();
            String updateSql = update.genFinalStatement();
            assertEquals(updateBefore, update.genFinalParamList());
            assertEquals(List.of("NEXT", true, "S", "F", "OPEN"), updateBefore.stream()
                    .filter(value -> value instanceof Boolean || value instanceof String).toList());
            assertEquals(5, updateSql.split(":\\?", -1).length - 1);

            DeleteDaoImpl<SoftDeletedRecord> delete = new DeleteDaoImpl<>(mini(), false, SoftDeletedRecord.class, "r");
            delete.appendByQueryObj(new ScopeReq());
            delete.limit(0, 10);
            List<?> deleteBefore = delete.genFinalParamList();
            String deleteSql = delete.genFinalStatement();
            assertEquals(deleteBefore, delete.genFinalParamList());
            assertEquals(deleteSql, delete.genFinalStatement());
            assertEquals(List.of(true, true, "S", "F", "OPEN"), deleteBefore.stream()
                    .filter(value -> value instanceof Boolean || value instanceof String).toList());
            assertEquals(5, deleteSql.split(":\\?", -1).length - 1);
        } finally {
            DaoContext.setEntityClassFieldNullableTestFun(previous, false);
        }
    }

    @Test
    void laterAppendReusesFirstNodeAndReordersParametersWithIt() {
        SelectDaoImpl<Record> query = new SelectDaoImpl<>(mini(), false, Record.class, "r");
        query.appendByQueryObj(new FirstMember()).where("r.state = :?", "OPEN");
        assertEquals(List.of("S", "OPEN"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
        query.genFinalStatement();
        query.appendByQueryObj(new LaterMember());
        String sql = query.genFinalStatement();
        assertTrue(sql.indexOf("r.financeOrgId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("S", "F", "OPEN"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    private static MiniDao mini() {
        return (MiniDao) Proxy.newProxyInstance(NamedLogicGroupTest.class.getClassLoader(), new Class[]{MiniDao.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getParamPlaceholder")) return ":?";
                    if (method.getName().equals("getSafeModeMaxLimit")) return 10;
                    if (method.getName().equals("getNamingStrategy"))
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
    }
}
