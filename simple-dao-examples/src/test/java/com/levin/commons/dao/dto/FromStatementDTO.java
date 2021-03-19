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
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(isNative = true,
        tableName = "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group_id = g.id"
       // , fromStatement = "from jpa_dao_test_User u left join jpa_dao_test_Group g on u.group = g.id"
)
public class FromStatementDTO {

    @Select(value = "u.id",isDistinct = true)
    @Gt(value = E_User.id, domain = E_User.ALIAS)
    Long uid = 1l;

    @Select(value = E_Group.id, domain = E_Group.ALIAS)
    @Gte("g.id")
    Long gid;

    @Select(domain = E_User.ALIAS)
    String name;

    @Select(domain = E_Group.ALIAS, value = E_Group.name)
    String groupName;

}
