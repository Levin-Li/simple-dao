package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = User.class, alias = "u")
public class IncrUpdateUserDTO {

    @Update(incrementMode = true)
    String name = "-User";

    @Update(incrementMode = true, paramExpr = "1", condition = "", desc = "乐观锁更新 + 1")
    protected Integer orderCode;

    @Update(incrementMode = true, paramExpr = "1", condition = "", convertNullValueForIncrementMode = false)
    protected Integer score;

    @Update(incrementMode = true)
    protected Date createTime = null;

    @Update(incrementMode = true, convertNullValueForIncrementMode = false)
    protected String remark = "-desc";

    @Eq(require = true)
    Long id;
}
