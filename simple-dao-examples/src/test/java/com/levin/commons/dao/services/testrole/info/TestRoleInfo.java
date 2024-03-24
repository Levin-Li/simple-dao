package com.levin.commons.dao.services.testrole.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.levin.commons.dao.domain.TestRole.*;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.levin.commons.dao.domain.E_TestRole.*;
import static com.levin.commons.dao.domain.EntityConst.*;

////////////////////////////////////

/**
 * 测试角色
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[2e5f39f134067aabf427fe0ab9fa7d7d]，请不要修改和删除此行内容。
 */
@Schema(title = BIZ_NAME)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {})
@FieldNameConstants
@JsonIgnoreProperties({tenantId})
public class TestRoleInfo implements Serializable {

    private static final long serialVersionUID = 1530906614L;

    @NotBlank
    @Size(max = 64)
    @Schema(title = L_id)
    String id;

    @NotBlank
    @Size(max = 128)
    @Schema(title = L_code)
    String code;

    @Schema(title = L_icon)
    String icon;

    @NotNull
    @Schema(title = L_orgDataScope)
    OrgDataScope orgDataScope;

    @InjectVar(domain = "dao", converter = PrimitiveArrayJsonConverter.class, isRequired = "false")
    @Schema(title = L_assignedOrgIdList, description = D_assignedOrgIdList)
    List<String> assignedOrgIdList;

    @InjectVar(domain = "dao", converter = PrimitiveArrayJsonConverter.class, isRequired = "false")
    @Schema(title = L_permissionList, description = D_permissionList)
    List<String> permissionList;

    @Size(max = 128)
    @Schema(title = L_tenantId)
    String tenantId;

    @NotBlank
    @Size(max = 128)
    @Schema(title = L_name)
    String name;

    @Size(max = 128)
    @Schema(title = L_pinyinName, description = D_pinyinName)
    String pinyinName;

    @Size(max = 128)
    @Schema(title = L_creator)
    String creator;

    @NotNull
    @Schema(title = L_createTime)
    Date createTime;

    @Schema(title = L_lastUpdateTime)
    Date lastUpdateTime;

    @Schema(title = L_orderCode)
    Integer orderCode;

    @NotNull
    @Schema(title = L_enable)
    Boolean enable;

    @NotNull
    @Schema(title = L_editable)
    Boolean editable;

    @Size(max = 512)
    @Schema(title = L_remark)
    String remark;
}
