package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.update.Update;
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

import java.util.Collection;
import java.util.List;

/**
 * 多租户多部门查询对象
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 */
@Schema(title = "多租户多组织查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
@ToString(callSuper = true)
public class MultiTenantOrgReq<T extends MultiTenantOrgReq<T>>
        extends MultiTenantReq<T> implements OrganizedScopeObject {

    public static final String IS_ALL_ORG_SCOPE = " (#" + InjectConst.IS_ALL_ORG_SCOPE + "?:false) ";
    public static final String NOT_ALL_ORG_SCOPE = " !" + IS_ALL_ORG_SCOPE;

    @Schema(title = "是否能访问所有组织", hidden = true)
    @InjectVar(InjectVar.SPEL_PREFIX + IS_ALL_ORG_SCOPE)
    @Ignore
    protected boolean isAllOrgScope = false;

    //注入当前用户有权限的机构ID列表
    @InjectVar(value = InjectConst.ORG_ID_LIST
            , isOverride = InjectVar.SPEL_PREFIX + NOT_ALL_ORG_SCOPE // 如果不是超管 也不是 租户管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + NOT_ALL_ORG_SCOPE // 如果不是超管 也不是 租户管理员，那么值是必须的
    )
    @Schema(title = "机构ID列表", description = "查询指定机构的数据，未指定时默认查询所有有权限的数据, 该参数只对查询操作有效")
    @OrderBy(condition = "enableDefaultOrderBy && #_isQuery && !(isSuperAdmin || isSaasAdmin || isTenantAdmin) && !isAllOrgScope && isContainsOrgPublicData() && #isNotEmpty(#_fieldVal) && !isOrgShared()", value = InjectConst.ORG_ID,
            order = Integer.MIN_VALUE + 1, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "本排序规则是本部门的数据排第一个，通常用于只取一个数据时，先取自己部门的数据")
    @OR(autoClose = true, desc = "查询、更新和删除都会增加这个条件")
    @In(InjectConst.ORG_ID)
    @IsNull(condition = "#_isQuery && !(isSuperAdmin || isSaasAdmin || isTenantAdmin) && !isAllOrgScope && isContainsOrgPublicData() && #isNotEmpty(#_fieldVal)", value = InjectConst.ORG_ID, desc = "查询结果包含租户内的公共数据(orgId为NULL的数据)，不仅仅是本部门数据")
    @Eq(condition = "#_isQuery && !isAllOrgScope && isOrgShared()", value = "orgShared", paramExpr = "true", desc = "如果有可共享的部门数据，允许包括非该部门的数据")
    protected Collection<String> orgIdList;

    //注入当前用户有权限的机构ID列表
    @InjectVar(value = InjectConst.ORG_ID
            , isOverride = InjectVar.SPEL_PREFIX + NOT_ALL_ORG_SCOPE // 如果不是超管 也不是 租户管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + NOT_ALL_ORG_SCOPE // 如果不是超管 也不是 租户管理员，那么值是必须的
    )
    @Schema(title = "机构ID", description = "通常用于创建和更新orgId，机构ID默认从当前用户获取")
    @Update(condition = "(#_isUpdate) && (isSuperAdmin || isSaasAdmin || isTenantAdmin) && (#isNotEmpty(#_fieldVal) || isForceUpdateField(#_fieldName))", desc = "只有管理员才能变更归属的机构ID") // 正常来说只允许管理员修改部门数据的归属 (isSuperAdmin || isSaasAdmin || isTenantAdmin)
    protected String orgId;

    @Schema(title = "组织机构名称", hidden = true)
    @InjectVar(value = InjectConst.ORG_NAME, isRequired = "false")
    @Ignore
    protected String orgName;

    /**
     * 是否为公共数据
     *
     * @return
     */
    @Schema(title = "是否包含组织的公共的数据", hidden = true)
    public boolean isContainsOrgPublicData() {
        return false;
    }

    /**
     * 是否为可分享的数据
     *
     * @return
     */
    @Schema(title = "请求是否包含组织可共享的数据", hidden = true)
    public boolean isOrgShared() {
        return false;
    }

    @Schema(title = "是否能访问所有组织", hidden = true)
    public boolean isAllOrgScope() {
        return this.isAllOrgScope;
    }

    /**
     * 设置部门ID列表
     *
     * @param orgIdList
     * @return
     */
    public T setOrgIdList(Collection<String> orgIdList) {
        this.orgIdList = orgIdList;
        return (T) this;
    }

    /**
     * 设置部门ID
     *
     * @param orgId
     * @return
     */
    public T setOrgId(String orgId) {
        this.orgId = orgId;
        return (T) this;
    }

}
