package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.Avg;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.stat.Sum;

//import com.levin.commons.dao.annotation.Not;
//import com.levin.commons.dao.annotation.stat.Having;


/**
 * 数据传输对象(兼查询对象，通过注解产生SQL语句)
 */
@TargetOption(alias = "u", fromStatement = "jpa_dao_test_User u")
public class UserStatDTO {

    @GroupBy(havingOp = Op.Eq)
    @OrderBy
    String state;

    @Avg(havingOp = Op.Gt)
    @Sum(fieldFuncs = {@Func("ABS")}, havingOp = Op.Lt)
    @Gt
    Integer score = 500;


    //    @Having
    @Ignore
    Boolean enable = true;

    //    @Not
//    @Having
    @StartsWith
    @EndsWith
    @Eq
    @Select(havingOp = Op.Like)
    String name = "Echo";

}
