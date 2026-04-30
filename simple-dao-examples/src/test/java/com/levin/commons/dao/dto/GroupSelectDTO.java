package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.Group;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = E_Group.ALIAS, maxResults = 100)
public class GroupSelectDTO {

    @Select(domain = E_Group.ALIAS, value = "parent.name")
    String parentName;

    @Select
    String name;

    @Schema(description = "可编辑条件", hidden = true)
    @Eq
    final boolean eqEditable = true;
}
