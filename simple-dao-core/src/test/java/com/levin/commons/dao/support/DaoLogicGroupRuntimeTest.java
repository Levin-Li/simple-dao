package com.levin.commons.dao.support;

import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.exception.StatementBuildException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DaoLogicGroupRuntimeTest {

    static class Record {
        String orgId;
        String userId;
        String state;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2", remark = "受信的记录范围")
    static class ScopeReq {
        @OR(autoClose = true) @In(value = "orgId", group = "visible")
        @IsNull(value = "orgId", group = "visible", condition = C.VALUE_EMPTY)
        List<String> orgIds = List.of("O1", "O2");

        @Eq("state") String state = "OPEN";

        @Eq(value = "userId", group = "visible") String userId = "U1";
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class MemberBeforeOtherCondition {
        @Eq(value = "userId", group = "visible") String userId = "U1";
        @Eq("state") String state = "OPEN";
    }

    static class MultiRecord {
        String orgId;
        String state;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "blocked", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 1, unless = "1=2")
    static class MultiReq {
        @NotIn(value = "state", group = "blocked") List<String> blockedStates = List.of("CLOSED");
        @Eq(value = "orgId", group = "visible") String orgId = "O1";
    }

    static class LocalRecord { String userId; String state; }
    static class LocalReq {
        @DaoLogicGroup(name = "local", logicOp = DaoLogicGroup.LogicOp.OR,
                minConditions = 1, unless = "1=2")
        @Ignore Object groupAnchor;
        @OR(autoClose = true) @Eq(value = "userId", group = "local")
        @IsNull(value = "userId", group = "local") String userId = "U1";
        @Eq("state") String state = "OPEN";
    }

    @DaoLogicGroup(name = "both", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 1, unless = "1=2")
    static class LocalOrWithinAndReq {
        @OR(autoClose = true)
        @Eq(value = "userId", group = "both")
        @IsNull(value = "userId", group = "both") String userId = "U1";
    }

    static class UnknownGroupReq {
        @Eq(value = "userId", group = "missing") String userId = "U1";
    }

    static class UnknownInactiveGroupReq {
        @Eq(value = "userId", group = "missing", condition = "false") String userId = "U1";
    }

    static class DuplicateFieldDefinitions {
        @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
        @Ignore Object first;
        @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
        @Ignore Object second;
    }

    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
    static class DuplicateClassDefinitions { }

    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
    static class ParentDefinition { }

    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.AND)
    static class ChildDuplicateDefinition extends ParentDefinition { }

    @DaoLogicGroup(name = "org", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1)
    @DaoLogicGroup(name = "user", logicOp = DaoLogicGroup.LogicOp.AND, minConditions = 1)
    static class TwoGroupsOnOneField {
        @Eq(value = "orgId", group = "org")
        @Eq(value = "userId", group = "user") String value = "X";
    }

    static class DefinitionAfterMember {
        @Eq(value = "userId", group = "late") String userId = "U1";
        @Eq("state") String state = "OPEN";
        @DaoLogicGroup(name = "late", logicOp = DaoLogicGroup.LogicOp.OR,
                minConditions = 1, unless = "1=2")
        @Ignore Object anchor;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class FalseOnly {
        @DaoAlwaysFalse(group = "visible") Object ignored;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class DuplicateDefinition {
        @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.AND)
        @Eq("userId") String userId = "U1";
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class EmptyScopeReq { }

    @DaoLogicGroup(name = "two", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=2")
    static class TwoRequiredConditions {
        @Eq(value = "orgId", group = "two") String orgId = "O1";
        @Eq(value = "userId", group = "two") String userId;
    }

    @DaoLogicGroup(name = "two", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2)
    static class TwoRequiredWithoutUnless {
        @Eq(value = "orgId", group = "two") String orgId = "O1";
    }

    @DaoLogicGroup(name = "optional", logicOp = DaoLogicGroup.LogicOp.OR)
    static class EmptyOptionalGroup { }

    @DaoLogicGroup(name = "parent", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=3")
    @DaoLogicGroup(name = "child", parent = "parent", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=2")
    static class NestedMinimumRequest {
        @Eq(value = "state", group = "parent") String state = "OPEN";
        @Eq(value = "orgId", group = "child") String orgId = "O1";
        @Eq(value = "userId", group = "child") String userId;
    }

    @DaoLogicGroup(name = "two", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=2")
    static class LocalOrIsOneChild {
        @OR(autoClose = true)
        @Eq(value = "userId", group = "two")
        @IsNull(value = "userId", group = "two") String userId = "U1";
    }

    @DaoLogicGroup(name = "two", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=2")
    static class OneInWithTwoValues {
        @In(value = "orgId", group = "two") List<String> orgIds = List.of("O1", "O2");
    }

    static class SelfParentReq {
        @DaoLogicGroup(name = "loop", parent = "loop", logicOp = DaoLogicGroup.LogicOp.OR)
        @Eq("userId") String userId = "U1";
    }

    static class CycleReq {
        @DaoLogicGroup(name = "a", parent = "b", logicOp = DaoLogicGroup.LogicOp.OR)
        @Eq("userId") String userId = "U1";
        @DaoLogicGroup(name = "b", parent = "a", logicOp = DaoLogicGroup.LogicOp.AND)
        @Eq("state") String state = "OPEN";
    }

    static class MissingParentReq {
        @DaoLogicGroup(name = "child", parent = "missing", logicOp = DaoLogicGroup.LogicOp.OR)
        @Eq("userId") String userId = "U1";
    }

    @DaoLogicGroup(name = "loop", parent = "loop", logicOp = DaoLogicGroup.LogicOp.OR)
    static class ClassSelfParentReq { }

    @DaoLogicGroup(name = "a", parent = "b", logicOp = DaoLogicGroup.LogicOp.OR)
    @DaoLogicGroup(name = "b", parent = "a", logicOp = DaoLogicGroup.LogicOp.AND)
    static class ClassCycleReq { }

    @DaoLogicGroup(name = "a", parent = "b", logicOp = DaoLogicGroup.LogicOp.OR)
    @DaoLogicGroup(name = "b", parent = "c", logicOp = DaoLogicGroup.LogicOp.AND)
    @DaoLogicGroup(name = "c", parent = "a", logicOp = DaoLogicGroup.LogicOp.OR)
    static class ThreeLevelCycleReq { }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class NestedReq {
        @DaoLogicGroup(name = "child", parent = "visible", logicOp = DaoLogicGroup.LogicOp.AND,
                minConditions = 2, unless = "1=3")
        @Eq(value = "orgId", group = "child") String orgId = "O1";
        @Eq(value = "userId", group = "child") String userId = "U1";
        @Eq("state") String state = "OPEN";
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            condition = "false", minConditions = 1, unless = "1=2")
    static class DisabledReq {
        @Eq(value = "userId", group = "visible") String userId = "U1";
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            condition = "false", minConditions = 1)
    static class DisabledWithoutUnlessReq {
        @Eq(value = "userId", group = "visible") String userId = "U1";
        @Eq("state") String state = "OPEN";
    }

    static class DisabledFieldGroupReq {
        @DaoLogicGroup(name = "local", logicOp = DaoLogicGroup.LogicOp.OR,
                condition = "false", minConditions = 1, unless = "1=3")
        @Eq(value = "userId", group = "local") String userId = "U1";
        @Eq("state") String state = "OPEN";
    }

    @DaoLogicGroup(name = "root", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "child", parent = "root", logicOp = DaoLogicGroup.LogicOp.AND,
            condition = "false", minConditions = 1)
    static class DisabledChildGroupReq {
        @Eq(value = "userId", group = "child") String userId = "U1";
        @Eq(value = "state", group = "root") String state = "OPEN";
    }

    @DaoLogicGroup(name = "root", logicOp = DaoLogicGroup.LogicOp.OR,
            condition = "false", minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "child", parent = "root", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 1)
    static class DisabledParentGroupReq {
        @Eq(value = "userId", group = "child") String userId = "U1";
        @Eq("state") String state = "OPEN";
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            condition = "#enabled", minConditions = 1, unless = "1=2")
    static class MutableClassConditionReq {
        boolean enabled = true;
        @Eq(value = "userId", group = "visible") String userId = "U1";
    }

    static class MutableFieldConditionReq {
        @DaoLogicGroup(name = "local", logicOp = DaoLogicGroup.LogicOp.OR,
                condition = "#_fieldVal['enabled']", minConditions = 1, unless = "1=2")
        @Ignore Map<String, Boolean> options = new HashMap<>(Map.of("enabled", true));
        @Eq(value = "userId", group = "local") String userId = "U1";
    }

    static class ParentRecord { String orgId; }

    static class ChildRecord extends ParentRecord { String userId; }

    @DaoLogicGroup(name = "parent", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class BaseGroupsReq {
        @Eq(value = "orgId", group = "parent") String orgId = "O1";
    }

    @DaoLogicGroup(name = "child", parent = "parent", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=3")
    static class InheritedGroupsReq extends BaseGroupsReq {
        @Eq(value = "userId", group = "child") String userId = "U1";
    }

    @Test
    void entityGroupIsAddedOutsideBusinessWhereWithFinalParameterOrder() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new ScopeReq());
        List<?> paramsBeforeSql = query.genFinalParamList();
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.state ="), sql);
        assertTrue(sql.contains("r.orgId IN"), sql);
        assertTrue(sql.contains("r.userId ="), sql);
        assertTrue(sql.indexOf("r.orgId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("O1", "O2", "U1", "OPEN"), paramsBeforeSql.stream()
                .filter(String.class::isInstance).toList());
        assertEquals(paramsBeforeSql, query.genFinalParamList());
        assertEquals(sql, query.genFinalStatement());
    }

    @Test
    void entityGroupWithoutMarkedMembersUsesUnlessEvenForIdQuery() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new EmptyScopeReq()).eq("userId", "U1");
        assertTrue(query.genFinalStatement().contains("1=2"));
        assertEquals(List.of("U1"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void minConditionsUsesActivePredicatesAndUnlessReplacesInsufficientMembers() {
        TwoRequiredConditions request = new TwoRequiredConditions();
        SelectDaoImpl<Record> insufficient = select(Record.class);
        insufficient.appendByQueryObj(request);
        assertTrue(insufficient.genFinalStatement().contains("1=2"));
        assertFalse(insufficient.genFinalStatement().contains("r.orgId"));
        assertTrue(insufficient.genFinalParamList().stream().noneMatch(String.class::isInstance));

        request.userId = "U1";
        SelectDaoImpl<Record> sufficient = select(Record.class);
        sufficient.appendByQueryObj(request);
        List<?> paramsBeforeSql = sufficient.genFinalParamList();
        String sql = sufficient.genFinalStatement();
        assertTrue(sql.contains("r.orgId ="), sql);
        assertTrue(sql.contains("r.userId ="), sql);
        assertFalse(sql.contains("1=2"), sql);
        assertEquals(List.of("O1", "U1"), paramsBeforeSql.stream()
                .filter(String.class::isInstance).toList());

        SelectDaoImpl<Record> noFallback = select(Record.class);
        noFallback.appendByQueryObj(new TwoRequiredWithoutUnless());
        assertThrows(StatementBuildException.class, noFallback::genFinalStatement);
    }

    @Test
    void emptyOptionalGroupAddsNoPredicate() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new EmptyOptionalGroup()).eq("state", "OPEN");
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.state ="), sql);
        assertFalse(sql.contains("1=2"), sql);
        assertEquals(List.of("OPEN"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void childFallbackCountsAsOneProducedParentChild() {
        NestedMinimumRequest request = new NestedMinimumRequest();
        SelectDaoImpl<Record> insufficient = select(Record.class);
        insufficient.appendByQueryObj(request);
        String fallbackSql = insufficient.genFinalStatement();
        assertFalse(fallbackSql.contains("1=3"), fallbackSql);
        assertTrue(fallbackSql.contains("1=2"), fallbackSql);
        assertTrue(fallbackSql.contains("r.state"), fallbackSql);
        assertEquals(List.of("OPEN"), insufficient.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());

        request.userId = "U1";
        SelectDaoImpl<Record> sufficient = select(Record.class);
        sufficient.appendByQueryObj(request);
        List<?> params = sufficient.genFinalParamList();
        String sql = sufficient.genFinalStatement();
        assertTrue(sql.contains("r.state ="), sql);
        assertTrue(sql.contains("r.orgId ="), sql);
        assertTrue(sql.contains("r.userId ="), sql);
        assertFalse(sql.contains("1=3"), sql);
        assertEquals(List.of("OPEN", "O1", "U1"), params.stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void minimumCountsDetachedConditionsIndividuallyButInAsOne() {
        SelectDaoImpl<Record> detached = select(Record.class);
        detached.appendByQueryObj(new LocalOrIsOneChild());
        String sql = detached.genFinalStatement();
        assertTrue(sql.contains("r.userId ="), sql);
        assertTrue(sql.contains("r.userId IS NULL"), sql);
        assertFalse(sql.contains("1=2"), sql);
        assertEquals(List.of("U1"), detached.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());

        SelectDaoImpl<Record> in = select(Record.class);
        in.appendByQueryObj(new OneInWithTwoValues());
        assertTrue(in.genFinalStatement().contains("1=2"));
        assertTrue(in.genFinalParamList().stream().noneMatch(String.class::isInstance));
    }

    @Test
    void multipleEntityGroupsKeepDefinitionOrderRegardlessOfMemberFieldOrder() {
        SelectDaoImpl<MultiRecord> query = select(MultiRecord.class);
        query.appendByQueryObj(new MultiReq());
        String sql = query.genFinalStatement();
        assertTrue(sql.indexOf("r.orgId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("O1", "CLOSED"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void fieldGroupWithoutEntityDefinitionWrapsItsOwnConditionsAtFieldPosition() {
        SelectDaoImpl<LocalRecord> query = select(LocalRecord.class);
        query.appendByQueryObj(new LocalReq());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.userId IS NULL"), sql);
        assertTrue(sql.indexOf("r.userId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("U1", "OPEN"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void groupedConditionsLeaveLocalOrAndJoinNamedAndGroupSeparately() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new LocalOrWithinAndReq());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains(" AND "), sql);
        assertFalse(sql.contains(" OR "), sql);
        assertTrue(sql.contains("r.userId IS NULL"), sql);
        assertEquals(List.of("U1"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void undefinedGroupAndDuplicateFieldDefinitionsFail() {
        SelectDaoImpl<Record> missing = select(Record.class);
        missing.appendByQueryObj(new UnknownGroupReq());
        assertThrows(StatementBuildException.class, missing::genFinalStatement);
        SelectDaoImpl<Record> inactive = select(Record.class);
        inactive.appendByQueryObj(new UnknownInactiveGroupReq());
        assertThrows(StatementBuildException.class, inactive::genFinalStatement);
        assertThrows(StatementBuildException.class,
                () -> select(Record.class).appendByQueryObj(new DuplicateFieldDefinitions()));
        assertThrows(StatementBuildException.class,
                () -> select(Record.class).appendByQueryObj(new DuplicateClassDefinitions()));
        assertThrows(StatementBuildException.class,
                () -> select(Record.class).appendByQueryObj(new ChildDuplicateDefinition()));
    }

    @Test
    void repeatableConditionsOnOneFieldCanReferenceDifferentGroups() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new TwoGroupsOnOneField());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.orgId ="), sql);
        assertTrue(sql.contains("r.userId ="), sql);
        assertEquals(List.of("X", "X"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void fieldDefinitionKeepsItsPositionWhenMemberAppearsFirst() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new DefinitionAfterMember());
        List<?> beforeSql = query.genFinalParamList();
        String sql = query.genFinalStatement();
        assertTrue(sql.indexOf("r.state") < sql.indexOf("r.userId"), sql);
        assertEquals(List.of("OPEN", "U1"), beforeSql.stream().filter(String.class::isInstance).toList());
        assertEquals(beforeSql, query.genFinalParamList());
    }

    @Test
    void falseOnlyMemberDoesNotSatisfyMinimumAndOperatorMismatchFails() {
        SelectDaoImpl<Record> denied = select(Record.class);
        denied.appendByQueryObj(new FalseOnly());
        assertEquals(1, denied.genFinalStatement().split("1=2", -1).length - 1);
        assertTrue(denied.genFinalParamList().stream().noneMatch(String.class::isInstance));
        assertThrows(StatementBuildException.class,
                () -> select(Record.class).appendByQueryObj(new DuplicateDefinition()));
    }

    @Test
    void markedMemberMayAppearBeforeOrdinaryBusinessCondition() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new MemberBeforeOtherCondition());
        String sql = query.genFinalStatement();
        assertTrue(sql.indexOf("r.userId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("U1", "OPEN"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void subclassAndParentEntityGroupsBothApply() {
        SelectDaoImpl<ChildRecord> query = select(ChildRecord.class);
        query.appendByQueryObj(new InheritedGroupsReq());
        String sql = query.genFinalStatement();
        assertTrue(sql.contains("r.orgId ="), sql);
        assertTrue(sql.contains("r.userId ="), sql);
        assertEquals(List.of("O1", "U1"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void parentGroupCombinesChildConditionsAndKeepsParametersInSqlOrder() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new NestedReq());
        List<?> params = query.genFinalParamList();
        String sql = query.genFinalStatement();
        assertTrue(sql.indexOf("r.orgId") < sql.indexOf("r.userId"), sql);
        assertTrue(sql.indexOf("r.userId") < sql.indexOf("r.state"), sql);
        assertEquals(List.of("O1", "U1", "OPEN"), params.stream().filter(String.class::isInstance).toList());
        assertEquals(params, query.genFinalParamList());
    }

    @Test
    void selfCycleAndMissingParentFailClearly() {
        assertThrows(StatementBuildException.class, () -> select(Record.class).appendByQueryObj(new SelfParentReq()));
        assertThrows(StatementBuildException.class, () -> select(Record.class).appendByQueryObj(new ClassSelfParentReq()));
        SelectDaoImpl<Record> cycle = select(Record.class);
        cycle.appendByQueryObj(new CycleReq());
        assertThrows(StatementBuildException.class, cycle::genFinalStatement);
        SelectDaoImpl<Record> missing = select(Record.class);
        missing.appendByQueryObj(new MissingParentReq());
        assertThrows(StatementBuildException.class, missing::genFinalStatement);
        SelectDaoImpl<Record> classCycle = select(Record.class);
        classCycle.appendByQueryObj(new ClassCycleReq());
        assertThrows(StatementBuildException.class, classCycle::genFinalStatement);
        SelectDaoImpl<Record> deepCycle = select(Record.class);
        deepCycle.appendByQueryObj(new ThreeLevelCycleReq());
        assertThrows(StatementBuildException.class, deepCycle::genFinalStatement);
    }

    @Test
    void disabledClassGroupIgnoresUnlessAndMemberParameters() {
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(new DisabledReq());
        assertFalse(query.genFinalStatement().contains("1=2"));
        assertFalse(query.genFinalStatement().contains("r.userId"));
        assertTrue(query.genFinalParamList().stream().noneMatch(String.class::isInstance));
    }

    @Test
    void disabledGroupWithoutUnlessDisappearsAtClassFieldAndChildPositions() {
        for (Object request : List.of(new DisabledWithoutUnlessReq(), new DisabledFieldGroupReq(),
                new DisabledChildGroupReq(), new DisabledParentGroupReq())) {
            SelectDaoImpl<Record> query = select(Record.class);
            query.appendByQueryObj(request);
            List<?> paramsBeforeSql = query.genFinalParamList();
            String sql = query.genFinalStatement();
            assertTrue(sql.contains("r.state ="), sql);
            assertFalse(sql.contains("r.userId"), sql);
            assertFalse(sql.contains("1=2"), sql);
            assertFalse(sql.contains("1=3"), sql);
            assertEquals(List.of("OPEN"), paramsBeforeSql.stream()
                    .filter(String.class::isInstance).toList());
            assertEquals(paramsBeforeSql, query.genFinalParamList());
        }
    }

    @Test
    void logicGroupsAreIsolatedPerDaoBuilder() {
        SelectDaoImpl<Record> first = select(Record.class);
        first.appendByQueryObj(new ScopeReq());
        assertTrue(first.genFinalStatement().contains("r.orgId IN"));
        assertThrows(StatementBuildException.class,
                () -> first.appendByQueryObj(new EmptyScopeReq()));

        SelectDaoImpl<Record> second = select(Record.class);
        second.appendByQueryObj(new EmptyScopeReq());
        String secondSql = second.genFinalStatement();
        assertTrue(secondSql.contains("1=2"), secondSql);
        assertFalse(secondSql.contains("r.orgId"), secondSql);
        assertTrue(second.genFinalParamList().stream().noneMatch(String.class::isInstance));

        SelectDaoImpl<Record> third = select(Record.class);
        assertFalse(third.genFinalStatement().contains("1=2"));
    }

    @Test
    void classConditionIsBoundBeforeFieldsAndNotReevaluatedDuringRendering() {
        MutableClassConditionReq request = new MutableClassConditionReq();
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(request);
        request.enabled = false;
        assertTrue(query.genFinalStatement().contains("r.userId ="));
        assertEquals(List.of("U1"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    @Test
    void fieldConditionIsBoundBeforeItsGroupNodeIsAdded() {
        MutableFieldConditionReq request = new MutableFieldConditionReq();
        SelectDaoImpl<Record> query = select(Record.class);
        query.appendByQueryObj(request);
        request.options.put("enabled", false);
        assertTrue(query.genFinalStatement().contains("r.userId ="));
        assertEquals(List.of("U1"), query.genFinalParamList().stream()
                .filter(String.class::isInstance).toList());
    }

    private static <T> SelectDaoImpl<T> select(Class<T> type) {
        return new SelectDaoImpl<>(mini(), false, type, "r");
    }

    private static MiniDao mini() {
        return (MiniDao) Proxy.newProxyInstance(DaoLogicGroupRuntimeTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
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
