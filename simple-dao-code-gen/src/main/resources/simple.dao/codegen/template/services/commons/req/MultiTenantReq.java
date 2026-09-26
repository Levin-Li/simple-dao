package ${modulePackageName}.services.commons.req;

import cn.hutool.core.lang.Assert;
import com.levin.commons.dao.CtxVar;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.Validator;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.domain.*;
import com.levin.commons.rbac.DataMasking;
import com.levin.commons.rbac.RbacRoleInfo;
import com.levin.commons.rbac.ResAuthorize;
import com.levin.commons.service.domain.*;
import com.levin.commons.service.support.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;


/**
 * 多租户请求对象。
 *
 * <p>租户查询范围按最终的 {@code tenantId}、调用者身份和请求对象声明的公共/共享能力确定：</p>
 * <ul>
 *     <li>超管或 SaaS 管理员未指定 {@code tenantId} 时，不追加租户范围条件。</li>
 *     <li>平台用户未指定 {@code tenantId} 时，加入 {@code tenantId IS NULL} 条件。</li>
 *     <li>平台用户指定 {@code tenantId} 时，始终按该值查询；仅请求对象实现
 *     {@link MultiTenantPublicObject} 且允许包含公共数据时，额外加入 {@code tenantId IS NULL} 条件。</li>
 *     <li>租户用户的 {@code tenantId} 必须由服务端注入；仅请求对象实现
 *     {@link MultiTenantPublicObject} 且允许包含公共数据时，额外加入 {@code tenantId IS NULL} 条件。</li>
 *     <li>请求对象实现 {@link MultiTenantSharedObject} 且允许包含共享数据时，查询额外加入
 *     {@code tenantShared = true} 条件；该分支只用于查询。</li>
 * </ul>
 *
 * <p>租户用户的 {@code tenantId} 是必填的安全边界：注入阶段无法取得值必须抛异常；请求校验阶段也必须
 * 再次校验并抛异常，避免内部调用或构造请求对象时绕过变量注入。</p>
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Schema(title = "多租户查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
@ToString(callSuper = true)
public class MultiTenantReq<T extends MultiTenantReq<T>>
        extends BaseReq implements MultiTenantObject {

    /**
     * 最终参与租户隔离的租户 ID。
     *
     * <p>超管与 SaaS 管理员不强制覆盖；其他身份按 {@link InjectVar#isOverride()} 从上下文覆盖。
     * 非平台用户必须由上下文取得该值，不能信任请求端传入的值。</p>
     */
    @Schema(title = "租户ID", hidden = true, description = "租户ID：超管与SaaS管理员不强制覆盖；非平台用户由服务端注入且必须非空")
    @InjectVar(value = InjectConst.TENANT_ID
             // 除超管和 SaaS 管理员外，最终值由服务端上下文覆盖。
             , isOverride = InjectVar.SPEL_PREFIX + NOT_SUPER_ADMIN_AND_NOT_SAAS_ADMIN
             // 非平台用户（租户用户）缺少上下文租户 ID 时，注入阶段必须失败。
             , isRequired = InjectVar.SPEL_PREFIX + NOT_PLATFORM_USER
    )

    // 默认排序仅用于租户用户查询平台公共数据、且不包含共享数据的场景：把当前租户记录排在前面。
    // 若还包含 tenantShared 数据，单列 tenantId 的排序不能保证“当前租户记录优先”，故不启用该排序。
    @OrderBy(condition = "isMultiTenantObject() && isEnableDefaultOrderBy() && #_isQuery && !isPlatformUser() && #isNotEmpty(#_fieldVal) && isContainsPublicData() && !isTenantShared()",
            order = Integer.MIN_VALUE, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "本排序规则是把租户ID不为NULL的排在前面")

    // 三个候选分支属于同一组 OR；具体是否启用由 tenantIsNullCondition、tenantSharedCondition 决定。
    @OR(autoClose = true, condition = "isMultiTenantObject()", desc = "仅多租户对象启用；租户、平台和共享分支取并集")
    @Eq
    @IsNull(condition = "tenantIsNullCondition(#_isQuery)", desc = "")
    @Eq(condition     = "tenantSharedCondition(#_isQuery)", value = "tenantShared", paramExpr = "true", desc = "如果有平台可共享的租户数据，查询结果包括非该租户的数据")
    @DataMasking(showAuthorize = @ResAuthorize(anyRoles = {RbacRoleInfo.SA_ROLE, RbacRoleInfo.SAAS_ROLE_PREFIX + "*"}), remark = "SAAS管理员才能显示")
    protected String tenantId;

    @Schema(title = "租户名称", hidden = true)
    @InjectVar(value = InjectConst.TENANT_NAME, isRequired = "false")
    @Ignore
    protected String _tenantName;

    /**
     * 在 SpEL 条件计算前复核租户用户的租户 ID。
     *
     * <p>该方法刻意保持 {@code protected}：它由公开的 SpEL 入口方法调用，而不作为 SpEL 的直接入口。
     * 这用于拦截未经过 {@link InjectVar} 注入、但被内部代码直接构造的租户用户请求。</p>
     */
    protected void checkTenantIdParam(){

        if(isTenantUser()){
            //不允许访问平台的数据
            Assert.notBlank(tenantId, "非法的越界访问-1");

            //不允许访问其他租户的数据
            Assert.isTrue(tenantId.equals(get_currentUserTenantId()), "非法的越界访问-2");
        }
    }

    /**
     * 判断查询是否处于超管或 SaaS 管理员的全局视角。
     *
     * <p>全局视角仅适用于查询且未指定租户 ID 的情形。此时租户 ID、平台公共数据和租户共享数据
     * 均不应追加过滤条件；更新、删除仍沿用各自的安全范围。</p>
     *
     * @param isQueryAction 是否为查询动作
     * @return 是否不应追加任何租户范围条件
     */
    protected boolean isUnscopedPlatformAdminQuery(boolean isQueryAction) {
        return isQueryAction
                && (isSuperAdmin() || isSaasAdmin())
                && (tenantId == null || tenantId.isBlank());
    }

    /**
     * 判断是否生成 {@code tenantId IS NULL} 分支。
     *
     * <p>此方法是 SpEL 的公开入口，不能降为 {@code protected/private}。查询时，平台用户未指定
     * {@code tenantId} 会生成该分支；平台用户已指定 {@code tenantId} 与租户用户则仅在请求对象实现
     * {@link MultiTenantPublicObject} 且 {@link #isContainsPublicData()} 为 {@code true} 时生成。
     * 更新、删除时仅允许未指定租户 ID 的平台用户命中平台数据。</p>
     *
     * @param isQueryAction 是否为查询动作
     * @return 是否生成 {@code tenantId IS NULL} 条件
     */
    public boolean tenantIsNullCondition(boolean isQueryAction){

        //优先检查租户用户必须设置租户ID
        checkTenantIdParam();

        // 超管或 SaaS 管理员未指定租户时处于全局视角，不能被 tenantId IS NULL 收窄范围。
        if (isUnscopedPlatformAdminQuery(isQueryAction)) {
            return false;
        }

        if(isQueryAction) {
            //平台用户天然对住户ID为空的数据，也就是平台数据拥有权限;
            //非平台用户只能对租户共享对象，并且当前请求说要包含共享对象的时候

            if(isPlatformUser()){
                //无租户Id(那就是平台用户查平台的数据), 或是平台用户用租户的逻辑查询,平台公开并且当前要求查询公开数据
                return (tenantId == null || tenantId.isBlank()) || ((this instanceof MultiTenantPublicObject) && isContainsPublicData());
            }else {
                //当前是租户公开数据，并且当前查询要求包含公开数据
                return ((this instanceof MultiTenantPublicObject) && isContainsPublicData());
            }

        }else {
            //如果是更新或删除动作
            return isPlatformUser() && (tenantId == null || tenantId.isBlank());
        }

    }
    /**
     * 判断是否生成 {@code tenantShared = true} 分支。
     *
     * <p>此方法是 SpEL 的公开入口。共享分支只用于查询，且仅当实体实现
     * {@link MultiTenantSharedObject}、请求明确包含共享数据时生成；它与平台数据分支相互独立。</p>
     *
     * @param isQueryAction 是否为查询动作
     * @return 是否生成 {@code tenantShared = true} 条件
     */
    public boolean tenantSharedCondition(boolean isQueryAction){

        checkTenantIdParam();

        // 与 tenantIsNullCondition 保持一致：全局视角不追加 tenantShared 过滤。
        if (isUnscopedPlatformAdminQuery(isQueryAction)) {
            return false;
        }

        return isQueryAction && ((this instanceof MultiTenantSharedObject) && isTenantShared());
    }

    /**
     * 请求是否允许包含平台公共数据；具体请求类型按实体是否实现
     * {@link MultiTenantPublicObject} 覆盖该默认值。
     *
     * @return 是否包含平台公共数据
     */
    @Schema(title = "请求是否包含平台的公共数据", hidden = true)
    public boolean isContainsPublicData() {
        return false;
    }

    /**
     * 请求是否允许包含租户共享数据；具体请求类型可按实体能力覆盖该默认值。
     *
     * @return 是否包含租户共享数据
     */
    @Schema(title = "请求是否包含可分享的数据", hidden = true)
    public boolean isTenantShared() {
        return false;
    }

    /**
     * 设置租户ID
     * @param tenantId
     * @return
     */
    public T setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return (T) this;
    }

}
