package com.levin.commons.dao.services.testorg.req;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.domain.E_TestOrg;
import com.levin.commons.dao.domain.TestOrg;
import com.levin.commons.dao.services.commons.req.MultiTenantReq;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;

import static com.levin.commons.dao.domain.E_TestOrg.*;
import static com.levin.commons.dao.domain.EntityConst.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

////////////////////////////////////

/**
 * 删除测试机构
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[09a6442246250f8a5e1642824f83b59f]，请不要修改和删除此行内容。
 */
@Schema(title = DELETE_ACTION + BIZ_NAME)
@Data

// @AllArgsConstructor

@NoArgsConstructor
@Builder
// @EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = TestOrg.class, alias = E_TestOrg.ALIAS)
public class DeleteTestOrgReq extends MultiTenantReq {

    private static final long serialVersionUID = 2127591268L;

    @Schema(description = "可编辑条件", hidden = true)
    @Eq(condition = "!#" + InjectConst.IS_SUPER_ADMIN)
    final boolean eqEditable = true;

    @Schema(title = L_id + "集合", required = true, requiredMode = REQUIRED)
    @In(value = E_TestOrg.id)
    @NotEmpty
    private String[] idList;

    public DeleteTestOrgReq(String... idList) {
        this.idList = idList;
    }

    public DeleteTestOrgReq setIdList(String... idList) {
        this.idList = idList;
        return this;
    }

    @PostConstruct
    public void preDelete() {
        // @todo 删除之前初始化数据
    }
}
