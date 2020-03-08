/**
 * 注解包说明
 * <p/>
 * Not注解和Having注解可以和其它标签一起组合使用
 * <p/>
 * <p/>
 * 默认格式：value + operator + prefix + expr + suffix
 * <p/>
 * <p/>
 * <p/>
 * 如果是复合对象，则递归处理
 * <p/>
 * 如果被注解字段或是参数是复杂对象（不是原子对象），则只有 @Ignore 和Logic注解 会被处理
 * <p/>
 * <p/>
 * <p/>
 * 注解处理关键流程
 * <p/>
 * 1、判定是否是 Ignore，如果是则停止处理
 * <p/>
 * 2、判定是否有逻辑注解，有就优先处理，并且无论是否异常，都检查是否有逻辑结束注解@END，有则表示结束当前逻辑组
 * <p/>
 * 3、判定当前逻辑组是否有效，如果无效，则忽略直到有效的逻辑分组
 * <p/>
 * 4、如果当前字段或是参数是复杂对象，不是原子对象，则忽略其它所有注解，进行递归处理
 * <p/>
 * <p/>
 * <p/>
 * 5、循环处理一个注解
 * 判定当前字段或是参数是否有效，就是注解的condition字段SpEL表达式返回值是否是true，如果是false则停止处理该注解
 * ConditionBuilderImpl对象会处理where相关的条件注解
 * UpdateDaoImpl 对象会处理增加处理UpdateColumn注解
 * SelectDaoImpl  对象会增加处理 统计，排序，选择，Having等注解
 * <p/>
 * 一个字段对应多个注解，会生产多次循环，如果有一个注解的processAttrAnno方法返回false，则表示该字段或参数的其它注解将不再处理
 * <p/>
 * 6、完成一个字段处理，进行下一个字段或参数进行处理
 */
package com.levin.commons.dao.annotation;