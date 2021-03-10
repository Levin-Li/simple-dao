package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Task;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;
import com.levin.commons.utils.MapUtils;

import java.util.Date;
import java.util.Map;


@TargetOption(entityClass = User.class, alias = E_User.ALIAS, resultClass = SimpleSubQueryDTO.class)
public class SimpleSubQueryDTO {

    Paging paging = new PagingQueryReq(1, 20);


    @Gt(value = "(select count(*) from " + E_Task.CLASS_NAME + "   where " + E_Task.user + " = u.id)")
    @Select(value = "select count(*) from " + E_Task.CLASS_NAME + "   where " + E_Task.user + " = u.id")
    int taskCnt = 1;

    // 以上字段生成语句： (select count(*) from com.levin.commons.dao.domain.Task   where user = u.id) AS taskCnt


    @Ignore
    Integer taskSum;

    @Select(value = "select ${fun}(score) from " + E_Task.CLASS_NAME + "   where  " + E_Task.user + " = u.id and ${p2} != ${:p1}", alias = "taskSum")
    Map<String, Object> params = MapUtils.put("p1",(Object) "1").put("p2",2).put("fun","sum").build();
    //以上字段生成语句： (select sum(score) from com.levin.commons.dao.domain.Task   where  user = u.id and 2 !=  ?1 ) AS taskSum


    //总的语句
    //Select  (select count(*) from com.levin.commons.dao.domain.Task   where user = u.id) AS taskCnt  ,  (select sum(score) from com.levin.commons.dao.domain.Task   where  user = u.id and 2 !=  ?1 ) AS taskSum   From com.levin.commons.dao.domain.User u   Where (select count(*) from com.levin.commons.dao.domain.Task   where user = u.id) >   ?2

}
