package com.levin.commons.dao.support;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonMappingTest {

    private final JsonObjectMapping objectMapping = new JsonObjectMapping() { };
    private final JsonArrayMapping arrayMapping = new JsonArrayMapping() { };

    @Test
    void shouldConvertJsonObjectInBothDirections() {
        JSONObject value = objectMapping.map("{\"name\":\"dao\"}");
        assertEquals("dao", value.getString("name"));
        assertEquals(value, objectMapping.map(objectMapping.map(value)));
        assertNull(objectMapping.map((String) null));
        assertNull(objectMapping.map((JSONObject) null));
    }

    @Test
    void shouldConvertStringArrayInBothDirections() {
        List<String> values = List.of("dao", "a\"b");
        assertEquals(values, arrayMapping.fromJsonArray(arrayMapping.toJsonArray(values)));
        assertNull(arrayMapping.fromJsonArray(null));
        assertNull(arrayMapping.toJsonArray(null));
    }
}
