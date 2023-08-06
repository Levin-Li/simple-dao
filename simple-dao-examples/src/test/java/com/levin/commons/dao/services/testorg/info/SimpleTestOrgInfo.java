package com.levin.commons.dao.services.testorg.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.TestOrg.*;
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
import java.util.Set;

import static com.levin.commons.dao.domain.E_TestOrg.*;

////////////////////////////////////

/**
 * 测试机构
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[a84a970707a179db18caa72a0ff5937a]，请不要修改和删除此行内容。
 */
@Schema(title = BIZ_NAME)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(
        exclude = {
            "parent",
            "children",
        })
@FieldNameConstants
@JsonIgnoreProperties({tenantId})
@Select
public class SimpleTestOrgInfo implements Serializable {

    private static final long serialVersionUID = 2127591268L;

    @NotBlank
    @Size(max = 64)
    @Schema(title = L_id)
    String id;

    @Size(max = 64)
    @Schema(title = L_parentId)
    String parentId;

    @Size(max = 64)
    @Schema(title = L_tenantId)
    String tenantId;

    @Size(max = 64)
    @Schema(title = L_code, description = D_code)
    String code;

    @Schema(title = L_icon)
    String icon;

    @NotNull
    @Schema(title = L_state)
    State state;

    @NotNull
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

    @NotNull
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

    // @Fetch //默认不加载，请通过查询对象控制
    @Schema(title = L_parent)
    TestOrgInfo parent;

    // @Fetch //默认不加载，请通过查询对象控制
    @Schema(title = L_children)
    Set<TestOrgInfo> children;

    @Size(max = 1800)
    @Schema(title = L_idPath, description = D_idPath)
    String idPath;

    @NotBlank
    @Size(max = 128)
    @Schema(title = L_name)
    String name;

    @Size(max = 128)
    @Schema(title = L_pinyinName, description = D_pinyinName)
    String pinyinName;
}
