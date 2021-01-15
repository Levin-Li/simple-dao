package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(tableName = E_User.CLASS_NAME,alias = E_User.ALIAS,
        joinOptions = {
        @JoinOption(tableOrStatement = E_Group.CLASS_NAME,
                alias = E_Group.ALIAS,joinColumn = E_Group.id,joinTargetAlias = E_User.ALIAS,joinTargetColumn = E_User.group)
})
public class TableJoin3 {

    @Select(domain = E_User.ALIAS, value = E_User.id, isDistinct = true)
    @Gt(value = E_User.id, domain = E_User.ALIAS)
    Long uid = 1l;

    @Select(value = E_Group.id, domain = E_Group.ALIAS)
    @Gte(domain = E_Group.ALIAS,value = E_Group.id)
    Long gid;

    @Select
    String name;

    @Select(domain = E_Group.ALIAS, value = E_Group.name)
    String groupName;

}
