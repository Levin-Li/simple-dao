package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.Group;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = E_Group.ALIAS, maxResults = 100, resultClass = CustomSelectDTO.class)
public class CustomSelectDTO {

    @Select(value = C.FIELD_VALUE, alias = C.BLANK_VALUE)
    String[] columns = new String[]{"name", "category", "score"};


    String name;

    String category;

    Integer score;


    @Select(domain = E_Group.ALIAS, value = "parent.name")
    String parentName;

}
