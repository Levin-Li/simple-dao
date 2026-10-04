package com.levin.commons.dao.support;

import com.levin.commons.dao.Converter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectDaoImplTest {

    @Test
    void defaultConverterShouldApplyOnceToListAndSingleResults() {
        Source source = new Source("dao");
        AtomicInteger calls = new AtomicInteger();
        SelectDaoImpl<Source> dao = sourceDao(source);
        dao.setDefaultResultConverter(value -> {
            calls.incrementAndGet();
            return value.name;
        });

        assertEquals(List.of("dao"), dao.find());
        assertEquals("dao", dao.findOne());
        assertEquals("dao", dao.findUnique());
        assertEquals(3, calls.get());
    }

    @Test
    void explicitConverterShouldOverrideDefaultAndReceiveOriginalResult() {
        Source source = new Source("dao");
        AtomicInteger calls = new AtomicInteger();
        SelectDaoImpl<Source> dao = sourceDao(source);
        dao.setDefaultResultConverter(value -> {
            calls.incrementAndGet();
            return "default";
        });
        Converter<Source, Source> explicit = value -> value;

        assertSame(source, dao.find(explicit).get(0));
        assertSame(source, dao.findOne(explicit));
        assertEquals(0, calls.get());
        assertFalse(dao.isAllowLazyLoading());
        assertSame(dao, dao.setAllowLazyLoading(true));
        assertTrue(dao.isAllowLazyLoading());
    }

    private SelectDaoImpl<Source> sourceDao(Source source) {
        return new SelectDaoImpl<Source>() {
            @Override
            public <E> List<E> findList(Class<E> resultClass) {
                return (List<E>) List.of(source);
            }
        };
    }

    private static class Source {
        final String name;

        Source(String name) {
            this.name = name;
        }
    }
}
