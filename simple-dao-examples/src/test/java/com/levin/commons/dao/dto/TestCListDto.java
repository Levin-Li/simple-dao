package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.domain.E_TestEntity;
import com.levin.commons.dao.domain.support.TestEntity;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@TargetOption(entityClass = TestEntity.class)
public class TestCListDto {

    List<String> name = Arrays.asList("Test", "LLW");

    @In
    List<String> inName = Arrays.asList("Test", "LLW");

    @Contains
    @CList({@C(op = Op.In,value = "name")})
    String[] containsName = new String[]{"Test", null, "LLW"};

    @StartsWith
    List<String> startsWithName = null;

    @EndsWith
    @OrderBy("name")
    List<String> endsWithName = Arrays.asList();


}
