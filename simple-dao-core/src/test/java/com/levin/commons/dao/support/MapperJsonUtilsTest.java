package com.levin.commons.dao.support;

import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapperJsonUtilsTest {

    @Test
    void defaultAndReplacementShouldPreserveDeclaredGenericType() {
        Type targetType = new TypeReference<List<String>>() {}.getType();
        assertEquals(List.of("a"), MapperJsonUtils.<List<String>>fromJson("[\"a\"]", targetType));
        assertEquals("[\"a\"]", MapperJsonUtils.toJson(List.of("a")));
        assertNull(MapperJsonUtils.toJson(null));
        assertNull(MapperJsonUtils.fromJson(null, targetType));
        assertNull(MapperJsonUtils.fromJson("", targetType));
        assertThrows(com.alibaba.fastjson2.JSONException.class,
                () -> MapperJsonUtils.fromJson("[bad", targetType));

        AtomicReference<Type> receivedType = new AtomicReference<>();
        MapperJsonUtils.JsonCodec original = MapperJsonUtils.codec.get();
        try {
            MapperJsonUtils.codec.set(new MapperJsonUtils.JsonCodec() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T parse(String json, Type type) {
                    assertEquals("custom", json);
                    receivedType.set(type);
                    return (T) List.of("mapped");
                }

                @Override
                public String stringify(Object value) {
                    assertEquals(List.of("mapped"), value);
                    return "serialized";
                }
            });
            assertNull(MapperJsonUtils.toJson(null));
            assertNull(MapperJsonUtils.fromJson(null, targetType));
            assertNull(MapperJsonUtils.fromJson("", targetType));
            List<String> value = MapperJsonUtils.fromJson("custom", targetType);
            assertEquals(List.of("mapped"), value);
            assertEquals(targetType, receivedType.get());
            assertEquals("serialized", MapperJsonUtils.toJson(value));
        } finally {
            MapperJsonUtils.codec.set(original);
        }
        assertEquals(List.of("a"), MapperJsonUtils.<List<String>>fromJson("[\"a\"]", targetType));
    }
}
