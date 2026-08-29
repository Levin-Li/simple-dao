package com.levin.commons.dao;

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
 * Created by echo on 2015/11/17.
 */

public class DaoExamplesTest extends DaoExamplesTestSupport {
    @Test
    public void testSPEL() {

        Object v = ExprUtils.evalSpEL(new SimpleUserQO().setQueryStatus(true), "#ABC", Collections.emptyList());

        Assert.isNull(v, "true");

        Map<String, ? extends Object> abc = MapUtils.put("ABC", (Object) this.hashCode()).build();

        v = ExprUtils.evalSpEL(new SimpleUserQO().setQueryStatus(true), "#ABC", Arrays.asList(abc));

        Assert.isTrue(v.equals(this.hashCode()), "true");

    }

    @SneakyThrows
    @Test
    public void testUniqueTestObj() {

        try {
            dao.create(new UniqueTestObj());
//            throw new Throwable("未能正确抛出创建异常");
        } catch (Exception e) {

        }

        String uuid = UUID.randomUUID().toString();

        dao.create(new UniqueTestObj()
                .setUuid1(uuid)
                .setUuid2(uuid)
        );

        try {
            dao.create(new UniqueTestObj()
                    .setUuid1(uuid)
                    .setUuid2(uuid)
            );
            throw new Throwable("未能正确抛出创建异常");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDtoInject() {

        Task task = dao.create(new CreateTask()
                .setName("测试任务")
                .setArea("福州")
                .setState("新状态")
                .setActions(Arrays.asList(12, 2, 3, 4, 55, 99))
        );

        Assert.hasText(task.getActions(), "字段转换错误1");

        TaskInfo one = dao.selectFrom(Task.class)
                .eq(E_Task.id, task.getId())
                .findOne(TaskInfo.class);

        Assert.notEmpty(one.getActions(), "字段转换错误2");

        one = dao.findOneByQueryObj(new QueryTaskReq());


        int i = dao.updateByQueryObj(one);

        System.out.println(one);

    }

    @Test
    public void testIncrementUpdate() {

        User user = dao.findOneByQueryObj(User.class);

        Assert.notNull(user, "user is null");

        IncrUpdateUserDTO incrUpdateUserDTO = new IncrUpdateUserDTO();

        //
        incrUpdateUserDTO.setId(user.getId());

        boolean ok = dao.singleUpdateByQueryObj(incrUpdateUserDTO);

        Assert.isTrue(ok);


        try {
            incrUpdateUserDTO.setCreateTime(LocalDateTime.now());

            dao.singleUpdateByQueryObj(incrUpdateUserDTO);

            ok = true;

            //如果没有抛出异常
        } catch (Exception e) {
            ok = false;
            //发生异常才是正确逻辑
        }

        Assert.isTrue(!ok, "错误的更新，但并没有抛出异常");


        incrUpdateUserDTO.setCreateTime(null);

        UpdateDao<Object> objectUpdateDao = dao.forUpdate(incrUpdateUserDTO);


        String statement = objectUpdateDao.genFinalStatement();

        System.out.println(statement);

        Assert.isTrue(statement.contains(" = CONCAT(")
                //  && statement.contains(" IS NULL")
                && statement.contains(" + ")
                && statement.contains("''")
                && statement.contains("0")
        );


    }

    @Test
    public void testSimpleUserQO() {

//        Assert.isTrue(Boolean.TRUE.equals(v),"");

        List<SimpleUserQO.QResult> list = dao.findByQueryObj(new SimpleUserQO());

        Assert.isTrue(list.size() > 0, "空");
        Assert.notNull(list.get(0).getName(), "空");
        Assert.notNull(list.get(0).getScore(), "空");

        Object byQueryObj = dao.findByQueryObj(SimpleUserQO.QResult2.class, new SimpleUserQO());

        byQueryObj = dao.findByQueryObj(new SimpleUserQO().setQueryStatus(true), SimpleUserQO.QResult.class);


        byQueryObj = dao.findByQueryObj(SimpleUserQO.QResult.class, new SimpleUserQO().setQueryStatus(false));

        System.out.println(byQueryObj);


    }

    @Test
    public void testFieldConvert() {


        User user = dao.find(User.class, 1L);


        UserInfo userInfo = dao.findOneByQueryObj(UserInfo.class, new UserInfo());


        System.out.println(userInfo);
    }

    @Test
    public void testJoinFetch() {


        Group one = dao.selectFrom(Group.class).gt(E_Group.id, 5L).findOne();

        GroupInfo queryDto = new GroupInfo().setId("" + one.getId());

        Object byQueryObj = dao.findByQueryObj(GroupInfo.class, queryDto);

        long count = dao.forSelect(queryDto).joinFetch(E_Group.children).count();

        Object ss = dao.countByQueryObj(queryDto);

        Assert.notNull(byQueryObj);

    }

    public static String anToStr(Annotation an) {
        Class<? extends Annotation> annotationType = an.annotationType();
        String prefix = "@" + annotationType.getPackage().getName();
        return "@" + an.toString().substring(prefix.length() + 1);
    }

    @Test
    public void testFromStatementDTO() {

//        List<FromStatementDTO> byQueryObj = dao.findByQueryObj(FromStatementDTO.class, new FromStatementDTO());
//        assert byQueryObj.size() > 0;

        Entity an = User.class.getAnnotation(Entity.class);


        final String prefix = anToStr(an);


        List<TableJoin3> byQueryObj1 = dao.findByQueryObj(TableJoin3.class, new TableJoin3());


        // System.out.println(byQueryObj1);

        assert byQueryObj1.size() > 0;

    }


    @Test
    public void testTableJoin4() {

        List<User> byQueryObj1 = dao.findByQueryObj(new TableJoin4());

        // System.out.println(byQueryObj1);

        assert byQueryObj1.size() > 0;

        assert byQueryObj1.get(0) instanceof User;

    }

    @Test
    public void testGroupDTO() {

        List<GroupInfo> list = dao.findByQueryObj(new GroupDTO());

        System.out.println(list);

    }

    @Test
    public void testCustomSelectDTO() {

        List<CustomSelectDTO> list = dao.findByQueryObj(new CustomSelectDTO());

        System.out.println(list);

        CustomSelectDTO dto = list.get(0);

        Assert.hasText(dto.getParentName(), "父节点名称为空");
        Assert.hasText(dto.getName(), "名称为空");
        Assert.hasText(dto.getCategory(), "类别为空");
        Assert.notNull(dto.getScore(), "分数为空");

    }

    @Test
    public void testJpaDtoProjectionDefaultAliasDTO() {

        List<JpaDtoProjectionDefaultAliasDTO> list = dao.findByQueryObj(JpaDtoProjectionDefaultAliasDTO.class,
                new JpaDtoProjectionDefaultAliasDTO());

        Assert.notEmpty(list, "默认别名 DTO 查询结果为空");

        JpaDtoProjectionDefaultAliasDTO dto = list.get(0);

        Assert.hasText(dto.getName(), "默认别名 DTO 名称映射失败");
        Assert.hasText(dto.getCategory(), "默认别名 DTO 类别映射失败");
        Assert.notNull(dto.getScore(), "默认别名 DTO 分数映射失败");
    }

    @Test
    public void testJpaDtoProjectionNoAliasDTO() {

        List<JpaDtoProjectionNoAliasDTO> list = dao.findByQueryObj(JpaDtoProjectionNoAliasDTO.class,
                new JpaDtoProjectionNoAliasDTO());

        Assert.notEmpty(list, "无别名 DTO 查询结果为空");

        JpaDtoProjectionNoAliasDTO dto = list.get(0);

        Assert.hasText(dto.getName(), "无别名 DTO 名称映射失败");
        Assert.hasText(dto.getCategory(), "无别名 DTO 类别映射失败");
        Assert.notNull(dto.getScore(), "无别名 DTO 分数映射失败");
    }

    @Test
    public void testJpaDtoProjectionExpressionAliasDTO() {

        List<JpaDtoProjectionExpressionAliasDTO> list = dao.findByQueryObj(JpaDtoProjectionExpressionAliasDTO.class,
                new JpaDtoProjectionExpressionAliasDTO());

        Assert.notEmpty(list, "表达式 DTO 查询结果为空");

        JpaDtoProjectionExpressionAliasDTO dto = list.get(0);

        Assert.hasText(dto.getName(), "表达式 DTO 名称映射失败");
        Assert.notNull(dto.getScorePlusOne(), "表达式 DTO 分数映射失败");
        Assert.isTrue(dto.getScorePlusOne() > 0, "表达式 DTO 分数结果异常");
    }

    @Test
    public void testJpaDtoProjectionDoesNotBreakDynamicSelectDTO() {

        CustomSelectDTO query = new CustomSelectDTO();
        query.setColumns(new String[]{"name", "category"});

        List<CustomSelectDTO> list = dao.findByQueryObj(query);

        Assert.notEmpty(list, "动态字段 DTO 查询结果为空");

        CustomSelectDTO dto = list.get(0);

        Assert.hasText(dto.getName(), "动态字段 DTO 名称映射失败");
        Assert.hasText(dto.getCategory(), "动态字段 DTO 类别映射失败");
        Assert.isNull(dto.getScore(), "动态字段未选择 score 时不应被错误映射");
    }

    @Test
    public void testJpaDtoProjectionCountAliasTotalsDTO() {

        JpaDtoProjectionTotalsDTO.Result result = dao.findOneByQueryObj(new JpaDtoProjectionTotalsDTO());

        Assert.notNull(result, "统计 DTO 查询结果为空");
        Assert.notNull(result.getTotals(), "统计 DTO totals 映射失败");
        Assert.isTrue(result.getTotals() > 0, "统计 DTO totals 结果异常");
    }

    @Test
    public void testNumericAliasMapResultKeepsNumericKey() {

        List<Map> list = dao.selectFrom("jpa_dao_test_Group", "g")
                .selectByStatement(true, "g.name AS \"0\"")
                .limit(0, 1)
                .find(Map.class);

        Assert.notEmpty(list, "数字别名 Map 查询结果为空");

        Map row = list.get(0);
        Object aliasKey = row.keySet().iterator().next();
        Object numericAliasValue = row.get(aliasKey);

        Assert.isTrue(!row.containsKey("name") && String.valueOf(aliasKey).contains("0"),
                "目标类型是 Map 时，数字别名应作为 Map key 保留，不应被映射成 DTO 字段：" + row.keySet());
        Assert.hasText(String.valueOf(numericAliasValue), "数字别名 Map 值为空");
    }

    @Test
    public void testNativeTableJoinDTO() {

        List<NativeTableJoinDTO> byQueryObj = dao.findByQueryObj(NativeTableJoinDTO.class, new NativeTableJoinDTO());

        System.out.println(byQueryObj);

    }

    @Test
    public void testCaseQL() {

        List<CaseTestDto> byQueryObj = dao.findByQueryObj(CaseTestDto.class, new CaseTestDto());

        byQueryObj = dao.findByQueryObj(CaseTestDto.class, new CaseTestDto().setScoreLevel(2).setQueryState(true));

        System.out.println(byQueryObj);

        String ql = new Case()
                .column("status")
                .when("'A'", "0")
                .when("'B'", "1")
                .elseExpr("2")
                .toString();

    }

    @Test
    public void testInjectForUpdate() {

        TestRole role = dao.create(new CreateTestRoleReq()
                .setCode("R_SA")
                .setName("TestRole1")
                .setAssignedOrgIdList(Arrays.asList("1", "2", "3"))
                .setOrgDataScope(TestRole.OrgDataScope.Assigned)
                .setPermissionList(Arrays.asList("P1", "P2", "p3"))
        );

        TestRoleInfo info = dao.findUnique(new QueryTestRoleReq().setId(role.getId()));


        int i = dao.updateByQueryObj(new UpdateTestRoleReq().setId(info.getId())
                .setPermissionList(Arrays.asList("P4", "P5", "P6")));

        Assert.isTrue(i == 1, "更新失败");

        info = dao.findUnique(new QueryTestRoleReq().setId(role.getId()));

        Assert.isTrue(info.getPermissionList().contains("P5"), "dddd");

    }

    @Test
    public void testJoinAndStat() {

        List<Map> g = dao.selectFrom(Group.class, "g")
                .join("left join " + User.class.getName() + " u on g.id = u.group.id")
                .join("left join " + Task.class.getName() + " t on u.id = t.user.id")
                .count("1", "cnt")
                .avg("t.score + ${v}", "ts", MapUtils.put("v", (Object) 5L).build())
                .avg("u.score", "us")
                .avg("g.score", "gs")
                .sum("t.score", "ts2")
//                .where("u.name = :?","sss")
                .groupByAndSelect(AbstractNamedEntityObject::getName, "groupName")
//                .groupBy("g.name")

                //Select (Count( 1 )) AS cnt , (Avg( t.score + 5 )) AS ts , (Avg( u.score )) AS us , (Avg( g.score )) AS gs , (Sum( t.score )) AS ts2 , (g.name) AS groupName  From com.levin.commons.dao.domain.Group g left join com.levin.commons.dao.domain.User u on g.id = u.group.id  left join com.levin.commons.dao.domain.Task t on u.id = t.user.id    Group By g.name Order By g.ts2

                //@todo 待修复bug
                //支持别名处理，如果是排序别名，不能加点
                .orderByStatement(OrderBy.Type.Desc, "ts2")
                .find(Map.class);

        Assert.isTrue(g.size() > 0);

        Assert.isTrue(g.get(0).containsKey("cnt"));
        Assert.isTrue(g.get(0).containsKey("groupName"));
    }

    @Test
    public void testProxyBean() {

        String emf = userApi.getEMF();

        System.out.println(emf);

        emf = userApi2.getEMF();

        System.out.println(emf);


        emf = userApi3.getEMF();

        System.out.println(emf);

    }


    @Test
    public void testAnno() {

//
        List<User> byQueryObj = dao.findByQueryObj(User.class, new AnnoTest());
//
////        System.out.println(byQueryObj);
//
//        Object aa = jpaDao.find("  From com.levin.commons.dao.domain.User e   Where e.id =   ?1  AND e.state =   ?2   Order By  e.id DESC", 1, "ss");
//
//        System.out.println(aa);

//        List<Object> objects = jpaDao.find(" select count(*) from  Group g");

        //       System.out.println(byQueryObj);


    }

    @Test
    public void testUserService() {


        List<UserInfo> userInfoList = userService.findUserInfo(new QueryUserEvt().setState("正常"));

        Assert.isTrue(userInfoList.size() > 0);

        UserInfo userInfo2 = dao.findOneByQueryObj(UserInfo.class, new QueryUserEvt().setId(userInfoList.get(0).getId()));


        Assert.isTrue(userService.addUserScore(new UserUpdateEvt().setId(userInfo2.getId()).setAddScore(5)));

        UserInfo userInfo3 = dao.findOneByQueryObj(UserInfo.class, new QueryUserEvt().setId(userInfo2.getId()));

        Assert.isTrue(userInfo3.getScore() == userInfo2.getScore() + 5);

    }


    @Test
    @Transactional
    public void testTransactional2() throws InterruptedException {

        EntityOption entityOption = TestEntity.class.getAnnotation(EntityOption.class);

        TestEntity entity = (TestEntity) dao.create(new TestEntity()
                .setScore(random.nextInt(750))
                .setName("test" + random.nextInt(750))
                .setRemark("system-" + random.nextInt(750))
                .setOrderCode(random.nextInt(750))
        );

        System.out.println("1 ------------------------------");
        Thread.sleep(1000);

        List<Object> objectList = dao.selectFrom(TestEntity.class, "e")
                .gt(E_TestEntity.id, 20)
                .find();

        System.out.println("2 ------------------------------");
        Thread.sleep(1000);

        int orderCode = -1;

        dao.updateTo(TestEntity.class)
                .set(E_TestEntity.orderCode, orderCode)
                .eq(E_TestEntity.id, 1)
                .update();

        System.out.println("3 ------------------------------");
        Thread.sleep(1000);

        orderCode = -1234567;

        dao.updateTo(TestEntity.class)
                .set(E_TestEntity.orderCode, orderCode)
                .eq(E_TestEntity.id, entity.getId())
                .update();

        //   boolean disableDel = entityOption != null &&  Stream.of(entityOption.disableActions()).filter(a -> EntityOption.Action.Delete.equals(a)).count() > 0;

        Assert.isTrue(dao.find(TestEntity.class, entity.getId()).getOrderCode() == orderCode, "变更没有生效");

        orderCode = -67890;

        dao.updateTo(TestEntity.class)
                .set(E_TestEntity.orderCode, orderCode)
                .gt(E_TestEntity.id, entity.<Long>getId() - 50)
                .update();

        System.out.println("4------------------------------");
        Thread.sleep(1000);

        Assert.isTrue(dao.find(TestEntity.class, entity.getId()).getOrderCode() == orderCode, "变更没有生效");


        System.out.println("5------------------------------");
        Thread.sleep(1000);

        objectList = dao.selectFrom(TestEntity.class, "e")
                .find();

        System.out.println("6------------------------------");
        // Thread.sleep(1000);

        Assert.isTrue(objectList.contains(entity), "");

    }

    @Test
    @Transactional
    public void testTransactional() {


        TestEntity entity = (TestEntity) new TestEntity()
                .setScore(15)
                .setName("test" + 11)
                .setRemark("system-" + 11)
                .setOrderCode(11);

        entity = (TestEntity) dao.create(entity);

        long id = entity.getId();

        entity = dao.find(TestEntity.class, id);
        Assert.isTrue(entity != null && entity.getId().equals(id), "1. 刚插入的数据无法加载 " + id);


        entity = dao.selectFrom(TestEntity.class).eq(E_TestEntity.id, id).findOne();
        Assert.isTrue(entity != null && entity.getId().equals(id), "2. 刚插入的数据无法加载 " + id);


        entity = (TestEntity) new TestEntity()
                .setScore(15)
                .setName("test" + 11)
                .setRemark("system-" + 11)
                .setOrderCode(11);

        entity = (TestEntity) dao.create(entity);

        id = entity.getId();

        entity = dao.selectFrom(TestEntity.class).eq(E_TestEntity.id, id).findOne();
        Assert.isTrue(entity != null && entity.getId().equals(id), "3. 刚插入的数据无法加载 " + id);


        entity = dao.find(TestEntity.class, id);
        Assert.isTrue(entity != null && entity.getId().equals(id), "4. 刚插入的数据无法加载 " + id);


        String newName = "" + id + "" + entity.hashCode();
        dao.updateTo(TestEntity.class)
                .set(E_TestEntity.name, newName)
                .eq(E_TestEntity.id, id)
                .update();


        entity = dao.find(TestEntity.class, id);
        Assert.isTrue(entity != null && entity.getName().equals(newName), "5. 刚更新的数据无法获取 " + id);

        newName = System.currentTimeMillis() + "_" + id + "" + entity.hashCode();
        dao.updateTo(TestEntity.class)
                .set(E_TestEntity.name, newName)
                .eq(E_TestEntity.id, id)
                .update();

        List<Object> objects = dao.selectFrom(TestEntity.class).find();

        entity = dao.selectFrom(TestEntity.class).eq(E_TestEntity.id, id).findOne();
        Assert.isTrue(entity != null && entity.getName().equals(newName), "6. 刚更新的数据无法获取 " + id);

        dao.delete(entity);

        objects = dao.selectFrom(TestEntity.class).find();

        Assert.isTrue(!objects.contains(entity), "7. 刚删除的数据还能获取 " + id);

        entity = dao.selectFrom(TestEntity.class).eq(E_TestEntity.id, id).findOne();
        Assert.isTrue(entity == null, "8. 刚删除的数据还能获取 " + id);


        entity = dao.selectFrom(TestEntity.class).findOne();

        entity.setCategory(newName);

        Object save = dao.save(entity);

        entity = dao.selectFrom(TestEntity.class).eq(E_TestEntity.id, entity.getId()).findOne();
        Assert.isTrue(entity.getCategory().equals(newName), "9. 刚save数据更新失败");


    }

    @Test
    public void testExists() {

        long cnt = dao.selectFrom(User.class)
                .setContext(MapUtils.put("tab", (Object) User.class.getName()).build())
                .exists("select count(1) from ${tab} ")
                .count();

        Assert.isTrue(cnt > 0);
    }

    @Test
    public void testGroupDao() {

        System.out.println(userDao);
        System.out.println(groupDao.hashCode());
        System.out.println(group2Dao.toString());

        Group group = groupDao.findOne(null, "Group", null, null);

        System.out.println(group);

        List<Group> groups = groupDao.find(null, "Group", null, null);

        System.out.println(groups);

        try {
            groupDao.noAnnoMethod(1L, "无注解方法");
            throw new RuntimeException("无注解方法没有抛出异常");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            groupDao.findOneAndRepeatGetResult(null, "Group", null, new PagingQueryReq(1, 10));
            throw new RuntimeException("重复获取结果方法没有抛出异常");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testObjectArrayType() {

        assert new String[0] instanceof Object[];

        assert new Integer[0] instanceof Object[];
        assert new User[0] instanceof Object[];

        Object obj = new int[0];

        assert !(obj instanceof Object[]);

    }


    @Test
    public void testTestEntityStatDto() {

        List<TestEntityStatDto> dtoList = dao.findByQueryObj(TestEntityStatDto.class, new TestEntityStatDto());

        //  Assert.isTrue(dtoList.size() > 0, "TestEntity统计结果错误");

    }

    @Test
    public void testOrderBy() {

        String sql = dao.selectFrom(User.class)
                .sum(User::getScore, "sumScore")
                .avg(User::getScore, "avgScore")
                .max(User::getScore, "maxScore")
                .min(User::getScore, "minScore")
                .appendByQueryObj(new OrderByExam())
                .genFinalStatement();

        Assert.isTrue(sql.contains(" sumScore "));

        Assert.isTrue(sql.contains(" score "));
        Assert.isTrue(sql.contains(E_User.createTime));
        Assert.isTrue(!sql.contains(E_User.area));

        System.out.println(sql);
    }


    @Test
    public void testNullOrEq() {

        LocalDateTime  paramValue = LocalDateTime.now();

        long cnt = dao.updateTo(User.class).set(E_User.lastUpdateTime, paramValue)
                .disableSafeMode()
                .update();

        long nullCnt = dao.updateTo(User.class).set(E_User.lastUpdateTime, null)
                .gt(E_User.id, 20)
                .update();


        long tn = dao.selectFrom(User.class)
                .isNullOrEq(E_User.lastUpdateTime, paramValue)
                .count();

        Assert.isTrue(tn == cnt);

    }

    @Test
    public void testUserDao() {

        PagingQueryReq paging = new PagingQueryReq();

        paging.setPageSize(10);

        List<User> users = userDao.find(null, "User", 5, paging);

        cn.hutool.core.lang.Assert.isTrue(!users.get(0).getRoleList().isEmpty());

        //    paging.setPageSize(1);

        User user = userDao.findOne(null, null, null, paging);

        int update = userDao.delete(null, "SSSS");

        Assert.isTrue(update == 0, "更新条数错误");

        update = userDao.update(user.getId(), "User-12");

        Assert.isTrue(update == 1, "更新条数错误");
    }

    @Test
    public void testNotOp() throws Exception {

        dao.selectFrom(User.class).not().eq(E_User.area, "test").end().find();

        try {
            dao.selectFrom(User.class).not().eq(E_User.area, "test").gt(E_User.score, 50).end().find();

            throw new Exception("用例应该抛出异常");

        } catch (RuntimeException e) {
            System.out.println("" + e.getMessage());
        }

        String statement = dao.selectFrom(User.class)
                .not()

                .or()

                .isNotNull(E_User.createTime)
                .eq(E_User.area, "test")

                //包含
                .and()
                .gt(E_User.score, 5)
                .isNotNull(E_User.score)
                .end()

                //或结束
                .end()

                .end()
                .genFinalStatement();

        // 预期生成的语句
        // From com.levin.commons.dao.domain.User     Where NOT((createTime IS NOT NULL OR area =  :? OR (score >  :? AND score IS NOT NULL)))

        Assert.isTrue(statement.contains("NOT((createTime IS NOT NULL OR area =  :? OR (score >  :? AND score IS NOT NULL)))"));

    }

    @Test
    public void testGetIdAttr() {

        /// JpaDaoImpl jpaDao = new JpaDaoImpl();

        String attrName = dao.getEntityIdAttrName(User.class);

        Assert.isTrue(E_User.id.equals(attrName));

        Long id = 1234567L;

        Object entityId = dao.getEntityId(new Group(id, "test"));

        Assert.isTrue(entityId.equals(id));

    }


    @Test
    //@Transactional
    public void testJoinFetch2() {

        List<UserJoinFetchDTO> byQueryObj = dao.findByQueryObj(UserJoinFetchDTO.class, new UserJoinFetchDTO());

        Object user = byQueryObj.get(0);

        System.out.println(byQueryObj);
    }

    @Test
    //@Transactional
    public void testGroupJoinFetch() {


        List<GroupJoinFetchDTO> byQueryObj = dao.findByQueryObj(GroupJoinFetchDTO.class, new GroupJoinFetchDTO());

        Object user = byQueryObj.get(0);

        List byQueryObjs = dao.findByQueryObj(null, new GroupJoinFetchDTO());

        System.out.println(byQueryObj);
    }

    @Test
    //@Transactional
    public void testSelectGroupDto() {

        List<GroupSelectDTO> byQueryObj = dao.findByQueryObj(GroupSelectDTO.class, new GroupSelectDTO());

        Object user = byQueryObj.get(0);

        System.out.println(byQueryObj);
    }

    //@Test
    public void testNativeQuery() {


        Group entity = new Group();
        entity.setName("adfsdafas");

        dao.save(entity);

        List r = dao.find(true, Group.class, 1, 100
                , "select * from jpa_dao_test_Group where 1 = ? and 2 = ? and '3'=:name"
                , 1, 2, MapUtils.asMap("name", "3"));


        List<Group> groups = dao
                .selectFrom(Group.class, "t")
                //   .select("*")
                //  .select("id")
                //    .appendWhere("count(distinct o)")

                .eq(E_Group.F_category, "adfsdafas")
                .eq(E_Group.F_name, "adfsdafas")
                .find(e -> {
                    // jpaDao.getEntityManager().detach(e);
                    return (Group) e;
                });

        //   System.out.println(r);

        System.out.println(groups);

    }

    @Test
    public void testJpaEntityStatusTest() throws Exception {

        User user = dao.selectFrom(User.class).findOne();
        Long id = user.getId();

        String description = "Update_" + LocalDateTime.now();
        user.setDescription(description);

        dao.save(user);
        user = dao.find(User.class, id);
        Assert.isTrue(user.getDescription().equals(description));

        user.setId(null);
        user.setOptimisticLock(null);
        dao.save(user);

        user.setId(null);
        user.setOptimisticLock(null);
        user = (User) dao.create(user);

        System.out.println(user);

    }

    @Test
    public void testSave() throws Exception {


        User user = dao.selectFrom(User.class).findOne();

        Long uid = user.getId();

        user = dao.find(User.class, uid);

        String description = "Update_" + LocalDateTime.now();

        user.setDescription(description);

        dao.save(user);


        user = dao.find(User.class, uid);


        Assert.isTrue(user.getDescription().equals(description));

    }

    @Test
    public void testDelete() throws Exception {

        Task one = dao.selectFrom(Task.class).findOne();

        dao.delete(one);


        one = dao.find(Task.class, one.getId());

        Assert.isNull(one);

    }

    @Test
    public void testFindAndConvert() throws Exception {

        dao.selectFrom(User.class, "u")
                .joinFetch(E_User.group)
                .gt(E_User.id, "100")
                .isNotNull(E_User.name)
                .find((User u) -> u.getGroup())
                .stream()
                .map(g -> (dao.copy(g, new Group(), 2)))
                .forEach(System.out::println)
//                .findFirst()
//                .ifPresent(System.out::println)
        ;

        System.out.println("ss");

    }

    @Test
    public void testGetEntityId() throws Exception {

        Group group = new Group(15L, "test");

        Object entityId = dao.getEntityId(group);

        Assert.isTrue(entityId.equals(15L));

    }

    @Test
    public void testEnv() throws Exception {


        DaoContext.threadContext.put("DATE_FORMAT", "YYYY/MM/DD");

        DaoContext.threadContext.put("orgId", 5L);

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
    static class JsonPathExistsConditionQO {

        @Where(op = Op.Exists, value = "logs", jsonPath = "$[0].logText")
        Boolean hasLog;
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

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = User.class, alias = "u")
    static class JsonArrayAppendUpdateDTO {

        @Update(value = "roleList", incrementMode = true)
        String role;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = User.class, alias = "u")
    static class JsonArrayAppendWildcardUpdateDTO {

        @Update(value = "roleList", jsonPath = "$[*]", incrementMode = true)
        List<String> roleList;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = PgJsonAppendUser.class, alias = "u")
    static class PgJsonActionLogAppendReq {

        @Update(value = "actionLog", incrementMode = true)
        List<PgJsonAppendUser.ActionLog> actionLog;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = PgJsonAppendUser.class, alias = "u")
    static class PgJsonProfileSetReq {

        @Update(value = "profile", jsonPath = "$.latestAction")
        PgJsonAppendUser.ActionLog actionLog;
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

    @Data
    static class JsonPathSelectResult {
        String logsJson;
        String firstLogText;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = Group.class, alias = E_Group.ALIAS, maxResults = 10, resultClass = JpaDtoProjectionDefaultAliasDTO.class)
    static class JpaDtoProjectionDefaultAliasDTO {

        @Select(E_Group.name)
        String name;

        @Select(E_Group.category)
        String category;

        @Select(E_Group.score)
        Integer score;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = Group.class, alias = E_Group.ALIAS, maxResults = 10, resultClass = JpaDtoProjectionNoAliasDTO.class)
    static class JpaDtoProjectionNoAliasDTO {

        @Select(value = E_Group.name, alias = C.BLANK_VALUE)
        String name;

        @Select(value = E_Group.category, alias = C.BLANK_VALUE)
        String category;

        @Select(value = E_Group.score, alias = C.BLANK_VALUE)
        Integer score;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = Group.class, alias = E_Group.ALIAS, maxResults = 10, resultClass = JpaDtoProjectionExpressionAliasDTO.class)
    static class JpaDtoProjectionExpressionAliasDTO {

        @Select(E_Group.name)
        String name;

        @Select(value = E_Group.score + " + 1", alias = "scorePlusOne")
        Integer scorePlusOne;
    }

    @Data
    @Accessors(chain = true)
    @TargetOption(entityClass = Group.class, alias = E_Group.ALIAS, resultClass = JpaDtoProjectionTotalsDTO.Result.class)
    static class JpaDtoProjectionTotalsDTO {

        @Data
        static class Result {

            @Count
            Integer totals;
        }
    }

    protected User prepareJsonPathUser(String uniqueRole, String logText) {

        Group group = dao.selectFrom(Group.class).findOne();

        if (group == null) {
            Group newGroup = new Group("JsonPathGroup-" + System.nanoTime(), null);
            newGroup.setState("正常");
            newGroup.setCategory("临时");
            newGroup.setScore(1);
            group = dao.create(newGroup);
        }

        User newUser = new User();
        newUser.setName("JsonPathUser-" + System.nanoTime());
        newUser.setState("正常");
        newUser.setScore(1);
        newUser.setArea("上海");
        newUser.setGroup(group);
        newUser.setRoleList(Arrays.asList(uniqueRole, "R_JSON_PATH_BASE"));
        newUser.setLogs(StringUtils.hasText(logText)
                ? Arrays.asList(new OperationLog().setLogText(logText))
                : Collections.emptyList());

        User user = dao.create(newUser);

        entityManager.clear();

        return user;
    }

}
