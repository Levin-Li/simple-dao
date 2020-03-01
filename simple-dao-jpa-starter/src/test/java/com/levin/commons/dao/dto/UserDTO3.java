package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Like;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.SelectColumn;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.DefaultPaging;
import lombok.Data;


@Data
@TargetOption(entityClass = User.class, alias = "u", maxResults = 100)
public class UserDTO3 {


    Paging paging = new DefaultPaging(1, 20);

    @SelectColumn
    @Gt
    Long id = 0L;

    @OrderBy
    @SelectColumn
    @Like
    String name = "User";


    @SelectColumn("score")
    @Gt
    Integer scoreGt = 0;


}
