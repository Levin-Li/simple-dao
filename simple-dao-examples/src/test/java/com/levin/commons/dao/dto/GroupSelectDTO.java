package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.Group;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = E_Group.ALIAS, maxResults = 100)
public class GroupSelectDTO {

    @Select(domain = E_Group.ALIAS, value = "parent.name")
    String parentName;

    @Select
    String name;

}
