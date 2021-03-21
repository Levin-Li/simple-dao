package com.levin.commons.dao.util;

import com.levin.commons.dao.annotation.E_Func;
import com.levin.commons.dao.annotation.Func;
import com.levin.commons.dao.annotation.misc.Case;
import com.levin.commons.dao.annotation.misc.E_Case;
import com.levin.commons.dao.annotation.misc.E_When;
import com.levin.commons.utils.ClassUtils;
import com.levin.commons.utils.MapUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

class ExprUtilsTest {

    @Test
    void genCaseExpr() {


        Map<String, Object> caseMap = MapUtils
                .put(E_Case.value, (Object) "column_name")
                .put(E_Case.condition, "")
                .put(E_Case.elseExpr, Case.NULL_VALUE)
                .build();


        Map<String, Object> whenMap = MapUtils
                .put(E_When.whenExpr, (Object) "'A'")
                .put(E_When.thenExpr, (Object) "1")
                .build();

        Case.When when1 = (Case.When) ClassUtils.newAnnotation(Case.When.class, null, whenMap, false);

        whenMap.put(E_When.whenExpr, "'B'");
        Case.When when2 = (Case.When) ClassUtils.newAnnotation(Case.When.class, null, whenMap, false);

        whenMap.put(E_When.whenExpr, "'C'");
        Case.When when3 = (Case.When) ClassUtils.newAnnotation(Case.When.class, null, whenMap, false);

        Case.When[] whenOptions = {when1, when2, when3};


        caseMap.put(E_Case.whenOptions, whenOptions);

        Case aCase = (Case) ClassUtils.newAnnotation(Case.class, null, caseMap, false);


        String initExpr = ExprUtils.genCaseExpr(null, "initExpr", aCase);

        caseMap.put(E_Case.value, "");

        aCase = (Case) ClassUtils.newAnnotation(Case.class, null, caseMap, false);

        initExpr = ExprUtils.genCaseExpr(c -> true, "initExpr", aCase);

        System.out.println(initExpr);

    }

    @Test
    void funcExpr() {

        Map<String, Object> value = MapUtils
                .put(E_Func.value, (Object) "Fun1")
                .put(E_Func.prefix, "( ")
                .put(E_Func.params, new String[]{Func.ORIGIN_EXPR, "param2"})
                .put(E_Func.suffix, " )")
                .build();

        Func func1 = (Func) ClassUtils.newAnnotation(Func.class, null, value, false);

        value.put(E_Func.value, "Fun2");

        Func func2 = (Func) ClassUtils.newAnnotation(Func.class, null, value, false);

        value.put(E_Func.value, "Fun3");
        //  value.put(E_Func.params, new String[]{"param1", "param2"});
        Func func3 = (Func) ClassUtils.newAnnotation(Func.class, null, value, false);

        value.put(E_Func.value, "Fun4");
        value.put(E_Func.params, new String[]{Func.ORIGIN_EXPR});
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
}