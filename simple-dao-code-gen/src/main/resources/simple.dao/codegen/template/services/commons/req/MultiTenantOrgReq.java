package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.CtxVar;
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
 * 多租户多组织请求对象。
 *
 * <p>组织数据访问权限由变量注入提供方预先完成校验。本请求对象不校验 {@code orgIdList} 中的组织是否属于
 * 当前用户；查询、更新和删除优先用该列表限定旧记录范围，列表为空时按下述操作规则处理单个 {@code orgId}。
 * 在不安全上下文中，若没有全组织权限且没有
 * {@code orgIdList}/{@code orgId}，本请求对象会直接抛异常，不能退化为无组织条件查询。</p>
 *
 * <p>{@code isCanAccessAllOrg} 表示拥有全部组织权限：当没有 {@code orgIdList} 和 {@code orgId} 时，
 * 不追加组织条件；当操作者明确提供组织范围时，仍进入组织视角。{@code orgIdList} 优先于单个
 * {@code orgId}；后者在查询、删除和非管理员更新时用于单组织筛选，管理员更新时则表示新的组织归属。</p>
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 */
@Schema(title = "多租户多组织查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
@ToString(callSuper = true)
public class MultiTenantOrgReq<T extends MultiTenantOrgReq<T>>
        extends MultiTenantReq<T> implements OrganizedObject, OrganizedScopeObject{

    public static final String EXPR_IS_CAN_ACCESS_ALL_ORG = " (#" + InjectConst.IS_CAN_ACCESS_ALL_ORG + "?:false) ";
    public static final String EXPR_NOT_ALL_ORG_SCOPE = " !" + EXPR_IS_CAN_ACCESS_ALL_ORG;


    @Schema(title = "是否能访问所有组织", hidden = true)
    @InjectVar(InjectVar.SPEL_PREFIX + EXPR_IS_CAN_ACCESS_ALL_ORG)
    @Ignore
    protected boolean isCanAccessAllOrg = false;

    /**
     * 注入方已校验通过的可访问组织列表。
     *
     * <p>列表作为查询、更新和删除优先采用的 {@code WHERE org_id IN (...)} 条件。
     * 本类不重复校验列表成员的访问权限；但会在不安全上下文下校验是否存在组织范围。</p>
     */
    @InjectVar(value = InjectConst.ORG_ID_LIST
            , isOverride = InjectVar.SPEL_PREFIX + EXPR_NOT_ALL_ORG_SCOPE
            , isRequired = InjectVar.SPEL_PREFIX + EXPR_NOT_ALL_ORG_SCOPE
    )
    @Schema(title = "机构ID列表", description = "注入方已授权的组织范围，优先于orgId，并用于查询、更新和删除条件")

    @OrderBy(condition = "isOrganizedObject() && isEnableDefaultOrderBy() && #_isQuery && #isNotEmpty(#_fieldVal) && isContainsOrgPublicData() && !isOrgShared()", value = InjectConst.ORG_ID,
            order = Integer.MIN_VALUE + 1, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "本排序规则是本部门的数据排第一个，通常用于只取一个数据时，先取自己部门的数据")

    @OR(autoClose = true, condition = "isOrganizedObject()", desc = "组织范围、组织公共数据和组织共享数据取并集")

    @In(value = InjectConst.ORG_ID, condition = "orgIdListCondition(#_isUpdate)")
    @IsNull(condition = "orgDataCondition(#_isQuery, true)", value = InjectConst.ORG_ID, desc = "组织视角查询时，结果包含组织ID为空的公共数据")
    @Eq(condition = "orgDataCondition(#_isQuery, false)", value = "orgShared", paramExpr = "true", desc = "组织视角查询时，结果包含组织共享数据")
    protected Collection<String> orgIdList;

    /**
     * 单个组织 ID。
     *
     * <p>查询、删除和非管理员更新时，仅在 {@code orgIdList} 为空时作为单组织条件。
     * 管理员更新时作为新的组织归属值，不替代列表对旧记录的范围限制。
     * 因此受限外部管理员更新必须提供旧组织列表；全部组织权限者仍可不限制旧组织范围。</p>
     */
    @InjectVar(value = InjectConst.ORG_ID
            , isOverride = InjectVar.SPEL_PREFIX + EXPR_NOT_ALL_ORG_SCOPE
            , isRequired = "false"
    )
    @Schema(title = "机构ID", description = "查询、删除和非管理员更新时是列表为空时的筛选条件；管理员更新时是新组织归属")
    @Eq(condition = "orgIdCondition(#_isQuery, #_isDelete)", desc = "列表为空时，查询、删除及非管理员更新按单个组织ID筛选")
    @Update(condition = "isOrganizedObject() && #_isUpdate && isAdmin() && (#isNotEmpty(#_fieldVal) || isForceUpdateField(#_fieldName))", desc = "只有管理员才能变更归属的机构ID")
    protected String orgId;

    @Schema(title = "组织机构名称", hidden = true)
    @InjectVar(value = InjectConst.ORG_NAME, isRequired = "false")
    @Ignore
    protected String _orgName;

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

    protected void checkOrgScopeParam() {
        // 空成员可能被 IN 参数过滤掉，不能让一个无效列表退化为没有组织条件。
        if (orgIdList != null && orgIdList.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new IllegalArgumentException("组织范围列表不能包含空组织ID");
        }
        if (isUnsafeContext()
                && !isCanAccessAllOrg()
                && (orgIdList == null || orgIdList.isEmpty())
                && (orgId == null || orgId.isBlank())) {
            throw new IllegalArgumentException("必须指定组织");
        }
    }

    /** 组织列表条件入口：同时在不安全上下文复核组织范围。 */
    public boolean orgIdListCondition() {
        checkOrgScopeParam();
        return orgIdList != null && !orgIdList.isEmpty();
    }

    /** 管理员更新的 orgId 是 SET 新值，不能用于证明旧记录的组织范围已受限。 */
    public boolean orgIdListCondition(boolean isUpdateAction) {
        boolean hasOrgList = orgIdListCondition();
        if (isUpdateAction && isAdmin() && isUnsafeContext() && !isCanAccessAllOrg() && !hasOrgList) {
            throw new IllegalArgumentException("管理员更新必须指定旧记录的组织范围列表");
        }
        return hasOrgList;
    }

    /** 单组织条件的兼容入口；参数表示当前操作是否允许把单值作为筛选条件。 */
    public boolean orgIdCondition(boolean isQueryAction) {
        checkOrgScopeParam();
        return isQueryAction
                && (orgIdList == null || orgIdList.isEmpty())
                && orgId != null
                && !orgId.isBlank();
    }

    /** 管理员更新用于 SET；其他场景用于 WHERE。列表始终优先于单值。 */
    public boolean orgIdCondition(boolean isQueryAction, boolean isDeleteAction) {
        return orgIdCondition(isQueryAction || isDeleteAction || !isAdmin());
    }

    /** 组织公共或共享数据的查询条件入口。 */
    public boolean orgDataCondition(boolean isQueryAction, boolean isPublicData) {
        checkOrgScopeParam();
        boolean hasOrgView = (orgIdList != null && !orgIdList.isEmpty()) || (orgId != null && !orgId.isBlank());
        return isQueryAction
                && hasOrgView
                && (isPublicData
                ? this instanceof OrganizedPublicObject && isContainsOrgPublicData()
                : this instanceof OrganizedSharedObject && isOrgShared());
    }

    @Schema(title = "是否能访问所有组织", hidden = true)
    public boolean isCanAccessAllOrg() {
        return this.isCanAccessAllOrg;
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
