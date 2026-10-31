package com.levin.commons.dao;

import com.levin.commons.dao.annotation.*;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConditionGroupAttributeTest {

    @Test
    void wherePredicatesExposeOptionalGroupReference() throws Exception {
        List<Class<? extends Annotation>> predicates = List.of(
                Between.class, Contains.class, DaoAlwaysFalse.class, EndsWith.class,
                Eq.class, Exists.class, Gt.class, Gte.class, In.class,
                IsNotNull.class, IsNull.class, Like.class, Lt.class, Lte.class,
                NotBetween.class, NotContains.class, NotEndsWith.class,
                NotEq.class, NotExists.class, NotIn.class, NotLike.class,
                NotStartsWith.class, StartsWith.class, Where.class);

        for (Class<? extends Annotation> predicate : predicates)
            assertEquals("", predicate.getMethod("group").getDefaultValue(), predicate.getName());
    }
}
