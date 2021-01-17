package com.levin.commons.dao;

import com.levin.commons.dao.domain.*;
import com.levin.commons.dao.domain.support.E_TestEntity;
import com.levin.commons.dao.domain.support.TestEntity;
import com.levin.commons.dao.dto.*;
import com.levin.commons.dao.proxy.UserApi;
import com.levin.commons.dao.proxy.UserApi2;
import com.levin.commons.dao.proxy.UserApi3;
import com.levin.commons.dao.repository.Group2Dao;
import com.levin.commons.dao.repository.GroupDao;
import com.levin.commons.dao.repository.SimpleDaoRepository;
import com.levin.commons.dao.repository.UserDao;
import com.levin.commons.dao.service.UserService;
import com.levin.commons.dao.service.dto.QueryUserEvt;
import com.levin.commons.dao.service.dto.UserInfo;
import com.levin.commons.dao.service.dto.UserUpdateEvt;
import com.levin.commons.dao.support.PagingQueryHelper;
import com.levin.commons.dao.support.PagingQueryReq;
import com.levin.commons.dao.support.PagingData;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.utils.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by echo on 2015/11/17.
 */

@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TestConfiguration.class})
//@Transactional
public class DaoExamplesTest {

    @Autowired
    JpaDao jpaDao;

    @Autowired(required = false)
    SimpleDaoRepository simpleDaoRepository;

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

    Random random = new Random(this.hashCode());

    /**
     * 注意测试时，使用的是h2的内存数据库，所以没有使用事务
     *
     * @throws Exception
     */

    @Before
    public void injectCheck() throws Exception {
        Assert.notNull(jpaDao, "通用DAO没有注入");
        Assert.notNull(simpleDaoRepository, "simpleDao没有注入");
        Assert.notNull(userDao, "userDao没有注入");
        Assert.notNull(groupDao, "groupDao没有注入");


        System.out.println("getInstalledPlugins:" + pluginManager.getInstalledPlugins());
    }


    @Before
    public void testGetEntityManager() throws Exception {
        EntityManager entityManager = jpaDao.getEntityManager();
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


    @Before
    public void initTestEntity() throws Exception {

        jpaDao.deleteFrom(TestEntity.class)
                .disableSafeMode()
                .delete();


        int n = 0;

        String[] categories = {"C1", "C2", "C3", "C4"};
        String[] states = {"S1", "S2", "S3", "S4"};

        while (n++ < 30) {

            jpaDao.create(new TestEntity()
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


        long count = jpaDao.selectFrom(TestEntity.class, "e")
                .select(E_TestEntity.name)
                .contains(E_TestEntity.name, "test")
                .count();


        Assert.isTrue(count == n, "查询数量错误1");


        count = jpaDao.selectFrom(TestEntity.class, "e")
                .startsWith(E_TestEntity.name, "test")
                .count();

        Assert.isTrue(count == n, "查询数量错误2");


        n = n - jpaDao.updateTo(TestEntity.class, "e")
                .set(E_TestEntity.name, "updateName")
                .in(E_TestEntity.state, "S2","S4")
                .notIn(E_TestEntity.category, "C1", "C4")
                .eq(E_TestEntity.editable, true)
                .update();


        count = jpaDao.selectFrom("jpa_dao_test_entity")
                .startsWith(E_TestEntity.name, "test")
                .count();

        Assert.isTrue(count == n, "查询数量错误3");


        count = jpaDao.selectFrom("jpa_dao_test_entity", "e")
                .select(E_TestEntity.name)
                .startsWith(E_TestEntity.name, "test")
                .count();

        Assert.isTrue(count == n, "查询数量错误3");

    }

    @Before
    public void initTestData() throws Exception {

        try {
            DaoContext.setAutoFlush(false, false);
            initTestData2();
        } finally {
            DaoContext.setAutoFlush(false, true);
        }
    }

    public void initTestData2() throws Exception {

        if (jpaDao.selectFrom(User.class).count() > 0) {
            return;
        }


        //先删除旧数据
        jpaDao.deleteFrom(Task.class)
                .disableSafeMode()
                .delete();

        jpaDao.deleteFrom(User.class)
                .disableSafeMode()
                .delete();

        jpaDao.deleteFrom(Group.class)
                .disableSafeMode()
                .delete();

        int gCount = 15;

        String[] states = {"正常", "已取消", "审请中", "已删除", "已冻结"};

        String[] types = {"虚拟组织", "部门", "小组", "协会"};

        String[] categories = {"临时", "常设", "月度", "年度"};

        String[] areas = {"福州", "厦门", "深圳", "上海"};


        Object one = jpaDao.selectFrom(Group.class).select("max(id)").findOne();

        long n = (one == null) ? 1 : (long) one;

        Long parentId = null;


        while (gCount-- > 0) {

            //  n++;

            Group group = new Group("Group-" + n++, parentId);

//            group.setId((long) n);

            group.setState(states[Math.abs(random.nextInt()) % states.length]);
            group.setCategory(categories[Math.abs(random.nextInt()) % categories.length]);
            group.setType(types[Math.abs(random.nextInt()) % categories.length]);

            group.setScore(Math.abs(random.nextInt(100)));

            jpaDao.create(group);

            long uCount = 3 * gCount;


            while (uCount-- > 0) {

                User user = new User();
                user.setName("User-" + group.getId() + "-" + uCount);

//                  user.setId((long) uCount);

                user.setState(states[Math.abs(random.nextInt()) % states.length]);
                user.setScore(Math.abs(random.nextInt(100)));
                user.setGroup(group)
                        .setArea(areas[Math.abs(random.nextInt()) % areas.length]);
                jpaDao.create(user);


                long taskCount = 3 * uCount;

                //创建任务

                while (taskCount-- > 0) {

                    Task task = new Task();
                    task.setName("Task-" + taskCount);

                    jpaDao.create(task
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
    }

    @Test
    public void testJoinFetch() {


        Group one = jpaDao.selectFrom(Group.class).gt(E_Group.id, 5L).findOne();

        GroupInfo queryDto = new GroupInfo().setId("" + one.getId());

        Object byQueryObj = jpaDao.findByQueryObj(GroupInfo.class, queryDto);

        long count = jpaDao.forSelect(queryDto).joinFetch(E_Group.children).count();

        Object ss = jpaDao.countByQueryObj(queryDto);

        Assert.notNull(byQueryObj);

    }

    @Test
    public void testFromStatementDTO() {

        List<FromStatementDTO> byQueryObj = jpaDao.findByQueryObj(FromStatementDTO.class, new FromStatementDTO());

        // System.out.println(byQueryObj);

        assert byQueryObj.size() > 0;


        List<TableJoin3> byQueryObj1 = jpaDao.findByQueryObj(TableJoin3.class, new TableJoin3());


        // System.out.println(byQueryObj1);

        assert byQueryObj1.size() > 0;

    }


    @Test
    public void testJoinAndStat() {

        List<Map> g = jpaDao.selectFrom(Group.class, "g")
                .join("left join " + User.class.getName() + " u on g.id = u.group.id")
                .join("left join " + Task.class.getName() + " t on u.id = t.user.id")
                .count("1", "cnt")
                .avg("t.score", "ts")
                .avg("u.score", "us")
                .avg("g.score", "gs")
                .sum("t.score", "ts2")
                .groupByAsAnno(E_Group.name, "")
                .orderBy("ts2")
//                .groupBy("g.name")
                .find(Map.class);


        Assert.isTrue(g.size() > 0);

        Assert.isTrue(g.get(0).containsKey("cnt"));
        Assert.isTrue(g.get(0).containsKey(E_Group.name));
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
        List<User> byQueryObj = jpaDao.findByQueryObj(User.class, new AnnoTest());
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

        UserInfo userInfo2 = jpaDao.findOneByQueryObj(UserInfo.class, new QueryUserEvt().setId(userInfoList.get(0).getId()));


        Assert.isTrue(userService.addUserScore(new UserUpdateEvt().setId(userInfo2.getId()).setAddScore(5)));

        UserInfo userInfo3 = jpaDao.findOneByQueryObj(UserInfo.class, new QueryUserEvt().setId(userInfo2.getId()));

        Assert.isTrue(userInfo3.getScore() == userInfo2.getScore() + 5);


    }


    @Test
    @Transactional
    public void testTransactional2() throws InterruptedException {

        TestEntity entity = (TestEntity) jpaDao.create(new TestEntity()
                .setScore(random.nextInt(750))
                .setName("test" + random.nextInt(750))
                .setRemark("system-" + random.nextInt(750))
                .setOrderCode(random.nextInt(750))
        );

        System.out.println("1 ------------------------------");
        Thread.sleep(1000);

        List<Object> objectList = jpaDao.selectFrom(TestEntity.class, "e")
                .gt(E_TestEntity.id, 20)
                .find();

        System.out.println("2 ------------------------------");
        Thread.sleep(1000);

        int orderCode = -1;

        jpaDao.updateTo(TestEntity.class)
                .set(E_TestEntity.orderCode, orderCode)
                .eq(E_TestEntity.id, 1)
                .update();

        System.out.println("3 ------------------------------");
        Thread.sleep(1000);

        orderCode = -1234567;

        jpaDao.updateTo(TestEntity.class)
                .set(E_TestEntity.orderCode, orderCode)
                .eq(E_TestEntity.id, entity.getId())
                .update();

        Assert.isTrue(jpaDao.find(TestEntity.class, entity.getId()).getOrderCode() == orderCode, "变更没有生效");

        orderCode = -67890;

        jpaDao.updateTo(TestEntity.class)
                .set(E_TestEntity.orderCode, orderCode)
                .gt(E_TestEntity.id, entity.getId() - 50)
                .update();

        System.out.println("4------------------------------");
        Thread.sleep(1000);

        Assert.isTrue(jpaDao.find(TestEntity.class, entity.getId()).getOrderCode() == orderCode, "变更没有生效");


        System.out.println("5------------------------------");
        Thread.sleep(1000);

        objectList = jpaDao.selectFrom(TestEntity.class, "e")
                .find();

        System.out.println("6------------------------------");
        // Thread.sleep(1000);

        Assert.isTrue(objectList.contains(entity), "");

    }

    @Test
    @Transactional
    public void testTransactional() {

        TestEntity entity = (TestEntity) jpaDao.create(new TestEntity()
                .setScore(random.nextInt(750))
                .setName("test" + 11)
                .setRemark("system-" + 11)
                .setOrderCode(11)
        );


        User user = jpaDao.selectFrom(User.class).gt(E_User.id, 20).findOne();

        Long uid = user.getId();


        Object one = jpaDao.selectFrom(User.class, "u").eq(E_User.id, uid).findOne();


        Assert.notNull(one);


        one = jpaDao.selectFrom(User.class, "u")
                .where("u.id = ?1  order by u.id desc", uid)
                .findOne();


        Assert.notNull(one);


    }

    @Test
    public void testExists() {

        long cnt = jpaDao.selectFrom(User.class)
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
    public void testSimpleDao() {

        try {
            User user = simpleDaoRepository.findOne(new UserDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<User> users = simpleDaoRepository.find(new UserDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            int update = simpleDaoRepository.update(new UserUpdateDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int delete = simpleDaoRepository.delete(new UserDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testTestEntityStatDto() {

        List<TestEntityStatDto> dtoList = jpaDao.findByQueryObj(TestEntityStatDto.class, new TestEntityStatDto());

        Assert.isTrue(dtoList.size() > 0, "TestEntity统计结果错误");

    }

    @Test
    public void testOrderBy() {

        String sql = jpaDao.selectFrom(User.class).appendByQueryObj(new OrderByExam()).genFinalStatement();


        Assert.isTrue(sql.contains(E_User.createTime));
        Assert.isTrue(sql.contains(E_User.area));

        System.out.println(sql);
    }


    @Test
    public void testNullOrEq() {

        Date paramValue = new Date();

        long cnt = jpaDao.updateTo(User.class).set(E_User.lastUpdateTime, paramValue)
                .disableSafeMode()
                .update();

        long nullCnt = jpaDao.updateTo(User.class).set(E_User.lastUpdateTime, null)
                .gt(E_User.id, 20)
                .update();


        long tn = jpaDao.selectFrom(User.class)
                .isNullOrEq(E_User.lastUpdateTime, paramValue)
                .count();

        Assert.isTrue(tn == cnt);

    }

    @Test
    public void testUserDao() {

        PagingQueryReq paging = new PagingQueryReq();

        paging.setPageSize(10);

        List<User> users = userDao.find(null, "User", 5, paging);

        //    paging.setPageSize(1);

        User user = userDao.findOne(null, null, null, paging);

        int update = userDao.delete(null, "SSSS");

        Assert.isTrue(update == 0, "更新条数错误");

        update = userDao.update(user.getId(), "User-12");

        Assert.isTrue(update == 1, "更新条数错误");
    }


    @Test
    public void testGetIdAttr() {

        /// JpaDaoImpl jpaDao = new JpaDaoImpl();

        String attrName = jpaDao.getEntityIdAttrName(User.class);

        Assert.isTrue(E_User.id.equals(attrName));

        Long id = 1234567L;

        Object entityId = jpaDao.getEntityId(new Group(id, "test"));

        Assert.isTrue(entityId.equals(id));

    }


    @Test
    //@Transactional
    public void testJoinFetch2() {


        ;
        List<UserJoinFetchDTO> byQueryObj = jpaDao.findByQueryObj(UserJoinFetchDTO.class, new UserJoinFetchDTO());

        Object user = byQueryObj.get(0);

        System.out.println(byQueryObj);
    }

    @Test
    //@Transactional
    public void testGroupJoinFetch() {


        List<GroupJoinFetchDTO> byQueryObj = jpaDao.findByQueryObj(GroupJoinFetchDTO.class, new GroupJoinFetchDTO());

        Object user = byQueryObj.get(0);

        System.out.println(byQueryObj);
    }

    @Test
    //@Transactional
    public void testSelectGroupDto() {

        List<GroupSelectDTO> byQueryObj = jpaDao.findByQueryObj(GroupSelectDTO.class, new GroupSelectDTO());

        Object user = byQueryObj.get(0);

        System.out.println(byQueryObj);
    }

    @Test
    public void testNativeQuery() {


        Group entity = new Group();
        entity.setName("adfsdafas");

        jpaDao.save(entity);

        List r = jpaDao.find(true, Group.class, 1, 100
                , "select * from jpa_dao_test_Group where 1 = ? and 2 = ? and '3'=:name"
                , 1, 2, MapUtils.asMap("name", "3"));


        List<Group> groups = jpaDao
                .selectFrom(Group.class, "t")
                //   .select("*")
                //  .select("id")
                //    .appendWhere("count(distinct o)")

                .eq(E_Group.T_category, "adfsdafas")
                .eq(E_Group.T_name, "adfsdafas")
                .find(e -> {
                    // jpaDao.getEntityManager().detach(e);
                    return (Group) e;
                });

        //   System.out.println(r);

        System.out.println(groups);

    }


    @org.junit.Test
    public void testSave() throws Exception {


        User user = jpaDao.selectFrom(User.class).findOne();

        Long uid = user.getId();

        user = jpaDao.find(User.class, uid);

        String description = "Update_" + new Date();

        user.setDescription(description);

        jpaDao.save(user);


        user = jpaDao.find(User.class, uid);


        Assert.isTrue(user.getDescription().equals(description));

    }

    @org.junit.Test
    public void testDelete() throws Exception {

        Task one = jpaDao.selectFrom(Task.class).findOne();

        jpaDao.delete(one);


        one = jpaDao.find(Task.class, one.getId());

        Assert.isNull(one);

    }

    @org.junit.Test
    public void testFindAndConvert() throws Exception {

        jpaDao.selectFrom(User.class, "u")
                .joinFetch(E_User.group)
                .gt(E_User.id, "100")
                .isNotNull(E_User.name)
                .find((User u) -> u.getGroup())
                .stream()
                .map(g -> (jpaDao.copyProperties(g, new Group(), 2)))
                .forEach(System.out::println)
//                .findFirst()
//                .ifPresent(System.out::println)
        ;

        System.out.println("ss");

    }

    @org.junit.Test
    public void testGetEntityId() throws Exception {

        Group group = new Group(15L, "test");

        Object entityId = jpaDao.getEntityId(group);

        Assert.isTrue(entityId.equals(15L));

    }

    @org.junit.Test
    public void testEnv() throws Exception {


        DaoContext.setGlobalVar("DATE_FORMAT", "YYYY/MM/DD");

        DaoContext.setThreadVar("orgId", 5L);


    }


    @org.junit.Test
    public void testUpdateDTO() throws Exception {

        UpdateDao<User> userUpdateDao = jpaDao.updateTo(User.class);

        userUpdateDao
                .set(E_User.name, "name1")
                .eq(E_User.enable, false)
                .update();
    }


    @org.junit.Test
    public void testStatDTO() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");

        selectDao
                .limit(1, 10)
                .appendByQueryObj(new UserStatDTO());

        System.out.println(selectDao.genFinalStatement() + "  -->   params:" + selectDao.genFinalParamList());
    }


    @org.junit.Test
    public void testPagingQueryHelper() throws Exception {

        int n = 0;
        while (n++ < 20) {

            long st = System.currentTimeMillis();

            PagingData<TableJoinDTO> resp = PagingQueryHelper.findByPageOption(jpaDao,
                    new PagingData<TableJoinDTO>(), new TableJoinDTO().setRequireTotals(true));


            System.out.println(n + " response takes " + (System.currentTimeMillis() - st) + " , totals" + resp.getTotals());

        }

    }

    @org.junit.Test
    public void testPagingQueryHelper2() throws Exception {

        PagingData<TableJoin3> resp = PagingQueryHelper.findByPageOption(jpaDao,
                PagingData.class, new TableJoin3().setRequireTotals(true));

        System.out.println(resp.getTotals());
    }


    @org.junit.Test
    public void testStatDTO2() throws Exception {

        List<UserStatDTO> byQueryObj = jpaDao.findByQueryObj(UserStatDTO.class, new UserStatDTO());

        System.out.println(byQueryObj);
    }

    @org.junit.Test
    public void testQueryFrom() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");

        selectDao
                .limit(1, 10)
                .appendByQueryObj(new UserDTO())
                // .and().or().end()
                .where("222 != :orderCode")
//                .appendWhere("3333 < :lastUpdateTime")
                .find();


//        System.out.println(selectDao.genFinalStatement());
//
//        System.out.println(selectDao.genFinalParamList());


        //   System.out.println(entities);

    }


    @org.junit.Test
    public void testEnvQueryFrom() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");

        DaoContext.setGlobalVar("env.g.P1", "全局参数1");

        DaoContext.setGlobalVar("id", "默认全局id");

        DaoContext.setThreadVar("env.thread.P1", "线程参数1");

        DaoContext.setThreadVar("id", "默认线程Id");


        HashMap<String, Object> context = new HashMap<>();

        context.put("env.jpaDao.P1", "Dao参数1");

        selectDao
                .limit(1, 10)
                .setContext(context)
                .appendByQueryObj(new UserDTO2())
                .find();

        System.out.println("ok");

    }


    @org.junit.Test
    public void testNativeSelect() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom("jpa_dao_test_User");

        List entities = selectDao
                .limit(1, 10)
                //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
//               .appendSelectColumns("id , ( name || 'ddddd' ) AS name ")
//               .appendSelectColumns(" score AS scoreGt")
                .appendByQueryObj(new UserDTO3())
                .where("score > :maxScore", MapUtils.put("maxScore", 500L).build())
                .gt(E_User.T_score, 300)
                .find(UserDTO3.class);

        System.out.println("testSelectFrom:" + entities);

    }


    @org.junit.Test
    public void testSelect() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class);


        List entities = selectDao
                .limit(1, 10)
                //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
//               .appendSelectColumns("id , ( name || 'ddddd' ) AS name ")
//               .appendSelectColumns(" score AS scoreGt")
                .appendByQueryObj(new UserDTO3())
                .where("score > :maxScore", MapUtils.put("maxScore", 500).build())
                .gt(E_User.T_score, 300)
                .find(UserDTO3.class);

        System.out.println("testSelectFrom:" + entities);

    }


    @org.junit.Test
    public void testStat() throws Exception {

        Object groupSelectDao = jpaDao.selectFrom(Group.class).appendByQueryObj(new CommDto()).find(CommDto.class);

        List<GroupStatDTO> objects = jpaDao.findByQueryObj(GroupStatDTO.class, new GroupStatDTO());

        System.out.println(objects);

    }


    @org.junit.Test
    public void testDeleteById() throws Exception {

        jpaDao.deleteById(TestEntity.class, 1L);

    }


    @org.junit.Test
    public void testJoinDto() throws Exception {


        List<MulitTableJoinDTO> objects = jpaDao.findByQueryObj(MulitTableJoinDTO.class, new MulitTableJoinDTO());


        org.junit.Assert.assertNotNull(objects);

    }


    @org.junit.Test
    public void testJoinDto2() throws Exception {


        List<TableJoinDTO> objects = jpaDao.findByQueryObj(new TableJoinDTO());


        org.junit.Assert.assertNotNull(objects);

    }

    @org.junit.Test
    public void testTableJoinStatDTO() throws Exception {


        List<TableJoinStatDTO> objects = jpaDao.findByQueryObj(new TableJoinStatDTO(), new PagingQueryReq(1, 10));
//        List<TableJoinStatDTO> objects = jpaDao.findByQueryObj(new TableJoinStatDTO() );

        String aa = "Select Count( 1 ) , Sum( u.score ) , Avg( u.score ) AS avg , g.name  From com.levin.commons.dao.domain.User u  Left join com.levin.commons.dao.domain.Group g on u.group = g.id     Group By  g.name Having  Count( 1 ) >   ?1  AND Avg( u.score ) >   ?2  Order By  Count( 1 ) Desc , avg Desc , g.name Desc";

        org.junit.Assert.assertNotNull(objects);

    }


    @org.junit.Test
    public void testCListAnno() throws Exception {


        List<TestEntity> objects = jpaDao.findByQueryObj(TestEntity.class, new TestEntityDto());

        Assert.notNull(objects, "");

    }


    @org.junit.Test
    public void testJoin() throws Exception {


        List<MulitTableJoinDTO> objects = jpaDao.selectFrom(User.class, "u")
                .join("left join jpa_dao_test_Group g on u.group.id = g.id")
                .appendByQueryObj(new MulitTableJoinDTO())

                .where("u.id > :mapParam1", MapUtils.put("mapParam1", "2").build())

                .gt("u.id", "1")
                .find(MulitTableJoinDTO.class);

        org.junit.Assert.assertNotNull(objects);
    }


    @org.junit.Test
    public void testJoin2() throws Exception {


        List<MulitTableJoinDTO> objects = jpaDao.selectFrom(false, "jpa_dao_test_User u")
                .join("left join jpa_dao_test_Group g on u.group.id = g.id")
                .select("u.id AS uid ,g.id AS gid")

                .where("g.id > " + jpaDao.getParamPlaceholder(false), 2L)

                .limit(-1, 100)
                .find(MulitTableJoinDTO.class);


        org.junit.Assert.assertNotNull(objects);
    }


    @org.junit.Test
    public void testJoin3() throws Exception {


        List<MulitTableJoinDTO> objects = jpaDao.selectFrom(false, "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group.id = g.id")
                .appendByQueryObj(new MulitTableJoinDTO())
                .where("u.id > :mapParam1", MapUtils.put("mapParam1", "2").build())
                .find(MulitTableJoinDTO.class);


        org.junit.Assert.assertNotNull(objects);
    }

    @org.junit.Test
    public void testSelectFrom() throws Exception {

        long millis = System.currentTimeMillis();

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");

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

    @org.junit.Test
    public void testSelectTime() throws Exception {

        long millis = System.currentTimeMillis();

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");


        jpaDao.selectFrom(User.class, "u")
                .appendByQueryObj(new GroupStatDTO())
                .genFinalStatement();


        millis = System.currentTimeMillis() - millis;

        System.out.println("1 testSelectTime:" + millis);


        millis = System.currentTimeMillis();

        jpaDao.selectFrom(User.class, "u")
                .appendByQueryObj(new TestEntityStatDto())
                .genFinalStatement();


        millis = System.currentTimeMillis() - millis;

        System.out.println("2 testSelectTime:" + millis);


        millis = System.currentTimeMillis();

        String ql = jpaDao.selectFrom(User.class, "u")
                .appendByQueryObj(new SubQueryDTO())
                .genFinalStatement();


        millis = System.currentTimeMillis() - millis;

        System.out.println("3 testSelectTime:" + millis);


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

    @org.junit.Test
    public void testUpdateFrom() throws Exception {

        UpdateDao<User> updateDao = jpaDao.updateTo(User.class, "u");

        int update = updateDao
                //对象不为null的属性做为查询条件
                .appendByQueryObj(new UserUpdateDTO())
                .update();

        System.out.println(update);


        int n = jpaDao.updateTo(Group.class)
                .set(E_Group.lastUpdateTime, new Date())
//                .appendColumn(E_Group.description, "" + System.currentTimeMillis())
                .contains(E_Group.name, "2")
                .update();

        System.out.println("Group update:" + n);


        n = jpaDao.updateTo(User.class)
                .set(E_User.lastUpdateTime, new Date())
//                .appendColumn(E_User.description, "" + System.currentTimeMillis())
                .contains(E_User.name, "2")
                .update();


        System.out.println("Group E_User:" + n);
    }


    @org.junit.Test
    public void testQueryFrom2() throws Exception {

        List<Object> list = jpaDao
                .selectFrom(Group.class)
                .contains(E_Group.name, "2")
                .find();

        System.out.println(list);


        jpaDao.selectFrom("table").and().or().and().end().end().end();

    }


    @org.junit.Test
    public void testDeleteFrom() throws Exception {

        int r = jpaDao.deleteFrom(User.class, "u")
                //  .appendWhere("name like ?", "%0%")
                //   .appendWhereEquals("name", "10")
                //   .appendWhere(" orderCode > ?", 10)
                .appendByQueryObj(new UserDTO())
                //  .appendWhereByQueryObj(c)
//                .appendWhereByEL("Q_", elMap)
                .delete();

        //以上查询会生成条件，包括map对应的查询条件

        System.out.println(r);
    }

    @org.junit.Test
    public void testMapFrom() throws Exception {

        Map elMap = new LinkedHashMap();

        elMap.put("Q_Between_id", " 12 , 34");
        elMap.put("Q_Not_In_id", " 12,34, 534,546, 456");
        elMap.put("Q_NotIn_name", "12,34,534,546,456");
        elMap.put("Q_Gt_createTime", "2012/01/30 23:59:00");
        elMap.put("Q_Not_parentId", 90);

        int r = jpaDao.deleteFrom(Group.class, "e")
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

}