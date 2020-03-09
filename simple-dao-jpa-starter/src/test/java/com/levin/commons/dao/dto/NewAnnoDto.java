package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.CList;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.support.TestEntity;
import lombok.Data;

@Data
@TargetOption(entityClass = TestEntity.class)
public class NewAnnoDto {


    @C(op = Op.In)
    String[] state = {"A", "B", "C"};

    @OR
    @CList({@C(op = Op.StartsWith),@C(op = Op.IsNull)})
    String name="test";

}
