package com.levin.commons.dao.codegen;

import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.GenDaoLogicGroup;

@jakarta.persistence.Entity
@EntityOption(innerAccessOnly = true)
@DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = "1=2")
public class Sample {
    @jakarta.persistence.Id String id;
    @GenDaoLogicGroup(name = "visible") String name;
}
