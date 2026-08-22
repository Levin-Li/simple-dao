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
import java.util.stream.Stream;

/**
 * Created by echo on 2015/11/17.
 */

@ActiveProfiles("dev")
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TestConfiguration.class}, properties = {
        "spring.jpa.properties.hibernate.query.hql.json_functions_enabled=true"
})
//@Transactional
public class DaoExamplesTest {

    private static final class Assert {

        private static final String DEFAULT_MESSAGE = "assertion failed";

        static void isTrue(boolean expression) {
            org.springframework.util.Assert.isTrue(expression, DEFAULT_MESSAGE);
        }

        static void isTrue(boolean expression, String message) {
            org.springframework.util.Assert.isTrue(expression, message);
        }

        static void notNull(Object object) {
            org.springframework.util.Assert.notNull(object, DEFAULT_MESSAGE);
        }

        static void notNull(Object object, String message) {
            org.springframework.util.Assert.notNull(object, message);
        }

        static void isNull(Object object) {
            org.springframework.util.Assert.isNull(object, DEFAULT_MESSAGE);
        }

        static void isNull(Object object, String message) {
            org.springframework.util.Assert.isNull(object, message);
        }

        static void hasText(String text) {
            org.springframework.util.Assert.hasText(text, DEFAULT_MESSAGE);
        }

        static void hasText(String text, String message) {
            org.springframework.util.Assert.hasText(text, message);
        }

        static void notEmpty(Collection<?> collection, String message) {
            org.springframework.util.Assert.notEmpty(collection, message);
        }
    }

    @Autowired
    SimpleDao dao;

    @Autowired
    UserDao userDao;

    @Autowired
    GroupDao groupDao;

    @Autowired
    Group2Dao group2Dao;

    @Autowired
    UserApi userApi;

    @Autowired
    UserApi2 userApi2;

    @Autowired
    UserApi3 userApi3;

    @Autowired
    UserService userService;


    @Autowired
    PluginManager pluginManager;


    @Autowired
    EntityManager entityManager;

    Random random = new Random(this.hashCode());


    /**
     * 注意测试时，使用的是h2的内存数据库，所以没有使用事务
     *
     * @throws Exception
     */

    @BeforeEach
    public void injectCheck() throws Exception {

        Assert.notNull(dao, "通用DAO没有注入");
        Assert.notNull(userDao, "userDao没有注入");
        Assert.notNull(groupDao, "groupDao没有注入");


        System.out.println("getInstalledPlugins:" + pluginManager.getInstalledPlugins());
    }


    @BeforeEach
    public void testGetEntityManager() throws Exception {
        //  EntityManager entityManager = dao.getEntityManager();
        Assert.notNull(entityManager);

    }


    static Map<String, String> parse(String extra) throws UnsupportedEncodingException {

        Map<String, String> result = new HashMap<String, String>();

        if (StringUtils.hasText(extra)) {
            for (String param : extra.split("&")) {
                String[] pk = param.split("=");
                if (pk.length > 1) {
                    result.put(pk[0].trim(), URLDecoder.decode(pk[1].trim(), "utf-8"));
                }
            }
        }

        return result;
    }


    @BeforeEach
    public void initTestEntity() throws Exception {

        int n = dao.deleteFrom(TestEntity.class)
                .disableSafeMode()
                .delete();


        n = 0;

        String[] categories = {"C1", "C2", "C3", "C4"};
        String[] states = {"S1", "S2", "S3", "S4"};

        while (n++ < 30) {

            dao.create(new TestEntity()
                    .setScore(random.nextInt(750))
                    .setCategory(categories[n % categories.length])
                    .setState(states[n % states.length])
                    .setName("test" + n)
                    .setRemark("system-" + n)
                    .setEditable(n % 2 == 0)
                    .setEnable(n % 10 == 0)
                    .setOrderCode(n)
            );

        }

        n = 30;


        long count = dao.selectFrom(TestEntity.class, "e")
                .select(AbstractNamedEntityObject::getName)
                .contains(AbstractNamedEntityObject::getName, "test")
                .count();


        Assert.isTrue(count == n, "查询数量错误1");


        count = dao.selectFrom(TestEntity.class, "e")
                .startsWith(E_TestEntity.name, "test")
                .count();

        Assert.isTrue(count == n, "查询数量错误2");


        n = n - dao.updateTo(TestEntity.class, "e")
                .set(E_TestEntity.name, "updateName")
                .in(E_TestEntity.state, "S2", "S4")
                .update();

        count = dao.selectFrom("simple_dao_test_entity")
                .startsWith(E_TestEntity.name, "test")
                .count();

        Assert.isTrue(count == n, "查询数量错误3");


        count = dao.selectFrom("simple_dao_test_entity", "e")
                .select(E_TestEntity.name)
                .startsWith(E_TestEntity.name, "test")
                .count();

        Assert.isTrue(count == n, "查询数量错误4");

    }

    @BeforeEach
    public void initTestData() throws Exception {

        try {
            //  DaoContext.setAutoFlush(false, false);
            initTestData2();
        } finally {
            //  DaoContext.setAutoFlush(false, true);
        }

    }

    public void initTestData2() throws Exception {


        if (dao.selectFrom(Group.class).count() >= 15
                && dao.selectFrom(User.class).count() > 0
                && dao.selectFrom(Task.class).count() > 0) {
            return;
        }

        //先删除旧数据
        dao.deleteFrom(Task.class)
                .disableSafeMode()
                .delete();

        dao.deleteFrom(User.class)
                .disableSafeMode()
                .delete();

        dao.deleteFrom(Group.class)
                .disableSafeMode()
                .delete();

        int gCount = 15;

        String[] states = {"正常", "已取消", "审请中", "已删除", "已冻结"};

        String[] types = {"虚拟组织", "部门", "小组", "协会"};

        String[] categories = {"临时", "常设", "月度", "年度"};

        String[] areas = {"福州", "厦门", "深圳", "上海"};


        Object one = dao.selectFrom(Group.class).select("max(id)").findOne();

        long n = (one == null) ? 1 : (long) one;

        Long parentId = null;


        while (gCount-- > 0) {

            //  n++;

            Group group = new Group("Group-" + n++, parentId);

//            group.setId((long) n);

            group.setState(states[Math.abs(random.nextInt()) % states.length]);
            group.setCategory(categories[Math.abs(random.nextInt()) % categories.length]);
            //  group.setType(types[Math.abs(random.nextInt()) % categories.length]);

            group.setScore(Math.abs(random.nextInt(100)));

            group = dao.create(group);

            long uCount = 3 * gCount;


            while (uCount-- > 0) {

                User user = new User();
                user.setName("User-" + group.getId() + "-" + uCount);

//                  user.setId((long) uCount);

                user.setState(states[Math.abs(random.nextInt()) % states.length]);
                user.setScore(Math.abs(random.nextInt(100)));
                user.setGroup(group)
                        .setArea(areas[Math.abs(random.nextInt()) % areas.length]);

                user.setRoleList(Arrays.asList("R_SA", "R_TEST"));

                user.setLogs(Arrays.asList(new OperationLog().setLogText("" + user.hashCode())));

                dao.create(user);


                long taskCount = 3 * uCount;

                //创建任务

                while (taskCount-- > 0) {

                    Task task = new Task();
                    task.setName("Task-" + taskCount);

                    dao.create(task
                            .setScore(random.nextInt(100))
                            .setUser(user)
                            .setState(states[Math.abs(random.nextInt()) % states.length])
                            .setArea(areas[Math.abs(random.nextInt()) % areas.length])
                    );
                }
            }

            if (parentId == null || (gCount % 5) == 0) {
                parentId = group.getId();
            }

        }


        List<String> names = dao.selectFrom(Group.class).select(E_Group.name).in(E_Group.id, Arrays.asList(1L, 2L, 3, 4, 5)).find();


        System.out.println(names);


    }

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

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonConditionUser-" + System.nanoTime())
                .setProfile(profileWithAction("conditionAction", "条件 JSON 对象")));

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
                .jsonContains(PgJsonAppendUser::getProfile, "$.conditionAction.action", "JSON")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();
        long notContainsCount = dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(PgJsonAppendUser::getProfile, "$.conditionAction.action", "不存在")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count();

        Assert.isTrue(existsCount == 1, "jsonExists 应该命中 PG JSON 路径");
        Assert.isTrue(notExistsCount == 1, "jsonNotExists 应该命中缺失 PG JSON 路径");
        Assert.isTrue(eqCount == 1, "jsonEq 应该命中 PG JSON 标量值");
        Assert.isTrue(notEqCount == 1, "jsonNotEq 应该命中不等于的 PG JSON 标量值");
        Assert.isTrue(containsCount == 1, "jsonContains 应该命中 PG JSON 标量值");
        Assert.isTrue(notContainsCount == 1, "jsonNotContains 应该命中不包含的 PG JSON 标量值");
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
                "jsonContains(Boolean,String,String,String)",
                "jsonContains(LambdaMethodAttr,String,String)",
                "jsonContains(String,String,String)",
                "jsonNotContains(Boolean,LambdaMethodAttr,String,String)",
                "jsonNotContains(Boolean,String,String,String)",
                "jsonNotContains(LambdaMethodAttr,String,String)",
                "jsonNotContains(String,String,String)",
                "jsonEq(Boolean,LambdaMethodAttr,String,Object)",
                "jsonEq(Boolean,String,String,Object)",
                "jsonEq(LambdaMethodAttr,String,Object)",
                "jsonEq(String,String,Object)",
                "jsonNotEq(Boolean,LambdaMethodAttr,String,Object)",
                "jsonNotEq(Boolean,String,String,Object)",
                "jsonNotEq(LambdaMethodAttr,String,Object)",
                "jsonNotEq(String,String,Object)",
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

        PgJsonAppendUser user = dao.create(new PgJsonAppendUser()
                .setName("PgJsonConditionOverloadUser-" + System.nanoTime())
                .setProfile(profileWithAction("conditionAction", "条件重载对象")));

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
                .jsonContains("profile", "$.conditionAction.action", "重载")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(true, "profile", "$.conditionAction.action", "重载")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Boolean, String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(PgJsonAppendUser::getProfile, "$.conditionAction.action", "重载")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Lambda, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonContains(true, PgJsonAppendUser::getProfile, "$.conditionAction.action", "重载")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonContains(Boolean, Lambda, String, String) 应命中");

        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains("profile", "$.conditionAction.action", "不存在")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(true, "profile", "$.conditionAction.action", "不存在")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Boolean, String, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(PgJsonAppendUser::getProfile, "$.conditionAction.action", "不存在")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Lambda, String, String) 应命中");
        Assert.isTrue(dao.selectFrom(PgJsonAppendUser.class)
                .jsonNotContains(true, PgJsonAppendUser::getProfile, "$.conditionAction.action", "不存在")
                .eq(PgJsonAppendUser::getId, user.getId())
                .count() == 1, "jsonNotContains(Boolean, Lambda, String, String) 应命中");
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
                .jsonContains(false, "profile", "$.guardAction.action", "不应该命中")
                .jsonContains(false, PgJsonAppendUser::getProfile, "$.guardAction.action", "不应该命中")
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
    public void testUpdateDTO() throws Exception {

        UpdateDao<User> userUpdateDao = dao.updateTo(User.class);

        userUpdateDao
                .set(E_User.name, "name1")
                .eq(E_User.enable, false)
                .update();
    }

    @Test
    public void testCount() throws Exception {

        long count = dao.selectFrom(User.class, E_User.ALIAS)
                .leftJoin(Group.class, E_Group.ALIAS)
                .count();

        count = dao.selectFrom(User.class, E_User.ALIAS)
                .leftJoin(Group.class)
                .count();

        Assert.isTrue(count == dao.selectFrom(User.class).count(), "左连接数据错误");


        dao.selectByNative(User.class, E_User.ALIAS)
                .leftJoin(Group.class)
                .count();


        System.out.println(count);
    }

    @Test
    public void testHaving() throws Exception {

        SelectDao<User> selectDao = dao.selectFrom(User.class, "u");

        selectDao
                .limit(1, 10)
                .appendByQueryObj(new UserStatDTO());

        String statement = selectDao.genFinalStatement();

        System.out.println(statement + "  -->   params:" + selectDao.genFinalParamList());

        Assert.isTrue(statement.contains(" Having "));
    }


    @Test
    public void testStatDTO() throws Exception {

        SelectDao<User> selectDao = dao.selectFrom(User.class, "u");

        selectDao
                .limit(1, 10)
                .appendByQueryObj(new UserStatDTO());

        String statement = selectDao.genFinalStatement();
        Assert.isTrue(statement.contains(" Having "));

        System.out.println(statement + "  -->   params:" + selectDao.genFinalParamList());


    }


    @Test
    public void testPagingQueryHelper() throws Exception {

        int n = 0;
        while (n++ < 20) {

            long st = System.currentTimeMillis();

            DefaultPagingData<TableJoinDTO> resp = PagingQueryHelper.findByPageOption(dao, null,
                    new DefaultPagingData<TableJoinDTO>(), new TableJoinDTO().setRequireTotals(true), null);

            System.out.println(n + " response takes " + (System.currentTimeMillis() - st) + " , totals" + resp.getTotals());

        }

    }

    @Test
    public void testPagingQueryHelper2() throws Exception {

        DefaultPagingData<TableJoin3> resp = PagingQueryHelper.findByPageOption(dao, null,
                DefaultPagingData.class, new TableJoin3().setRequireTotals(true), null);

        System.out.println(resp.getTotals());
    }


    @Test
    public void testStatDTO2() throws Exception {


        List<UserStatDTO> byQueryObj = dao.findByQueryObj(UserStatDTO.class, new UserStatDTO());

        System.out.println(byQueryObj);
    }

    @Test
    public void testJpaStatDTOCount() throws Exception {

        UserStatDTO query = new UserStatDTO();

        long totals = dao.forSelect(query).count();

        List<UserStatDTO> resultList = dao.findByQueryObj(UserStatDTO.class, query);

        Assert.isTrue(totals == resultList.size(), "统计查询总数应该等于分组后的结果行数");
    }

    @Test
    public void testQueryFrom() throws Exception {

        SelectDao<User> selectDao = dao.selectFrom(User.class, "u");

        selectDao
                .limit(1, 10)
                .appendByQueryObj(new UserDTO())
                // .and().or().end()
                .where("222 != :orderCode")
//                .appendWhere("3333 < :lastUpdateTime")
                .find();


        final String statement = selectDao.genFinalStatement();

        System.out.println("生成的语句：" + statement);

        Assert.isTrue(statement.toLowerCase().contains(" between "), "Between语句生成错误");

        //必须存在正确的Having字句
        Assert.isTrue(statement.contains("Having NOT( u.state IN ( :?, :?, :? ) )"), "Having语句生成错误");

        //必须存在正确的NOT 字句
        Assert.isTrue(statement.contains("AND NOT((u.enable =  :? AND u.editable =  :? AND (u.createTime >  :? OR u.score Between  :? AND  :?) AND u.remark LIKE  :?)) AND u.createTime Between  :? AND  :?")
                , "Where 语句生成错误");

    }


    @Test
    public void testEnvQueryFrom() throws Exception {

        SelectDao<User> selectDao = dao.selectFrom(User.class, "u");

        DaoContext.globalContext.put("env.g.P1", "全局参数1");

        DaoContext.globalContext.put("id", "默认全局id");

        DaoContext.threadContext.put("env.thread.P1", "线程参数1");

        DaoContext.threadContext.put("id", "默认线程Id");

        HashMap<String, Object> context = new HashMap<>();

        context.put("env.jpaDao.P1", "Dao参数1");

        PagingData<Object> data = dao.findPagingDataByQueryObj(new UserDTO2());

        System.out.println("ok");

    }


    //    @Test
    public void testNativeSelect() throws Exception {

        SelectDao<User> selectDao = dao.selectFrom("jpa_dao_test_User");

        List entities = selectDao
                .limit(1, 10)
                //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
//               .appendSelectColumns("id , ( name || 'ddddd' ) AS name ")
//               .appendSelectColumns(" score AS scoreGt")
                .appendByQueryObj(new UserDTO3())
                .where("score > :maxScore", MapUtils.put("maxScore", 500L).build())
                .gt(E_User.F_score, 300)
                .find(UserDTO3.class);

        System.out.println("testSelectFrom:" + entities);

    }


    @Test
    public void testLogicDelete() throws Exception {

        SelectDao<TestEntity> dao = this.dao.selectFrom(TestEntity.class);

        List<TestEntity> testEntities = dao.find();


        List<Long> logicDeletedIds = new ArrayList<>();

        for (TestEntity testEntity : testEntities) {

            if (testEntity.<Long>getId() % 2 == 0) {

                int n = this.dao.deleteFrom(TestEntity.class)
                        .eq(E_TestEntity.id, testEntity.getId())
                        .delete();

                Assert.isTrue(n > 0, "逻辑删除失败");

                logicDeletedIds.add(testEntity.getId());
            }

        }


        for (Long id : logicDeletedIds) {

            int n = this.dao.updateTo(TestEntity.class).set(E_TestEntity.remark, "逻辑删除备注更新").eq(E_TestEntity.id, id).update();
            Assert.isTrue(n < 1, "已经逻辑删除的对象，还能被更新");

            n = this.dao.deleteFrom(TestEntity.class).eq(E_TestEntity.id, id).delete();
            Assert.isTrue(n < 1, "已经逻辑删除的对象，还能被删除");

            TestEntity entity = this.dao.selectFrom(TestEntity.class).eq(E_TestEntity.id, id).findOne();
            Assert.isTrue(entity == null, "已经逻辑删除的对象，还能被查询到");

        }


        testEntities = this.dao.selectFrom(TestEntity.class).find();

        for (TestEntity testEntity : testEntities) {
            Assert.isTrue(testEntity.<Long>getId() % 2 == 1, "已经逻辑删除的数据仍然被查询出来");
        }


        //
        testEntities = this.dao.selectFrom(TestEntity.class)
                .filterLogicDeletedData(false)
                .find();


        EntityOption entityOption = (TestEntity.class.getAnnotation(EntityOption.class));


        if (entityOption != null) {
            //ID 为偶数的记录数必须大于0

            boolean disableDel = Stream.of(entityOption.disableActions()).filter(a -> EntityOption.Action.Delete.equals(a)).count() > 0;

            Assert.isTrue(!disableDel || testEntities.stream().filter(e -> e.<Long>getId() % 2 == 0).count() > 0, "逻辑删除的数据没有出现");
        }


    }


    @Test

    public void testSelect() throws Exception {

        SelectDao<User> selectDao = dao.selectFrom(User.class);


        List entities = selectDao
                .limit(1, 10)
                //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
//               .appendSelectColumns("id , ( name || 'ddddd' ) AS name ")
//               .appendSelectColumns(" score AS scoreGt")
                .appendByQueryObj(new UserDTO3())
                .where("score > :maxScore", MapUtils.put("maxScore", 500).build())
                .gt(E_User.F_score, 300)
                .find(UserDTO3.class);

        System.out.println("testSelectFrom:" + entities);

    }


    @Test
    public void tesSelectSimpleType() throws Exception {


        Boolean one = dao.selectFrom(Group.class).select(E_Group.enable).findOne(Boolean.class);
        boolean one2 = dao.selectFrom(Group.class).select(E_Group.enable).findOne(boolean.class);

        String name = dao.selectFrom(Group.class).select(E_Group.name).findOne(String.class);

        List<String> names = dao.selectFrom(Group.class).select(E_Group.name).limit(0, 8).find(String.class);

        Group parent = dao.selectFrom(Group.class).select(E_Group.parent).findOne(Group.class);


        GroupInfo info = dao.selectFrom(Group.class).select(E_Group.id, E_Group.name).findOne(GroupInfo.class);

        Assert.notNull(info);
        Assert.hasText(info.getId());
        Assert.hasText(info.getName());

        System.out.println(names);
    }


    @Test
    public void testStat() throws Exception {

        Object commDto = dao.selectFrom(Group.class).appendByQueryObj(new CommDto()).find(CommDto.class);

        List<GroupStatDTO> objects = dao.findByQueryObj(GroupStatDTO.class, new GroupStatDTO());

        System.out.println(objects);

    }


    @Test
    public void testDeleteById() throws Exception {

        dao.deleteById(TestEntity.class, 1L);

    }


    @Test
    public void testJoinDto() throws Exception {


        List<MulitTableJoinDTO> objects = dao.findByQueryObj(MulitTableJoinDTO.class, new MulitTableJoinDTO());


        Assert.notNull(objects, "null");

    }


    @Test
    public void testJoinDto2() throws Exception {


        List<TableJoinDTO> objects = dao.findByQueryObj(new TableJoinDTO());


        Assert.notNull(objects);

    }

    @Test
    public void testEntityClassSet() throws Exception {

        ResultClassSupplier classSupplier = () -> UserInfo.class;

        EntityClassSupplier entityClassSupplier = () -> User.class;

        List<UserInfo> objects = dao.findByQueryObj(entityClassSupplier, classSupplier, Group.class, new CommDto());

        Assert.notNull(objects);

    }


    @Test
    public void testTableJoinStatDTO() throws Exception {


        SelectDao selectDao = dao.newDao(SelectDao.class, new TableJoinStatDTO());

        String sql = selectDao.genFinalStatement();

        //
        Assert.isTrue(sql.contains(E_Group.ALIAS + ".name Desc"), "预期的排序语句不存在");

        List<TableJoinStatDTO> objects = dao.findByQueryObj(new TableJoinStatDTO(), new PagingQueryReq(1, 10));
//        List<TableJoinStatDTO> objects = jpaDao.findByQueryObj(new TableJoinStatDTO() );

        String aa = "Select Count( 1 ) , Sum( u.score ) , Avg( u.score ) AS avg , g.name  From com.levin.commons.dao.domain.User u  Left join com.levin.commons.dao.domain.Group g on u.group = g.id     Group By  g.name Having  Count( 1 ) >   ?1  AND Avg( u.score ) >   ?2  Order By  Count( 1 ) Desc , avg Desc , g.name Desc";

        Assert.notNull(objects);

    }


    @Test
    public void testCListAnno1() throws Exception {


        List<TestEntity> objects = dao.findByQueryObj(new TestEntityDto());

        Assert.notNull(objects, "");

    }


    @Test
    public void testCListAnno2() throws Exception {

        List<TestEntity> objects = dao.findByQueryObj(new TestCListDto());

        Assert.notNull(objects, "");

    }


    @Test
    public void testJoin() throws Exception {


        List<MulitTableJoinDTO> objects = dao.selectFrom(User.class, "u")
                .join("left join jpa_dao_test_Group g on u.group.id = g.id")
                .appendByQueryObj(new MulitTableJoinDTO())

                .where("u.id > :mapParam1", MapUtils.put("mapParam1", "2").build())

                .gt("u.id", "1")
                .find(MulitTableJoinDTO.class);

        Assert.notNull(objects);
    }


    @Test
    public void testJoin2() throws Exception {


        List<MulitTableJoinDTO> objects = dao.selectFrom("jpa_dao_test_User u")
                .join("left join jpa_dao_test_Group g on u.group_id = g.id")
                .select("u.id AS uid ,g.id AS gid")

                .where("g.id > " + dao.getParamPlaceholder(false), 2L)

                .limit(-1, 100)
                .find(MulitTableJoinDTO.class);


        Assert.notNull(objects);
    }


    /**
     * 测试混合参数
     *
     * @throws Exception
     */
    @Test
    public void testMixParam() throws Exception {

        List<User> objects =
                dao.selectFrom(User.class)
                        .gt(E_User.score, 1)
                        .where("id > :mapParam1  and id < :p2 ",
                                MapUtils.put("mapParam1", "2")
                                        .put("p1", "123456")
                                        .put("p2", "23456")
                                        .build())
                        .gte(E_User.id, 2)
                        .find(User.class);

        //From com.levin.commons.dao.domain.User     Where score >   ?1  AND id > :mapParam1  and :p1 < :p2  AND id >=   ?2


        OperationLog operationLog = dao.find(OperationLog.class, 1L);

        Assert.notNull(objects);

    }

    //@Test
    public void testSelectFrom() throws Exception {

        long millis = System.currentTimeMillis();

        SelectDao<User> selectDao = dao.selectFrom(User.class, "u");

        List entities = selectDao
                .limit(1, 10)
                //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
                .appendByQueryObj(new UserSelectDTO()
                        .setNamedParams(MapUtils.asMap("minScore", 224, "groupName", "'group'")))
                //  .appendWhereEquals("", "")
                .find();


        millis = System.currentTimeMillis() - millis;

        System.out.println("testSelectFrom:" + entities);

    }

    @Test
    public void testSimpleSubQuery() {


        int[] oj = {2, 33,};

        Integer[] ojb = {2, 3, 4};

        Object[] ddd = {"dafa", 3.4, 23423};

        User[] sss = {};


        System.out.println(sss instanceof Object[]);

        System.out.println(int[].class == oj.getClass());
        System.out.println(int[].class == oj.getClass());
        System.out.println(Object[].class.isAssignableFrom(oj.getClass()));

        System.out.println(Object[].class.isAssignableFrom(ojb.getClass()));
        System.out.println(Object[].class.isAssignableFrom(ddd.getClass()));
        System.out.println(Object[].class.isAssignableFrom(sss.getClass()));

        List<Object> byQueryObj = dao.findByQueryObj(new SimpleSubQueryDTO());

        System.out.println(byQueryObj);

    }


    /**
     * 笛卡儿积 连接
     */
    @Test
    public void testSimpleJoin() {


        List<Object> objects = dao.selectFrom(User.class, "u")
                .leftJoin(Group.class, "g")
                .selectByStatement(true, "u")
                .where("u.group.id = g.id ")
                .isNotNull(E_User.id)
                .gt(E_User.score, 5)
                .limit(0, 20)
                .find();

        System.out.println(objects);

        Assert.isTrue(objects.size() == 20);

//        //自然连接
//        List result = dao.selectByNative(User.class, "u")
//                .join(true, Group.class, "g")
//                .select("u.*")
//                .where("F$:u.group.id = g.id ")
//                .isNotNull(E_User.id)
//                .gt(E_User.score, 5)
//                .limit(0, 20)
//                .find();
//
//        System.out.println(result);
//
//        Assert.isTrue(result.size() == 20);

    }

    @Test
    public void testNativeSQL() {

        EntityType<User> entity = entityManager.getMetamodel().entity(User.class);

        int n = dao.updateTo(E_User.E_ENTITY_NAME, "u")
                .setByStatement(String.format("%s = %s + 1", E_User.score, E_User.score))
                .set(E_User.lastUpdateTime, LocalDateTime.now())
                .or()
                .isNull(E_User.score)
                .isNotNull(E_User.createTime)
                .end()
                .limit(-1, 3)
                .enableAutoAppendLimitStatement(true)
                // .appendToLast(true,"order by id limit 1")
                .update();

        Assert.isTrue(n == 3, "更新记录数错误1");


        n = dao.updateByNative(User.class, E_User.ALIAS)
                .set(true, true, User::getScore, 3)
                .set(E_User.lastUpdateTime, LocalDateTime.now())
                .or()
                .isNull(User::getScore)
                .isNotNull(E_User.createTime)
                .end()
                .limit(-1, 5)
                .enableAutoAppendLimitStatement(true)
                // .appendToLast(true,"order by id limit 1")
                .update();

        Assert.isTrue(n == 5, "更新记录数错误3");


        System.out.println(n);

    }


    /**
     * 测试语句生成时间
     *
     * @throws Exception
     */
    @Test
    public void testGenSQLSpeed() throws Exception {

        long millis = System.currentTimeMillis();

        SelectDao<User> selectDao = dao.selectFrom(User.class, "u");

        dao.selectFrom(User.class, "u")
                .appendByQueryObj(new GroupStatDTO())
                .genFinalStatement();

        millis = System.currentTimeMillis() - millis;

        System.out.println("1 testSelectTime:" + millis);


        millis = System.currentTimeMillis();

        dao.selectFrom(User.class, "u")
                .appendByQueryObj(new TestEntityStatDto())
                .genFinalStatement();


        millis = System.currentTimeMillis() - millis;

        System.out.println("2 testSelectTime:" + millis);


        millis = System.currentTimeMillis();

        String ql = dao.selectFrom(User.class, "u")
                .appendByQueryObj(new SubQueryDTO())
                .genFinalStatement();


        millis = System.currentTimeMillis() - millis;

        System.out.println("3 speed testSelectTime:" + millis);


        millis = System.currentTimeMillis();


        selectDao
                .limit(1, 10)
                //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
                .appendByQueryObj(new UserSelectDTO()
                        .setNamedParams(MapUtils.asMap("minScore", 224, "groupName", "'group'")))
                //  .appendWhereEquals("", "")
                .genFinalStatement();

        selectDao.genFinalParamList();


        millis = System.currentTimeMillis() - millis;

        System.out.println("4 testSelectTime:" + millis);

    }


    @Test
    public void testLogicDto() throws Exception {


    }

    @Test
    public void testUpdateFrom() throws Exception {

        UpdateDao<User> updateDao = dao.updateTo(User.class, "u");

        int update = dao.updateByQueryObj(new UserUpdateDTO());

        // Assert.isTrue(update == 1, "更新记录错误");

        System.out.println(update);


        int n = dao.updateTo(Group.class)
                .set(E_Group.lastUpdateTime, LocalDateTime.now())
//                .appendColumn(E_Group.description, "" + System.currentTimeMillis())
                .contains(E_Group.name, "2")
                .update();

        System.out.println("Group update:" + n);


        n = dao.updateTo(User.class)
                .set(E_User.lastUpdateTime, LocalDateTime.now())
//                .appendColumn(E_User.description, "" + System.currentTimeMillis())
                .contains(E_User.name, "2")
                .update();


        System.out.println("Group E_User:" + n);
    }

    @Test
    public void testCtxVar() throws Exception {

        CtxVarTestReq req = new CtxVarTestReq();

        List<CtxVarTestReq.Info> infoList = dao.findByQueryObj(req.setIsQueryName(true));

        boolean ok = infoList.stream().allMatch(info -> StringUtils.hasText(info.getName()));

        Assert.isTrue(ok, "CtxVar err");

        infoList = dao.findByQueryObj(req.setIsQueryName(false));

        ok = infoList.stream().allMatch(info -> !StringUtils.hasText(info.getName()));

        Assert.isTrue(ok, "CtxVar err");


        ok = infoList.stream().anyMatch(info -> StringUtils.hasText(info.getParentName()));

        Assert.isTrue(ok, "CtxVar err");

    }

    @Test
    public void testCtxVarCaseStat() throws Exception {

        long expectedScore = dao.selectFrom(TestEntity.class, "e")
                .find(TestEntity.class)
                .stream()
                .map(TestEntity::getScore)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();

        CtxVarCaseStatReq.Result result = dao.findOneByQueryObj(new CtxVarCaseStatReq()
                .setBeginTime(LocalDateTime.now().minusDays(1))
                .setFutureTime(LocalDateTime.now().plusDays(1)));

        Assert.notNull(result, "CtxVar Case 统计结果为空");
        Assert.isTrue(Objects.equals(expectedScore, Objects.requireNonNullElse(result.getScoreFromBegin(), 0L)),
                "CtxVar Case 统计未正确读取 beginTime：" + result);
        Assert.isTrue(Objects.equals(0L, Objects.requireNonNullElse(result.getScoreFromFuture(), 0L)),
                "CtxVar Case 统计未正确读取 futureTime：" + result);
    }


    @Test
    public void testObjectUtils() {

        String n = "" + System.currentTimeMillis();

        List<InjectTestObj.QResult> list = Arrays.asList(
                new InjectTestObj.QResult().setName(n),
                new InjectTestObj.QResult().setName("2")
        );

        String json = new Gson().toJson(list);

        Map<String, String> build = MapUtil.builder("product_infos", json).build();

        InjectTestObj testObj = new InjectTestObj();

        try {
            ObjectUtil.VARIABLE_INJECTOR_THREAD_LOCAL.set(DaoContext.getVariableInjector());
            ObjectUtil.copyProperties(build, testObj, -1);
        } finally {
            ObjectUtil.VARIABLE_INJECTOR_THREAD_LOCAL.set(null);
        }

        Assert.notNull(testObj.getProduct_infos());

        Assert.isTrue(testObj.getProduct_infos().get(0).getName().equals(n));

    }

    @Test
    public void testJsonPathSelectAndWhereStatements() {

        String statement = dao.selectFrom(User.class, "u")
                .appendByQueryObj(new JsonPathSelectQO()
                        .setRole("R_ADMIN")
                        .setHasLog(Boolean.TRUE))
                .genFinalStatement();

        Assert.isTrue(statement.contains("json_query(") && statement.contains("'$[*]'"), "wildcard where 条件应生成 json_query");
        Assert.isTrue(statement.contains("json_exists(") && statement.contains("'$[0].logText'"), "Exists 注解应生成 json_exists");
        Assert.isTrue(statement.contains("COALESCE(str(json_query(") && statement.contains("json_value("), "Select 注解应同时兼容对象/数组和标量 JSON 路径");
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
    public void testJsonPathUpdateStatement() {

        String statement = dao.updateTo(User.class, "u")
                .appendByQueryObj(new JsonPathUpdateDTO()
                        .setFirstLogText("changed")
                        .setRole("R_ADMIN"))
                .genFinalStatement();

        Assert.isTrue(statement.contains("u.logs = json_set(u.logs, '$[0].logText'"), "Update 注解应生成 json_set 更新语句");
        Assert.isTrue(statement.contains("json_query(") && statement.contains("'$[*]'"), "Update 场景中的 where 条件也应支持 wildcard JSON 路径");
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

    @Test
    public void testQueryFrom2() throws Exception {

        List<Object> list = dao.selectFrom(Group.class)
                .select(AbstractBaseEntityObject::getCreateTime)
                .contains(E_Group.name, "2")
                .find();

        Object createTime = list.get(0);

        Assert.isTrue(createTime instanceof Date || createTime instanceof Temporal, "预期的第一列不是时间");


        dao.selectFrom("table").and().or().and().end().end().end();

    }


    @Test
    public void testDeleteFrom() throws Exception {

        int r = dao.deleteFrom(User.class, "u")
                //  .appendWhere("name like ?", "%0%")
                //   .appendWhereEquals("name", "10")
                //   .appendWhere(" orderCode > ?", 10)
                .appendByQueryObj(new UserDTO())
                //  .appendWhereByQueryObj(c)
//                .appendWhereByEL("Q_", elMap)
                .isNull(User::getState)
                .delete();

        //以上查询会生成条件，包括map对应的查询条件

        System.out.println(r);
    }

    @Test
    public void testMapFrom() throws Exception {

        Map elMap = new LinkedHashMap();

        elMap.put("Q_Between_id", " 12 , 34");
        elMap.put("Q_Not_In_id", " 12,34, 534,546, 456");
        elMap.put("Q_NotIn_name", "12,34,534,546,456");
        elMap.put("Q_Gt_createTime", LocalDateTime.of(2012, 1, 30, 23, 59));
        elMap.put("Q_Not_parentId", 90);
        elMap.put("Q_Lt_id", -1);

        int r = dao.deleteFrom(Group.class, "e")
                //  .appendWhere("name like ?", "%0%")
                //   .appendWhereEquals("name", "10")
                //   .appendWhere(" orderCode > ?", 10)
//                .appendWhereByQueryObj(new DeleteDTO())
                //  .appendWhereByQueryObj(c)
                .appendByEL("Q_", elMap)
                .delete();

        //以上查询会生成条件，包括map对应的查询条件

        System.out.println(r);
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

    private User prepareJsonPathUser(String uniqueRole, String logText) {

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
