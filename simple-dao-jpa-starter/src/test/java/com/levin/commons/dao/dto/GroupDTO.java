package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.annotation.update.UpdateColumn;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.support.DefaultPaging;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
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

    @Lt(fieldFuncs = @Func(value = "DATE_FORMAT",params = {"$$","${:format}"}))
    protected Date createTime = new Date();

    @Update
    protected Date lastUpdateTime = new Date();

    @Like
    protected String description = " info ";

    @Ignore
    String format ="YYYY-MM-DD";

}
