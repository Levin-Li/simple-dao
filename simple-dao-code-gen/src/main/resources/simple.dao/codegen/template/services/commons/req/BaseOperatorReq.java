package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.domain.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.service.support.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;


/**
 * 多租户操作员请求对象
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Schema(title = "操作员请求")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class BaseOperatorReq
        extends MultiTenantOrgReq {

    @InjectVar(value = InjectConst.USER_ID)
    @Schema(title = "操作用户ID" , hidden = true)
    protected String userId;

    @InjectVar(value = InjectConst.USER_NAME, isRequired = "false")
    @Schema(title = "操作用户名字" , hidden = true)
    protected String userName;

    @InjectVar(value = InjectConst.ORG_NAME, isRequired = "false")
    @Schema(title = "组织名称" , hidden = true)
    protected String orgName;

    @InjectVar(value = InjectConst.IP_ADDR, isRequired = "false")
    @Schema(title = "IP地址", hidden = true)
    protected String ipAddr;

    @InjectVar(value = InjectConst.IS_TENANT_ADMIN, isRequired = "false")
    @Schema(title = "是否是租户管理员", hidden = true)
    protected boolean isTenantAdmin = false;

    @InjectVar(value = InjectConst.IS_SUPER_ADMIN, isRequired = "false")
    @Schema(title = "是否是超级管理员", hidden = true)
    protected boolean isSuperAdmin = false;

    @InjectVar(value = InjectConst.IS_WEB_CONTEXT, isRequired = "false")
    @Schema(title = "是否是web请求", hidden = true)
    protected boolean isWebContext = true;

}
