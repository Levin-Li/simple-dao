package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.support.PagingQueryReq;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(
        nativeQL = true,
        entityClass = User.class, alias = E_User.ALIAS,
        resultClass = NativeTableJoinDTO.class,
        safeMode = false,
        //连接表
        joinOptions = {
                @JoinOption(alias = E_Group.ALIAS, joinTargetColumn = E_User.group, entityClass = Group.class)
        })
public class NativeTableJoinDTO extends PagingQueryReq {

    @Select(value = E_User.id)
    @Gt(value = E_User.id, domain = E_User.ALIAS)
    Long uid;

    @Select(value = E_Group.id, domain = E_Group.ALIAS)
    @Gte(value = E_User.id, domain = E_User.ALIAS)
    Long gid;

    @Select
    String name;

    @Select(domain = E_Group.ALIAS, value = E_Group.name)
    String groupName;

}
