/**
 * JPA简单 DAO封装包
 * <p/>
 * Created by echo on 2015/11/17.
 * <p/>
 * <p/>
 * 主要的使用入口类：com.levin.commons.dao.JpaDao
 * <p/>
 * 典型使用方式:
 * 1、通过spring 扫描并注入Context @Repository("com.levin.commons.dao.support.JpaDaoImpl")
 * <p/>
 * 2、在使用的地方通过  @Autowired JpaDao dao; 引入JpaDao实例
 * <p>
 * <p>
 * jdk8 编译参数：-parameters
 */

package com.levin.commons.dao;

/**
 * 实现groupby 注解 支持5个标量语法
 * Count	统计数量，参数是要统计的字段名（可选）
 * Max	获取最大值，参数是要统计的字段名（必须）
 * Min	获取最小值，参数是要统计的字段名（必须）
 * Avg	获取平均值，参数是要统计的字段名（必须）
 * Sum	获取总分，参数是要统计的字段名（必须）
 * 2、创建通用 Service 支持缓存key处理
 * <p/>
 * 3、DTO 对象支持 缓存key定义，可以定义GroupBy字段，可以定义标量定义
 * <p/>
 * 4、优化appendWhere
 * <p/>
 * 5、支持if条件，支持表达式，el表达式
 * <p/>
 * 6、支持逻辑分组
 *
 * @todo 实现jsr数据校验
 * @todo 实现jsr数据校验
 **/


/**

 @todo 实现jsr数据校验



 **/