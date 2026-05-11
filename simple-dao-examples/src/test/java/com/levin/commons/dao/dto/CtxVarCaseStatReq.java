package com.levin.commons.dao.dto;

import com.levin.commons.dao.CtxVar;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.misc.Case;
import com.levin.commons.dao.annotation.stat.Sum;
import com.levin.commons.dao.domain.support.TestEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = TestEntity.class, alias = "e", resultClass = CtxVarCaseStatReq.Result.class)
public class CtxVarCaseStatReq {

    @Ignore
    @CtxVar
    LocalDateTime beginTime;

    @Ignore
    @CtxVar
    LocalDateTime futureTime;

    @Data
    @Accessors(chain = true)
    public static class Result {

        @Sum(fieldCases = @Case(
                column = "",
                whenOptions = @Case.When(
                        whenExpr = "e.createTime >= ${:beginTime}",
                        thenExpr = "e.score"
                ),
                elseExpr = "0"
        ))
        Long scoreFromBegin;

        @Sum(fieldCases = @Case(
                column = "",
                whenOptions = @Case.When(
                        whenExpr = "e.createTime >= ${:futureTime}",
                        thenExpr = "e.score"
                ),
                elseExpr = "0"
        ))
        Long scoreFromFuture;
    }
}
