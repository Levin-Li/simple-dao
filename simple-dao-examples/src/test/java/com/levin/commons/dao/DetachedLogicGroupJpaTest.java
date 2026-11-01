package com.levin.commons.dao;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.ClassGroupRecord;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DetachedLogicGroupJpaTest extends DaoExamplesTestSupport {

    @DaoLogicGroup(name = "org", logicOp = DaoLogicGroup.LogicOp.AND, minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "user", logicOp = DaoLogicGroup.LogicOp.AND, minConditions = 1, unless = "1=2")
    static class DisabledLocalOr {
        @OR(autoClose = true, condition = "false")
        @Eq(value = "orgId", group = "org")
        @Eq(value = "userId", group = "user")
        @Eq("state")
        String shared = "X";

        @Eq("state") String requiredState = "READY";
    }

    @DaoLogicGroup(name = "org", logicOp = DaoLogicGroup.LogicOp.AND, minConditions = 1, unless = "1=2")
    @DaoLogicGroup(name = "user", logicOp = DaoLogicGroup.LogicOp.AND, minConditions = 1, unless = "1=2")
    static class EnabledLocalOr {
        @OR(autoClose = true)
        @Eq(value = "orgId", group = "org")
        @Eq(value = "userId", group = "user")
        @Eq("state")
        String shared = "X";
    }

    @Test
    @Transactional
    void detachedConditionsExecuteInTwoGroupsDespiteDisabledFieldOr() {
        ClassGroupRecord match = record("X", "X", "READY");
        record("X", "other", "READY");
        record("other", "X", "READY");
        record("X", "X", "OTHER");

        var query = dao.selectFrom(ClassGroupRecord.class, "r").appendByQueryObj(new DisabledLocalOr());
        String statement = ((StatementBuilder) query).genFinalStatement();
        List<?> params = ((StatementBuilder) query).genFinalParamList();
        assertTrue(statement.contains("r.orgId"), statement);
        assertTrue(statement.contains("r.userId"), statement);
        assertTrue(statement.contains("r.state"), statement);
        assertFalse(statement.contains("1=2"), statement);
        assertEquals(List.of("X", "X", "READY"), params.stream().filter(String.class::isInstance).toList());
        assertEquals(1, query.count());
        assertEquals(match.getId(), query.findOne(ClassGroupRecord.class).getId());
    }

    @Test
    @Transactional
    void enabledFieldOrDoesNotAbsorbDetachedConditions() {
        ClassGroupRecord match = record("X", "X", "X");
        record("X", "other", "X");
        record("other", "X", "X");
        record("X", "X", "READY");

        var query = dao.selectFrom(ClassGroupRecord.class, "r").appendByQueryObj(new EnabledLocalOr());
        assertEquals(1, query.count());
        assertEquals(match.getId(), query.findOne(ClassGroupRecord.class).getId());
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
