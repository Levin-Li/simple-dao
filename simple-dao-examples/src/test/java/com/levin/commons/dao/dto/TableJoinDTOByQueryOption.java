package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.QueryOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)

public class TableJoinDTOByQueryOption implements QueryOption {

    @Select(value = "u.id", distinct = true)
    @Gt(value = E_User.id, domain = "u")
    Long uid = 1L;

    @Select(value = E_Group.id, domain = "g")
    @Gte("g.id")
    Long gid = 2L;

    @Select
    String name;

    @Deprecated
    @Select(domain = "g", value = E_Group.name)
    String groupName;

    @Override
    public Class getEntityClass() {
        return User.class;
    }

    @Override
    public String getAlias() {
        return "u";
    }

    @Override
    public JoinOption[] getJoinOptions() {
        return new JoinOption[0];
    }
}
