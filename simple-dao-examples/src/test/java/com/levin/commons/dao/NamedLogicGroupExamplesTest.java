package com.levin.commons.dao;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamedLogicGroupExamplesTest extends DaoExamplesTestSupport {

    static class Query {
        @OR(group = "visible") @Eq("name") String salesName;
        @Eq("state") String state;
        @OR(group = "visible") @Eq("name") String financeName;

        Query(String salesName, String state, String financeName) {
            this.salesName = salesName;
            this.state = state;
            this.financeName = financeName;
        }
    }

    @Test
    @Transactional
    void namedGroupExecutesWithParametersInFinalSqlOrder() {
        String prefix = "named-group-" + UUID.randomUUID();
        User first = new User();
        first.setName(prefix + "-sales");
        first.setState("OPEN");
        dao.create(first);
        User second = new User();
        second.setName(prefix + "-finance");
        second.setState("CLOSED");
        dao.create(second);

        Query query = new Query(first.getName(), "OPEN", second.getName());
        assertEquals(1, dao.selectFrom(User.class, "u").appendByQueryObj(query).count());
        User found = dao.selectFrom(User.class, "u").appendByQueryObj(query).findOne(User.class);
        assertEquals((Long) first.getId(), (Long) found.getId());
    }

    @Test
    @Transactional
    void updateBindsSetValueBeforeNamedWhereGroup() {
        String prefix = "named-update-" + UUID.randomUUID();
        User first = new User();
        first.setName(prefix + "-sales");
        first.setState("OPEN");
        dao.create(first);
        User second = new User();
        second.setName(prefix + "-finance");
        second.setState("CLOSED");
        dao.create(second);

        Query query = new Query(first.getName(), "OPEN", second.getName());
        assertEquals(1, dao.updateTo(User.class, "u")
                .set("state", "DONE").appendByQueryObj(query).limit(0, 10).update());
        assertEquals(1, dao.selectFrom(User.class, "u")
                .eq("name", first.getName()).eq("state", "DONE").count());
        assertEquals(1, dao.selectFrom(User.class, "u")
                .eq("name", second.getName()).eq("state", "CLOSED").count());
    }
}
