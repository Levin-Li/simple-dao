package com.levin.commons.dao.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/** MapStruct 复用的 JSON 对象转换。 */
public interface JsonObjectMapping {

    default JSONObject map(String json) {
        return json == null ? null : JSON.parseObject(json);
    }

    default String map(JSONObject value) {
        return value == null ? null : JSON.toJSONString(value);
    }
}
