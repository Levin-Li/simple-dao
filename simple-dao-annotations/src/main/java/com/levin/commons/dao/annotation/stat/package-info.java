/**
 * 分组统计注解包
 * <p/>
 * 只有标量注解和GroupBy注解才支持Having操作
 * <p/>
 * 一个字段可以有多个标量注解
 * <p/>
 * <p/>
 * 注意：Having 的所有条件目前只支持并且的关系
 * <p/>
 * <p/>
 * //注解示例
 * class UserStatDTO {
 *
 * @GroupBy(havingOp="LIKE") String name;
 * <p/>
 * //以下字段多个标题注解
 * @Sum(havingOp=" > ")
 * @Avg int num;
 * @Avg int age;
 * <p/>
 * ``
 * @Like String txt;
 * }
 * <p/>
 * #将生成以下语句:
 * #SELECT name,sum(num),avg(num),avg(age) FROM t_table WHERE txt like ? group by name HAVING name LIKE name AND sum(num) > ?
 * <p/>
 * #参数值：
 * #txt，num
 */
package com.levin.commons.dao.annotation.stat;