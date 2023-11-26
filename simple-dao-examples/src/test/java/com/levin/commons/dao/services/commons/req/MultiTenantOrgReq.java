package com.levin.commons.dao.services.commons.req;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * 多租户查询对象
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[1b7779877ccd1c8dccf84e6b3466c785]，请不要修改和删除此行内容。
 */
@Schema(title = "多租户查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class MultiTenantOrgReq extends MultiTenantReq implements MultiTenantObject {

    // 注意需要在注入服务中设置isTenantAdmin变量
    @InjectVar(
            value = InjectConst.ORG_ID,
            isOverride =
                    InjectVar.SPEL_PREFIX
                            + "!#"
                            + InjectConst.IS_SUPER_ADMIN
                            + " && !#"
                            + InjectConst.IS_TENANT_ADMIN // 如果不是超管 也不是 租户管理员, 那么覆盖必须的
            ,
            isRequired =
                    InjectVar.SPEL_PREFIX
                            + "!#"
                            + InjectConst.IS_SUPER_ADMIN
                            + " && !#"
                            + InjectConst.IS_TENANT_ADMIN // 如果不是超管 也不是 租户管理员，那么值是必须的
            )
    @Schema(title = "机构ID", hidden = true)
    @Eq
    protected String orgId;

    /**
     * 设置部门ID
     *
     * @param orgId
     * @return
     * @param <T>
     */
    public <T extends MultiTenantOrgReq> T setOrgId(String orgId) {
        this.orgId = orgId;
        return (T) this;
    }
}
