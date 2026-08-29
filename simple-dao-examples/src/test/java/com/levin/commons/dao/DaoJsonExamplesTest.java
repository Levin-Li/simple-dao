package com.levin.commons.dao;

import static com.levin.commons.dao.DaoExamplesTest.*;

import cn.hutool.core.map.MapUtil;
import com.google.gson.Gson;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.Where;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.Count;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.*;
import com.levin.commons.dao.domain.support.AbstractBaseEntityObject;
import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import com.levin.commons.dao.domain.support.E_TestEntity;
import com.levin.commons.dao.domain.support.TestEntity;
import com.levin.commons.dao.dto.*;
import com.levin.commons.dao.dto.task.CreateTask;
import com.levin.commons.dao.dto.task.QueryTaskReq;
import com.levin.commons.dao.dto.task.TaskInfo;
import com.levin.commons.dao.exception.StatementBuildException;
import com.levin.commons.dao.inject.InjectTestObj;
import com.levin.commons.dao.proxy.UserApi;
import com.levin.commons.dao.proxy.UserApi2;
import com.levin.commons.dao.proxy.UserApi3;
import com.levin.commons.dao.repository.Group2Dao;
import com.levin.commons.dao.repository.GroupDao;
import com.levin.commons.dao.repository.UserDao;
import com.levin.commons.dao.services.UserService;
import com.levin.commons.dao.services.dto.QueryUserEvt;
import com.levin.commons.dao.services.dto.UserInfo;
import com.levin.commons.dao.services.dto.UserUpdateEvt;
import com.levin.commons.dao.services.testrole.info.TestRoleInfo;
import com.levin.commons.dao.services.testrole.req.CreateTestRoleReq;
import com.levin.commons.dao.services.testrole.req.QueryTestRoleReq;
import com.levin.commons.dao.services.testrole.req.UpdateTestRoleReq;
import com.levin.commons.dao.support.DefaultPagingData;
import com.levin.commons.dao.support.PagingQueryHelper;
import com.levin.commons.dao.support.PagingQueryReq;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.utils.MapUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.hibernate.Session;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * DAO JSON 能力的端到端示例。
 */
class DaoJsonExamplesTest extends DaoExamplesTestSupport {
    @Test
    public void testIncrementModeUpdateDTO() throws Exception {

        UpdateDao<User> userUpdateDao = dao.updateTo(User.class);

        userUpdateDao
                .set(true, true, E_User.score, 1)
                .set(true, true, E_User.name, "+")

                // .set()

                //
                .set(User::getRoleList, Arrays.asList("R_NEW_ADMIN1", "R_NEW_ADMIN2"))

                .notEq(User::getScore, -123456)

                .eq(E_User.enable, false);

        String statement = userUpdateDao.genFinalStatement().replace("  ", "");

        System.out.println(statement);

        Assert.isTrue(statement.contains("score = ( COALESCE(score , 0)  +  COALESCE(:? , 0) ) , name = CONCAT( COALESCE(name , '')  ,  COALESCE(:? , '') )".replace("  ", "")));
        Assert.isTrue(statement.contains("roleList = :?"));
        Assert.isTrue(statement.replace(" ", "").contains("score!=:?"));

        userUpdateDao.update();
    }

    @Test
    public void testIncrementModeJsonArrayAppendStatement() {

        UpdateDao<User> userUpdateDao = dao.updateTo(User.class);

        userUpdateDao
                .set(true, true, User::getRoleList, "R_APPEND_1" )
                .eq(E_User.enable, false);

        String statement = userUpdateDao.genFinalStatement().replace("  ", "");
        List<Object> params = userUpdateDao.genFinalParamList();

        Assert.isTrue(statement.contains("roleList = json_array_append(COALESCE(roleList , json_array()) , '$' , :?)".replace("  ", "")),
                "Json 数组字段增量更新应生成 json_array_append 追加表达式");
//        Assert.isTrue(params.contains("R_APPEND_1") ,
//                "Json 数组字段增量更新应展开集合参数为多个追加元素");
    }

    @Test
    public void testIncrementModeJsonArrayAppendByUpdateAnnotation() {

        UpdateDao<User> userUpdateDao = dao.updateTo(User.class, "u")
                .appendByQueryObj(new JsonArrayAppendUpdateDTO()
                        .setRole( "R_DTO_APPEND_1" ))
                .eq(E_User.enable, false);

        String statement = userUpdateDao.genFinalStatement().replace("  ", "");
        List<Object> params = userUpdateDao.genFinalParamList();

        Assert.isTrue(statement.contains("u.roleList = json_array_append(COALESCE(u.roleList , json_array()) , '$' , :?)".replace("  ", "")),
                "Update 注解的 Json 数组字段增量更新应生成 json_array_append 追加表达式: " + statement);
//        Assert.isTrue(params.contains("R_DTO_APPEND_1") ,
//                "Update 注解的 Json 数组字段增量更新应展开集合参数为多个追加元素");
    }

    @Test
    public void testIncrementModeJsonArrayAppendByUpdateAnnotationShouldRejectWildcardJsonPath() {

        boolean threw = false;

        try {
            dao.updateTo(User.class, "u")
                    .appendByQueryObj(new JsonArrayAppendWildcardUpdateDTO()
                            .setRoleList(Arrays.asList("R_DTO_APPEND_WILDCARD_1", "R_DTO_APPEND_WILDCARD_2")))
                    .eq(E_User.enable, false)
                    .genFinalStatement();
        } catch (StatementBuildException e) {
            threw = true;
        }

        Assert.isTrue(threw, "Update 注解的 Json 数组字段增量更新不应支持 wildcard jsonPath");
    }

    @Test
    public void testPostgreSQLJsonArrayAppendObjectKeepsJsonObjectElement() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonAppendUser-" + System.nanoTime())
                .setActionLog(Collections.emptyList()));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 10:30:00")
                .setOperator("codex-test")
                .setAction("提交审核");

        dao.updateTo(PgJsonAppendUser.class)
                .set(true, true, PgJsonAppendUser::getActionLog, actionLog)
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        entityManager.clear();

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        select jsonb_typeof(action_log),
                               jsonb_typeof(action_log -> 0),
                               jsonb_array_length(action_log),
                               action_log -> 0 ->> 'action'
                        from pg_json_append_user
                        where id = ?1
                        """)
                .setParameter(1, user.getId())
                .getSingleResult();

        Assert.isTrue("array".equals(row[0]), "action_log 应该仍然是 JSON 数组: " + Arrays.toString(row));
        Assert.isTrue("object".equals(row[1]), "追加元素应该是 JSON object，不应该是 string: " + Arrays.toString(row));
        Assert.isTrue(((Number) row[2]).intValue() == 1, "action_log 应该追加 1 个元素: " + Arrays.toString(row));
        Assert.isTrue("提交审核".equals(row[3]), "追加对象字段内容不正确: " + Arrays.toString(row));
    }

    @Test
    public void testPostgreSQLJsonArrayAppendObjectByUpdateDtoKeepsJsonObjectElement() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonAppendUser-DTO-" + System.nanoTime())
                .setActionLog(Collections.emptyList()));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 11:10:00")
                .setOperator("codex-dto-test")
                .setAction("提交审核");

        dao.updateTo(PgJsonAppendUser.class)
                .appendByQueryObj(new PgJsonActionLogAppendReq().setActionLog(Collections.singletonList(actionLog)))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        entityManager.clear();

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        select jsonb_typeof(action_log),
                               jsonb_typeof(action_log -> 0),
                               jsonb_array_length(action_log),
                               action_log -> 0 ->> 'action'
                        from pg_json_append_user
                        where id = ?1
                        """)
                .setParameter(1, user.getId())
                .getSingleResult();

        Assert.isTrue("array".equals(row[0]), "action_log 应该仍然是 JSON 数组: " + Arrays.toString(row));
        Assert.isTrue("object".equals(row[1]), "DTO 追加元素应该是 JSON object，不应该是 string: " + Arrays.toString(row));
        Assert.isTrue(((Number) row[2]).intValue() == 1, "DTO action_log 应该追加 1 个元素: " + Arrays.toString(row));
        Assert.isTrue("提交审核".equals(row[3]), "DTO 追加对象字段内容不正确: " + Arrays.toString(row));
    }

    @Test
    public void testPostgreSQLJsonArrayAppendMethodObjectKeepsJsonObjectElement() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonArrayAppendMethodUser-" + System.nanoTime())
                .setActionLog(Collections.emptyList()));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 11:15:00")
                .setOperator("codex-array-append-method")
                .setAction("直接追加数组对象");

        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend(PgJsonAppendUser::getActionLog, "$", actionLog)
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertActionLogElement(user.getId(), 0, 1, "直接追加数组对象", "直接追加元素应该是 JSON object，不应该是 string");
    }

    @Test
    public void testPostgreSQLJsonArrayInsertObjectKeepsJsonObjectElement() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser.ActionLog oldActionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 11:20:00")
                .setOperator("codex-array-insert-old")
                .setAction("旧对象");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonArrayInsertUser-" + System.nanoTime())
                .setActionLog(Collections.singletonList(oldActionLog)));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 11:30:00")
                .setOperator("codex-array-insert")
                .setAction("插入数组对象");

        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayInsert(PgJsonAppendUser::getActionLog, "$[0]", actionLog)
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertActionLogElement(user.getId(), 0, 2, "插入数组对象", "插入元素应该是 JSON object，不应该是 string");
    }

    @Test
    public void testPostgreSQLJsonSetObjectKeepsJsonObjectValue() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonSetUser-" + System.nanoTime())
                .setProfile(new LinkedHashMap<>()));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 12:10:00")
                .setOperator("codex-json-set")
                .setAction("设置 JSON 对象");

        dao.updateTo(PgJsonAppendUser.class)
                .jsonSet(PgJsonAppendUser::getProfile, "$.latestAction", actionLog)
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertProfileJsonObject(user.getId(), "latestAction", "设置 JSON 对象");
    }

    @Test
    public void testPostgreSQLJsonSetObjectByUpdateDtoKeepsJsonObjectValue() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonSetDtoUser-" + System.nanoTime())
                .setProfile(new LinkedHashMap<>()));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 12:20:00")
                .setOperator("codex-json-set-dto")
                .setAction("DTO 设置 JSON 对象");

        dao.updateTo(PgJsonAppendUser.class)
                .appendByQueryObj(new PgJsonProfileSetReq().setActionLog(actionLog))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertProfileJsonObject(user.getId(), "latestAction", "DTO 设置 JSON 对象");
    }

    @Test
    public void testPostgreSQLJsonInsertObjectKeepsJsonObjectValue() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonInsertUser-" + System.nanoTime())
                .setProfile(new LinkedHashMap<>()));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 12:30:00")
                .setOperator("codex-json-insert")
                .setAction("插入 JSON 对象");

        dao.updateTo(PgJsonAppendUser.class)
                .jsonInsert(PgJsonAppendUser::getProfile, "$.insertedAction", actionLog)
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertProfileJsonObject(user.getId(), "insertedAction", "插入 JSON 对象");
    }

    @Test
    public void testPostgreSQLJsonReplaceObjectKeepsJsonObjectValue() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("replaceAction", Map.of("action", "旧值"));

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonReplaceUser-" + System.nanoTime())
                .setProfile(profile));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 12:40:00")
                .setOperator("codex-json-replace")
                .setAction("替换 JSON 对象");

        dao.updateTo(PgJsonAppendUser.class)
                .jsonReplace(PgJsonAppendUser::getProfile, "$.replaceAction", actionLog)
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertProfileJsonObject(user.getId(), "replaceAction", "替换 JSON 对象");
    }

    @Test
    public void testPostgreSQLJsonMergePatchObjectKeepsJsonObjectValue() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonMergePatchUser-" + System.nanoTime())
                .setProfile(new LinkedHashMap<>()));

        PgJsonAppendUser.ActionLog actionLog = new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 12:50:00")
                .setOperator("codex-json-mergepatch")
                .setAction("合并 JSON 对象");

        dao.updateTo(PgJsonAppendUser.class)
                .jsonMergePatch(PgJsonAppendUser::getProfile, Map.of("patchAction", actionLog))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertProfileJsonObject(user.getId(), "patchAction", "合并 JSON 对象");
    }

    @Test
    public void testPostgreSQLJsonRemoveDeletesPath() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 jsonb_typeof");

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("removeAction", Map.of("action", "待删除"));
        profile.put("keepAction", Map.of("action", "保留对象"));

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonRemoveUser-" + System.nanoTime())
                .setProfile(profile));

        dao.updateTo(PgJsonAppendUser.class)
                .jsonRemove(PgJsonAppendUser::getProfile, "$.removeAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        entityManager.clear();

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        select jsonb_exists(profile, 'removeAction'),
                               jsonb_typeof(profile -> 'keepAction'),
                               profile -> 'keepAction' ->> 'action'
                        from pg_json_append_user
                        where id = ?1
                        """)
                .setParameter(1, user.getId())
                .getSingleResult();

        Assert.isTrue(Boolean.FALSE.equals(row[0]), "jsonRemove 应该删除指定路径: " + Arrays.toString(row));
        Assert.isTrue("object".equals(row[1]), "jsonRemove 不应该破坏保留对象: " + Arrays.toString(row));
        Assert.isTrue("保留对象".equals(row[2]), "jsonRemove 保留对象内容不正确: " + Arrays.toString(row));
    }

    @Test
    public void testPostgreSQLJsonConditionMethods() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 Hibernate PostgreSQL JSON 条件函数");

        Map<String, Object> profile = profileWithAction("conditionAction", "条件 JSON 对象");
        profile.put("roles", List.of("JSON_ROLE"));

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonConditionUser-" + System.nanoTime())
                .setProfile(profile));

        long existsCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonExists(PgJsonAppendUser::getProfile, "$.conditionAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long notExistsCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotExists(PgJsonAppendUser::getProfile, "$.missingAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long eqCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonEq(PgJsonAppendUser::getProfile, "$.conditionAction.action", "条件 JSON 对象")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long notEqCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEq(PgJsonAppendUser::getProfile, "$.conditionAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long containsCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextLike(PgJsonAppendUser::getProfile, "$.conditionAction.action", "%JSON%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long notContainsCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextNotLike(PgJsonAppendUser::getProfile, "$.conditionAction.action", "%不存在%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long arrayContainsCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(PgJsonAppendUser::getProfile, "$.roles[*]", "JSON_ROLE")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long arrayNotContainsCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(PgJsonAppendUser::getProfile, "$.roles[*]", "MISSING_ROLE")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();

        Assert.isTrue(existsCount == 1, "jsonExists 应该命中 PG JSON 路径");
        Assert.isTrue(notExistsCount == 1, "jsonNotExists 应该命中缺失 PG JSON 路径");
        Assert.isTrue(eqCount == 1, "jsonEq 应该命中 PG JSON 标量值");
        Assert.isTrue(notEqCount == 1, "jsonNotEq 应该命中不等于的 PG JSON 标量值");
        Assert.isTrue(containsCount == 1, "jsonTextLike 应该命中 PG JSON 标量值");
        Assert.isTrue(notContainsCount == 1, "jsonTextNotLike 应该命中不包含的 PG JSON 标量值");
        Assert.isTrue(arrayContainsCount == 1, "jsonContains 应该命中 PG JSON 数组元素");
        Assert.isTrue(arrayNotContainsCount == 1, "jsonNotContains 应该命中不存在的 PG JSON 数组元素");
    }

    @Test
    public void testPostgreSQLJsonSelectMethods() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 Hibernate PostgreSQL JSON select 函数");

        String name = "PgJsonSelectUser-" + System.nanoTime();
        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName(name)
                .setProfile(profileWithAction("selectAction", "选择 JSON 对象")));

        String jsonSelect = dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonSelect(PgJsonAppendUser::getProfile, "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);
        String jsonValueSelect = dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonValueSelect(PgJsonAppendUser::getProfile, "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);
        String jsonQuerySelect = dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonQuerySelect(PgJsonAppendUser::getProfile, "$.selectAction", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);
        String jsonObjectSelect = dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonObjectSelect("jsonObject", "'name' value u.name")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);
        String jsonArraySelect = dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonArraySelect("jsonArray", "u.name")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);
        String jsonArrayAggSelect = dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonArrayAggSelect("u.name", "jsonArrayAgg")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);
        String jsonObjectAggSelect = dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonObjectAggSelect("u.name", "u.name", "jsonObjectAgg")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);

        Assert.isTrue(jsonTextEquals("选择 JSON 对象", jsonSelect), "jsonSelect 应该返回 PG JSON 标量值: " + jsonSelect);
        Assert.isTrue("选择 JSON 对象".equals(jsonValueSelect), "jsonValueSelect 应该返回 PG JSON 标量值: " + jsonValueSelect);
        Assert.isTrue(jsonQuerySelect != null && jsonQuerySelect.contains("选择 JSON 对象"),
                "jsonQuerySelect 应该返回 PG JSON 对象文本: " + jsonQuerySelect);
        Assert.isTrue(jsonObjectSelect != null && jsonObjectSelect.contains(name),
                "jsonObjectSelect 应该构造 PG JSON 对象: " + jsonObjectSelect);
        Assert.isTrue(jsonArraySelect != null && jsonArraySelect.contains(name),
                "jsonArraySelect 应该构造 PG JSON 数组: " + jsonArraySelect);
        Assert.isTrue(jsonArrayAggSelect != null && jsonArrayAggSelect.contains(name),
                "jsonArrayAggSelect 应该聚合 PG JSON 数组: " + jsonArrayAggSelect);
        Assert.isTrue(jsonObjectAggSelect != null && jsonObjectAggSelect.contains(name),
                "jsonObjectAggSelect 应该聚合 PG JSON 对象: " + jsonObjectAggSelect);
    }

    @Test
    public void testDaoJsonPublicApiCoverageMatrixIsCurrent() {

        Assert.isTrue(jsonMethodSignatures(UpdateBuilder.class).equals(new TreeSet<>(List.of(
                "jsonArrayAppend(Boolean,LambdaMethodAttr,Object)",
                "jsonArrayAppend(Boolean,LambdaMethodAttr,String,Object)",
                "jsonArrayAppend(Boolean,String,Object)",
                "jsonArrayAppend(Boolean,String,String,Object)",
                "jsonArrayAppend(LambdaMethodAttr,Object)",
                "jsonArrayAppend(LambdaMethodAttr,String,Object)",
                "jsonArrayAppend(String,Object)",
                "jsonArrayAppend(String,String,Object)",
                "jsonArrayInsert(Boolean,LambdaMethodAttr,String,Object)",
                "jsonArrayInsert(Boolean,String,String,Object)",
                "jsonArrayInsert(LambdaMethodAttr,String,Object)",
                "jsonArrayInsert(String,String,Object)",
                "jsonInsert(Boolean,LambdaMethodAttr,String,Object)",
                "jsonInsert(Boolean,String,String,Object)",
                "jsonInsert(LambdaMethodAttr,String,Object)",
                "jsonInsert(String,String,Object)",
                "jsonMergePatch(Boolean,LambdaMethodAttr,Object)",
                "jsonMergePatch(Boolean,String,Object)",
                "jsonMergePatch(LambdaMethodAttr,Object)",
                "jsonMergePatch(String,Object)",
                "jsonRemove(Boolean,LambdaMethodAttr,String[])",
                "jsonRemove(Boolean,String,String[])",
                "jsonRemove(LambdaMethodAttr,String[])",
                "jsonRemove(String,String[])",
                "jsonReplace(Boolean,LambdaMethodAttr,String,Object)",
                "jsonReplace(Boolean,String,String,Object)",
                "jsonReplace(LambdaMethodAttr,String,Object)",
                "jsonReplace(String,String,Object)",
                "jsonSet(Boolean,LambdaMethodAttr,String,Object)",
                "jsonSet(Boolean,String,String,Object)",
                "jsonSet(LambdaMethodAttr,String,Object)",
                "jsonSet(String,String,Object)"
        ))), "UpdateBuilder JSON 方法清单变化时，需要同步补齐测试矩阵");

        Assert.isTrue(jsonMethodSignatures(SimpleConditionBuilder.class).equals(new TreeSet<>(List.of(
                "jsonContains(Boolean,LambdaMethodAttr,String,String)",
                "jsonContains(Boolean,LambdaMethodAttr,String,Object)",
                "jsonContains(Boolean,String,String,String)",
                "jsonContains(Boolean,String,String,Object)",
                "jsonContains(LambdaMethodAttr,String,String)",
                "jsonContains(LambdaMethodAttr,String,Object)",
                "jsonContains(String,String,String)",
                "jsonContains(String,String,Object)",
                "jsonNotContains(Boolean,LambdaMethodAttr,String,String)",
                "jsonNotContains(Boolean,LambdaMethodAttr,String,Object)",
                "jsonNotContains(Boolean,String,String,String)",
                "jsonNotContains(Boolean,String,String,Object)",
                "jsonNotContains(LambdaMethodAttr,String,String)",
                "jsonNotContains(LambdaMethodAttr,String,Object)",
                "jsonNotContains(String,String,String)",
                "jsonNotContains(String,String,Object)",
                "jsonTextLike(Boolean,LambdaMethodAttr,String,String)",
                "jsonTextLike(Boolean,String,String,String)",
                "jsonTextLike(LambdaMethodAttr,String,String)",
                "jsonTextLike(String,String,String)",
                "jsonTextNotLike(Boolean,LambdaMethodAttr,String,String)",
                "jsonTextNotLike(Boolean,String,String,String)",
                "jsonTextNotLike(LambdaMethodAttr,String,String)",
                "jsonTextNotLike(String,String,String)",
                "jsonEq(Boolean,LambdaMethodAttr,String,Object)",
                "jsonEq(Boolean,String,String,Object)",
                "jsonEq(LambdaMethodAttr,String,Object)",
                "jsonEq(String,String,Object)",
                "jsonGt(Boolean,LambdaMethodAttr,String,Object)",
                "jsonGt(Boolean,String,String,Object)",
                "jsonGt(LambdaMethodAttr,String,Object)",
                "jsonGt(String,String,Object)",
                "jsonGte(Boolean,LambdaMethodAttr,String,Object)",
                "jsonGte(Boolean,String,String,Object)",
                "jsonGte(LambdaMethodAttr,String,Object)",
                "jsonGte(String,String,Object)",
                "jsonLt(Boolean,LambdaMethodAttr,String,Object)",
                "jsonLt(Boolean,String,String,Object)",
                "jsonLt(LambdaMethodAttr,String,Object)",
                "jsonLt(String,String,Object)",
                "jsonLte(Boolean,LambdaMethodAttr,String,Object)",
                "jsonLte(Boolean,String,String,Object)",
                "jsonLte(LambdaMethodAttr,String,Object)",
                "jsonLte(String,String,Object)",
                "jsonNotEq(Boolean,LambdaMethodAttr,String,Object)",
                "jsonNotEq(Boolean,String,String,Object)",
                "jsonNotEq(LambdaMethodAttr,String,Object)",
                "jsonNotEq(String,String,Object)",
                "jsonNotEqOrNull(Boolean,LambdaMethodAttr,String,Object)",
                "jsonNotEqOrNull(Boolean,String,String,Object)",
                "jsonNotEqOrNull(LambdaMethodAttr,String,Object)",
                "jsonNotEqOrNull(String,String,Object)",
                "jsonExists(Boolean,LambdaMethodAttr,String)",
                "jsonExists(Boolean,String,String)",
                "jsonExists(LambdaMethodAttr,String)",
                "jsonExists(String,String)",
                "jsonNotExists(Boolean,LambdaMethodAttr,String)",
                "jsonNotExists(Boolean,String,String)",
                "jsonNotExists(LambdaMethodAttr,String)",
                "jsonNotExists(String,String)"
        ))), "SimpleConditionBuilder JSON 方法清单变化时，需要同步补齐测试矩阵");

        Assert.isTrue(jsonMethodSignatures(SelectBuilder.class).equals(new TreeSet<>(List.of(
                "jsonArrayAggSelect(Boolean,String,String,String[])",
                "jsonArrayAggSelect(String,String,String[])",
                "jsonArraySelect(Boolean,String,String[])",
                "jsonArraySelect(String,String[])",
                "jsonObjectAggSelect(Boolean,String,String,String,String[])",
                "jsonObjectAggSelect(String,String,String,String[])",
                "jsonObjectSelect(Boolean,String,String[])",
                "jsonObjectSelect(String,String[])",
                "jsonQuerySelect(Boolean,LambdaMethodAttr,String,String,String[])",
                "jsonQuerySelect(Boolean,String,String,String,String[])",
                "jsonQuerySelect(LambdaMethodAttr,String,String,String[])",
                "jsonQuerySelect(String,String,String,String[])",
                "jsonSelect(Boolean,LambdaMethodAttr,String,String)",
                "jsonSelect(Boolean,String,String,String)",
                "jsonSelect(LambdaMethodAttr,String,String)",
                "jsonSelect(String,String,String)",
                "jsonValueSelect(Boolean,LambdaMethodAttr,String,String,String[])",
                "jsonValueSelect(Boolean,String,String,String,String[])",
                "jsonValueSelect(LambdaMethodAttr,String,String,String[])",
                "jsonValueSelect(String,String,String,String[])"
        ))), "SelectBuilder JSON 方法清单变化时，需要同步补齐测试矩阵");
    }

    @Test
    public void testPostgreSQLJsonUpdateBuilderEveryMethodOverload() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 DAO JSON 更新方法重载");

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("replaceAttr", Map.of("action", "旧值"));
        profile.put("replaceBoolAttr", Map.of("action", "旧值"));
        profile.put("replaceLambda", Map.of("action", "旧值"));
        profile.put("replaceBoolLambda", Map.of("action", "旧值"));
        profile.put("removeAttr", Map.of("action", "待删除"));
        profile.put("removeBoolAttr", Map.of("action", "待删除"));
        profile.put("removeLambda", Map.of("action", "待删除"));
        profile.put("removeBoolLambda", Map.of("action", "待删除"));

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonUpdateOverloadUser-" + System.nanoTime())
                .setActionLog(Collections.emptyList())
                .setProfile(profile));

        dao.updateTo(PgJsonAppendUser.class)
                .jsonSet("profile", "$.setAttr", actionLog("setAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonSet(true, "profile", "$.setBoolAttr", actionLog("setBoolAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonSet(PgJsonAppendUser::getProfile, "$.setLambda", actionLog("setLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonSet(true, PgJsonAppendUser::getProfile, "$.setBoolLambda", actionLog("setBoolLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        dao.updateTo(PgJsonAppendUser.class)
                .jsonReplace("profile", "$.replaceAttr", actionLog("replaceAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonReplace(true, "profile", "$.replaceBoolAttr", actionLog("replaceBoolAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonReplace(PgJsonAppendUser::getProfile, "$.replaceLambda", actionLog("replaceLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonReplace(true, PgJsonAppendUser::getProfile, "$.replaceBoolLambda", actionLog("replaceBoolLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        dao.updateTo(PgJsonAppendUser.class)
                .jsonInsert("profile", "$.insertAttr", actionLog("insertAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonInsert(true, "profile", "$.insertBoolAttr", actionLog("insertBoolAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonInsert(PgJsonAppendUser::getProfile, "$.insertLambda", actionLog("insertLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonInsert(true, PgJsonAppendUser::getProfile, "$.insertBoolLambda", actionLog("insertBoolLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        dao.updateTo(PgJsonAppendUser.class)
                .jsonMergePatch("profile", Map.of("patchAttr", actionLog("patchAttr")))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonMergePatch(true, "profile", Map.of("patchBoolAttr", actionLog("patchBoolAttr")))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonMergePatch(PgJsonAppendUser::getProfile, Map.of("patchLambda", actionLog("patchLambda")))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonMergePatch(true, PgJsonAppendUser::getProfile, Map.of("patchBoolLambda", actionLog("patchBoolLambda")))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        dao.updateTo(PgJsonAppendUser.class)
                .jsonRemove("profile", "$.removeAttr")
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonRemove(true, "profile", "$.removeBoolAttr")
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonRemove(PgJsonAppendUser::getProfile, "$.removeLambda")
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonRemove(true, PgJsonAppendUser::getProfile, "$.removeBoolLambda")
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend("actionLog", actionLog("appendDefaultAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend(true, "actionLog", actionLog("appendDefaultBoolAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend(PgJsonAppendUser::getActionLog, actionLog("appendDefaultLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend(true, PgJsonAppendUser::getActionLog, actionLog("appendDefaultBoolLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend("actionLog", "$", actionLog("appendPathAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend(true, "actionLog", "$", actionLog("appendPathBoolAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend(PgJsonAppendUser::getActionLog, "$", actionLog("appendPathLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayAppend(true, PgJsonAppendUser::getActionLog, "$", actionLog("appendPathBoolLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayInsert("actionLog", "$[0]", actionLog("insertArrayAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayInsert(true, "actionLog", "$[0]", actionLog("insertArrayBoolAttr"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayInsert(PgJsonAppendUser::getActionLog, "$[0]", actionLog("insertArrayLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();
        dao.updateTo(PgJsonAppendUser.class)
                .jsonArrayInsert(true, PgJsonAppendUser::getActionLog, "$[0]", actionLog("insertArrayBoolLambda"))
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        assertProfileJsonObject(user.getId(), "setAttr", "setAttr");
        assertProfileJsonObject(user.getId(), "setBoolAttr", "setBoolAttr");
        assertProfileJsonObject(user.getId(), "setLambda", "setLambda");
        assertProfileJsonObject(user.getId(), "setBoolLambda", "setBoolLambda");
        assertProfileJsonObject(user.getId(), "replaceAttr", "replaceAttr");
        assertProfileJsonObject(user.getId(), "replaceBoolAttr", "replaceBoolAttr");
        assertProfileJsonObject(user.getId(), "replaceLambda", "replaceLambda");
        assertProfileJsonObject(user.getId(), "replaceBoolLambda", "replaceBoolLambda");
        assertProfileJsonObject(user.getId(), "insertAttr", "insertAttr");
        assertProfileJsonObject(user.getId(), "insertBoolAttr", "insertBoolAttr");
        assertProfileJsonObject(user.getId(), "insertLambda", "insertLambda");
        assertProfileJsonObject(user.getId(), "insertBoolLambda", "insertBoolLambda");
        assertProfileJsonObject(user.getId(), "patchAttr", "patchAttr");
        assertProfileJsonObject(user.getId(), "patchBoolAttr", "patchBoolAttr");
        assertProfileJsonObject(user.getId(), "patchLambda", "patchLambda");
        assertProfileJsonObject(user.getId(), "patchBoolLambda", "patchBoolLambda");
        assertProfileKeysRemoved(user.getId(), "removeAttr", "removeBoolAttr", "removeLambda", "removeBoolLambda");
        assertActionLogAllObjects(user.getId(), 12);
    }

    @Test
    public void testPostgreSQLJsonConditionBuilderEveryMethodOverload() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 DAO JSON 条件方法重载");

        Map<String, Object> profile = profileWithAction("conditionAction", "条件重载对象");
        profile.put("roles", List.of("ROLE_MATCH"));
        profile.put("scalarValues", List.of(7, true));
        profile.put("score", 7);

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonConditionOverloadUser-" + System.nanoTime())
                .setProfile(profile));

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonExists("profile", "$.conditionAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonExists(String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonExists(true, "profile", "$.conditionAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonExists(Boolean, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonExists(PgJsonAppendUser::getProfile, "$.conditionAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonExists(Lambda, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonExists(true, PgJsonAppendUser::getProfile, "$.conditionAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonExists(Boolean, Lambda, String) 应命中");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotExists("profile", "$.missingAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotExists(String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotExists(true, "profile", "$.missingAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotExists(Boolean, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotExists(PgJsonAppendUser::getProfile, "$.missingAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotExists(Lambda, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotExists(true, PgJsonAppendUser::getProfile, "$.missingAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotExists(Boolean, Lambda, String) 应命中");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains("profile", "$.roles[*]", "ROLE_MATCH")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(String, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(true, "profile", "$.roles[*]", "ROLE_MATCH")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Boolean, String, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(PgJsonAppendUser::getProfile, "$.roles[*]", "ROLE_MATCH")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Lambda, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(true, PgJsonAppendUser::getProfile, "$.roles[*]", "ROLE_MATCH")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Boolean, Lambda, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains("profile", "$.scalarValues[*]", 7)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Object) 应命中数值数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(true, "profile", "$.scalarValues[*]", 7)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Boolean, String, String, Object) 应命中数值数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(PgJsonAppendUser::getProfile, "$.scalarValues[*]", Boolean.TRUE)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Object) 应命中布尔数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(true, PgJsonAppendUser::getProfile, "$.scalarValues[*]", Boolean.TRUE)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Boolean, Lambda, String, Object) 应命中布尔数组元素");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains("profile", "$.roles[*]", "MISSING_ROLE")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(String, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(true, "profile", "$.roles[*]", "MISSING_ROLE")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Boolean, String, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(PgJsonAppendUser::getProfile, "$.roles[*]", "MISSING_ROLE")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Lambda, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(true, PgJsonAppendUser::getProfile, "$.roles[*]", "MISSING_ROLE")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Boolean, Lambda, String, String) 应命中数组元素");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains("profile", "$.scalarValues[*]", 8)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Object) 应对缺失的数值数组元素命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(true, "profile", "$.scalarValues[*]", 8)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Boolean, String, String, Object) 应对缺失的数值数组元素命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(PgJsonAppendUser::getProfile, "$.scalarValues[*]", 8)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Lambda, String, Object) 应对缺失的数值数组元素命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(true, PgJsonAppendUser::getProfile, "$.scalarValues[*]", 8)
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Boolean, Lambda, String, Object) 应对缺失的数值数组元素命中");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonEq("profile", "$.conditionAction.action", "条件重载对象")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonEq(String, String, Object) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonEq(true, "profile", "$.conditionAction.action", "条件重载对象")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonEq(Boolean, String, String, Object) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonEq(PgJsonAppendUser::getProfile, "$.conditionAction.action", "条件重载对象")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonEq(Lambda, String, Object) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonEq(true, PgJsonAppendUser::getProfile, "$.conditionAction.action", "条件重载对象")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonEq(Boolean, Lambda, String, Object) 应命中");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEq("profile", "$.conditionAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEq(String, String, Object) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEq(true, "profile", "$.conditionAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEq(Boolean, String, String, Object) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEq(PgJsonAppendUser::getProfile, "$.conditionAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEq(Lambda, String, Object) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEq(true, PgJsonAppendUser::getProfile, "$.conditionAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEq(Boolean, Lambda, String, Object) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEqOrNull("profile", "$.missingAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEqOrNull(String, String, Object) 应把缺失路径视为匹配");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEqOrNull(true, "profile", "$.missingAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEqOrNull(Boolean, String, String, Object) 应把缺失路径视为匹配");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEqOrNull(PgJsonAppendUser::getProfile, "$.missingAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEqOrNull(Lambda, String, Object) 应把缺失路径视为匹配");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotEqOrNull(true, PgJsonAppendUser::getProfile, "$.missingAction.action", "错误值")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotEqOrNull(Boolean, Lambda, String, Object) 应把缺失路径视为匹配");

        assertJsonConditionMatches(user, "jsonGt(String, String, Object) 应按 JSON 数值比较", query -> query.jsonGt("profile", "$.score", 6));
        assertJsonConditionMatches(user, "jsonGt(Boolean, String, String, Object) 应按 JSON 数值比较", query -> query.jsonGt(true, "profile", "$.score", 6));
        assertJsonConditionMatches(user, "jsonGt(Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonGt(PgJsonAppendUser::getProfile, "$.score", 6));
        assertJsonConditionMatches(user, "jsonGt(Boolean, Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonGt(true, PgJsonAppendUser::getProfile, "$.score", 6));

        assertJsonConditionMatches(user, "jsonGte(String, String, Object) 应按 JSON 数值比较", query -> query.jsonGte("profile", "$.score", 7));
        assertJsonConditionMatches(user, "jsonGte(Boolean, String, String, Object) 应按 JSON 数值比较", query -> query.jsonGte(true, "profile", "$.score", 7));
        assertJsonConditionMatches(user, "jsonGte(Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonGte(PgJsonAppendUser::getProfile, "$.score", 7));
        assertJsonConditionMatches(user, "jsonGte(Boolean, Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonGte(true, PgJsonAppendUser::getProfile, "$.score", 7));

        assertJsonConditionMatches(user, "jsonLt(String, String, Object) 应按 JSON 数值比较", query -> query.jsonLt("profile", "$.score", 8));
        assertJsonConditionMatches(user, "jsonLt(Boolean, String, String, Object) 应按 JSON 数值比较", query -> query.jsonLt(true, "profile", "$.score", 8));
        assertJsonConditionMatches(user, "jsonLt(Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonLt(PgJsonAppendUser::getProfile, "$.score", 8));
        assertJsonConditionMatches(user, "jsonLt(Boolean, Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonLt(true, PgJsonAppendUser::getProfile, "$.score", 8));

        assertJsonConditionMatches(user, "jsonLte(String, String, Object) 应按 JSON 数值比较", query -> query.jsonLte("profile", "$.score", 7));
        assertJsonConditionMatches(user, "jsonLte(Boolean, String, String, Object) 应按 JSON 数值比较", query -> query.jsonLte(true, "profile", "$.score", 7));
        assertJsonConditionMatches(user, "jsonLte(Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonLte(PgJsonAppendUser::getProfile, "$.score", 7));
        assertJsonConditionMatches(user, "jsonLte(Boolean, Lambda, String, Object) 应按 JSON 数值比较", query -> query.jsonLte(true, PgJsonAppendUser::getProfile, "$.score", 7));

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextLike("profile", "$.conditionAction.action", "%重载%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextLike(String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextLike(true, "profile", "$.conditionAction.action", "%重载%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextLike(Boolean, String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextLike(PgJsonAppendUser::getProfile, "$.conditionAction.action", "%重载%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextLike(Lambda, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextLike(true, PgJsonAppendUser::getProfile, "$.conditionAction.action", "%重载%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextLike(Boolean, Lambda, String, String) 应命中");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextNotLike("profile", "$.conditionAction.action", "%不存在%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextNotLike(String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextNotLike(true, "profile", "$.conditionAction.action", "%不存在%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextNotLike(Boolean, String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextNotLike(PgJsonAppendUser::getProfile, "$.conditionAction.action", "%不存在%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextNotLike(Lambda, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonTextNotLike(true, PgJsonAppendUser::getProfile, "$.conditionAction.action", "%不存在%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonTextNotLike(Boolean, Lambda, String, String) 应命中");
    }

    @Test
    public void testPostgreSQLJsonSelectBuilderEveryMethodOverload() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 DAO JSON select 方法重载");

        String name = "PgJsonSelectOverloadUser-" + System.nanoTime();
        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName(name)
                .setProfile(profileWithAction("selectAction", "选择重载对象")));

        Assert.isTrue(jsonTextEquals("选择重载对象", dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonSelect("profile", "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonSelect(String, String, String) 应返回标量");
        Assert.isTrue(jsonTextEquals("选择重载对象", dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonSelect(true, "profile", "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonSelect(Boolean, String, String, String) 应返回标量");
        Assert.isTrue(jsonTextEquals("选择重载对象", dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonSelect(PgJsonAppendUser::getProfile, "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonSelect(Lambda, String, String) 应返回标量");
        Assert.isTrue(jsonTextEquals("选择重载对象", dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonSelect(true, PgJsonAppendUser::getProfile, "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonSelect(Boolean, Lambda, String, String) 应返回标量");

        Assert.isTrue("选择重载对象".equals(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonValueSelect("profile", "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonValueSelect(String, String, String) 应返回标量");
        Assert.isTrue("选择重载对象".equals(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonValueSelect(true, "profile", "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonValueSelect(Boolean, String, String, String) 应返回标量");
        Assert.isTrue("选择重载对象".equals(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonValueSelect(PgJsonAppendUser::getProfile, "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonValueSelect(Lambda, String, String) 应返回标量");
        Assert.isTrue("选择重载对象".equals(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonValueSelect(true, PgJsonAppendUser::getProfile, "$.selectAction.action", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "jsonValueSelect(Boolean, Lambda, String, String) 应返回标量");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonQuerySelect("profile", "$.selectAction", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains("选择重载对象"), "jsonQuerySelect(String, String, String) 应返回对象");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonQuerySelect(true, "profile", "$.selectAction", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains("选择重载对象"), "jsonQuerySelect(Boolean, String, String, String) 应返回对象");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonQuerySelect(PgJsonAppendUser::getProfile, "$.selectAction", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains("选择重载对象"), "jsonQuerySelect(Lambda, String, String) 应返回对象");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonQuerySelect(true, PgJsonAppendUser::getProfile, "$.selectAction", "selectedAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains("选择重载对象"), "jsonQuerySelect(Boolean, Lambda, String, String) 应返回对象");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonObjectSelect("jsonObject", "'name' value u.name")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonObjectSelect(String, String...) 应构造对象");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonObjectSelect(true, "jsonObject", "'name' value u.name")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonObjectSelect(Boolean, String, String...) 应构造对象");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonArraySelect("jsonArray", "u.name")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonArraySelect(String, String...) 应构造数组");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonArraySelect(true, "jsonArray", "u.name")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonArraySelect(Boolean, String, String...) 应构造数组");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonArrayAggSelect("u.name", "jsonArrayAgg")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonArrayAggSelect(String, String, String...) 应聚合数组");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonArrayAggSelect(true, "u.name", "jsonArrayAgg")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonArrayAggSelect(Boolean, String, String, String...) 应聚合数组");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonObjectAggSelect("u.name", "u.name", "jsonObjectAgg")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonObjectAggSelect(String, String, String, String...) 应聚合对象");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonObjectAggSelect(true, "u.name", "u.name", "jsonObjectAgg")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class).contains(name), "jsonObjectAggSelect(Boolean, String, String, String, String...) 应聚合对象");
    }

    @Test
    public void testPostgreSQLJsonBooleanGuardOverloadsAreNoop() {

        Assumptions.assumeTrue(isPostgreSQL(), "仅在 PostgreSQL 上验证 DAO JSON Boolean isAppend=false 重载");

        String name = "PgJsonBooleanGuardUser-" + System.nanoTime();
        String updatedName = name + "-updated";
        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName(name)
                .setActionLog(Collections.emptyList())
                .setProfile(profileWithAction("guardAction", "布尔保护对象")));

        dao.updateTo(PgJsonAppendUser.class)
                .jsonSet(false, "profile", "$.shouldNotSetAttr", actionLog("shouldNotSetAttr"))
                .jsonSet(false, PgJsonAppendUser::getProfile, "$.shouldNotSetLambda", actionLog("shouldNotSetLambda"))
                .jsonReplace(false, "profile", "$.guardAction", actionLog("shouldNotReplaceAttr"))
                .jsonReplace(false, PgJsonAppendUser::getProfile, "$.guardAction", actionLog("shouldNotReplaceLambda"))
                .jsonInsert(false, "profile", "$.shouldNotInsertAttr", actionLog("shouldNotInsertAttr"))
                .jsonInsert(false, PgJsonAppendUser::getProfile, "$.shouldNotInsertLambda", actionLog("shouldNotInsertLambda"))
                .jsonRemove(false, "profile", "$.guardAction")
                .jsonRemove(false, PgJsonAppendUser::getProfile, "$.guardAction")
                .jsonMergePatch(false, "profile", Map.of("shouldNotPatchAttr", actionLog("shouldNotPatchAttr")))
                .jsonMergePatch(false, PgJsonAppendUser::getProfile, Map.of("shouldNotPatchLambda", actionLog("shouldNotPatchLambda")))
                .jsonArrayAppend(false, "actionLog", actionLog("shouldNotAppendDefaultAttr"))
                .jsonArrayAppend(false, PgJsonAppendUser::getActionLog, actionLog("shouldNotAppendDefaultLambda"))
                .jsonArrayAppend(false, "actionLog", "$", actionLog("shouldNotAppendPathAttr"))
                .jsonArrayAppend(false, PgJsonAppendUser::getActionLog, "$", actionLog("shouldNotAppendPathLambda"))
                .jsonArrayInsert(false, "actionLog", "$[0]", actionLog("shouldNotInsertArrayAttr"))
                .jsonArrayInsert(false, PgJsonAppendUser::getActionLog, "$[0]", actionLog("shouldNotInsertArrayLambda"))
                .set("name", updatedName)
                .eq(PgJsonAppendUser::getId, user.getId())
                .update();

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonExists(false, "profile", "$.missingGuard")
                .jsonExists(false, PgJsonAppendUser::getProfile, "$.missingGuard")
                .jsonNotExists(false, "profile", "$.guardAction")
                .jsonNotExists(false, PgJsonAppendUser::getProfile, "$.guardAction")
                .jsonEq(false, "profile", "$.guardAction.action", "不应该命中")
                .jsonEq(false, PgJsonAppendUser::getProfile, "$.guardAction.action", "不应该命中")
                .jsonNotEqOrNull(false, "profile", "$.guardAction.action", "不应该命中")
                .jsonNotEqOrNull(false, PgJsonAppendUser::getProfile, "$.guardAction.action", "不应该命中")
                .jsonGt(false, "profile", "$.score", 0)
                .jsonGt(false, PgJsonAppendUser::getProfile, "$.score", 0)
                .jsonGte(false, "profile", "$.score", 0)
                .jsonGte(false, PgJsonAppendUser::getProfile, "$.score", 0)
                .jsonLt(false, "profile", "$.score", 0)
                .jsonLt(false, PgJsonAppendUser::getProfile, "$.score", 0)
                .jsonLte(false, "profile", "$.score", 0)
                .jsonLte(false, PgJsonAppendUser::getProfile, "$.score", 0)
                .jsonContains(false, "profile", "$.roles[*]", 7)
                .jsonContains(false, PgJsonAppendUser::getProfile, "$.roles[*]", 7)
                .jsonNotContains(false, "profile", "$.roles[*]", 7)
                .jsonNotContains(false, PgJsonAppendUser::getProfile, "$.roles[*]", 7)
                .jsonTextLike(false, "profile", "$.guardAction.action", "%不应该命中%")
                .jsonTextLike(false, PgJsonAppendUser::getProfile, "$.guardAction.action", "%不应该命中%")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "JSON 条件 Boolean isAppend=false 重载不应该追加条件");

        String selectedName = dao.selectFrom(PgJsonAppendUser.class, "u")
                .select("u.name")
                .jsonSelect(false, "profile", "$.guardAction.action", "shouldNotSelectAttr")
                .jsonSelect(false, PgJsonAppendUser::getProfile, "$.guardAction.action", "shouldNotSelectLambda")
                .jsonValueSelect(false, "profile", "$.guardAction.action", "shouldNotValueAttr")
                .jsonValueSelect(false, PgJsonAppendUser::getProfile, "$.guardAction.action", "shouldNotValueLambda")
                .jsonQuerySelect(false, "profile", "$.guardAction", "shouldNotQueryAttr")
                .jsonQuerySelect(false, PgJsonAppendUser::getProfile, "$.guardAction", "shouldNotQueryLambda")
                .jsonObjectSelect(false, "shouldNotObject", "'name' value u.name")
                .jsonArraySelect(false, "shouldNotArray", "u.name")
                .jsonArrayAggSelect(false, "u.name", "shouldNotArrayAgg")
                .jsonObjectAggSelect(false, "u.name", "u.name", "shouldNotObjectAgg")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class);

        Assert.isTrue(updatedName.equals(selectedName), "JSON select Boolean isAppend=false 重载不应该追加选择表达式");
        Assert.isTrue(jsonTextEquals("布尔保护对象", dao.selectFrom(PgJsonAppendUser.class, "u")
                .jsonSelect(PgJsonAppendUser::getProfile, "$.guardAction.action", "guardAction")
                .eq(PgJsonAppendUser::getId, user.getId())
                .findOne(String.class)), "JSON 更新 Boolean isAppend=false 重载不应该改变 profile");
        assertActionLogAllObjects(user.getId(), 0);
    }

    private void assertJsonConditionMatches(PgJsonAppendUser user, String message,
                                            Consumer<SelectDao<PgJsonAppendUser>> conditionAppender) {
        SelectDao<PgJsonAppendUser> query = dao.selectFrom(PgJsonAppendUser.class);
        conditionAppender.accept(query);
        Assert.isTrue(query.eq(PgJsonAppendUser::getId, user.getId()).count() == 1, message);
    }

    private boolean jsonTextEquals(String expected, String actual) {
        if (Objects.equals(expected, actual)) {
            return true;
        }
        return actual != null
                && actual.length() >= 2
                && actual.startsWith("\"")
                && actual.endsWith("\"")
                && Objects.equals(expected, actual.substring(1, actual.length() - 1));
    }

    private TreeSet<String> jsonMethodSignatures(Class<?> type) {
        TreeSet<String> signatures = new TreeSet<>();
        Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().startsWith("json"))
                .map(this::jsonMethodSignature)
                .forEach(signatures::add);
        return signatures;
    }

    private String jsonMethodSignature(java.lang.reflect.Method method) {
        return method.getName() + "("
                + Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .reduce((left, right) -> left + "," + right)
                .orElse("")
                + ")";
    }

    private PgJsonAppendUser.ActionLog actionLog(String action) {
        return new PgJsonAppendUser.ActionLog()
                .setOccurTime("2026-05-23 13:00:00")
                .setOperator("codex-json-overload")
                .setAction(action);
    }

    private Map<String, Object> profileWithAction(String key, String action) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put(key, Map.of("action", action));
        return profile;
    }

    private void assertActionLogElement(Long id, int index, int length, String action, String typeMessage) {
        entityManager.clear();

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        select jsonb_typeof(action_log),
                               jsonb_typeof(action_log -> ?1),
                               jsonb_array_length(action_log),
                               action_log -> ?1 ->> 'action'
                        from pg_json_append_user
                        where id = ?2
                        """)
                .setParameter(1, index)
                .setParameter(2, id)
                .getSingleResult();

        Assert.isTrue("array".equals(row[0]), "action_log 应该仍然是 JSON 数组: " + Arrays.toString(row));
        Assert.isTrue("object".equals(row[1]), typeMessage + ": " + Arrays.toString(row));
        Assert.isTrue(((Number) row[2]).intValue() == length, "action_log 数组长度不正确: " + Arrays.toString(row));
        Assert.isTrue(action.equals(row[3]), "action_log 对象字段内容不正确: " + Arrays.toString(row));
    }

    private void assertActionLogAllObjects(Long id, int length) {
        entityManager.clear();

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        select jsonb_typeof(action_log),
                               jsonb_array_length(action_log),
                               coalesce((
                                   select bool_and(jsonb_typeof(elem) = 'object')
                                   from jsonb_array_elements(action_log) elem
                               ), true)
                        from pg_json_append_user
                        where id = ?1
                        """)
                .setParameter(1, id)
                .getSingleResult();

        Assert.isTrue("array".equals(row[0]), "action_log 应该仍然是 JSON 数组: " + Arrays.toString(row));
        Assert.isTrue(((Number) row[1]).intValue() == length, "action_log 数组长度不正确: " + Arrays.toString(row));
        Assert.isTrue(Boolean.TRUE.equals(row[2]), "action_log 所有元素都应该是 JSON object: " + Arrays.toString(row));
    }

    private void assertProfileKeysRemoved(Long id, String... keys) {
        entityManager.clear();

        for (String key : keys) {
            Object exists = entityManager.createNativeQuery("""
                            select jsonb_exists(profile, ?1)
                            from pg_json_append_user
                            where id = ?2
                            """)
                    .setParameter(1, key)
                    .setParameter(2, id)
                    .getSingleResult();

            Assert.isTrue(Boolean.FALSE.equals(exists), key + " 应该已被 jsonRemove 删除");
        }
    }

    private void assertProfileJsonObject(Long id, String key, String action) {
        entityManager.clear();

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        select jsonb_typeof(profile -> ?1),
                               profile -> ?1 ->> 'action'
                        from pg_json_append_user
                        where id = ?2
                        """)
                .setParameter(1, key)
                .setParameter(2, id)
                .getSingleResult();

        Assert.isTrue("object".equals(row[0]), key + " 应该是 JSON object，不应该是 string: " + Arrays.toString(row));
        Assert.isTrue(action.equals(row[1]), key + " 的 action 字段内容不正确: " + Arrays.toString(row));
    }

    private boolean isPostgreSQL() {
        return entityManager.unwrap(Session.class).doReturningWork(connection -> {
            String databaseName = connection.getMetaData().getDatabaseProductName();
            return databaseName != null && databaseName.toLowerCase().contains("postgresql");
        });
    }
    @Test
    public void testJsonPathSelectAndWhereStatements() {

        String statement = dao.selectFrom(User.class, "u")
                .appendByQueryObj(new JsonPathSelectQO()
                        .setRole("R_ADMIN")
                        .setHasLog(Boolean.TRUE))
                .genFinalStatement();

        Assert.isTrue(statement.contains("json_exists(cast(u.roleList as String), '$[*]?(@ == $value)' passing :? as value)"),
                "Contains 注解的 wildcard JSON 路径应生成数组元素精确匹配条件");
        Assert.isTrue(statement.contains("json_exists(") && statement.contains("'$[0].logText'"), "Exists 注解应生成 json_exists");
        Assert.isTrue(statement.contains("COALESCE(cast(json_query(") && statement.contains("json_value("), "Select 注解应同时兼容对象/数组和标量 JSON 路径");
    }

    @Test
    public void testJsonPathConditionQueryShouldHitDatabase() {

        dao.deleteFrom(Task.class).disableSafeMode().delete();
        dao.deleteFrom(User.class).disableSafeMode().delete();
        dao.deleteFrom(Group.class).disableSafeMode().delete();

        prepareJsonPathUser("R_JSON_PATH_MATCH_" + System.nanoTime(), "json-log-select-" + System.currentTimeMillis());

        long count = dao.selectFrom(User.class, "u")
                .appendByQueryObj(new JsonPathExistsConditionQO().setHasLog(Boolean.TRUE))
                .count();

        Assert.isTrue(count == 1, "jsonPath 条件查询应命中真实数据库记录");
    }

    @Test
    public void testJsonArrayContainsShouldUseUnquotedPassingAlias() {

        dao.deleteFrom(Task.class).disableSafeMode().delete();
        dao.deleteFrom(User.class).disableSafeMode().delete();
        dao.deleteFrom(Group.class).disableSafeMode().delete();

        String uniqueRole = "R_JSON_ARRAY_MATCH_" + System.nanoTime();
        User user = prepareJsonPathUser(uniqueRole, null);

        String statement = dao.selectFrom(User.class, "u")
                .jsonContains("roleList", "$[*]", uniqueRole)
                .jsonNotContains("roleList", "$[*]", "R_JSON_ARRAY_MISSING")
                .eq(User::getId, user.getId())
                .genFinalStatement();

        Assert.isTrue(statement.contains("passing :? as value"), "JSON path passing 别名必须是不带双引号的标识符：" + statement);
        Assert.isTrue(!statement.contains("as \"value\""), "JSON path passing 别名不应包含双引号：" + statement);

        long count = dao.selectFrom(User.class, "u")
                .jsonContains("roleList", "$[*]", uniqueRole)
                .jsonNotContains("roleList", "$[*]", "R_JSON_ARRAY_MISSING")
                .eq(User::getId, user.getId())
                .count();

        Assert.isTrue(count == 1, "jsonContains/jsonNotContains 应在 JSON 数组元素级完成精确匹配");
    }

    @Test
    public void testJsonPathUpdateStatement() {

        String statement = dao.updateTo(User.class, "u")
                .appendByQueryObj(new JsonPathUpdateDTO()
                        .setFirstLogText("changed")
                        .setRole("R_ADMIN"))
                .genFinalStatement();

        Assert.isTrue(statement.contains("u.logs = json_set(u.logs, '$[0].logText'"), "Update 注解应生成 json_set 更新语句");
        Assert.isTrue(statement.contains("json_exists(cast(u.roleList as String), '$[*]?(@ == $value)' passing :? as value)"),
                "Update 场景的 Contains 注解应支持数组元素精确匹配");
    }

    @Test
    public void testJsonPathRejectWildcardForStatAnnotations() {

        boolean threw = false;

        try {
            dao.selectFrom(User.class, "u")
                    .appendByQueryObj(new JsonPathWildcardStatQO())
                    .genFinalStatement();
        } catch (StatementBuildException e) {
            threw = true;
        }

        Assert.isTrue(threw, "统计注解使用 wildcard jsonPath 时应抛出异常");
    }

    @Test
    public void testJsonPathRejectWildcardForUpdateAnnotations() {

        boolean threw = false;

        try {
            dao.updateTo(User.class, "u")
                    .appendByQueryObj(new JsonPathWildcardUpdateDTO().setFirstLogText("changed"))
                    .genFinalStatement();
        } catch (StatementBuildException e) {
            threw = true;
        }

        Assert.isTrue(threw, "Update 注解使用 wildcard jsonPath 时应抛出异常");
    }

}
