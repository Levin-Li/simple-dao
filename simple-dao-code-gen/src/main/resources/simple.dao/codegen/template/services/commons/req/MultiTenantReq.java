package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.IsNull;
import com.levin.commons.dao.annotation.logic.OR;
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
@Schema(title = "多租户查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class MultiTenantReq
        extends BaseReq
        implements MultiTenantObject {

    @Schema(title = "租户ID" , hidden = true)
    @InjectVar(value = InjectConsts.TENANT_ID
            , isOverride = InjectVar.SPEL_PREFIX + "!#" + InjectConsts.IS_SUPER_ADMIN // 如果不是超级管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + "!#" + InjectConsts.IS_SUPER_ADMIN // 如果不是超级管理员，那么值是必须的
    )
    @OR(autoClose = true)
    @Eq
    @IsNull(condition = "#_this.isContainsPublicData()") //如果是公共数据，允许包括非该租户的数据
    protected String tenantId;

    /**
     * 是否为公共数据
     *
     * @return
     */
    @Schema(title = "请求是否包含公共数据", hidden = true)
    public boolean isContainsPublicData() {
        return false;
    }

    public <T extends MultiTenantReq> T setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return (T) this;
    }

}