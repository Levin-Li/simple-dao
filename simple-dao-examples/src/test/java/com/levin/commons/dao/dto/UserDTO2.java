package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.END;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;


@TargetOption(entityClass = User.class, alias = "u", resultClass = UserDTO2.UserInfo.class)
public class UserDTO2 {

    @Data
    @Select
    public static class UserInfo {

//        @Desc("分数")
//        Integer score;

        String description;

        @Schema(description = "归属的虚拟组织")
        @InjectVar(converter = PrimitiveArrayJsonConverter.class)
        List<String> belongOrgList;

    }

    Paging paging = new PagingQueryReq(1, 20);

    Long id;

    @OrderBy
    String name = "User";

    @OrderBy
    protected Integer orderCode;

    @Ignore
    protected Boolean enable = true;

    @AND
    protected Boolean editable = true;

    @Lt
    @OR(condition = "#_val!=null")
    protected Date createTime = new Date();

    @In
    String[] state = new String[]{"A", "B", "C"};


    @Between("score")
    @END
    protected Integer[] scores = new Integer[]{200, 100, null, null};

    @END(containCurrentField = false)
    @Like
    protected String description = "desc";


    @Update
    protected Date lastUpdateTime = new Date();

}
