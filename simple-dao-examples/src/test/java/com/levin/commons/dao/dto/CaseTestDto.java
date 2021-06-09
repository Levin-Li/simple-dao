package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.CList;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.misc.Case;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(nativeQL = true, entityClass = User.class, alias = E_User.ALIAS, maxResults = 20)
public class CaseTestDto {

    @C
    @CList({@C(op = Op.StartsWith)})
    @Select
    String name;

    // states = {"正常", "已取消", "审请中", "已删除", "已冻结"};

    @Select(value = E_User.score, fieldCases = {
            @Case(column = "", elseExpr = "5", condition = "#_val == 1", whenOptions = {
                    @Case.When(whenExpr = "F$:score > 95 AND F$:u.lastUpdateTime is null", thenExpr = "1")
                    , @Case.When(whenExpr = "score > 85", thenExpr = "2")
                    , @Case.When(whenExpr = "score > 60", thenExpr = "3")
                    , @Case.When(whenExpr = "score > 30", thenExpr = "4")
            })

            , @Case(column = E_User.state, elseExpr = "5", condition = "#_val == 2 && queryState"
            , whenOptions = {
              @Case.When(whenExpr = "'正常'", thenExpr = "1")
            , @Case.When(whenExpr = "'已取消'", thenExpr = "2")
            , @Case.When(whenExpr = "'审请中'", thenExpr = "3")
            , @Case.When(whenExpr = "'已删除'", thenExpr = "4")
    })
    })
    int scoreLevel = 1;

    @Ignore
    boolean queryState = false;

}
