package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.dao.domain.OrganizedObject;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConsts;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;


/**
 * 多租户查询对象
 *
 * @Author Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "多租户查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class MultiTenantReq
        extends BaseReq
        implements MultiTenantObject {

    @Schema(description = "租户ID" , hidden = true)
    @InjectVar(value = InjectConsts.TENANT_ID
            , isOverride = InjectVar.SPEL_PREFIX + "!#user.isSuperAdmin()" // 如果不是超级管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + "!#user.isSuperAdmin()" // 如果不是超级管理员，那么值是必须的
    )
    @Eq
    protected String tenantId;

    public <T extends MultiTenantReq> T setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return (T) this;
    }

}
