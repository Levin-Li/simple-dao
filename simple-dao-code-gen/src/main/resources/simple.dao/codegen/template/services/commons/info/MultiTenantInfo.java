package ${modulePackageName}.services.commons.info;

import com.levin.commons.dao.domain.*;
import com.levin.commons.rbac.DataMasking;
import com.levin.commons.rbac.RbacRoleObject;
import com.levin.commons.rbac.ResAuthorize;
import com.levin.commons.service.domain.*;
import com.levin.commons.service.support.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.Size;


/**
 * 多租户查询对象
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Schema(title = "多租户基本信息")
@Data
@Accessors(chain = true)
@FieldNameConstants
@ToString(callSuper = true)
public class MultiTenantInfo
        extends BaseInfo
        implements MultiTenantObject {

    @DataMasking(showAuthorize = @ResAuthorize(anyRoles = {RbacRoleObject.SA_ROLE, RbacRoleObject.SAAS_ROLE_PREFIX + "*"}), remark = "SAAS管理员才能显示")
    @Size(max = 128)
    @Schema(title = "租户Id")
    String tenantId;

    @RefInject(refObjectType = "Tenant", idExpr = InjectConst.TENANT_ID, valueExpr = "name")
    @DataMasking(showAuthorize = @ResAuthorize(anyRoles = {RbacRoleObject.SA_ROLE, RbacRoleObject.SAAS_ROLE_PREFIX + "*"}), remark = "SAAS管理员才能显示")
    @Schema(title = "租户名称")
    String tenantName = ""; // 默认值, Jackson序列化时默认不会处理null值，所以特意设置了空串

}
