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

@ActiveProfiles("dev")
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TestConfiguration.class}, properties = {
        "spring.jpa.properties.hibernate.query.hql.json_functions_enabled=true",
        // DAO 集成测试不需要 MVC/Jackson 扩展；service-support 的该扩展与当前 Jackson 运行时独立演进。
        "com.levin.commons.service.support.DefaultSpringMvcEnumFormatterConfiguration.enabled=false",
        "com.levin.commons.service.support.DefaultSpringMvcJsonDeserializerConfiguration.enabled=false"
})
//@Transactional
abstract class DaoExamplesTestSupport {

    protected static final class Assert {

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
