package com.levin.commons.dao;

import com.levin.commons.dao.annotation.DaoAlwaysFalse;
import com.levin.commons.dao.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DaoAlwaysFalseExamplesTest extends DaoExamplesTestSupport {
    static class DenyAll { @DaoAlwaysFalse Object ignored; }

    @Test
    void fixedFalsePredicateExecutesAsEmptyResult() {
        assertEquals(0, dao.selectFrom(User.class, "u")
                .appendByQueryObj(new DenyAll()).count());
    }
}
