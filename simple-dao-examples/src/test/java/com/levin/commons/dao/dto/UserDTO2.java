package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.OrderBy;
//import com.levin.commons.dao.annotation.update.UpdateColumn;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;

import java.util.Date;


@TargetOption(entityClass = User.class, alias = "u", maxResults = 100)
public class UserDTO2 {

    Paging paging = new PagingQueryReq(1, 20);

    Long id;

    @OrderBy
    String name = "User";

    @OrderBy
    protected Integer orderCode;

    @In()
    String[] state = new String[]{"A", "B", "C"};

    @Ignore
    protected Boolean enable = true;

    @AND
    protected Boolean editable = true;

    @Lt()
    @OR(condition = "#_val!=null")
    protected Date createTime = new Date();

    @Between("score")
    @END
    protected Integer[] scores = new Integer[]{200, 100, null, null};

    @END(containCurrentField = false)
    @Like
    protected String description = "desc";


    @Update
    protected Date lastUpdateTime = new Date();

}
