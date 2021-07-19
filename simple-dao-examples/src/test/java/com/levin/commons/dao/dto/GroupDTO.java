package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.support.PagingQueryReq;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.annotation.PostConstruct;
import java.util.Date;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, maxResults = 100)
public class GroupDTO {

    Long id = 1L;

    @OrderBy(order = 3)
    String name = "lile";

    @Gt
    @OrderBy(order = 1)
    int scope = 10;

    @OrderBy(order = 2)
    protected Integer orderCode;

    Paging paging = new PagingQueryReq().setPageIndex(1).setPageSize(20);

    String state = "A";

    String category = "LL";


    @Ignore
    protected Boolean enable = true;

    protected Boolean editable = true;

    @Lt(fieldFuncs = @Func(value = "DATE_FORMAT", params = {C.ORIGIN_EXPR, "${:format}"}, condition = "true"))
    protected Date createTime = new Date();

    @Update
    protected Date lastUpdateTime = new Date();

    @Like
    protected String description = " info ";

    @Ignore
    String format = "YYYY-MM-DD";


    @PostConstruct
    void init() {
        System.out.println(getClass().getName() + " init 1 ...");
    }

    @PostConstruct
    void init2() {
        System.out.println(getClass().getName() + " init 2 ...");
    }


    @PostConstruct
    void init3() {
        System.out.println(getClass().getName() + " init 3 ...");
    }

}
