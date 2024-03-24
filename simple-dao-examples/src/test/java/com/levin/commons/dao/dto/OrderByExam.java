package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.order.SimpleOrderBy;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = User.class)
public class OrderByExam {


    @Contains
    @OrderBy
    @SimpleOrderBy(expr = "'score desc'")
    String name = "test";

    @OrderBy(E_User.createTime)
    @OrderBy(value = E_User.area, order = 5, type = OrderBy.Type.Asc, scope = OrderBy.Scope.OnlyForGroupBy)
    @OrderBy(condition = C.VALUE_NOT_EMPTY)
    String orderCode = "1111";

}
