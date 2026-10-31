package com.levin.commons.dao.codegen;

import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.GenDaoLogicGroup;
import com.levin.commons.dao.annotation.In;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@EntityOption(innerAccessOnly = true)
@Schema(title = "用户订单")
@DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
        minConditions = 1, unless = "1=2", remark = "用户订单可见范围 @CopyToGenCode @*")
public class UserOrder {

    @Id
    @Schema(title = "订单ID")
    String id;

    @Schema(title = "销售部门ID")
    @GenDaoLogicGroup(name = "visible", filterOp = In.class, injectVarName = "accessibleOrgIds",
            injectOverride = "true", injectRequired = "false")
    String salesOrgId;

    @Schema(title = "销售负责人ID")
    @GenDaoLogicGroup(name = "visible", injectVarName = "currentUserId",
            injectOverride = "true", injectRequired = "false")
    String salesUserId;

    @Schema(title = "客户部门ID")
    @GenDaoLogicGroup(name = "visible", filterOp = In.class, injectVarName = "accessibleOrgIds",
            injectOverride = "true", injectRequired = "false")
    String customerOrgId;

    @Schema(title = "客户负责人ID")
    @GenDaoLogicGroup(name = "visible", injectVarName = "currentUserId",
            injectOverride = "true", injectRequired = "false")
    String customerUserId;

    @Schema(title = "售后部门ID")
    @GenDaoLogicGroup(name = "visible", filterOp = In.class, injectVarName = "accessibleOrgIds",
            injectOverride = "true", injectRequired = "false")
    String afterSalesOrgId;

    @Schema(title = "售后负责人ID")
    @GenDaoLogicGroup(name = "visible", injectVarName = "currentUserId",
            injectOverride = "true", injectRequired = "false")
    String afterSalesUserId;

    @Schema(title = "售前部门ID")
    @GenDaoLogicGroup(name = "visible", filterOp = In.class, injectVarName = "accessibleOrgIds",
            injectOverride = "true", injectRequired = "false")
    String preSalesOrgId;

    @Schema(title = "售前负责人ID")
    @GenDaoLogicGroup(name = "visible", injectVarName = "currentUserId",
            injectOverride = "true", injectRequired = "false")
    String preSalesUserId;
}
