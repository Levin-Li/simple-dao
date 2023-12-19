package com.levin.commons.dao.services.testorg.req;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.E_TestOrg;
import com.levin.commons.dao.domain.TestOrg;
import com.levin.commons.dao.domain.TestOrg.*;
import com.levin.commons.dao.services.commons.req.MultiTenantReq;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

import static com.levin.commons.dao.domain.E_TestOrg.*;
import static com.levin.commons.dao.domain.EntityConst.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

////////////////////////////////////

/**
 * 更新测试机构
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[66fdcb0794d835ee431081a5c8597481]，请不要修改和删除此行内容。
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
@TargetOption(entityClass = TestOrg.class, alias = E_TestOrg.ALIAS)
// 默认更新注解
@Update
public class UpdateTestOrgReq extends MultiTenantReq {

    private static final long serialVersionUID = 2127591268L;

    @Schema(title = L_id, required = true, requiredMode = REQUIRED)
    @NotNull
    @Eq(require = true)
    String id;

    @Schema(description = "可编辑条件", hidden = true)
    @Eq(condition = "!#" + InjectConst.IS_SUPER_ADMIN)
    final boolean eqEditable = true;

    @Size(max = 64)
    @Schema(title = L_parentId)
    String parentId;

    @Size(max = 64)
    @Schema(title = L_code, description = D_code)
    String code;

    @Schema(title = L_icon)
    String icon;

    @Schema(title = L_state)
    State state;

    @Schema(title = L_type)
    OrgType type;

    @Size(max = 64)
    @Schema(title = L_industries)
    String industries;

    @NotBlank
    @Size(max = 64)
    @Schema(title = L_areaCode)
    String areaCode;

    @Size(max = 128)
    @Schema(title = L_level, description = D_level)
    String level;

    @NotBlank
    @Size(max = 128)
    @Schema(title = L_category, description = D_category)
    String category;

    @Schema(title = L_isExternal)
    Boolean isExternal;

    @Schema(title = L_extInfo)
    String extInfo;

    @Size(max = 64)
    @Schema(title = L_contacts)
    String contacts;

    @Size(max = 20)
    @Schema(title = L_phones)
    String phones;

    @Size(max = 32)
    @Schema(title = L_emails)
    String emails;

    @Schema(title = L_address)
    String address;

    @Size(max = 32)
    @Schema(title = L_zipCode)
    String zipCode;

    @Size(max = 1800)
    @Schema(title = L_nodePath, description = D_nodePath)
    String nodePath;

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

    public UpdateTestOrgReq(String id) {
        this.id = id;
    }

    public UpdateTestOrgReq updateIdWhenNotBlank(String id) {
        if (isNotBlank(id)) {
            this.id = id;
        }
        return this;
    }

    @PostConstruct
    public void preUpdate() {
        // @todo 更新之前初始化数据

        if (getLastUpdateTime() == null) {
            setLastUpdateTime(new Date());
        }
    }
}
