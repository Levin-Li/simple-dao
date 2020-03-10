package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.NotIn;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.domain.support.TestEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@TargetOption(entityClass = TestEntity.class)
public class TestEntityStatDto {

    @Min
    Long minScore;

    @Max
    Long maxScore;

    @Avg
    Long avgScore;

    @Count
    Long countScore;

    @GroupBy
    @NotIn
    String[] state = {"A", "B", "C"};

    @Contains
    String name = "test";

    //不加任何注解
    CommDto commDto = new CommDto();


    List<String>  categories = new ArrayList<>();

}
