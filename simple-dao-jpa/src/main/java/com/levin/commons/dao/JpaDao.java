
package com.levin.commons.dao;

import javax.persistence.EntityManager;

/**
 * JPA使用支持接口，提供了对jpa实体管理器的简单功能
 * <p/>
 * 最大的特点是对查询或是更新参数的支持，查询参数可以是数组,或是Map，或是List，或是具体的参数值，当是数组或是List时会对参数进行递归处理
 * 是Map时，会当成命名参数进行处理，当Map中的key是Int时，会当成位置参数来使用
 */

/**
 * 使用案例
 * public class JpaDaoImplTest {
 * JpaDao dao;
 * <p/>
 * <br/><br/>
 * public void testGetEntityManager() throws Exception {
 * Assert.notNull(dao.getEntityManager());
 * }
 * <br/><br/>
 * public void testCreate() throws Exception {
 * <p/>
 * final int count = 100;
 * <p/>
 * int n = 0;
 * <p/>
 * Long parentId = null;
 * <p/>
 * while (n++ < count) {
 * TestEntity entity = new TestEntity("Name-" + n, parentId);
 * dao.create(entity);
 * parentId = entity.getId();
 * }
 * <p/>
 * } <br/><br/>
 * public void testSave() throws Exception {
 * <p/>
 * dao.save(new TestEntity("testSave insert---"));
 * <p/>
 * dao.save(new TestEntity(9999l, "testSave update---"));
 * <p/>
 * dao.create(new TestEntity(8888l, "testSave update---"));
 * <p/>
 * } <br/><br/>
 * public void testDelete() throws Exception {
 * dao.delete(new TestEntity(9999l, "testDelete"));
 * }
 * public void testFind() throws Exception {
 * <p/>
 * List<Object> objectList = dao.find("From " + TestEntity.class.getName() + " where name like ? or name like ?", "%1%", "%0%");
 * <p/>
 * System.out.println(objectList);
 * <p/>
 * } <br/><br/>
 * public void testGetEntityId() throws Exception {
 * <p/>
 * Object entityId = dao.getEntityId(new TestEntity(1234l, "test"));
 * <p/>
 * Assert.isTrue(entityId.equals(1234l));
 * <p/>
 * dao.save(new TestEntity(1l, "test"));
 * <p/>
 * //从数据查找<br/><br/>
 * entityId = dao.getEntityId(dao.find(TestEntity.class, 1l));
 * <p/>
 * Assert.isTrue(entityId.equals(1l));
 * <p/>
 * } <br/><br/>
 * public void testGetEntityIdName() throws Exception {
 * <p/>
 * String entityIdAttrName = dao.getEntityIdAttrName(new TestEntity());
 * <p/>
 * Assert.isTrue("id".equals(entityIdAttrName));
 * <p/>
 * } <br/><br/>
 * public void testSelectFrom2() throws Exception {
 * <p/>
 * SelectDao<Object> selectDao = dao.newQueryDao();
 * <p/>
 * HashMap<Object, Object> map = new HashMap<Object, Object>();
 * <p/>
 * //命名参数<br/><br/>
 * map.put("likeName", "%5%");
 * map.put("test", 1);
 * <p/>
 * //同时支持定位参数<br/><br/>
 * map.put(2, 3);
 * map.put(1, 2);
 * <p/>
 * List entities = selectDao.select(" g ").from("  " + Group.class.getName() + " g left join fetch g.children ")
 * .range(1, 10)
 * //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
 * //   .appendWhere(new CD())
 * .find();
 * System.out.println(entities);
 * } <br/><br/>
 * <p/>
 * //查询<br/><br/>
 * public void testSelectFrom() throws Exception {
 * <p/>
 * SelectDao<TestEntity> selectDao = dao.selectFrom(TestEntity.class, "e");
 * <p/>
 * HashMap<Object, Object> map = new HashMap<Object, Object>();
 * <p/>
 * //命名参数
 * map.put("likeName", "%5%");
 * map.put("test", 1);
 * <p/>
 * //同时支持定位参数
 * map.put(2, 3);
 * map.put(1, 2);
 * <p/>
 * List entities = selectDao
 * .range(1, 10)
 * //.where(" 3=?2 and 1 = :test and 2 = ?1 AND e.name like :likeName", map)
 * .appendWhere(new CD())
 * .find();
 * <p/>
 * System.out.println(entities);
 * <p/>
 * } <br/><br/>
 * //更新测试<br/><br/>
 * public void testUpdateFrom() throws Exception {
 * <p/>
 * UpdateDao<TestEntity> updateDao = dao.updateTo(TestEntity.class);
 * <p/>
 * TestEntity testEntity = new TestEntity();
 * testEntity.setState("updateDao");
 * <p/>
 * int update = updateDao
 * .appendColumns(testEntity)
 * .appendColumns(new CD())
 * <p/>
 * //对象不为null的属性做为查询条件
 * .appendWhere(new CD())
 * .update();
 * <p/>
 * System.out.println(update);
 * } <br/><br/>
 * <p/>
 * //删除测试<br/><br/>
 * public void testDeleteFrom() throws Exception {
 * Map c = new HashMap();
 * <p/>
 * c.put("id", 10l);
 * c.put("parentId", 9l);
 * <p/>
 * int r = dao.deleteFrom(TestEntity.class, " e")
 * .appendWhereLike("name", "%0%")
 * .appendWhereEquals("name", "10")
 * .appendWhere(" orderCode > ?", 10)
 * .appendWhere(new CD())
 * .appendWhere(c)
 * .delete();
 * <p/>
 * //以上查询会生成条件，包括map对应的查询条件
 * <p/>
 * System.out.println(r);
 * } <br/><br/>
 * <p/>
 * <p/>
 * //值对象<br/><br/>
 * class CD {
 * <p/>
 * Integer orderCode = 1;
 * <p/>
 * String name = "daa";
 * <p/>
 * Long id = 2l;
 * Long parentId;
 * <p/>
 * public Long getId() {
 * return id;
 * }
 * <p/>
 * public void setId(Long id) {
 * this.id = id;
 * }
 * <p/>
 * public Long getParentId() {
 * return parentId;
 * }
 * <p/>
 * public void setParentId(Long parentId) {
 * this.parentId = parentId;
 * }
 * <p/>
 * public Integer getOrderCode() {
 * return orderCode;
 * }
 * <p/>
 * public void setOrderCode(Integer orderCode) {
 * this.orderCode = orderCode;
 * }
 * <p/>
 * public String getName() {
 * return name;
 * }
 * <p/>
 * public void setName(String name) {
 * this.name = name;
 * }
 * } <br/><br/>
 * } <br/><br/>
 */

public interface JpaDao extends SimpleDao {


    /**
     * 默认的占位符,挂号里面不能有空格
     */
    String DEFAULT_JPQL_PARAM_PLACEHOLDER = ":?";


    /**
     * 设置参数占位符
     *
     * @param paramPlaceholder
     * @return
     */
    JpaDao setParamPlaceholder(String paramPlaceholder);


    /**
     * 全局禁用 JPA 会话缓存
     *
     * @return
     */
    JpaDao disableSessionCache();


    /**
     * 获取jpa实体管理器
     *
     * @return
     */
    EntityManager getEntityManager();


    /**
     * 强制让对象脱管
     *
     * @param object
     */
    JpaDao detach(Object object);

}