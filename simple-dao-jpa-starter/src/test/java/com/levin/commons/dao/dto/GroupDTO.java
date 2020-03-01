package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.Like;
import com.levin.commons.dao.annotation.Lt;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.update.UpdateColumn;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.support.DefaultPaging;

import java.util.Date;


@TargetOption(entityClass = Group.class,   maxResults = 100)
public class GroupDTO {

    Long id = 1L;

    @OrderBy(order = 3)
    String name = "lile";

    @Gt
    @OrderBy(order = 1)
    int scope = 10;

    @OrderBy(order = 2)
    protected Integer orderCode;

    Paging paging = new DefaultPaging(1, 20);

    String state = "A";

    String category = "LL";


    @Ignore
    protected Boolean enable = true;

    protected Boolean editable = true;

    @Lt
    protected Date createTime = new Date();

    @UpdateColumn
    protected Date lastUpdateTime = new Date();

    @Like
    protected String description = " info ";

}
