package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

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
