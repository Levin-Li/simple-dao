package com.levin.commons.dao.util;

import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.E_Func;
import com.levin.commons.dao.annotation.Func;
import com.levin.commons.dao.annotation.misc.Case;
import com.levin.commons.dao.annotation.misc.E_Case;
import com.levin.commons.dao.annotation.misc.E_Case_When;
import com.levin.commons.dao.exception.StatementBuildException;
import com.levin.commons.dao.support.ValueHolder;
import com.levin.commons.utils.ClassUtils;
import com.levin.commons.utils.MapUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExprUtilsTest {

    private C newCTestAnnotation(Op op, String value, String domain, String jsonPath) {
        return (C) ClassUtils.newAnnotation(C.class, null, MapUtils
                .put("op", (Object) op)
                .put("value", value)
                .put("domain", domain)
                .put("jsonPath", jsonPath)
                .put("isAddAliasPrefixForValue", true)
                .put("having", false)
                .put("not", false)
                .put("require", false)
                .put("condition", C.VALUE_NOT_EMPTY)
                .put("filterNullValue", true)
                .put("fieldCases", new Case[0])
                .put("paramCases", new Case[0])
                .put("fieldFuncs", new Func[0])
                .put("paramFuncs", new Func[0])
                .put("surroundPrefix", "")
                .put("surroundSuffix", "")
                .put("paramExpr", "")
                .put("patterns", new String[0])
                .put("paramDelimiter", "")
                .build(), false);
    }

    @Test
    void genCaseExpr() {


        Map<String, Object> caseMap = MapUtils
                .put(E_Case.column, (Object) "column_name")
                .put(E_Case.condition, "")
                .put(E_Case.elseExpr, C.BLANK_VALUE)
                .build();


        Map<String, Object> whenMap = MapUtils
                .put(E_Case_When.whenExpr, (Object) "'A'")
                .put(E_Case_When.thenExpr, (Object) "1")
                .build();

        Case.When when1 = (Case.When) ClassUtils.newAnnotation(Case.When.class, null, whenMap, false);

        whenMap.put(E_Case_When.whenExpr, "'B'");
        Case.When when2 = (Case.When) ClassUtils.newAnnotation(Case.When.class, null, whenMap, false);

        whenMap.put(E_Case_When.whenExpr, "'C'");
        Case.When when3 = (Case.When) ClassUtils.newAnnotation(Case.When.class, null, whenMap, false);

        Case.When[] whenOptions = {when1, when2, when3};


        caseMap.put(E_Case.whenOptions, whenOptions);

        Case aCase = (Case) ClassUtils.newAnnotation(Case.class, null, caseMap, false);


        String initExpr = ExprUtils.genCaseExpr(null, null, null, "initExpr", aCase);

        caseMap.put(E_Case.column, "");

        aCase = (Case) ClassUtils.newAnnotation(Case.class, null, caseMap, false);

        initExpr = ExprUtils.genCaseExpr(null, null, c -> true, "initExpr", aCase);

        System.out.println(initExpr);

    }

    @Test
    void funcExpr() {

        Map<String, Object> value = MapUtils
                .put(E_Func.value, (Object) "Fun1")
                .put(E_Func.prefix, "( ")
                .put(E_Func.params, new String[]{C.ORIGIN_EXPR, "param2"})
                .put(E_Func.paramDelimiter, ",")
                .put(E_Func.suffix, " )")
                .build();

        Func func1 = (Func) ClassUtils.newAnnotation(Func.class, null, value, false);

        value.put(E_Func.value, "Fun2");

        Func func2 = (Func) ClassUtils.newAnnotation(Func.class, null, value, false);

        value.put(E_Func.value, "Fun3");
        //  value.put(E_Func.params, new String[]{"param1", "param2"});
        Func func3 = (Func) ClassUtils.newAnnotation(Func.class, null, value, false);

        value.put(E_Func.value, "Fun4");
        value.put(E_Func.params, new String[]{C.ORIGIN_EXPR});
        Func func4 = (Func) ClassUtils.newAnnotation(Func.class, null, value, false);

        String funcExpr = ExprUtils.genFuncExpr(null, " time ", func1, func2, func3, func4);

        System.out.println(funcExpr);

    }

    @Test
    void replace() {


        String txt = "select * from ${table} t where t.name = :?  and t.age > :age  and t.sex = ${:sex} and t.desc like ${likeDesc}";


        txt = ExprUtils.replace(txt, Arrays.asList(MapUtils.put("table", "person").put("likeDesc", "'%it'").build()));


        System.out.println(txt);


    }

    @Test
    void genExprShouldUseJsonValueForEq() {

        C c = newCTestAnnotation(Op.Eq, "ext", "u", "$.profile.name");

        String expr = ExprUtils.genExpr(c, "ext", false, String.class,
                new ValueHolder<>(null, "ext", "tom"),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>());

        assertTrue(expr.contains("json_value"), "Eq 的 JSON 路径应转换为 Hibernate 标准 json_value");
        assertTrue(expr.contains("$.profile.name"), "Eq 的 JSON 路径应出现在表达式中");
    }

    @Test
    void genExprShouldUseJsonQueryForWildcardContains() {

        C c = newCTestAnnotation(Op.Contains, "ext", "u", "$.items[*].sku");

        String expr = ExprUtils.genExpr(c, "ext", false, String.class,
                new ValueHolder<>(null, "ext", "sku-1"),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>());

        assertTrue(expr.contains("json_query"), "wildcard JSON 路径应转换为 Hibernate 标准 json_query");
        assertTrue(expr.contains("$.items[*].sku"), "wildcard JSON 路径应出现在表达式中");
    }

    @Test
    void genExprShouldUseJsonQueryForWildcardSelect() {

        C c = newCTestAnnotation(Op.Select, "ext", "u", "$.items[*].sku");

        String expr = ExprUtils.genExpr(c, "ext", false, String.class,
                new ValueHolder<>(null, "ext", null),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>());

        assertTrue(expr.contains("json_query"), "Select 的 wildcard JSON 路径应转换为 Hibernate 标准 json_query");
    }

    @Test
    void genExprShouldUseSelectableJsonExprForSelect() {

        C c = newCTestAnnotation(Op.Select, "roleList", "u", "$");

        String expr = ExprUtils.genExpr(c, "roleList", false, String.class,
                new ValueHolder<>(null, "roleList", null),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>());

        assertTrue(expr.contains("COALESCE(json_query"), "非 wildcard 的 Select 应同时兼容 JSON 对象/数组和标量提取");
        assertTrue(expr.contains("json_value"), "非 wildcard 的 Select 应包含 json_value 兜底");
    }

    @Test
    void genExprShouldUseJsonSetForUpdate() {

        C c = newCTestAnnotation(Op.Update, "ext", "u", "$.profile.name");

        String expr = ExprUtils.genExpr(c, "ext", false, String.class,
                new ValueHolder<>(null, "ext", "tom"),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>());

        assertTrue(expr.contains("json_set"), "Update 的 JSON 路径应转换为 Hibernate 标准 json_set");
        assertTrue(expr.startsWith("u.ext ="), "Update 仍应以原 JSON 列作为赋值左值");
    }

    @Test
    void genExprShouldUseJsonExistsForExists() {

        C c = newCTestAnnotation(Op.Exists, "logs", "u", "$[0].logText");

        String expr = ExprUtils.genExpr(c, "logs", false, String.class,
                new ValueHolder<>(null, "logs", Boolean.TRUE),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>());

        assertTrue(expr.contains("json_exists"), "Exists 的 JSON 路径应转换为 Hibernate 标准 json_exists");
        assertTrue(!expr.contains("EXISTS ("), "JSON 路径 Exists 不应继续生成子查询 EXISTS 语法");
    }

    @Test
    void genExprShouldRejectWildcardForUpdate() {

        C c = newCTestAnnotation(Op.Update, "logs", "u", "$[*].logText");

        assertThrows(StatementBuildException.class, () -> ExprUtils.genExpr(c, "logs", false, String.class,
                new ValueHolder<>(null, "logs", "test"),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>()));
    }

    @Test
    void genExprShouldRejectWildcardForStats() {

        C c = newCTestAnnotation(Op.Sum, "logs", "u", "$[*].score");

        assertThrows(StatementBuildException.class, () -> ExprUtils.genExpr(c, "logs", false, String.class,
                new ValueHolder<>(null, "logs", null),
                ":?",
                ignore -> true,
                domain -> domain,
                (domain, column) -> domain + "." + column,
                holder -> "",
                new ArrayList<>()));
    }
}
