package com.levin.commons.dao.services.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = User.class,alias= E_User.ALIAS)
public class UserInfo {

    String id;

    String name;

    String state;

    String area;

    Integer score;


    @Fetch
    Group group;

    @Fetch(value = "group.parent.parent.name b")
    String groupName;

    @Fetch(value = "group.children c" )
    List<Group> parentChildren;

}
