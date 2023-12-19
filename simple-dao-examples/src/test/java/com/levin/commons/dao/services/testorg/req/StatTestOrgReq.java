package com.levin.commons.dao.services.testorg.req;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.annotation.Lte;
import com.levin.commons.dao.annotation.misc.Fetch;
import com.levin.commons.dao.annotation.stat.Count;
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
import java.io.Serializable;
import java.util.Date;

import static com.levin.commons.dao.domain.E_TestOrg.*;
import static com.levin.commons.dao.domain.EntityConst.*;

////////////////////////////////////

/**
 * 统计测试机构
 *
 * @author Auto gen by simple-dao-codegen, @time: 2023年8月6日 下午6:02:04, 代码生成哈希校验码：[c1ee3ebb8eebe39fbc7ba3260839226e]，请不要修改和删除此行内容。
 */
@Schema(title = STAT_ACTION + BIZ_NAME)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// @EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(
        entityClass = TestOrg.class,
        alias = E_TestOrg.ALIAS,
        // 连接统计
        // joinOptions = { @JoinOption(entityClass = XXX.class,alias = E_XXX.ALIAS,joinColumn =
        // E_XXX.joinColumn)},
        resultClass = StatTestOrgReq.Result.class)
public class StatTestOrgReq extends MultiTenantReq {

    private static final long serialVersionUID = 2127591268L;

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

    public StatTestOrgReq(String id) {
        this.id = id;
    }

    //
    // @Schema(description = "是否按状态分组统计")
    // @CtxVar //增加当前字段名称和字段值到环境变量中
    // @Ignore
    // private boolean isGroupByStatus;

    // @Schema(description = "是否按日期分组统计")
    // @CtxVar //增加当前字段名称和字段值到环境变量中
    // @Ignore //
    // private boolean isGroupByDate;

    @PostConstruct
    public void preStat() {
        // @todo 统计之前初始化数据
    }

    @Schema(description = BIZ_NAME + "统计结果")
    @Data
    @Accessors(chain = true)
    @FieldNameConstants
    public static class Result implements Serializable {

        // @Schema(description = "状态分组统计")
        // @GroupBy(condition = "#isGroupByStatus")
        // Status status;

        // @Schema(description = "时间分组统计")
        // @GroupBy(condition = "#isGroupByDate", value = "date_format(" + E_TestOrg.createDate +
        // ",'%Y-%m-%d')", orderBy = @OrderBy(type = OrderBy.Type.Asc))
        // String createDate;

        @Schema(description = "记录数")
        @Count
        Integer cnt;

        // @Schema(description = "分类记录数")
        // @Count(fieldCases = {@Case(column = E_TestOrg.status, whenOptions = {@Case.When(whenExpr
        // = "OFF", thenExpr = "1")}, elseExpr = "NULL")})
        // Integer caseCnt;

        // @Schema(description = "累计" , havingOp=Op.Gt)
        // @Sum
        // Double sumGmv;

    }
}
