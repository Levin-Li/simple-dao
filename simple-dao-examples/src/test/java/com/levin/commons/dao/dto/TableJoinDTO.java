package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(
        entityClass = User.class, alias = E_User.ALIAS,
        resultClass = TableJoinDTO.class,
        isSafeMode = false,
        //连接表
        joinOptions = {
                @JoinOption(alias = E_Group.ALIAS, entityClass = Group.class)
        })
public class TableJoinDTO {

    @Select(value = "u.id", isDistinct = true)
    @Gt(value = E_User.id, domain = E_User.ALIAS)
    Long uid;

    @Select(value = E_Group.id, domain = E_Group.ALIAS)
    @Gte("g.id")
    Long gid;

    @Select
    String name;

    @Select(domain = E_Group.ALIAS, value = E_Group.name)
    String groupName;

}
