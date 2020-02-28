package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.Like;
import com.levin.commons.dao.annotation.Null;
import com.levin.commons.dao.annotation.logic.AND;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.service.domain.Desc;


@TargetOption(entityClass = Group.class, maxResults = 100)
public class GroupStatDTO {


    @Desc
    @GroupBy("state")
    String state;

    @GroupBy
    String category;


    @Count(value = "id", havingOp = " > ")
    Integer cnt = 1;

    @Avg(value = "score", suffix = " + 5 )")
    Double avgScore;

    @Sum("score")
    Double sumScore;


    @Min("score")
    Double minScore;


    @Max("score")
    Double maxScore;



    @Null
    @Eq
    @AND
    @OR(autoClose = true)
    String name = "Group";


    @Like("name")
    String name2 = "Group";

}
