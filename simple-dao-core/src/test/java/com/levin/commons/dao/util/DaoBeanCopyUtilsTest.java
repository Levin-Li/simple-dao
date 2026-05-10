package com.levin.commons.dao.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DaoBeanCopyUtilsTest {

    @Test
    void shouldExposeSamePublicStaticMethodSignaturesAsObjectUtil() {
        Arrays.stream(ObjectUtil.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> {
                    Method candidate = findSameSignature(method);
                    assertNotNull(candidate, () -> "DaoBeanCopyUtils missing method: " + method);
                    assertEquals(method.getReturnType(), candidate.getReturnType(),
                            () -> "Return type mismatch for " + method);
                });
    }

    @Test
    void shouldShareObjectUtilThreadLocalState() {
        assertSame(ObjectUtil.conversionService, DaoBeanCopyUtils.conversionService);
        assertSame(ObjectUtil.fetchPropertiesFilters, DaoBeanCopyUtils.fetchPropertiesFilters);
        assertSame(ObjectUtil.VARIABLE_INJECTOR_THREAD_LOCAL, DaoBeanCopyUtils.VARIABLE_INJECTOR_THREAD_LOCAL);
    }

    @Test
    void shouldKeepCopyPropertiesBehavior() {
        Source source = new Source();
        source.name = "dao";
        source.nested = new NestedSource();
        source.nested.value = "nested";

        Target objectUtilTarget = ObjectUtil.copyProperties(source, new Target(), 3);
        Target daoBeanCopyTarget = DaoBeanCopyUtils.copyProperties(source, new Target(), 3);

        assertEquals(objectUtilTarget.name, daoBeanCopyTarget.name);
        assertEquals(objectUtilTarget.nested.value, daoBeanCopyTarget.nested.value);
    }

    @Test
    void shouldKeepMapAndIndexValueBehavior() {
        Map<String, Object> source = new LinkedHashMap<>();
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("value", "v1");
        source.put("nested", nested);

        String objectUtilValue = ObjectUtil.getIndexValue(source, "nested.value");
        String daoBeanCopyValue = DaoBeanCopyUtils.getIndexValue(source, "nested.value");
        assertEquals(objectUtilValue, daoBeanCopyValue);

        Map objectUtilMap = ObjectUtil.copyField2Map(new Source("name"), null);
        Map daoBeanCopyMap = DaoBeanCopyUtils.copyField2Map(new Source("name"), null);

        assertEquals(objectUtilMap, daoBeanCopyMap);
    }

    @Test
    void shouldSkipUninitializedHibernateLazyFieldWithoutCallingGetter() {
        LazySource source = new LazySource();

        LazyTarget target = DaoBeanCopyUtils.copyProperties(source, new LazyTarget(), 3);

        assertNull(target.lazy);
        assertEquals(0, source.getterCalls);
    }

    private Method findSameSignature(Method method) {
        try {
            return DaoBeanCopyUtils.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    static class Source {
        String name;
        NestedSource nested;

        Source() {
        }

        Source(String name) {
            this.name = name;
        }
    }

    static class NestedSource {
        String value;
    }

    static class Target {
        String name;
        NestedTarget nested;
    }

    static class NestedTarget {
        String value;
    }

    static class LazySource {
        Object lazy = new HibernateLazyValue(false);
        int getterCalls;

        public Object getLazy() {
            getterCalls++;
            throw new IllegalStateException("getter should not be called for uninitialized lazy value");
        }
    }

    static class LazyTarget {
        Object lazy;
    }

    static class HibernateLazyValue {
        final boolean initialized;

        HibernateLazyValue(boolean initialized) {
            this.initialized = initialized;
        }
    }
}
