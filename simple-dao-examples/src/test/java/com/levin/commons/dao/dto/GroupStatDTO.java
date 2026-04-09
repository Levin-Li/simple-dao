package com.levin.commons.dao.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.service.domain.Desc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, maxResults = 100)
public class GroupStatDTO {


    @Desc
//    @GroupBy(alias = "state")
    @Ignore
    String state = "S1";

    @Schema(description = "可编辑条件，如果是web环境需要增加可编辑的过滤条件" , hidden = true)
    @Eq
    @JsonIgnore
    final boolean eqEditable = true;

    @Desc
    @Select("'2018-03-18'")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date groupByCreateTime;
//
//    @Desc
//    @Select
//    @DateTimeFormat(pattern = "yyyy-MM-DD")
//    String createTime;

    //    @GroupBy
    String category;


    @Count(value = "id", alias = "gid")
    Integer cnt;

    @Avg(surroundPrefix = "(", value = "score", surroundSuffix = " + 5 )")
    Double avgScore;

    @Sum()
    Double sumScore = 3.0;


    @Min
    Double minScore;


    @Max
    Double maxScore;


    @IsNull
    @IsNotNull
    @Eq
    @OR(autoClose = true)
    String name = "Group";


    @Like("name")
    String name2;

}
