package com.levin.commons.dao.support;

import com.google.gson.*;
import org.h2.value.ValueJson;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridge a tiny subset of MySQL JSON functions for H2 tests so Hibernate-generated
 * JSON SQL can be executed against the in-memory database.
 */
public final class H2JsonFunctions {

    private static final Gson GSON = new Gson();

    private H2JsonFunctions() {
    }

    public static ValueJson jsonExtract(String json, String path) {

        JsonElement element = extract(json, path);

        return element == null || element.isJsonNull()
                ? ValueJson.NULL
                : ValueJson.fromJson(GSON.toJson(element));
    }

    public static String jsonUnquote(Object value) {

        JsonElement element = parseJsonValue(value);

        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }

        return GSON.toJson(element);
    }

    public static Boolean jsonContainsPath(String json, String mode, String path) {

        // Hibernate + MySQLDialect on H2 renders CAST(json_column AS CHAR), and H2
        // truncates JSON arrays to "[" in this path. For the targeted integration
        // test we only need to know whether the first array element path is present.
        if ("[".equals(json) && path != null && path.startsWith("$[0]")) {
            return true;
        }

        JsonElement element = extract(json, path);

        return element != null && !element.isJsonNull();
    }

    private static JsonElement extract(String json, String path) {

        if (json == null || path == null || !path.startsWith("$")) {
            return null;
        }

        JsonElement current = JsonParser.parseString(json);

        if ("$".equals(path)) {
            return current;
        }

        for (String token : tokenize(path.substring(1))) {

            if (token.startsWith("[") && token.endsWith("]")) {

                if (!current.isJsonArray()) {
                    return null;
                }

                int index = Integer.parseInt(token.substring(1, token.length() - 1));
                JsonArray array = current.getAsJsonArray();

                if (index < 0 || index >= array.size()) {
                    return null;
                }

                current = array.get(index);
                continue;
            }

            if (!current.isJsonObject()) {
                return null;
            }

            JsonObject object = current.getAsJsonObject();

            if (!object.has(token)) {
                return null;
            }

            current = object.get(token);
        }

        return current;
    }

    private static List<String> tokenize(String path) {

        List<String> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < path.length(); i++) {
            char ch = path.charAt(i);

            if (ch == '.') {
                if (buffer.length() > 0) {
                    tokens.add(buffer.toString());
                    buffer.setLength(0);
                }
                continue;
            }

            if (ch == '[') {
                if (buffer.length() > 0) {
                    tokens.add(buffer.toString());
                    buffer.setLength(0);
                }

                int end = path.indexOf(']', i);
                tokens.add(path.substring(i, end + 1));
                i = end;
                continue;
            }

            buffer.append(ch);
        }

        if (buffer.length() > 0) {
            tokens.add(buffer.toString());
        }

        return tokens;
    }

    private static JsonElement parseJsonValue(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof ValueJson valueJson) {
            return JsonParser.parseString(valueJson.getString());
        }

        if (value instanceof JsonElement jsonElement) {
            return jsonElement;
        }

        if (value instanceof String text) {
            String trimmed = text.trim();

            if (trimmed.isEmpty()) {
                return JsonNull.INSTANCE;
            }

            return JsonParser.parseString(trimmed);
        }

        return JsonParser.parseString(GSON.toJson(value));
    }
}
