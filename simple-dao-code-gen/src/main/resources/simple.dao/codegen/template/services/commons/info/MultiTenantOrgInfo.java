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
@Schema(title = "多租户组织基本信息")
@Data
@Accessors(chain = true)
@FieldNameConstants
@ToString(callSuper = true)
public class MultiTenantOrgInfo
        extends MultiTenantInfo
        implements OrganizedObject {

    @Size(max = 128)
    @Schema(title = "组织Id")
    String orgId;

    @RefInject(refObjectType = "Org", idExpr = InjectConst.ORG_ID, valueExpr = "name")
    @Schema(title = "组织名称")
    String orgName;

}
