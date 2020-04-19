package com.levin.commons.dao.dto;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.DefaultPaging;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
 @TargetOption(entityClass = Group.class, alias = "g")
public class GroupInfo implements Serializable {

    String name;

    String id;

    @Fetch(joinType = Fetch.JoinType.Left)
    @Ignore
    List<GroupInfo> children;

}
