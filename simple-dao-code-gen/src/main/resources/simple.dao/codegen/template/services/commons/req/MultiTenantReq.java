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
 * <p>按三类用户与两类操作划分。超管类包含超级管理员和平台管理员；租户用户包含租户管理员。
 * 平台数据指 {@code tenantId IS NULL}；表中的公共/共享扩展均要求对应能力接口与请求开关同时允许。</p>
 * <table border="1">
 * <caption>租户数据范围：查询与更新/删除</caption>
 * <tr><th>用户与租户视角</th><th>查询</th><th>更新、删除</th></tr>
 * <tr><td>超管类，未指定租户</td><td>不追加任何租户条件</td><td>不追加任何租户条件</td></tr>
 * <tr><td>超管类，指定租户</td><td>指定租户 OR 平台数据，可额外加入共享数据</td>
 *     <td>与查询相同，可额外加入共享数据</td></tr>
 * <tr><td>普通平台用户，未指定租户</td><td>仅平台数据，不追加租户共享条件</td><td>仅平台数据</td></tr>
 * <tr><td>普通平台用户，指定租户（非安全上下文）</td><td>指定租户，可额外加入公共、共享数据</td><td>拒绝写入租户数据</td></tr>
 * <tr><td>普通平台用户，指定租户（可信内部调用）</td><td>指定租户，可额外加入公共、共享数据</td><td>指定租户 OR 平台数据；权限责任由调用方承担</td></tr>
 * <tr><td>租户用户，自身租户</td><td>自身租户，可额外加入公共、共享数据</td><td>仅精确匹配自身租户</td></tr>
 * </table>
 * <p>设计理由：租户 ID 是隔离边界，普通用户的可读权限不等于可写权限。超管类原本可操作任意租户，
 * 指定租户及共享开关用于主动收窄已有权限、减少误操作，因此其读写范围一致。
 * 所有租户 OR 分支必须成组，再与主键、乐观锁、组织和个人范围等条件取 AND。</p>
 *
 * <p>租户用户的 {@code tenantId} 在非安全上下文中是必填的安全边界：注入阶段无法取得值必须抛异常；DAO
 * 构建条件时也必须再次校验。可信内部调用的权限责任由调用方承担。租户用户的更新、删除只匹配自身租户，
 * 绝不因公共或共享查询开关而扩大写入范围。在不安全上下文中，普通平台用户不得通过指定租户 ID 写入租户数据。</p>
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
     * <p>超管与平台管理员可选择全局或指定租户视角。租户用户则必须由服务端上下文覆盖此值，
     * 且不能为空；绝不能信任请求端传入的租户 ID，否则会造成跨租户读写。</p>
     */
    @Schema(title = "租户ID", hidden = true, description = "租户ID：超管与平台管理员不强制覆盖；非平台用户由服务端注入且必须非空")
    @InjectVar(value = InjectConst.TENANT_ID
             // 除超管和平台管理员外，最终值由服务端上下文覆盖。
             , isOverride = InjectVar.SPEL_PREFIX + EXPR_NOT_SUPER_ADMIN_AND_NOT_PLATFORM_ADMIN
             // 租户用户缺少上下文租户 ID 时，注入阶段必须失败，不能降级为无租户条件。
             , isRequired = InjectVar.SPEL_PREFIX + EXPR_NOT_PLATFORM_USER
    )

    // 默认排序仅用于租户用户查询平台公共数据、且不包含共享数据的场景：把当前租户记录排在前面。
    // 若还包含 tenantShared 数据，单列 tenantId 的排序不能保证“当前租户记录优先”，故不启用该排序。
    @OrderBy(condition = "isMultiTenantObject() && isEnableDefaultOrderBy() && #_isQuery && !isPlatformUser() && #isNotEmpty(#_fieldVal) && isContainsPublicData() && !isTenantShared()",
            order = Integer.MIN_VALUE, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "本排序规则是把租户ID不为NULL的排在前面")

    // 三个候选分支属于同一组 OR；具体是否启用由 tenantIsNullCondition、tenantSharedCondition 决定。
    @OR(autoClose = true, condition = "isMultiTenantObject()", desc = "仅多租户对象启用；租户、平台和共享分支取并集")
    @Eq
    @IsNull(condition = "tenantIsNullCondition(#_isQuery)", desc = "")
    @Eq(condition     = "tenantSharedCondition(#_isQuery)", value = "tenantShared", paramExpr = "true", desc = "额外共享分支：普通用户仅查询，超管类查询、更新、删除均可按开关包含其他租户共享数据")
    @DataMasking(showAuthorize = @ResAuthorize(anyRoles = {RbacRoleInfo.PLATFORM_ROLE_PREFIX + "*"}), remark = "平台管理员才能显示")
    protected String tenantId;

    @Schema(title = "租户名称", hidden = true)
    @InjectVar(value = InjectConst.TENANT_NAME, isRequired = "false")
    @Ignore
    protected String _tenantName;

    /**
     * 在 SpEL 条件计算前复核租户用户的租户 ID。
     *
     * <p>注入阶段负责覆盖并校验当前租户；这里是非安全上下文中条件构建前的第二道防线。
     * 租户 ID 缺失或不等于当前用户租户时必须抛错，绝不能生成跨租户条件；可信内部调用
     * 不执行该复核，权限责任由调用方承担。</p>
     */
    protected void checkTenantIdParam(){

        if(isTenantUser() && isUnsafeContext()){
            // 基础租户范围必须是自身租户；额外公共/共享读取权限由查询分支单独控制。
            Assert.notBlank(tenantId, "非法的越界访问-1");

            Assert.isTrue(tenantId.equals(get_currentUserTenantId()), "非法的越界访问-2");
        }
    }

    /**
     * 判断是否生成 {@code tenantId IS NULL} 分支。
     *
     * <p>此方法是 SpEL 的公开入口，详细矩阵及设计理由见类注释。租户用户只在查询且具备公共能力与开关时加入平台公共数据。
     * 超管/平台管理员的查询、更新、删除规则一致：指定租户时加入平台数据，未指定时不追加条件。
     * 普通平台用户未指定租户时限定为平台数据，指定时只能以租户视角查询；仅在不安全上下文中拒绝其写操作。</p>
     *
     * @param isQueryAction 是否为查询动作
     * @return 是否生成 {@code tenantId IS NULL} 条件
     */
    public boolean tenantIsNullCondition(boolean isQueryAction){

        Assert.isTrue(isPlatformUser() || isTenantUser(), "当前用户必须是平台用户或租户用户");

        // 优先检查非安全上下文中的租户用户必须设置租户 ID。
        checkTenantIdParam();

        boolean tenantSpecified = tenantId != null && !tenantId.isBlank();

        // 租户身份的边界优先，管理员标志也不能绕过当前租户约束。
        if (isTenantUser()) {
            return isQueryAction && this instanceof MultiTenantPublicObject && isContainsPublicData();
        }

        // 超管和平台管理员在查询、更新、删除时采用同一租户视角：
        // 未指定租户不追加条件；指定租户则由 @Eq 与本 @IsNull 组成“指定租户或平台数据”。
        if (isSuperAdmin() || isPlatformAdmin()) {
            return tenantSpecified;
        }

        if (isQueryAction) {
            // 默认身份已经在上方按租户用户处理；剩余有效上下文均为平台用户。
            return !tenantSpecified || (this instanceof MultiTenantPublicObject && isContainsPublicData());
        } else {
            // 更新、删除采用精确写入范围，不能复用查询的公共/共享数据语义；
            // 租户用户在上方校验后只保留 @Eq 的当前租户条件。
            // 外部、不可信调用中，普通平台用户只能操作 tenant_id 为空的平台数据；
            // 内部可信调用由调用方承担权限责任，可按指定租户及平台数据范围操作。
            Assert.isTrue(!isUnsafeContext() || !tenantSpecified, "当前平台用户不能变更租户数据");
            return true;
        }
    }

    /**
     * 判断是否生成 {@code tenantShared = true} 分支。
     *
     * <p>此方法是 SpEL 的公开入口。共享分支仅当实体实现
     * {@link MultiTenantSharedObject}、请求明确包含共享数据且指定租户时生成。
     * 无租户的普通平台用户仅访问平台数据；无租户的管理员已处于全局视角；两者均不追加共享分支。</p>
     * <p>普通用户的共享权限仅用于读取。超管/平台管理员本来具有全局操作权限，指定租户及共享开关
     * 是主动收窄范围，因此查询、更新、删除使用相同共享范围；租户身份不能借管理员标志扩大写入权限。</p>
     *
     * @param isQueryAction 是否为查询动作
     * @return 是否生成 {@code tenantShared = true} 条件
     */
    public boolean tenantSharedCondition(boolean isQueryAction){

        Assert.isTrue(isPlatformUser() || isTenantUser(), "当前用户必须是平台用户或租户用户");

        checkTenantIdParam();

        boolean allowShared = isQueryAction || isSuperAdmin() || isPlatformAdmin();

        return allowShared && tenantId != null && !tenantId.isBlank()
                && this instanceof MultiTenantSharedObject && isTenantShared();
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
