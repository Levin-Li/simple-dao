package ${modulePackageName}.services.commons.req;

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
@NoArgsConstructor
@Builder
@ToString
@Accessors(chain = true)
@FieldNameConstants
public abstract class MultiTenantReq
        extends BaseReq
        implements MultiTenantObject, OrganizedObject {

    @Schema(description = "租户ID")
    @InjectVar(InjectConsts.TENANT_ID)
    String tenantId;

    @Schema(description = "组织ID")
    @InjectVar(value = InjectConsts.ORG_ID , isRequired = "false")
    String orgId;

}
