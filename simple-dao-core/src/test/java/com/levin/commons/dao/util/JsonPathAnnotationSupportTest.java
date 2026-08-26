package com.levin.commons.dao.util;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.Avg;
import com.levin.commons.dao.annotation.stat.Count;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.stat.Max;
import com.levin.commons.dao.annotation.stat.Min;
import com.levin.commons.dao.annotation.stat.Sum;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.support.JsonPathSpec;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonPathAnnotationSupportTest {

    @Test
    void allRootWhereAnnotationsShouldExposeJsonPath() {

        List<Class<? extends Annotation>> annotations = Arrays.asList(
                C.class,
                Eq.class, NotEq.class,
                Gt.class, Gte.class, Lt.class, Lte.class,
                In.class, NotIn.class,
                Between.class, NotBetween.class,
                Like.class, NotLike.class,
                Contains.class, NotContains.class,
                StartsWith.class, NotStartsWith.class,
                EndsWith.class, NotEndsWith.class,
                Exists.class, NotExists.class,
                IsNull.class, IsNotNull.class,
                Where.class
        );

        annotations.forEach(annotationType -> {
            Method method = assertDoesNotThrow(() -> annotationType.getDeclaredMethod("jsonPath"),
                    () -> annotationType.getName() + " 必须声明 jsonPath()");
            assertEquals(String.class, method.getReturnType(), () -> annotationType.getName() + ".jsonPath() 必须返回 String");
        });
    }

    @Test
    void selectUpdateAndStatAnnotationsShouldExposeJsonPath() {
        List<Class<? extends Annotation>> annotations = Arrays.asList(
                Select.class,
                Update.class,
                Avg.class,
                Count.class,
                GroupBy.class,
                Max.class,
                Min.class,
                Sum.class
        );

        annotations.forEach(annotationType -> {
            Method method = assertDoesNotThrow(() -> annotationType.getDeclaredMethod("jsonPath"),
                    () -> annotationType.getName() + " 必须声明 jsonPath()");
            assertEquals(String.class, method.getReturnType(), () -> annotationType.getName() + ".jsonPath() 必须返回 String");
        });
    }

    @Test
    void jsonPathSpecShouldRecognizeWildcardPaths() {
        JsonPathSpec spec = JsonPathSpec.parse("$.items[*].sku");

        assertTrue(spec.isWildcard(), "[*] 路径必须被识别为 wildcard");
        assertFalse(spec.isScalarOnlyAllowed(), "wildcard 路径不能作为统计单值路径");
    }

    @Test
    void jsonPathSpecShouldRecognizeOtherMultiValuedPaths() {
        assertTrue(JsonPathSpec.parse("$.items[0 to 2]").isMultiValued(), "数组范围路径必须被识别为多值路径");
        assertTrue(JsonPathSpec.parse("$.**.code").isMultiValued(), "递归路径必须被识别为多值路径");
        assertTrue(JsonPathSpec.parse("$.items?(@.enabled == true)").isMultiValued(), "过滤路径必须被识别为多值路径");
    }

    @Test
    void jsonPathSpecShouldRecognizeScalarPath() {
        JsonPathSpec spec = JsonPathSpec.parse("$.profile.name");

        assertFalse(spec.isWildcard(), "普通标量路径不应被识别为 wildcard");
        assertTrue(spec.isScalarOnlyAllowed(), "普通标量路径应允许用于统计");
    }

    @Test
    void jsonPathSpecShouldNotGuessObjectPathAsInvalidScalar() {
        JsonPathSpec spec = JsonPathSpec.parse("$.profile");

        assertFalse(spec.isWildcard(), "对象路径不应被识别为 wildcard");
        assertTrue(spec.isScalarOnlyAllowed(), "非 wildcard 路径不应在框架层被静态判定为非法标量路径");
    }

    @Test
    void jsonPathSpecShouldRejectUnsafeOrRelativePath() {
        assertThrows(IllegalArgumentException.class, () -> JsonPathSpec.parse("profile.name"));
        assertThrows(IllegalArgumentException.class, () -> JsonPathSpec.parse("$.profile['name']"));
    }
}
