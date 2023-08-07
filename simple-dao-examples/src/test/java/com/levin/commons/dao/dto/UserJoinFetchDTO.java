package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = User.class, alias = E_User.ALIAS, maxResults = 100)
public class UserJoinFetchDTO {

    @Fetch(domain = E_User.ALIAS, value = "group.name")
    String groupName;

    @Fetch(domain = E_User.ALIAS, value = "group")
    GroupInfo groupInfo;

    @Fetch
    String name;
}
