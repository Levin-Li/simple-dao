package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.stat.Avg;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.stat.Sum;
import com.levin.commons.dao.domain.User;
import lombok.Data;

//import com.levin.commons.dao.annotation.Not;
//import com.levin.commons.dao.annotation.stat.Having;


/**
 * 数据传输对象(兼查询对象，通过注解产生SQL语句)
 */
@TargetOption(entityClass = User.class,alias = "u")
@Data
public class UserStatDTO {

    @GroupBy
    @OrderBy
    String state;

    @Sum(havingOp = Op.Gt,domain = C.BLANK_VALUE)
    Integer score = 500;

    @Contains
    String name = "Echo";

}
