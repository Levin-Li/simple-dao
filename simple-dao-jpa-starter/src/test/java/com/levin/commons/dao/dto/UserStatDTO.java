package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
//import com.levin.commons.dao.annotation.Not;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.stat.Avg;
import com.levin.commons.dao.annotation.stat.GroupBy;
//import com.levin.commons.dao.annotation.stat.Having;
import com.levin.commons.dao.annotation.stat.Sum;


/**
 * 数据传输对象(兼查询对象，通过注解产生SQL语句)
 */
@TargetOption(alias = "u")
public class UserStatDTO {

    @GroupBy(having = true)
    @OrderBy
    String area="fuzhou";

    @Avg(having = true)
    @Sum(fieldFuncs = {@Func("ABS")})
//    @Having
    @Gt
    Integer score = 500;

//    @Having
    @Ignore
    Boolean enable = true;

    @Like
    String state = "Y";

//    @Not
//    @Having
    @Like
    @Eq
    @OR
    @END
    String name = "Echo";

}


/**
 * 产生语句：
 * <p/>
 * <p/>
 * SELECT
 * u.area,
 * AVG(u.score),
 * SUM(u.score)
 * FROM
 * t_users u
 * WHERE
 * u. ENABLE = TRUE
 * GROUP BY
 * u.area
 * HAVING
 * AVG(u.score) > ?
 * AND u. ENABLE = ?
 * ORDER BY
 * u.area
 */