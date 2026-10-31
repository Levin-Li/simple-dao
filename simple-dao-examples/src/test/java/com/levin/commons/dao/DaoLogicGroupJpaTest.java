package com.levin.commons.dao;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.domain.ClassGroupRecord;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
