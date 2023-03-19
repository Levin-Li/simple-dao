package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.IsNull;
import com.levin.commons.dao.annotation.NotIn;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.domain.TestEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@TargetOption(entityClass = TestEntity.class, resultClass = TestEntityStatDto.class)
public class TestEntityStatDto {

    @Min
    Long minScore;

    @Max
    Long maxScore;

    @Avg(havingOp = Op.Gt)
    Long avgScore = 10L;

    @Count
    Long countScore;

    @GroupBy
    @NotIn
    String[] state = {"A", "B", "C"};



    @IsNull
    @NotIn
    @OR(autoClose = true)
    String operation = "Eq,Lt,Gt,"; // Arrays.stream(Op.values()).reduce("", (op1, op2) -> op1 + "," + op2, null);

    @Contains
    @OR
    String name = "test";

    //不加任何注解
    CommDto commDto = new CommDto();


    List<String> categories = new ArrayList<>();

}
