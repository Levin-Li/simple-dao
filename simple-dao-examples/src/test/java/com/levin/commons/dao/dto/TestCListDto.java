package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.CList;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.support.TestEntity;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@TargetOption(entityClass = TestEntity.class)
public class TestCListDto {

    @OR
    @CList({@C(op = Op.StartsWith)})
    @In
    List<String> name = Arrays.asList("Test", "LLW");

}
