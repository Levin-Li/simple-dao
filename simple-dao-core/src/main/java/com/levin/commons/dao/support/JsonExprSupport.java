package com.levin.commons.dao.support;

public abstract class JsonExprSupport {

    public static String jsonValueExpr(String fieldExpr, String jsonPath) {
        return "json_value(" + fieldExpr + ", '" + jsonPath + "')";
    }

    public static String jsonSelectableExpr(String fieldExpr, String jsonPath) {
        return "COALESCE(json_query(" + fieldExpr + ", '" + jsonPath + "'), json_value(" + fieldExpr + ", '" + jsonPath + "'))";
    }

    public static String jsonQueryExpr(String fieldExpr, String jsonPath) {
        return "json_query(" + fieldExpr + ", '" + jsonPath + "')";
    }

    public static String jsonExistsExpr(String fieldExpr, String jsonPath) {
        return "json_exists(" + fieldExpr + ", '" + jsonPath + "')";
    }

    public static String jsonSetExpr(String fieldExpr, String jsonPath, String valueExpr) {
        return "json_set(" + fieldExpr + ", '" + jsonPath + "', " + valueExpr + ")";
    }
}
