package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.order.SimpleOrderBy;
import com.levin.commons.dao.domain.support.TestEntity;
import lombok.Data;

@Data
@TargetOption(entityClass = TestEntity.class)
public class TestEntityDto {

    @C(op = Op.In)
    String[] state = {"A", "B", "C"};

    @OR
    @CList({@C(op = Op.StartsWith),@C(op = Op.IsNull)})
    String name="test";

    // c.name like ? or c.name is null or c.phone like ? or c.nickName like


    @Like(paramFuncs = @Func( prefix = "'%#' || ",suffix = " || '#%'"))
    @Contains(paramFuncs = @Func( prefix = "'#' || ",suffix = " || '%'"))
     String category="T1";


    @SimpleOrderBy(condition = "state.length > 0")
    String[] orderBy = {"state desc", "name asc"};

    @SimpleOrderBy(condition = "name != null")
    String orderBy2 = "score desc , category asc";

}
