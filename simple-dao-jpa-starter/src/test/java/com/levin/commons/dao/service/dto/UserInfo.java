package com.levin.commons.dao.service.dto;


import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.domain.Group;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

@Data
@Accessors(chain = true)
public class UserInfo {

    String id;

    String name;

    String state;

    String area;

    Integer score;


    @Fetch
    Group group;

    @Fetch("group.name")
    String groupName;

    @Fetch("group.children")
    Collection<Group> parentChildren;

}
