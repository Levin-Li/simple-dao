package com.levin.commons.dao.support;

import com.levin.commons.dao.DeleteDao;
import com.levin.commons.dao.exception.StatementBuildException;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonProgrammaticBuilderTest {

    @Test
    void selectDaoShouldBuildJsonWhereAndSelectExpressions() {
        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(stubDao(), false, TestUser.class, "u");

        String statement = dao
                .jsonEq("logs", "$[0].logText", "hello")
                .jsonNotEq("logs", "$[0].logText", "goodbye")
                .jsonGt("logs", "$.score", 6)
                .jsonGte("logs", "$.score", 7)
                .jsonLt("logs", "$.score", 8)
                .jsonLte("logs", "$.score", 7)
                .jsonContains("roleList", "$[*]", "admin")
                .jsonNotContains("roleList", "$[*]", "guest")
                .jsonTextLike("roleList", "$[*]", "%admin%")
                .jsonTextNotLike("roleList", "$[*]", "%guest%")
                .jsonExists("logs", "$[0].logText")
                .jsonNotExists("logs", "$[1].logText")
                .jsonSelect("logs", "$[0].logText", "firstLogText")
                .jsonValueSelect("logs", "$[0].logText", "firstLogValue")
                .jsonQuerySelect("logs", "$[*]", "allLogs")
                .genFinalStatement();

        assertTrue(statement.contains("json_exists(cast(u.logs as String), '$[0].logText?(@ == $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("json_exists(cast(u.logs as String), '$[0].logText?(@ != $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("json_exists(cast(u.logs as String), '$.score?(@ > $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("json_exists(cast(u.logs as String), '$.score?(@ >= $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("json_exists(cast(u.logs as String), '$.score?(@ < $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("json_exists(cast(u.logs as String), '$.score?(@ <= $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("json_exists(cast(u.roleList as String), '$[*]?(@ == $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("not json_exists(cast(u.roleList as String), '$[*]?(@ == $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("cast(json_query(cast(u.roleList as String), '$[*]') as String) like :?"), statement);
        assertTrue(statement.contains("cast(json_query(cast(u.roleList as String), '$[*]') as String) not like :?"), statement);
        assertTrue(statement.contains("json_exists(cast(u.logs as String), '$[0].logText')"), statement);
        assertTrue(statement.contains("not json_exists(cast(u.logs as String), '$[1].logText')"), statement);
        assertTrue(statement.contains("COALESCE(cast(json_query(cast(u.logs as String), '$[0].logText') as String), json_value(cast(u.logs as String), '$[0].logText')) as firstLogText"), statement);
        assertTrue(statement.contains("json_value(cast(u.logs as String), '$[0].logText') as firstLogValue"), statement);
        assertTrue(statement.contains("json_query(cast(u.logs as String), '$[*]') as allLogs"), statement);
    }

    @Test
    void selectDaoShouldBuildJsonProjectionExpressions() {
        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(stubDao(), false, TestUser.class, "u");

        String statement = dao
                .jsonObjectSelect("userJson", "'name' value u.name", "'roles' value u.roleList")
                .jsonArraySelect("userArray", "u.name", "u.roleList")
                .jsonArrayAggSelect("u.name", "nameList", "order by u.name")
                .jsonObjectAggSelect("u.name", "u.id", "nameIdMap")
                .genFinalStatement();

        assertTrue(statement.contains("json_object('name' value u.name,'roles' value u.roleList) as userJson"), statement);
        assertTrue(statement.contains("json_array(u.name,u.roleList) as userArray"), statement);
        assertTrue(statement.contains("json_arrayagg(u.name order by u.name) as nameList"), statement);
        assertTrue(statement.contains("json_objectagg(u.name value u.id) as nameIdMap"), statement);
    }

    @Test
    void selectDaoShouldBuildJsonArrayElementConditionsForScalarsOnly() {
        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(stubDao(), false, TestUser.class, "u");

        String statement = dao
                .jsonContains("roleList", "$[*]", 7)
                .jsonNotContains("roleList", "$[*]", Boolean.FALSE)
                .jsonNotEqOrNull("logs", "$.state", "disabled")
                .genFinalStatement();

        assertTrue(statement.contains("json_exists(cast(u.roleList as String), '$[*]?(@ == $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("not json_exists(cast(u.roleList as String), '$[*]?(@ == $value)' passing :? as value)"), statement);
        assertTrue(statement.contains("not json_exists(cast(u.logs as String), '$.state?(@ == $value)' passing :? as value)"), statement);
        assertThrows(IllegalArgumentException.class,
                () -> dao.jsonContains("roleList", "$[*]", List.of("nested", "array")));
    }

    @Test
    void jsonValueSelectShouldRejectWildcardPath() {
        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(stubDao(), false, TestUser.class, "u");

        assertThrows(IllegalArgumentException.class,
                () -> dao.jsonValueSelect("logs", "$[*]", "allLogs"));
        assertThrows(IllegalArgumentException.class,
                () -> dao.jsonValueSelect("logs", "$.items[0 to 2]", "allLogs"));
    }

    @Test
    void selectDaoShouldOrderEqualValueThenNullThenOtherValues() {
        SelectDaoImpl<TestUser> dao = new SelectDaoImpl<>(stubDao(), false, TestUser.class, "u");

        String statement = dao.orderByDescForEqOrNull(true, "state", "preferred").genFinalStatement();

        assertTrue(statement.contains("CASE") && statement.contains("WHEN u.state = :? THEN 2"), statement);
        assertTrue(statement.contains("WHEN u.state IS NULL  THEN 1"), statement);
        assertTrue(statement.contains("ELSE 0 END") && statement.contains("Desc"), statement);
    }

    @Test
    void updateDaoShouldBuildJsonMutationExpressions() {
        UpdateDaoImpl<TestUser> dao = new UpdateDaoImpl<>(stubDao(), false, TestUser.class, "u");

        String statement = dao
                .jsonSet("logs", "$[0].logText", "new text")
                .jsonReplace("logs", "$[0].logText", "replace text")
                .jsonInsert("logs", "$[0].createdBy", "system")
                .jsonRemove("logs", "$[0].temp")
                .jsonMergePatch("logs", "{\"enabled\":true}")
                .jsonArrayAppend("roleList", "admin")
                .jsonArrayInsert("roleList", "$[0]", "owner")
                .where("u.id = :?", 1L)
                .limit(0, 1)
                .genFinalStatement();

        assertTrue(statement.contains("u.logs = json_set(u.logs, '$[0].logText', :?)"), statement);
        assertTrue(statement.contains("u.logs = json_replace(u.logs, '$[0].logText', :?)"), statement);
        assertTrue(statement.contains("u.logs = json_insert(u.logs, '$[0].createdBy', :?)"), statement);
        assertTrue(statement.contains("u.logs = json_remove(u.logs, '$[0].temp')"), statement);
        assertTrue(statement.contains("u.logs = json_mergepatch(u.logs, :?)"), statement);
        assertTrue(statement.contains("u.roleList = json_array_append(COALESCE(u.roleList , json_array()) , '$' , :?)"), statement);
        assertTrue(statement.contains("u.roleList = json_array_insert(COALESCE(u.roleList , json_array()), '$[0]', :?)"), statement);
    }

    @Test
    void updateDaoShouldRejectWildcardJsonPathForMutation() {
        UpdateDaoImpl<TestUser> dao = new UpdateDaoImpl<>(stubDao(), false, TestUser.class, "u");

        assertThrows(StatementBuildException.class,
                () -> dao.jsonSet("logs", "$[*].logText", "new text"));
        assertThrows(StatementBuildException.class,
                () -> dao.jsonArrayAppend("roleList", "$[*]", "admin"));
        assertThrows(StatementBuildException.class,
                () -> dao.jsonReplace("logs", "$[*].logText", "new text"));
        assertThrows(StatementBuildException.class,
                () -> dao.jsonInsert("logs", "$[*].logText", "new text"));
        assertThrows(StatementBuildException.class,
                () -> dao.jsonRemove("logs", "$[*].logText"));
        assertThrows(StatementBuildException.class,
                () -> dao.jsonArrayInsert("roleList", "$[*]", "admin"));
    }

    @Test
    void deleteDaoShouldReuseJsonConditionExpressions() {
        DeleteDaoImpl<TestUser> dao = new DeleteDaoImpl<>(stubDao(), false, TestUser.class, "u");

        DeleteDao<TestUser> builder = dao
                .jsonEq("logs", "$[0].logText", "hello")
                .jsonNotExists("logs", "$[0].deletedAt");

        assertSame(dao, builder);
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
                    if ("getNamingStrategy".equals(method.getName())) {
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
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
