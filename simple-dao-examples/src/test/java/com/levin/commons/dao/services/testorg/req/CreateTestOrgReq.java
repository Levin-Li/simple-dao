package com.levin.commons.dao.services.testorg.req;

// import static com.levin.commons.ModuleOption.*;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.domain.E_TestOrg;
import com.levin.commons.dao.domain.TestOrg;
import com.levin.commons.dao.domain.TestOrg.*;
import com.levin.commons.dao.services.commons.req.MultiTenantReq;
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

////////////////////////////////////

/**
 * 新增测试机构
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[0e77668e5a138a37001b1b78bcd274c4]，请不要修改和删除此行内容。
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
@TargetOption(entityClass = TestOrg.class, alias = E_TestOrg.ALIAS)
public class CreateTestOrgReq extends MultiTenantReq {

    private static final long serialVersionUID = 2127591268L;

    @Schema(title = L_parentId)
    @Size(max = 64)
    String parentId;

    @Schema(title = L_code, description = D_code)
    @Size(max = 64)
    String code;

    @Schema(title = L_icon)
    String icon;

    @Schema(title = L_state)
    @NotNull
    State state;

    @Schema(title = L_type)
    @NotNull
    OrgType type;

    @Schema(title = L_industries)
    @Size(max = 64)
    String industries;

    @Schema(title = L_areaCode)
    @NotBlank
    @Size(max = 64)
    String areaCode;

    @Schema(title = L_level, description = D_level)
    @Size(max = 128)
    String level;

    @Schema(title = L_category, description = D_category)
    @NotBlank
    @Size(max = 128)
    String category;

    @Schema(title = L_isExternal)
    @NotNull
    Boolean isExternal;

    @Schema(title = L_extInfo)
    String extInfo;

    @Schema(title = L_contacts)
    @Size(max = 64)
    String contacts;

    @Schema(title = L_phones)
    @Size(max = 20)
    String phones;

    @Schema(title = L_emails)
    @Size(max = 32)
    String emails;

    @Schema(title = L_address)
    String address;

    @Schema(title = L_zipCode)
    @Size(max = 32)
    String zipCode;

    @Schema(title = L_idPath, description = D_idPath)
    @Size(max = 1800)
    String idPath;

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
