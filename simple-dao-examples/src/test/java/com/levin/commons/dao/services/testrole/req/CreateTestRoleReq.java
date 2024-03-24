package com.levin.commons.dao.services.testrole.req;

// import static com.levin.commons.ModuleOption.*;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.domain.E_TestRole;
import com.levin.commons.dao.domain.TestRole;
import com.levin.commons.dao.domain.TestRole.*;
import com.levin.commons.dao.services.commons.req.MultiTenantReq;
import com.levin.commons.service.domain.InjectVar;
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

////////////////////////////////////

/**
 * 新增测试角色
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[854df190547bf3b66f3078d412f6aeb9]，请不要修改和删除此行内容。
 */
@Schema(title = CREATE_ACTION + BIZ_NAME)
@Data
@Accessors(chain = true)
@ToString
// @EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TargetOption(entityClass = TestRole.class, alias = E_TestRole.ALIAS)
public class CreateTestRoleReq extends MultiTenantReq {

    private static final long serialVersionUID = 1530906614L;

    @Schema(title = L_code)
    @NotBlank
    @Size(max = 128)
    String code;

    @Schema(title = L_icon)
    String icon;

    @Schema(title = L_orgDataScope)
    @NotNull
    OrgDataScope orgDataScope;

    @Schema(title = L_assignedOrgIdList, description = D_assignedOrgIdList)
    @InjectVar(
            domain = "dao",
            expectBaseType = String.class,
            converter = PrimitiveArrayJsonConverter.class,
            isRequired = "false")
    List<String> assignedOrgIdList;

    @Schema(title = L_permissionList, description = D_permissionList)
    @InjectVar(
            domain = "dao",
            expectBaseType = String.class,
            converter = PrimitiveArrayJsonConverter.class,
            isRequired = "false")
    List<String> permissionList;


    @Schema(title = L_name)
    @NotBlank
    @Size(max = 128)
    String name;

    @Schema(title = L_pinyinName, description = D_pinyinName)
    @Size(max = 128)
    String pinyinName;

    @Schema(title = L_creator, hidden = true)
    // @Size(max = 128)
    String creator;

    @Schema(title = L_createTime, hidden = true)
    // @NotNull
    Date createTime;

    @Schema(title = L_lastUpdateTime, hidden = true)
    Date lastUpdateTime;

    @Schema(title = L_orderCode, hidden = true)
    Integer orderCode;

    @Schema(title = L_enable, hidden = true)
    // @NotNull
    Boolean enable;

    @Schema(title = L_editable, hidden = true)
    // @NotNull
    Boolean editable;

    @Schema(title = L_remark, hidden = true)
    // @Size(max = 512)
    String remark;

    @PostConstruct
    public void prePersist() {
        // @todo 保存之前初始化数据，比如时间，初始状态等

        if (getCreateTime() == null) {
            setCreateTime(new Date());
        }
    }
}
