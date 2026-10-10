package com.levin.commons.dao.support;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** Shared JSON conversion functions used by generated entity mappers. */
public interface MapperJsonUtils {

    AtomicReference<JsonCodec> codec = new AtomicReference<>(new JsonCodec());

    public static class JsonCodec {
        public <T> T parse(String json, Type targetType) {
            return JSON.parseObject(json, targetType);
        }

        public String stringify(Object value) {
            return JSON.toJSONString(value);
        }
    }

    static <T> T fromJson(String json, Type targetType) {
        Objects.requireNonNull(targetType, "targetType");
        return json == null || json.isEmpty() ? null
                : Objects.requireNonNull(codec.get(), "codec").parse(json, targetType);
    }

    static String toJson(Object value) {
        return value == null ? null : Objects.requireNonNull(codec.get(), "codec").stringify(value);
    }
}
