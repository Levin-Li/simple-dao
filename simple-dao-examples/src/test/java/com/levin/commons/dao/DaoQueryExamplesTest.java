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
 * DAO 查询、统计和关联能力的端到端示例。
 */
class DaoQueryExamplesTest extends DaoExamplesTestSupport {
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

}
