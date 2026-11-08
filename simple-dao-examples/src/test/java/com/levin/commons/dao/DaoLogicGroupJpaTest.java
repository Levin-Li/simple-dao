package com.levin.commons.dao;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.domain.ClassGroupRecord;
import com.levin.commons.dao.domain.StringScopeRecord;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DaoLogicGroupJpaTest extends DaoExamplesTestSupport {

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class VisibleScope {
        @In(value = "orgId", group = "visible") List<String> orgIds;
        @Eq(value = "userId", group = "visible") String userId;

        VisibleScope(List<String> orgIds, String userId) {
            this.orgIds = orgIds;
            this.userId = userId;
        }
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class EmptyScope { }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "'恒假条件原因'='没有组织访问权限'")
    static class DiagnosticFallbackScope { }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=1")
    static class AlwaysTrueFallbackScope { }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            condition = "!(#isCanAccessAllOrg?:false)", minConditions = 1,
            unless = "'恒假条件原因'='没有组织访问权限'")
    static class ConditionalParticipantScope {
        @In(value = "orgId", group = "visible") List<String> orgIds;
        @Eq(value = "userId", group = "visible") String ownerId;

        ConditionalParticipantScope(List<String> orgIds, String ownerId) {
            this.orgIds = orgIds;
            this.ownerId = ownerId;
        }
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class VisibleStringIdsScope {
        @In(value = "orgId", group = "visible") List<String> orgIds;
        @In("id") @jakarta.persistence.Id List<String> idList;

        VisibleStringIdsScope(List<String> orgIds, List<String> idList) {
            this.orgIds = orgIds;
            this.idList = idList;
        }
    }

    static class SingleStringIdScope {
        @Eq("id") @jakarta.persistence.Id String id;
        SingleStringIdScope(String id) { this.id = id; }
    }

    @Test
    @Transactional
    void idAnnotatedStringListExpandsAndIntersectsAuthorizedScope() {
        StringScopeRecord first = stringRecord("first", "O1");
        StringScopeRecord second = stringRecord("second", "O1");
        StringScopeRecord hidden = stringRecord("hidden", "O2");

        assertEquals(List.of(first.getId()), stringIds(new VisibleStringIdsScope(
                List.of("O1"), List.of(first.getId()))));
        assertEquals(List.of(first.getId()), stringIds(new VisibleStringIdsScope(
                List.of("O1"), List.of(first.getId(), hidden.getId()))));
        assertEquals(java.util.Set.of(first.getId(), second.getId()),
                java.util.Set.copyOf(stringIds(new VisibleStringIdsScope(
                        List.of("O1"), List.of(first.getId(), second.getId())))));
        assertTrue(stringIds(new VisibleStringIdsScope(List.of("O1"), List.of(hidden.getId()))).isEmpty());
        assertEquals(List.of(first.getId()), dao.selectFrom(StringScopeRecord.class, "r")
                .appendByQueryObj(new SingleStringIdScope(first.getId()))
                .find(StringScopeRecord.class).stream().map(StringScopeRecord::getId).toList());
    }

    private StringScopeRecord stringRecord(String prefix, String orgId) {
        StringScopeRecord record = new StringScopeRecord();
        record.setId(prefix + "-" + java.util.UUID.randomUUID());
        record.setOrgId(orgId);
        dao.create(record);
        return record;
    }

    private List<String> stringIds(VisibleStringIdsScope scope) {
        return dao.selectFrom(StringScopeRecord.class, "r")
                .appendByQueryObj(scope).find(StringScopeRecord.class).stream()
                .map(StringScopeRecord::getId).toList();
    }

    @Test
    @Transactional
    void conditionalParticipantGroupKeepsOrgOwnerUnionAndTrustedAllOrgSwitch() {
        record("O1", "U2", "OPEN");
        record("O2", "U1", "OPEN");
        record("O2", "U3", "OPEN");
        record("O3", "U4", "CLOSED");
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .setContext(Map.of("isCanAccessAllOrg", false))
                .appendByQueryObj(new ConditionalParticipantScope(List.of("O1"), null)).count());
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .setContext(Map.of("isCanAccessAllOrg", false))
                .appendByQueryObj(new ConditionalParticipantScope(List.of(), "U1")).count());
        assertEquals(2, dao.selectFrom(ClassGroupRecord.class, "r")
                .setContext(Map.of("isCanAccessAllOrg", false))
                .appendByQueryObj(new ConditionalParticipantScope(List.of("O1"), "U1")).count());
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .setContext(Map.of("isCanAccessAllOrg", false))
                .appendByQueryObj(new ConditionalParticipantScope(List.of(), null)).count());
        assertEquals(3, dao.selectFrom(ClassGroupRecord.class, "r")
                .setContext(Map.of("isCanAccessAllOrg", true))
                .appendByQueryObj(new ConditionalParticipantScope(List.of(), null))
                .eq("state", "OPEN").count());
    }

    @DaoLogicGroup(name = "disabled", logicOp = DaoLogicGroup.LogicOp.OR,
            condition = "false", minConditions = 1, unless = "1=2")
    static class DisabledWithUnlessScope {
        @Eq(value = "orgId", group = "disabled") String orgId = "allowed-org";
    }

    @DaoLogicGroup(name = "disabled", logicOp = DaoLogicGroup.LogicOp.OR,
            condition = "false", minConditions = 1)
    static class DisabledWithoutUnlessScope {
        @Eq(value = "orgId", group = "disabled") String orgId = "allowed-org";
    }

    @DaoLogicGroup(name = "both", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=2")
    static class TwoRequiredScope {
        @Eq(value = "orgId", group = "both") String orgId = "allowed-org";
        @Eq(value = "userId", group = "both") String userId;
    }

    @DaoLogicGroup(name = "parent", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=1")
    @DaoLogicGroup(name = "child", parent = "parent", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=2")
    static class NestedRequiredScope {
        @Eq(value = "state", group = "parent") String state = "OPEN";
        @Eq(value = "orgId", group = "child") String orgId = "allowed-org";
        @Eq(value = "userId", group = "child") String userId;
    }

    @DaoLogicGroup(name = "root", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "branch", parent = "root", logicOp = DaoLogicGroup.LogicOp.AND,
            minConditions = 2, unless = "1=2")
    @DaoLogicGroup(name = "leaf", parent = "branch", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class ThreeLevelScope {
        @Eq(value = "orgId", group = "leaf") String orgId = "O1";
        @Eq(value = "state", group = "branch") String state = "OPEN";
        @Eq(value = "userId", group = "root") String userId = "U1";
    }

    @Test
    @Transactional
    void requestGroupAppliesToListCountAndUpdate() {
        ClassGroupRecord visible = new ClassGroupRecord();
        visible.setOrgId("allowed-org");
        visible.setUserId("other-user");
        visible.setState("OPEN");
        dao.create(visible);
        ClassGroupRecord hidden = new ClassGroupRecord();
        hidden.setOrgId("hidden-org");
        hidden.setUserId("hidden-user");
        hidden.setState("OPEN");
        dao.create(hidden);

        assertEquals(2, dao.selectFrom(ClassGroupRecord.class, "r").count());
        assertNotNull(dao.find(ClassGroupRecord.class, visible.getId()));
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(new EmptyScope()).count());
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(new DiagnosticFallbackScope()).count());
        assertEquals(2, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(new AlwaysTrueFallbackScope()).count());
        assertEquals(2, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(new DisabledWithUnlessScope()).count());
        assertEquals(2, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(new DisabledWithoutUnlessScope()).count());

        TwoRequiredScope twoRequired = new TwoRequiredScope();
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(twoRequired).count());
        twoRequired.userId = "other-user";
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(twoRequired).count());

        NestedRequiredScope nestedRequired = new NestedRequiredScope();
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(nestedRequired).count());
        nestedRequired.userId = "other-user";
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(nestedRequired).count());

        VisibleScope scope = new VisibleScope(List.of("allowed-org"), "viewer");
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).eq("state", "OPEN").count());
        ClassGroupRecord found = dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).findOne(ClassGroupRecord.class);
        assertEquals(visible.getId(), found.getId());

        assertEquals(1, dao.updateTo(ClassGroupRecord.class, "r")
                .set("state", "DONE").eq("id", visible.getId())
                .appendByQueryObj(scope).limit(0, 10).update());
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).eq("state", "DONE").count());
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).eq("id", hidden.getId()).count());
    }

    @Test
    @Transactional
    void threeLevelGroupExecutesWithCorrectParenthesesAndParameters() {
        ClassGroupRecord branchMatch = record("O1", "U2", "OPEN");
        ClassGroupRecord wrongState = record("O1", "U2", "CLOSED");
        ClassGroupRecord rootMatch = record("O2", "U1", "CLOSED");
        ClassGroupRecord wrongOrg = record("O2", "U2", "OPEN");
        ThreeLevelScope scope = new ThreeLevelScope();

        assertEquals(2, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).count());
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).eq("id", branchMatch.getId()).count());
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).eq("id", wrongState.getId()).count());
        assertEquals(1, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).eq("id", rootMatch.getId()).count());
        assertEquals(0, dao.selectFrom(ClassGroupRecord.class, "r")
                .appendByQueryObj(scope).eq("id", wrongOrg.getId()).count());
    }

    private ClassGroupRecord record(String orgId, String userId, String state) {
        ClassGroupRecord record = new ClassGroupRecord();
        record.setOrgId(orgId);
        record.setUserId(userId);
        record.setState(state);
        dao.create(record);
        return record;
    }
}
