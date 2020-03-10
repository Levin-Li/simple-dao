package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
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

    @Avg(havingOp = Op.Gt)
    Integer score = 500;

    @Contains
    String name = "Echo";

}
