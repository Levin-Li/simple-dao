package ${modulePackageName}.services.commons.info;

import com.levin.commons.dao.domain.*;
import com.levin.commons.service.domain.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
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
public class MultiTenantOrgPersonalInfo
        extends MultiTenantOrgInfo
        implements PersonalObject {

    @Size(max = 128)
    @Schema(title = "拥有者Id")
    String ownerId;

    @RefInject(refObjectType = "User", idExpr = "ownerId", valueExpr = "name")
    @Schema(title = "拥有者名称")
    String ownerName;

}
