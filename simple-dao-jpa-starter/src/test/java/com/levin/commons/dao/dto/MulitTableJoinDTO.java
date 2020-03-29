package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.Where;
import com.levin.commons.dao.annotation.select.Select;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(fromStatement = "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group.id = g.id", maxResults = 100)
public class MulitTableJoinDTO {


    @Select("u.id")
    @C(op = Op.Expr, paramExpr = "u.group.id = g.id", condition = "")
    Long uid;


    @Select("g.id")
    @Gt("g.id")
    @Where(paramExpr = " g.id = u.group.id ")
    Long gid = 2L;


}
