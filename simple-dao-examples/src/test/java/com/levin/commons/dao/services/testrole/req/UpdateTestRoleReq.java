package com.levin.commons.dao.services.testrole.req;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.E_TestRole;
import com.levin.commons.dao.domain.TestRole;
import com.levin.commons.dao.domain.TestRole.*;
import com.levin.commons.dao.services.commons.req.MultiTenantReq;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import static com.levin.commons.dao.domain.E_TestRole.*;
import static com.levin.commons.dao.domain.EntityConst.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

////////////////////////////////////

/**
 * 更新测试角色
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[7519877cf762c70466cf2a3aac670f21]，请不要修改和删除此行内容。
 */
@Schema(title = UPDATE_ACTION + BIZ_NAME)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// @EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = TestRole.class, alias = E_TestRole.ALIAS)
// 默认更新注解
@Update
public class UpdateTestRoleReq extends MultiTenantReq {

    private static final long serialVersionUID = 1530906614L;

    @Schema(title = L_id, required = true, requiredMode = REQUIRED)
    @NotNull
    @Eq(require = true)
    String id;

    //@Schema(description = "可编辑条件", hidden = true)
    //@Eq(condition = "!#" + InjectConst.IS_SUPER_ADMIN)
    //final boolean eqEditable = true;

    @NotBlank
    @Size(max = 128)
    @Schema(title = L_code)
    String code;

    @Schema(title = L_icon)
    String icon;

    @Schema(title = L_orgDataScope)
    OrgDataScope orgDataScope;

    @InjectVar(
            domain = "dao",
            expectBaseType = String.class,
            converter = PrimitiveArrayJsonConverter.class,
            isRequired = "false")
    @Schema(title = L_assignedOrgIdList, description = D_assignedOrgIdList)
    List<String> assignedOrgIdList;

    @InjectVar(
            domain = "dao",
            expectBaseType = String.class,
            converter = PrimitiveArrayJsonConverter.class,
            isRequired = "false")
    @Schema(title = L_permissionList, description = D_permissionList)
    List<String> permissionList;

    @NotBlank
    @Size(max = 128)
    @Schema(title = L_name)
    String name;

    @Size(max = 128)
    @Schema(title = L_pinyinName, description = D_pinyinName)
    String pinyinName;

    @Schema(title = L_lastUpdateTime)
    Date lastUpdateTime;

    @Schema(title = L_orderCode)
    Integer orderCode;

    @Schema(title = L_enable)
    Boolean enable;

    @Schema(title = L_editable)
    Boolean editable;

    @Size(max = 512)
    @Schema(title = L_remark)
    String remark;

    public UpdateTestRoleReq(String id) {
        this.id = id;
    }

    public UpdateTestRoleReq updateIdWhenNotBlank(String id) {
        if (isNotBlank(id)) {
            this.id = id;
        }
        return this;
    }

    @PostConstruct
    public void preUpdate() {
        // @todo 更新之前初始化数据
    }
}
