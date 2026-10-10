package com.levin.commons.dao.support;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/** MapStruct 复用的 JSON 字符串数组转换。 */
public interface JsonArrayMapping {

    default List<String> fromJsonArray(String json) {
        return json == null ? null : JSON.parseArray(json, String.class);
    }

    default String toJsonArray(List<String> values) {
        return values == null ? null : JSON.toJSONString(values);
    }
}
