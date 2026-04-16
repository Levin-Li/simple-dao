package com.levin.commons.dao;

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.Where;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.Count;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.exception.StatementBuildException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("dev")
@SpringBootTest(classes = {TestConfiguration.class})
class JsonPathAnnotationIntegrationTest {

    @Autowired
    SimpleDao dao;

    @Test
    void shouldGenerateJsonPathSelectAndWhereStatements() {

        String statement = dao.selectFrom(User.class, "u")
                .appendByQueryObj(new JsonPathSelectQO()
                        .setRole("R_ADMIN")
                        .setHasLog(Boolean.TRUE))
                .genFinalStatement();

        assertTrue(statement.contains("json_query(u.roleList, '$[*]')"), "wildcard where 条件应生成 json_query");
        assertTrue(statement.contains("json_exists(u.logs, '$[0].logText')"), "Exists 注解应生成 json_exists");
        assertTrue(statement.contains("COALESCE(json_query(u.logs, '$'), json_value(u.logs, '$'))"), "Select 注解应同时兼容对象/数组和标量 JSON 路径");
    }

    @Test
    void shouldGenerateJsonPathUpdateStatement() {

        String statement = dao.updateTo(User.class, "u")
                .appendByQueryObj(new JsonPathUpdateDTO()
                        .setFirstLogText("changed")
                        .setRole("R_ADMIN"))
                .genFinalStatement();

        assertTrue(statement.contains("u.logs = json_set(u.logs, '$[0].logText'"), "Update 注解应生成 json_set 更新语句");
        assertTrue(statement.contains("json_query(u.roleList, '$[*]')"), "Update 场景中的 where 条件也应支持 wildcard JSON 路径");
    }

    @Test
    void shouldRejectWildcardJsonPathForStatAnnotations() {
        assertThrows(StatementBuildException.class, () -> dao.selectFrom(User.class, "u")
                .appendByQueryObj(new JsonPathWildcardStatQO())
                .genFinalStatement());
    }

    @Test
    void shouldRejectWildcardJsonPathForUpdateAnnotations() {
        assertThrows(StatementBuildException.class, () -> dao.updateTo(User.class, "u")
                .appendByQueryObj(new JsonPathWildcardUpdateDTO().setFirstLogText("changed"))
                .genFinalStatement());
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = User.class, alias = "u")
    static class JsonPathSelectQO {

        @Contains(value = "roleList", jsonPath = "$[*]")
        String role;

        @Where(op = Op.Exists, value = "logs", jsonPath = "$[0].logText")
        Boolean hasLog;

        @Select(value = "logs", jsonPath = "$", alias = "logsJson")
        String logsJson;

        @Select(value = "logs", jsonPath = "$[0].logText", alias = "firstLogText")
        String firstLogText;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = User.class, alias = "u")
    static class JsonPathUpdateDTO {

        @Contains(value = "roleList", jsonPath = "$[*]")
        String role;

        @Update(value = "logs", jsonPath = "$[0].logText")
        String firstLogText;
    }

    @TargetOption(entityClass = User.class, alias = "u")
    static class JsonPathWildcardStatQO {

        @Count(value = "roleList", jsonPath = "$[*]", alias = "roleCount")
        Long roleCount;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = User.class, alias = "u")
    static class JsonPathWildcardUpdateDTO {

        @Update(value = "logs", jsonPath = "$[*].logText")
        String firstLogText;
    }
}
