package com.levin.commons.dao.dto;


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
@TargetOption(nativeQL = true,
        tableName = "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group_id = g.id"
       // , fromStatement = "from jpa_dao_test_User u left join jpa_dao_test_Group g on u.group = g.id"
)
public class FromStatementDTO {

    @Select(value = "u.id", distinct = true)
    @Gt(value = E_User.id, domain = "u")
    Long uid = 1l;

    @Select(value = E_Group.id, domain = "g")
    @Gte("g.id")
    Long gid;

    @Select(domain = "u")
    String name;

    @Select(domain = "g", value = E_Group.name)
    String groupName;

}
