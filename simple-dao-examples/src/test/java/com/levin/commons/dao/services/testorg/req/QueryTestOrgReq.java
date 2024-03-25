package com.levin.commons.dao.services.testorg.req;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.Lte;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.order.SimpleOrderBy;
import com.levin.commons.dao.domain.E_TestOrg;
import com.levin.commons.dao.domain.TestOrg;
import com.levin.commons.dao.domain.TestOrg.*;
import com.levin.commons.dao.services.commons.req.MultiTenantReq;
import com.levin.commons.dao.services.testorg.info.TestOrgInfo;
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
 * 查询测试机构
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[cad2f0cae947f778749f11ff14aede3f]，请不要修改和删除此行内容。
 */
@Schema(title = QUERY_ACTION + BIZ_NAME)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// @EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = TestOrg.class, alias = E_TestOrg.ALIAS, resultClass = TestOrgInfo.class)
public class QueryTestOrgReq extends MultiTenantReq {

    private static final long serialVersionUID = 2127591268L;

    @Ignore
    @Schema(title = "排序字段")
    String orderBy;

    // @Ignore
    @Schema(title = "排序方向")
    @SimpleOrderBy(
            expr = "orderBy + ' ' + orderDir",
            condition = "#isNotEmpty(orderBy) && #isNotEmpty(orderDir)",
            remark = "生成排序表达式")
    @OrderBy(
            value = createTime,
            condition = "#isEmpty(orderBy) || #isEmpty(orderDir)",
            order = Integer.MAX_VALUE,
            scope = OrderBy.Scope.OnlyForNotGroupBy,
            desc = "默认按时间排序")
    OrderBy.Type orderDir;

    @NotBlank
    @Size(max = 64)
    @Schema(title = L_id)
    String id;

    @Size(max = 64)
    @Schema(title = L_parentId)
    String parentId;

    @Size(max = 64)
    @Schema(title = L_code, description = D_code)
    String code;

    @Schema(title = "模糊匹配-" + L_code, description = D_code)
    @Contains
    String containsCode;

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

    @Schema(title = "模糊匹配-" + L_areaCode)
    @Contains
    String containsAreaCode;

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

    @Schema(title = "模糊匹配-" + L_contacts)
    @Contains
    String containsContacts;

    @Size(max = 20)
    @Schema(title = L_phones)
    String phones;

    @Schema(title = "模糊匹配-" + L_phones)
    @Contains
    String containsPhones;

    @Size(max = 32)
    @Schema(title = L_emails)
    String emails;

    @Schema(title = L_address)
    String address;

    @Schema(title = "模糊匹配-" + L_address)
    @Contains
    String containsAddress;

    @Size(max = 32)
    @Schema(title = L_zipCode)
    String zipCode;

    @Schema(title = "是否加载" + L_parent)
    @Fetch(attrs = E_TestOrg.parent, condition = "#_val == true")
    Boolean loadParent;

    @Schema(title = "是否加载" + L_children)
    @Fetch(attrs = E_TestOrg.children, condition = "#_val == true")
    Boolean loadChildren;

    @Size(max = 1800)
    @Schema(title = L_nodePath, description = D_nodePath)
    String nodePath;

    @NotBlank
    @Size(max = 128)
    @Schema(title = L_name)
    String name;

    @Schema(title = "模糊匹配-" + L_name)
    @Contains
    String containsName;

    @Size(max = 128)
    @Schema(title = L_pinyinName, description = D_pinyinName)
    String pinyinName;

    @Schema(title = "模糊匹配-" + L_pinyinName, description = D_pinyinName)
    @Contains
    String containsPinyinName;

    @Size(max = 128)
    @Schema(title = L_creator)
    String creator;

    @NotNull
    @Schema(title = L_createTime, description = "大于等于" + L_createTime)
    @Gte
    Date gteCreateTime;

    @Schema(title = L_createTime, description = "小于等于" + L_createTime)
    @Lte
    Date lteCreateTime;

    // @Schema(title = L_createTime + "-日期范围")
    // @Between(paramDelimiter = "-")
    // String betweenCreateTime;

    @Schema(title = L_lastUpdateTime, description = "大于等于" + L_lastUpdateTime)
    @Gte
    Date gteLastUpdateTime;

    @Schema(title = L_lastUpdateTime, description = "小于等于" + L_lastUpdateTime)
    @Lte
    Date lteLastUpdateTime;

    // @Schema(title = L_lastUpdateTime + "-日期范围")
    // @Between(paramDelimiter = "-")
    // String betweenLastUpdateTime;

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

    public QueryTestOrgReq(String id) {
        this.id = id;
    }

    @PostConstruct
    public void preQuery() {
        // @todo 查询之前初始化数据
    }
}
