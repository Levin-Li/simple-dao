package com.levin.commons.dao.dto;


import com.levin.commons.dao.CtxVar;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.support.E_TestEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = "g", resultClass = CtxVarTestReq.Info.class)
public class CtxVarTestReq implements Serializable {

    String name;

    String id;

    //    @Ignore
    String parentName;


    @CtxVar
    @Ignore
    Boolean isQueryName = true;

    @Data
    @Accessors(chain = true)
    public static class Info {

        @Select
        String id;

        @Select(condition = "#isQueryName")
        String name;

        @Select(domain = "g", value = "parent.name")
        String parentName;

    }

}
