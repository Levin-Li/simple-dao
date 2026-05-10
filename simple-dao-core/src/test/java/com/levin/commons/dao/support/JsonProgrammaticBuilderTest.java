package com.levin.commons.dao.support;

import com.levin.commons.dao.exception.StatementBuildException;
import com.levin.commons.dao.MiniDao;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonProgrammaticBuilderTest {

    @Test
    void selectDaoShouldBuildJsonWhereAndSelectExpressions() {
        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(stubDao(), false, TestUser.class, "u");

        String statement = dao
                .jsonEq("logs", "$[0].logText", "hello")
                .jsonContains("roleList", "$[*]", "admin")
                .jsonExists("logs", "$[0].logText")
                .jsonSelect("logs", "$[0].logText", "firstLogText")
                .genFinalStatement();

        assertTrue(statement.contains("json_value(str(u.logs), '$[0].logText') = :?"), statement);
        assertTrue(statement.contains("json_query(str(u.roleList), '$[*]') like :?"), statement);
        assertTrue(statement.contains("json_exists(str(u.logs), '$[0].logText')"), statement);
        assertTrue(statement.contains("COALESCE(json_query(str(u.logs), '$[0].logText'), json_value(str(u.logs), '$[0].logText')) as firstLogText"), statement);
    }

    @Test
    void updateDaoShouldBuildJsonSetAndArrayAppendExpressions() {
        UpdateDaoImpl<TestUser> dao = new UpdateDaoImpl<>(stubDao(), false, TestUser.class, "u");

        String statement = dao
                .jsonSet("logs", "$[0].logText", "new text")
                .jsonArrayAppend("roleList", "admin")
                .where("u.id = :?", 1L)
                .limit(0, 1)
                .genFinalStatement();

        assertTrue(statement.contains("u.logs = json_set(u.logs, '$[0].logText', :?)"), statement);
        assertTrue(statement.contains("u.roleList = json_array_append(COALESCE(u.roleList , json_array()) , '$' , :?)"), statement);
    }

    @Test
    void updateDaoShouldRejectWildcardJsonPathForMutation() {
        UpdateDaoImpl<TestUser> dao = new UpdateDaoImpl<>(stubDao(), false, TestUser.class, "u");

        assertThrows(StatementBuildException.class,
                () -> dao.jsonSet("logs", "$[*].logText", "new text"));
        assertThrows(StatementBuildException.class,
                () -> dao.jsonArrayAppend("roleList", "$[*]", "admin"));
    }

    static class TestUser {
        Long id;
        String logs;
        List<String> roleList;
    }

    private MiniDao stubDao() {
        return (MiniDao) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MiniDao.class},
                (proxy, method, args) -> {
                    if ("getParamPlaceholder".equals(method.getName())) {
                        return ":?";
                    }
                    if ("getSafeModeMaxLimit".equals(method.getName())) {
                        return 10;
                    }

                    if (method.getReturnType() == boolean.class) {
                        return false;
                    } else if (method.getReturnType() == int.class) {
                        return 0;
                    }

                    return null;
                });
    }
}
