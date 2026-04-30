package com.levin.commons.dao.support;

import org.springframework.util.StringUtils;

public abstract class JsonExprSupport {

    private static final String HIBERNATE_USER_GUIDE_JSON_FUNCTIONS
            = "https://docs.hibernate.org/orm/7.1/userguide/html_single/Hibernate_User_Guide.html#hql-json-functions";

    private static final String HIBERNATE_JSON_FUNCTION_JAVADOCS
            = "https://docs.jboss.org/hibernate/orm/7.0/javadocs/org/hibernate/dialect/function/json/package-summary.html";

    /**
     * 将实体 JSON 字段转换为 Hibernate JSON 查询函数可接受的文本表达式。
     * 来源文档：Hibernate ORM 7 User Guide 的 HQL JSON 函数章节要求开启并使用 Hibernate 标准 JSON 函数；
     * 参考：{@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS}。
     */
    private static String queryJsonExpr(String fieldExpr) {
        return "str(" + fieldExpr + ")";
    }

    /**
     * 生成 Hibernate 7 `json_value(jsonDocument, jsonPath)` 表达式，用于从 JSON 中提取标量值。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_value()`;
     * Javadocs: `JsonValueFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonValueExpr(String fieldExpr, String jsonPath) {
        return jsonValueExpr(fieldExpr, jsonPath, new String[0]);
    }

    /**
     * 生成带 Hibernate 7 `json_value` 可选子句的表达式，例如 `returning ...`、`null on error`。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_value()`;
     * Javadocs: `JsonValueFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonValueExpr(String fieldExpr, String jsonPath, String... clauses) {
        return "json_value(" + queryJsonExpr(fieldExpr) + ", '" + jsonPath + "'" + joinClauses(clauses) + ")";
    }

    /**
     * 生成适合 Select 场景的 JSON 提取表达式：优先 `json_query` 提取对象/数组，失败时用 `json_value` 兜底提取标量。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_query()` 与 `json_value()`;
     * Javadocs: `JsonQueryFunction`、`JsonValueFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonSelectableExpr(String fieldExpr, String jsonPath) {
        String jsonExpr = queryJsonExpr(fieldExpr);
        return "COALESCE(json_query(" + jsonExpr + ", '" + jsonPath + "'), json_value(" + jsonExpr + ", '" + jsonPath + "'))";
    }

    /**
     * 生成 Hibernate 7 `json_query(jsonDocument, jsonPath)` 表达式，用于从 JSON 中提取对象或数组。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_query()`;
     * Javadocs: `JsonQueryFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonQueryExpr(String fieldExpr, String jsonPath) {
        return jsonQueryExpr(fieldExpr, jsonPath, new String[0]);
    }

    /**
     * 生成带 Hibernate 7 `json_query` 可选子句的表达式，例如 `with wrapper`、`null on empty`。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_query()`;
     * Javadocs: `JsonQueryFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonQueryExpr(String fieldExpr, String jsonPath, String... clauses) {
        return "json_query(" + queryJsonExpr(fieldExpr) + ", '" + jsonPath + "'" + joinClauses(clauses) + ")";
    }

    /**
     * 生成 Hibernate 7 `json_exists(jsonDocument, jsonPath)` 表达式，用于判断 JSON 路径是否存在。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_exists()`;
     * Javadocs: `JsonExistsFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonExistsExpr(String fieldExpr, String jsonPath) {
        return jsonExistsExpr(fieldExpr, jsonPath, new String[0]);
    }

    /**
     * 生成带 Hibernate 7 `json_exists` 可选子句的表达式，例如 `true on error`、`false on error`。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_exists()`;
     * Javadocs: `JsonExistsFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonExistsExpr(String fieldExpr, String jsonPath, String... clauses) {
        return "json_exists(" + queryJsonExpr(fieldExpr) + ", '" + jsonPath + "'" + joinClauses(clauses) + ")";
    }

    /**
     * 生成 Hibernate 7 `json_set(jsonDocument, jsonPath, value)` 表达式，用于新增或替换 JSON 路径值。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_set()`;
     * Javadocs: `JsonSetFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonSetExpr(String fieldExpr, String jsonPath, String valueExpr) {
        return "json_set(" + fieldExpr + ", '" + jsonPath + "', " + valueExpr + ")";
    }

    /**
     * 生成 Hibernate 7 `json_remove(jsonDocument, jsonPath...)` 表达式，用于删除一个或多个 JSON 路径。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_remove()`;
     * Javadocs: `JsonRemoveFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonRemoveExpr(String fieldExpr, String... jsonPathList) {
        return "json_remove(" + fieldExpr + joinPathArgs(jsonPathList) + ")";
    }

    /**
     * 生成 Hibernate 7 `json_replace(jsonDocument, jsonPath, value)` 表达式，用于仅替换已存在的 JSON 路径值。
     * 来源文档：Hibernate ORM 7 JSON 函数 Javadocs: `JsonReplaceFunction`;
     * 参考 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonReplaceExpr(String fieldExpr, String jsonPath, String valueExpr) {
        return "json_replace(" + fieldExpr + ", '" + jsonPath + "', " + valueExpr + ")";
    }

    /**
     * 生成 Hibernate 7 `json_insert(jsonDocument, jsonPath, value)` 表达式，用于仅在路径不存在时插入 JSON 路径值。
     * 来源文档：Hibernate ORM 7 JSON 函数 Javadocs: `JsonInsertFunction`;
     * 参考 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonInsertExpr(String fieldExpr, String jsonPath, String valueExpr) {
        return "json_insert(" + fieldExpr + ", '" + jsonPath + "', " + valueExpr + ")";
    }

    /**
     * 生成 Hibernate 7 `json_mergepatch(jsonDocument, patchDocument)` 表达式，用于按 RFC 7396 合并 JSON patch。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_mergepatch()`;
     * Javadocs: `JsonMergepatchFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonMergepatchExpr(String fieldExpr, String patchExpr) {
        return "json_mergepatch(" + fieldExpr + ", " + patchExpr + ")";
    }

    /**
     * 生成 Hibernate 7 `json_object(...)` 表达式，用于构造 JSON 对象。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_object()`;
     * Javadocs: `JsonObjectFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonObjectExpr(String... entryExprList) {
        if (entryExprList == null) {
            entryExprList = new String[0];
        }
        return "json_object(" + String.join(",", entryExprList) + ")";
    }

    /**
     * 生成 Hibernate 7 `json_object` 的键值片段，例如 `'name' value u.name`。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_object()`;
     * Javadocs: `JsonObjectFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonObjectEntryExpr(String keyExpr, String valueExpr) {
        return keyExpr + " value " + valueExpr;
    }

    /**
     * 生成 Hibernate 7 `json_array(...)` 表达式，用于构造 JSON 数组。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_array()`;
     * Javadocs: `JsonArrayFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonArrayExpr(String... paramExprList) {
        if (paramExprList == null) {
            paramExprList = new String[0];
        }
        return "json_array(" + String.join(",", paramExprList) + ")";
    }

    /**
     * 生成 Hibernate 7 `json_arrayagg(value)` 表达式，用于聚合值为 JSON 数组。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_arrayagg()`;
     * Javadocs: `JsonArrayAggFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonArrayAggExpr(String valueExpr, String... clauses) {
        return "json_arrayagg(" + valueExpr + joinClauses(clauses) + ")";
    }

    /**
     * 生成 Hibernate 7 `json_objectagg(key value value)` 表达式，用于聚合键值对为 JSON 对象。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_objectagg()`;
     * Javadocs: `JsonObjectAggFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonObjectAggExpr(String keyExpr, String valueExpr, String... clauses) {
        return "json_objectagg(" + keyExpr + " value " + valueExpr + joinClauses(clauses) + ")";
    }

    /**
     * 生成 Hibernate 7 `json_array_append(jsonDocument, jsonPath, value...)` 表达式，用于向 JSON 数组追加元素。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_array_append()`;
     * Javadocs: `JsonArrayAppendFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonArrayAppendExpr(String fieldExpr, String jsonPath, String valueExpr) {
        return "json_array_append(" + fieldExpr + " , '" + jsonPath + "' , " + valueExpr + ")";
    }

    /**
     * 生成 Hibernate 7 `json_array_insert(jsonDocument, jsonPath, value...)` 表达式，用于向 JSON 数组指定位置插入元素。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_array_insert()`;
     * Javadocs: `JsonArrayInsertFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonArrayInsertExpr(String fieldExpr, String jsonPath, String valueExpr) {
        return "json_array_insert(" + fieldExpr + ", '" + jsonPath + "', " + valueExpr + ")";
    }

    /**
     * 生成 Hibernate 7 `json_table(...)` 表达式，用于把 JSON 文档投影为可查询的派生表。
     * `json_table` 的 `columns(...)` 语法较复杂，这里保留原始片段由调用方传入，避免限制 Hibernate 方言能力。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_table()`;
     * Javadocs: `JsonTableFunction`，参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonTableExpr(String jsonExpr, String jsonPath, String columnsClause) {
        return "json_table(" + jsonExpr + ", '" + jsonPath + "' " + columnsClause + ")";
    }

    /**
     * 生成 Hibernate 7 任意 JSON 函数表达式，作为上述强类型方法未覆盖新增语法时的兜底入口。
     * 来源文档：Hibernate ORM 7 User Guide 的 HQL JSON 函数章节与 JSON 函数 Javadocs；
     * 参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS} 和 {@value #HIBERNATE_JSON_FUNCTION_JAVADOCS}。
     */
    public static String jsonFunctionExpr(String functionName, String... argExprList) {
        if (argExprList == null) {
            argExprList = new String[0];
        }
        return functionName + "(" + String.join(", ", argExprList) + ")";
    }

    /**
     * 拼接 Hibernate JSON 函数的可选子句。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions 的 `returning`、`on empty`、`on error` 等子句；
     * 参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS}。
     */
    private static String joinClauses(String... clauses) {
        if (clauses == null || clauses.length == 0) {
            return "";
        }
        return " " + String.join(" ", clauses);
    }

    /**
     * 拼接 Hibernate JSON 修改函数中的 JSON path 参数列表。
     * 来源文档：Hibernate ORM 7 User Guide，JSON functions: `json_remove()`;
     * 参考 {@value #HIBERNATE_USER_GUIDE_JSON_FUNCTIONS}。
     */
    private static String joinPathArgs(String... jsonPathList) {
        if (jsonPathList == null || jsonPathList.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (String jsonPath : jsonPathList) {
            if (StringUtils.hasText(jsonPath)) {
                builder.append(", '").append(jsonPath).append("'");
            }
        }

        return builder.toString();
    }
}
