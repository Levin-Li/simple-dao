package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Like;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;
import lombok.Data;


@Data
@TargetOption(entityClass = User.class, alias = "u", maxResults = 100)
public class UserDTO3 {


    Paging paging = new PagingQueryReq(1, 20);

    @Select
    @Gt
    Long id = 0L;

    @OrderBy
    @Select
    @Like
    String name = "User";


    @Select("score")
    @Gt
    Integer gtScore = 0;


}
