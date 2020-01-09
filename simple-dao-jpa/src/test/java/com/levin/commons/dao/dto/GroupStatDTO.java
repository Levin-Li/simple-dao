package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Like;
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


    @Count(value = "id")
    Integer cnt;

    @Avg(value = "score", suffix = " + 5 )")
    Double avgScore;

    @Sum("score")
    Double sumScore;


    @Min("score")
    Double minScore;


    @Max("score")
    Double maxScore;


    @Like
    String name = "Group";

}
