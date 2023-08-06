package com.levin.commons.dao.services.testorg.req;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.domain.E_TestOrg;
import com.levin.commons.dao.domain.TestOrg;
import com.levin.commons.dao.services.commons.req.MultiTenantReq;
import com.levin.commons.dao.services.testorg.info.TestOrgInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.annotation.PostConstruct;

import static com.levin.commons.dao.domain.E_TestOrg.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

////////////////////////////////////

/**
 * 测试机构 主键通用请求
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[b069bc7520666b988397f437ce28ee08]，请不要修改和删除此行内容。
 */
@Schema(title = BIZ_NAME + " 主键通用查询")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// @EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = TestOrg.class, alias = E_TestOrg.ALIAS, resultClass = TestOrgInfo.class)
public class TestOrgIdReq extends MultiTenantReq {

    private static final long serialVersionUID = 2127591268L;

    @Schema(title = L_id, required = true, requiredMode = REQUIRED)
    @Eq(require = true)
    // @NotNull
    protected String id;

    public TestOrgIdReq updateIdWhenNotBlank(String id) {
        if (isNotBlank(id)) {
            this.id = id;
        }
        return this;
    }

    @PostConstruct
    public void preQuery() {
        // @todo ID 查询之前初始化数据
    }
}
