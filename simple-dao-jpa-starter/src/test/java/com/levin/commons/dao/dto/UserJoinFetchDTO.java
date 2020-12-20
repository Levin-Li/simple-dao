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
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = User.class, alias = "u")
public class UserJoinFetchDTO {

    //    @Fetch(domain = "u",value = "group",attrs = {"group.children"})
    @Fetch(value = "group.name" )
    String unused;

}
