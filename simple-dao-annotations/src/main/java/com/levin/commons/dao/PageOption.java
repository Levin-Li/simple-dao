package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 分页支持注解
 * <p>
 *
 * @author levin li
 * @since 2.2.10
 */

//只支持字段
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PageOption {

    enum Field {
        RecordTotals,//总记录总数
        PageCount,//页面总数
        PageSize,//排序字段
        PageIndex, //排序字段
        OrderBy, //排序字段
        QueryDto,//查询条件
        ResultSet, //查询结果集
    }

    /**
     * 字段类型
     *
     * @return
     */
    Field value();

    /**
     * 是否是必须的，如果条件不匹配，但又是必须的，将抛出异常
     *
     * @return
     */
//    boolean require() default false;

    /**
     * 注解生效条件
     *
     * @return
     */
    String condition() default "";

    /**
     * 备注
     *
     * @return
     */
    String remark() default "";

}
