package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@TargetOption(entityClass = User.class
)@Data
@Accessors(chain = true)
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
    @Gte(not = true)
    String state ="S1";



    @IsNotNull(value = "name",condition = "#_val != null and #_val")
    Boolean onStore = true;



    @Eq
    @NotEq
    @Like
    @NotLike(not = true)
    @Contains
    @StartsWith
    @EndsWith
    @In
    @NotIn(not = true)
    @Lt
    @Lte
    @Gt
    @Gte(not = true)
    String name ="S1";

}
