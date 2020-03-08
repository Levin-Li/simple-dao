package com.levin.commons.dao.support;

import com.levin.commons.dao.*;
import com.levin.commons.dao.domain.*;
import com.levin.commons.dao.domain.support.E_AbstractNamedEntityObject;
import com.levin.commons.dao.domain.support.TestEntity;
import com.levin.commons.dao.dto.*;
import com.levin.commons.dao.proxy.UserApi;
import com.levin.commons.dao.repository.SimpleDao;
import com.levin.commons.dao.service.GroupDao;
import com.levin.commons.dao.service.UserDao;
import com.levin.commons.utils.MapUtils;
import org.dao.test.repository.Group2Dao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * Created by echo on 2015/11/17.
 */

@ActiveProfiles("dev")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TestConfiguration.class})
@Transactional
public class JpaDaoImplTest {

    @Autowired
    JpaDao jpaDao;

    @Autowired(required = false)
    SimpleDao simpleDao;

    @Autowired
    UserDao userDao;

    @Autowired
    GroupDao groupDao;

    @Autowired
    Group2Dao group2Dao;

    @Autowired
    UserApi userApi;

    public enum AppType {
        Android, Ios, Weixin, Web, H5
    }


    /**
     * 注意测试时，使用的是h2的内存数据库，所以没有使用事务
     *
     * @throws Exception
     */

    @Before
    public void injectCheck() throws Exception {
        Assert.notNull(jpaDao, "通用DAO没有注入");
        Assert.notNull(simpleDao, "simpleDao没有注入");
        Assert.notNull(userDao, "userDao没有注入");
        Assert.notNull(groupDao, "groupDao没有注入");
    }


    @Before
    public void testGetEntityManager() throws Exception {
        EntityManager entityManager = jpaDao.getEntityManager();
        Assert.notNull(entityManager);
    }


    @Before
    public void initTestEntity() throws Exception {

        List<TestEntity> e = jpaDao.selectFrom(TestEntity.class, "e").find(TestEntity.class);



    }


    @Before
    public void initTestData() throws Exception {

        groupDao.update(1L,"name-1");

        long cnt = jpaDao.selectFrom(User.class).count();

        if (cnt > 0) {
            return;
        }

        int count = 15;

        String[] states = {"正常", "已取消", "审请中", "已删除", "已冻结"};

        String[] types = {"虚拟组织", "部门", "小组", "协会"};

        String[] categories = {"临时", "常设", "月度", "年度"};

        Random random = new Random(this.hashCode());

        Object one = jpaDao.selectFrom(Group.class).appendSelectColumns("max(id)").findOne();

        long n = (one == null) ? 1 : (long) one;

        Long parentId = null;


        while (count-- > 0) {

            //  n++;

            Group group = new Group("Group-" + n++, parentId);

//            group.setId((long) n);

            group.setState(states[Math.abs(random.nextInt()) % states.length]);
            group.setCategory(categories[Math.abs(random.nextInt()) % categories.length]);
//            group.setType(types[Math.abs(random.nextInt()) % types.length]);

            group.setScore(Math.abs(random.nextInt()));

            jpaDao.create(group);

            long uCount = 3 * count;

            while (uCount-- > 0) {
                User user = new User();
                user.setName("User-" + group.getId() + "-" + uCount);

//                  user.setId((long) uCount);

                user.setState(states[Math.abs(random.nextInt()) % states.length]);
                user.setScore(Math.abs(random.nextInt()));
                user.setGroup(group);
                jpaDao.create(user);
            }

            parentId = group.getId();
        }


    }

    @Test
    public void testProxyBean() {

        System.out.printf(userApi.getEMF());


    }

    @Test
    public void testGroupDao() {

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
            groupDao.findOneAndRepeatGetResult(null, "Group", null, new DefaultPaging(1, 10));
            throw new RuntimeException("重复获取结果方法没有抛出异常");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testSimpleDao() {


        try {
            User user = simpleDao.findOne(new UserDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<User> users = simpleDao.find(new UserDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            int update = simpleDao.update(new UserUpdateDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int delete = simpleDao.delete(new UserDTO());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testNullOrEq() {

        Object lily = jpaDao.selectFrom(User.class).isNullOrEq(E_User.name, "lily").findOne();

    }

    @Test
    public void testUserDao() {

        DefaultPaging paging = new DefaultPaging();
        paging.setPageSize(10);

        List<User> users = userDao.find(null, "User", 5, paging);

        paging.setPageSize(1);

        User user = userDao.findOne(null, "User", null, paging);


        int update = userDao.delete(null, "SSSS");

        Assert.isTrue(update == 0, "更新条数错误");

        update = userDao.update(12L, "User-12");

        Assert.isTrue(update == 1, "更新条数错误");
    }


    @Test
    public void testGetIdAttr() {

        /// JpaDaoImpl jpaDao = new JpaDaoImpl();

        String attrName = jpaDao.getEntityIdAttrName(User.class);

        Assert.isTrue("id".equals(attrName));

        Object entityId = jpaDao.getEntityId(new Group(1234567L, "test"));

        Assert.isTrue(entityId != null);

    }


    @Test
    //@Transactional
    public void testManyToOne() {

        B b = (B) jpaDao.save(new B());
        B b1 = (B) jpaDao.save(new B());
        B b2 = (B) jpaDao.save(new B());

        jpaDao.save(new A().setBid(b.getId()));
        jpaDao.save(new A().setBid(b1.getId()));
        jpaDao.save(new A().setBid(b2.getId()));


        List r = jpaDao.selectFrom(B.class).find();

        System.out.println(r);

    }


    @Test
    public void testNativeQuery() {


        Group entity = new Group();
        entity.setName("adfsdafas");

        jpaDao.save(entity);

        List r = jpaDao.find(true, Group.class, 1, 100
                , "select * from jpa_dao_test_Group where 1= ? and 2=? and '3'=:name"
                , 1, 2, MapUtils.asMap("name", "3"));


        List<Group> groups = jpaDao
                .selectFrom("jpa_dao_test_Group", "t")
                //   .select("*")
                .select("id")
                //    .appendWhere("count(distinct o)")

                .eq(E_Group.T_category, "adfsdafas")
                .eq(E_Group.T_name, "adfsdafas")
                .findOne();


        System.out.println(r);

    }


    @org.junit.Test
    public void testSave() throws Exception {

    }

    @org.junit.Test
    public void testDelete() throws Exception {
        jpaDao.delete(jpaDao.selectFrom(User.class).findOne());
    }

    @org.junit.Test
    public void testFindAndConvert() throws Exception {

        jpaDao.selectFrom(User.class, "u")
                .appendJoinFetchSet(true, E_User.group)
                .gt(E_User.id, "5")
                .isNotNull(E_User.name)
                .find((User u) -> u.getGroup())
                .stream()
                .forEach(g -> System.out.println(g));

    }

    @org.junit.Test
    public void testGetEntityId() throws Exception {

        Group group = new Group(15L, "test");

        Object entityId = jpaDao.getEntityId(group);

        Assert.isTrue(entityId.equals(15L));

    }


    @org.junit.Test
    public void testUpdateDTO() throws Exception {

        UpdateDao<User> userUpdateDao = jpaDao.updateTo(User.class);


        userUpdateDao
                .appendColumn(E_User.name, "name1")
                .eq(E_User.enable, false)
                .update();
    }


    @org.junit.Test
    public void testStatDTO() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");

        selectDao
                .limit(1, 10)
                .gt("area", AppType.Ios)
//                .in("area", AppType.Ios, AppType.Android)
                .appendByQueryObj(new UserStatDTO());

        System.out.println(selectDao.genFinalStatement() + "  -->   params:" + selectDao.genFinalParamList());
    }

    @org.junit.Test
    public void testQueryFrom() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");

        selectDao
                .limit(1, 10)
                .appendByQueryObj(new UserDTO())
                // .and().or().end()
                .appendWhere("222 != :orderCode")
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
                .appendWhere("score > :maxScore", MapUtils.put("maxScore", 500L).build())
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
                .appendWhere("score > :maxScore", MapUtils.put("maxScore", 500).build())
                .gt(E_User.T_score, 300)
                .find(UserDTO3.class);

        System.out.println("testSelectFrom:" + entities);

    }


    @org.junit.Test
    public void testStat() throws Exception {


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
    public void testJoin() throws Exception {


        List<MulitTableJoinDTO> objects = jpaDao.selectFrom(User.class, "u")
                .join("left join jpa_dao_test_Group g on u.group.id = g.id")
                .appendByQueryObj(new MulitTableJoinDTO())

                .appendWhere("u.id > :mapParam1", MapUtils.put("mapParam1", "2").build())

                .gt("u.id", "1")
                .find(MulitTableJoinDTO.class);

        org.junit.Assert.assertNotNull(objects);
    }


    @org.junit.Test
    public void testJoin2() throws Exception {


        List<MulitTableJoinDTO> objects = jpaDao.selectFrom(false, "jpa_dao_test_User u")
                .join("left join jpa_dao_test_Group g on u.group.id = g.id")
                .select("u.id AS uid ,g.id AS gid")

                .where("g.id > " + jpaDao.getParamPlaceholder(false), 2)

                .limit(-1, 100)
                .find(MulitTableJoinDTO.class);


        org.junit.Assert.assertNotNull(objects);
    }


    @org.junit.Test
    public void testJoin3() throws Exception {


        List<MulitTableJoinDTO> objects = jpaDao.selectFrom(false, "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group.id = g.id")
                .appendByQueryObj(new MulitTableJoinDTO())
                .appendWhere("u.id > :mapParam1", MapUtils.put("mapParam1", "2").build())
                .find(MulitTableJoinDTO.class);


        org.junit.Assert.assertNotNull(objects);
    }

    @org.junit.Test
    public void testSelectFrom() throws Exception {

        SelectDao<User> selectDao = jpaDao.selectFrom(User.class, "u");

        List entities = selectDao
                .limit(1, 10)
                //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
                .appendByQueryObj(new UserSelectDTO().setNamedParams(MapUtils.asMap("minScore", 224,"groupName","'group'")))
                //  .appendWhereEquals("", "")
                .find();

        System.out.println("testSelectFrom:" + entities);

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
                .appendColumn(E_Group.lastUpdateTime, new Date())
//                .appendColumn(E_Group.description, "" + System.currentTimeMillis())
                .contains(E_Group.name, "2").update();

        System.out.println("Group update:" + n);


        n = jpaDao.updateTo(User.class)
                .appendColumn(E_User.lastUpdateTime, new Date())
//                .appendColumn(E_User.description, "" + System.currentTimeMillis())
                .contains(E_User.name, "2")
                .update();


        System.out.println("Group E_User:" + n);
    }


    @org.junit.Test
    public void testQeuryFrom() throws Exception {

        List<Object> list = jpaDao.selectFrom(Group.class)
                .contains(E_Group.name, "2").find();

        System.out.println(list);

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

        elMap.put("Q_Between_id", "12,34");
        elMap.put("Q_Not_In_id", "12,34,534,546,456");
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