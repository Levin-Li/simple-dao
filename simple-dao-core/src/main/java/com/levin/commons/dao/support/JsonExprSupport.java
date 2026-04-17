package com.levin.commons.dao.support;

public abstract class JsonExprSupport {

    private static String queryJsonExpr(String fieldExpr) {
        return "str(" + fieldExpr + ")";
    }

    public static String jsonValueExpr(String fieldExpr, String jsonPath) {
        return "json_value(" + queryJsonExpr(fieldExpr) + ", '" + jsonPath + "')";
    }

    public static String jsonSelectableExpr(String fieldExpr, String jsonPath) {
        String jsonExpr = queryJsonExpr(fieldExpr);
        return "COALESCE(json_query(" + jsonExpr + ", '" + jsonPath + "'), json_value(" + jsonExpr + ", '" + jsonPath + "'))";
    }

    public static String jsonQueryExpr(String fieldExpr, String jsonPath) {
        return "json_query(" + queryJsonExpr(fieldExpr) + ", '" + jsonPath + "')";
    }

    public static String jsonExistsExpr(String fieldExpr, String jsonPath) {
        return "json_exists(" + queryJsonExpr(fieldExpr) + ", '" + jsonPath + "')";
    }

    public static String jsonSetExpr(String fieldExpr, String jsonPath, String valueExpr) {
        return "json_set(" + fieldExpr + ", '" + jsonPath + "', " + valueExpr + ")";
    }
}
