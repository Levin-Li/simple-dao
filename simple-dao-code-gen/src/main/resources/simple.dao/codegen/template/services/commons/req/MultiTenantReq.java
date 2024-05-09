package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.Validator;
import com.levin.commons.dao.annotation.order.OrderBy;
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


/**
 * 多租户查询对象
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
        extends BaseReq
        implements MultiTenantObject {

    @Schema(title = "租户ID", hidden = true,description = "租户ID默认从当前用户获取，超管可以设置，其他身份设置无效，服务端将自动覆盖字段值")
    @InjectVar(value = InjectConst.TENANT_ID
            , isOverride = InjectVar.SPEL_PREFIX + NOT_SUPER_ADMIN // 如果不是超级管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + NOT_SUPER_ADMIN // 如果不是超级管理员，那么值是必须的
    )
    @OrderBy(condition = "enableDefaultOrderBy && #_isQuery && isSuperAdmin && #isEmpty(#_fieldVal)", type = OrderBy.Type.Asc
            , order = Integer.MIN_VALUE, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "本排序规则是把租户ID为NULL的排在前面")
    @OrderBy(condition = "enableDefaultOrderBy && #_isQuery && !isSuperAdmin && #isNotEmpty(#_fieldVal) && isContainsPublicData() && !isTenantShared()",
            order = Integer.MIN_VALUE, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "本排序规则是把租户ID不为NULL的排在前面")
    @OR(autoClose = true)
    @Eq
    @IsNull(condition = "#_isQuery && !isSuperAdmin && isContainsPublicData() && #isNotEmpty(#_fieldVal)", desc = "查询结果包含公共数据(tenantId为NULL的数据)")
    @Eq(condition = "#_isQuery && !isSuperAdmin && isTenantShared()", value = "tenantShared", paramExpr = "true", desc = "如果有平台可共享的租户数据，查询结果包括非该租户的数据")
    //@Validator(expr = "isSuperAdmin || #isNotEmpty(#_fieldVal) " , promptInfo = "tenantId-不能为空")
    protected String tenantId;

    @Schema(title = "租户名称", hidden = true)
    @InjectVar(value = InjectConst.TENANT_NAME, isRequired = "false")
    @Ignore
    protected String tenantName;

    /**
     * 是否为公共数据
     *
     * @return
     */
    @Schema(title = "请求是否包含平台的公共数据", hidden = true)
    public boolean isContainsPublicData() {
        return false;
    }

    /**
     * 是否为可分享的数据
     *
     * @return
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
