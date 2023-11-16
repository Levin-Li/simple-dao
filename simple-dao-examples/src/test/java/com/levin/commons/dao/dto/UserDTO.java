package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.NOT;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@TargetOption(entityClass = User.class, alias = "u", maxResults = 100, tableName = "jpa_dao_test_User")
public class UserDTO {

    Paging paging = new PagingQueryReq(1, 20);

    Long id;

    @OrderBy
    String name = "User";

    @OrderBy
    protected Integer orderCode;

    @GroupBy
    @In(not = true, having = true)
    String[] state = new String[]{"A", "B", "C"};

    @NotIn(paramDelimiter = ",")
    String notInName = "A,B,C";

    @NOT(autoClose = false)
    @AND
    protected Boolean enable = true;

    protected Boolean editable = true;

    @C(op = Op.Gt)
    @OR
    protected Date createTime = new Date();

    @Between("score")
    @END
    protected Integer[] scores = new Integer[]{200, 100, null, null};

    @Like
    @END
    protected String remark = "desc";

    @END(containCurrentField = false)//end NOT
    @Between(paramDelimiter = "-", patterns = "yyyyMMDD")
    final String betweenCreateTime = "20190101-20220201";

    @Update
    protected Date lastUpdateTime = new Date();

}
