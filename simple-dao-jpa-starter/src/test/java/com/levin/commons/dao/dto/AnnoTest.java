package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.User;

@TargetOption(entityClass = User.class
)
public class AnnoTest {


    @OR
    @IsNull("name")

    @Eq
    @NotEq
    @Like
    @NotLike
    @Contains
    @StartsWith
    @EndsWith
    @In
    @NotIn
    @Lt
    @Lte
    @Gt
    @Gte

    String state ="S1";



    @IsNotNull(value = "name",condition = "#_val != null and #_val")
    Boolean onStore = true;

}
