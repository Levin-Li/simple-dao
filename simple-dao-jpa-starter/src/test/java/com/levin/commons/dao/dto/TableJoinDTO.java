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
        entityClass = User.class, alias = "u",
//        fromStatement = "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group.id = g.id",
        joinOptions = {
                @JoinOption(alias = "g", entityClass = Group.class)
        }
        , maxResults = 100)
public class TableJoinDTO {

    @Select(value = "u.id", isDistinct = true)
    @Gt(value = E_User.id, domain = "u")
    Long uid = 1L;

    @Select(value = E_Group.id, domain = "g")
    @Gte("g.id")
    Long gid = 2L;

    //@Select(domain = "u")
    @Select
    String name;

    @Select(domain = "g", value = E_Group.name)
    String groupName;




}
