package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.PrimitiveValueWrapper;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.misc.ForceSplitCondition;
import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonPrimitiveAnnotationQueryTest {

    @Test
    void queryWithoutPrimitiveValueStillTreatsJdbcTypeCodeCollectionAsWholeParameter() throws Exception {
        JpaDaoImpl jpaDao = new JpaDaoImpl();

        assertFalse(QueryWithoutPrimitiveValue.class
                .getDeclaredField("containsDomainList")
                .isAnnotationPresent(PrimitiveValue.class));
        assertTrue(jpaDao.hasPrimitiveAnnotation(TestUser.class
                .getDeclaredField("domainList")
                .getAnnotations()));

        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(primitiveAwareStub(jpaDao), false, TestUser.class, "u");
        String statement = dao.appendByQueryObj(new QueryWithoutPrimitiveValue()
                        .setContainsDomainList(List.of("example.com", "example.org")))
                .genFinalStatement();

        assertEquals(1, countOccurrences(statement, "json_exists("), statement);
        assertTrue(dao.genFinalParamList().stream()
                        .filter(PrimitiveValueWrapper.class::isInstance)
                        .map(PrimitiveValueWrapper.class::cast)
                        .map(wrapper -> ((PrimitiveValueWrapper<?>) wrapper).get())
                        .anyMatch(List.class::isInstance),
                () -> "实体 @JdbcTypeCode 仍应使整个 List 作为原子参数，实际参数：" + dao.genFinalParamList());
    }

    @Test
    void expandCollectionShouldGenerateOneConditionForEachCollectionElement() {
        JpaDaoImpl jpaDao = new JpaDaoImpl();
        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(primitiveAwareStub(jpaDao), false, TestUser.class, "u");

        String statement = dao.appendByQueryObj(new QueryWithExpandedCollection()
                        .setContainsDomainList(List.of("example.com", "example.org")))
                .genFinalStatement();

        assertEquals(2, countOccurrences(statement, "json_exists("), statement);
        assertTrue(statement.toLowerCase().contains(" or "), statement);
        assertFalse(dao.genFinalParamList().stream()
                        .filter(PrimitiveValueWrapper.class::isInstance)
                        .map(PrimitiveValueWrapper.class::cast)
                        .map(wrapper -> ((PrimitiveValueWrapper<?>) wrapper).get())
                        .anyMatch(List.class::isInstance),
                () -> "展开后不得将整个 List 作为一个参数，实际参数：" + dao.genFinalParamList());
    }

    @Test
    void forceSplitConditionShouldSupportArraysAndIgnoreEmptyCollections() {
        JpaDaoImpl jpaDao = new JpaDaoImpl();

        SelectDaoImpl<TestUser> arrayDao = new SelectDaoImpl<>(primitiveAwareStub(jpaDao), false, TestUser.class, "u");
        String arrayStatement = arrayDao.appendByQueryObj(new QueryWithExpandedArray()
                        .setContainsDomainArray(new String[]{"example.com", "example.org"}))
                .genFinalStatement();

        assertEquals(2, countOccurrences(arrayStatement, "json_exists("), arrayStatement);
        assertTrue(arrayStatement.toLowerCase().contains(" or "), arrayStatement);

        SelectDaoImpl<TestUser> emptyDao = new SelectDaoImpl<>(primitiveAwareStub(jpaDao), false, TestUser.class, "u");
        String emptyStatement = emptyDao.appendByQueryObj(new QueryWithExpandedCollection()
                        .setContainsDomainList(List.of()))
                .genFinalStatement();

        assertFalse(emptyStatement.contains(" Where "), emptyStatement);
    }

    private MiniDao primitiveAwareStub(JpaDaoImpl jpaDao) {
        return (MiniDao) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MiniDao.class},
                (proxy, method, args) -> {
                    if ("hasPrimitiveAnnotation".equals(method.getName())) {
                        return jpaDao.hasPrimitiveAnnotation((Annotation[]) args[0]);
                    }
                    if ("getParamPlaceholder".equals(method.getName())) {
                        return ":?";
                    }
                    if ("getSafeModeMaxLimit".equals(method.getName())) {
                        return 10;
                    }
                    if ("getNamingStrategy".equals(method.getName())) {
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    private int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    static class TestUser {
        @JdbcTypeCode(SqlTypes.JSON)
        List<String> domainList;
    }

    static class QueryWithoutPrimitiveValue {
        @Contains(value = "domainList", jsonPath = "$[*]")
        List<String> containsDomainList;

        QueryWithoutPrimitiveValue setContainsDomainList(List<String> containsDomainList) {
            this.containsDomainList = containsDomainList;
            return this;
        }
    }

    static class QueryWithExpandedCollection {
        @ForceSplitCondition
        @OR(autoClose = true)
        @Contains(value = "domainList", jsonPath = "$[*]")
        List<String> containsDomainList;

        QueryWithExpandedCollection setContainsDomainList(List<String> containsDomainList) {
            this.containsDomainList = containsDomainList;
            return this;
        }
    }

    static class QueryWithExpandedArray {
        @ForceSplitCondition
        @OR(autoClose = true)
        @Contains(value = "domainList", jsonPath = "$[*]")
        String[] containsDomainArray;

        QueryWithExpandedArray setContainsDomainArray(String[] containsDomainArray) {
            this.containsDomainArray = containsDomainArray;
            return this;
        }
    }
}
