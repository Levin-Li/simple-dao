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
@Schema(description = "多租户查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class MultiTenantOrgReq
        extends MultiTenantReq
        implements MultiTenantObject {

    //注意需要在注入服务中设置isTenantAdmin变量
    @InjectVar(value = InjectConsts.ORG_ID
            , isOverride = InjectVar.SPEL_PREFIX + "!#" + InjectConsts.IS_TENANT_ADMIN // 如果不是租户管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + "!#" + InjectConsts.IS_TENANT_ADMIN // 如果不是租户管理员，那么值是必须的
    )
    @Schema(description = "机构ID" , hidden = true)
    @Eq
    protected String orgId;

}