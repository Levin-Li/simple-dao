package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * 注解在实体类上
 * <p>
 * 目前暂时没错实现校验
 *
 * @author llw
 * @version 2.0.0
 * @TODO 2019/5/25 目前暂时没错实现校验
 */
// TODO: 2019/5/25  目前暂时没错实现校验
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DaoSafeOption {

    /**
     * 别名
     * <p>
     * 针对单个查询目标时有效
     *
     * @return
     */
    String alias() default "";

    /**
     * 最大结果数
     *
     * @return
     */
    int maxResults() default -1;

    /**
     * 固定条件
     * 如 enable = true
     *
     * @return
     */
    String fixedCondition() default "";

    /**
     * 在安全模式下
     *
     * @return
     */
    String[] requireColumns() default {};

}
