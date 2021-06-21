package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.domain.Group;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = "g")
public class GroupInfo implements Serializable {

    String name;

    String id;

    //    @Ignore
    @Fetch(value = "parent.name", joinType = Fetch.JoinType.Left, onlyForQueryObject = false)
    String parentName;


    @Fetch(joinType = Fetch.JoinType.Left, condition = "false")
    List<GroupInfo> children;

}
