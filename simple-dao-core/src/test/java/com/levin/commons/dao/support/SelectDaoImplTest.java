package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.Converter;
import com.levin.commons.dao.util.ObjectUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectDaoImplTest {

    @Test
    void defaultResultConverterShouldApplyToListAndSingleResultOnlyOnce() {
        Source source = new Source("dao");
        AtomicInteger calls = new AtomicInteger();
        SelectDaoImpl<Source> dao = sourceDao(source);
        dao.setDefaultResultConverter(value -> {
            calls.incrementAndGet();
            return new TargetDto(value.name);
        });

        assertEquals("dao", ((TargetDto) dao.find().get(0)).name);
        assertEquals("dao", ((TargetDto) dao.findOne()).name);
        assertEquals("dao", ((TargetDto) dao.findUnique()).name);
        assertEquals(3, calls.get());
    }

    @Test
    void explicitConverterShouldReceiveEntityAndOverrideDefault() {
        Source source = new Source("dao");
        AtomicInteger defaultCalls = new AtomicInteger();
        SelectDaoImpl<Source> dao = sourceDao(source);
        dao.setDefaultResultConverter(value -> {
            defaultCalls.incrementAndGet();
            return new TargetDto("default");
        });

        Converter<Source, String> explicit = value -> value.name;
        assertEquals(List.of("dao"), dao.find(explicit));
        assertEquals("dao", dao.findOne(explicit));
        assertEquals(0, defaultCalls.get());
    }

    private SelectDaoImpl<Source> sourceDao(Source source) {
        return new SelectDaoImpl<Source>() {
            @Override
            public <E> List<E> findList(Class<E> resultClass) {
                return (List<E>) List.of(source);
            }
        };
    }

    static class Source {
        final String name;

        Source(String name) {
            this.name = name;
        }
    }

    @Test
    void shouldConvertArrayResultToDtoByAlias() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyDao(), true);
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");

        Method method = SelectDaoImpl.class.getDeclaredMethod("tryConvertData",
                Object.class, Class.class, ValueHolder.class, int.class, String[].class);
        method.setAccessible(true);

        TargetDto dto = (TargetDto) method.invoke(dao,
                new Object[]{"dao", 12}, TargetDto.class, new ValueHolder<>(null), 3, new String[0]);

        assertEquals("dao", dto.name);
        assertEquals(12, dto.age);
    }

    @Test
    void shouldKeepPublicArrayToMapConversionBehavior() {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>();
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");

        Object result = dao.tryConvertArray2Map(new Object[]{"dao", 12}, new ValueHolder<>(null));

        assertTrue(result instanceof Map);
        assertEquals("dao", ((Map<?, ?>) result).get("name"));
        assertEquals(12, ((Map<?, ?>) result).get("age"));
    }

    @Test
    void shouldReturnMapTargetAsIsEvenWhenNumericKeysExist() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyDao(), true);
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");

        Map<String, Object> mapResult = new LinkedHashMap<>();
        mapResult.put("0", "dao");
        mapResult.put("1", 12);

        Object result = convert(dao, mapResult, Map.class);

        assertSame(mapResult, result);
    }

    static class TargetDto {
        String name;
        Integer age;

        TargetDto() {
        }

        TargetDto(String name) {
            this.name = name;
        }
    }

    private <T> T convert(SelectDaoImpl<Object> dao, Object data, Class<T> targetType) throws Exception {
        Method method = SelectDaoImpl.class.getDeclaredMethod("tryConvertData",
                Object.class, Class.class, ValueHolder.class, int.class, String[].class);
        method.setAccessible(true);
        return (T) method.invoke(dao, data, targetType, new ValueHolder<>(null), 3, new String[0]);
    }

    private MiniDao copyOnlyDao() {
        return copyOnlyDao(false);
    }

    private MiniDao copyOnlyDao(boolean jpa) {
        return (MiniDao) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MiniDao.class},
                (proxy, method, args) -> {
                    if ("copy".equals(method.getName())) {
                        return ObjectUtil.copyProperties(args[0], args[1], (Integer) args[2], (String[]) args[3]);
                    }

                    if ("isJpa".equals(method.getName())) {
                        return jpa;
                    }

                    if ("isEntityClass".equals(method.getName())) {
                        return false;
                    }

                    if (method.getReturnType() == boolean.class) {
                        return false;
                    } else if (method.getReturnType() == int.class) {
                        return 0;
                    }

                    return null;
                });
    }
}
