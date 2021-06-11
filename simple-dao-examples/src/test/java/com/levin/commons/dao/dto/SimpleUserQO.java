package com.levin.commons.dao.dto;


import com.levin.commons.dao.CtxVar;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = User.class, alias = E_User.ALIAS, resultClass = SimpleUserQO.QResult.class)
@FieldNameConstants
public class SimpleUserQO {

    @Data
    @NoArgsConstructor
    @Select
    public static class QResult {

        //@Select
        String name;

        //@Select
        Integer score;

        //有条件的查询状态信息
        @Select(condition = "#isQueryStatus")
        String state;

    }

    @Data
    @NoArgsConstructor
    public static class QResult2 {
        String name;
        Integer score;
    }

    @Lt
    protected Date createTime = new Date();

    @Ignore
    String format = "YYYY-MM-DD";

    // 把 isQueryStatus 变量注入到当前上下文中
    @CtxVar
    @Ignore
    boolean isQueryStatus = true;

}
