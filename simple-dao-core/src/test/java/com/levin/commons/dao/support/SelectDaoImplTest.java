package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.util.ObjectUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectDaoImplTest {

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
    void shouldConvertNumericKeyMapResultToDtoBySelectOrder() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyDao(), true);
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");

        Map<String, Object> hibernateMap = new LinkedHashMap<>();
        hibernateMap.put("0", "dao");
        hibernateMap.put("1", 12);

        Method method = SelectDaoImpl.class.getDeclaredMethod("tryConvertData",
                Object.class, Class.class, ValueHolder.class, int.class, String[].class);
        method.setAccessible(true);

        TargetDto dto = (TargetDto) method.invoke(dao,
                hibernateMap, TargetDto.class, new ValueHolder<>(null), 3, new String[0]);

        assertEquals("dao", dto.name);
        assertEquals(12, dto.age);
    }

    @Test
    void shouldConvertIntegerKeyMapResultToDtoBySelectOrder() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyDao(), true);
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");

        Map<Object, Object> hibernateMap = new LinkedHashMap<>();
        hibernateMap.put(0, "dao");
        hibernateMap.put(1, 12);

        TargetDto dto = convert(dao, hibernateMap, TargetDto.class);

        assertEquals("dao", dto.name);
        assertEquals(12, dto.age);
    }

    @Test
    void shouldPreferExistingDtoPropertyKeysOverNumericKeys() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyDao(), true);
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");

        Map<String, Object> hibernateMap = new LinkedHashMap<>();
        hibernateMap.put("name", "alias-value");
        hibernateMap.put("age", 18);
        hibernateMap.put("0", "numeric-value");
        hibernateMap.put("1", 99);

        TargetDto dto = convert(dao, hibernateMap, TargetDto.class);

        assertEquals("alias-value", dto.name);
        assertEquals(18, dto.age);
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

    @Test
    void shouldUseMapProjectionForJpaDtoWhenEverySelectHasMapping() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyJpaDao(), false);
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");
        dao.selectColumnsMap.put("name", new Object[]{"name", TargetDto.class.getDeclaredField("name")});
        dao.selectColumnsMap.put("age", new Object[]{"age", TargetDto.class.getDeclaredField("age")});

        assertEquals(Map.class, getProjectionResultClass(dao, TargetDto.class, false));
    }

    @Test
    void shouldNotUseMapProjectionWhenSelectMappingIsPartial() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyJpaDao(), false);
        dao.selectByStatement(true, "name as name");
        dao.selectByStatement(true, "age as age");
        dao.selectColumnsMap.put("name", new Object[]{"name", TargetDto.class.getDeclaredField("name")});

        assertNull(getProjectionResultClass(dao, TargetDto.class, false));
    }

    @Test
    void shouldNotUseMapProjectionForNativeQuery() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyJpaDao(), true);
        dao.selectByStatement(true, "name as name");
        dao.selectColumnsMap.put("name", new Object[]{"name", TargetDto.class.getDeclaredField("name")});

        assertNull(getProjectionResultClass(dao, TargetDto.class, false));
    }

    @Test
    void shouldNotUseMapProjectionForMapResultType() throws Exception {
        SelectDaoImpl<Object> dao = new SelectDaoImpl<>(copyOnlyJpaDao(), false);
        dao.selectByStatement(true, "name as name");
        dao.selectColumnsMap.put("name", new Object[]{"name", TargetDto.class.getDeclaredField("name")});

        assertNull(getProjectionResultClass(dao, Map.class, false));
    }

    static class TargetDto {
        String name;
        Integer age;
    }

    private <T> T convert(SelectDaoImpl<Object> dao, Object data, Class<T> targetType) throws Exception {
        Method method = SelectDaoImpl.class.getDeclaredMethod("tryConvertData",
                Object.class, Class.class, ValueHolder.class, int.class, String[].class);
        method.setAccessible(true);
        return (T) method.invoke(dao, data, targetType, new ValueHolder<>(null), 3, new String[0]);
    }

    private Class<?> getProjectionResultClass(SelectDaoImpl<Object> dao, Class<?> resultType, boolean noResultType) throws Exception {
        Method method = SelectDaoImpl.class.getDeclaredMethod("getProjectionResultClass", Class.class, boolean.class);
        method.setAccessible(true);
        return (Class<?>) method.invoke(dao, resultType, noResultType);
    }

    private MiniDao copyOnlyDao() {
        return copyOnlyDao(false);
    }

    private MiniDao copyOnlyJpaDao() {
        return copyOnlyDao(true);
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
