package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.Validator;
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
public class MultiTenantReq<T extends MultiTenantReq>
        extends BaseReq
        implements MultiTenantObject {

    @Schema(title = "租户ID", hidden = true)
    @InjectVar(value = InjectConst.TENANT_ID
            , isOverride = InjectVar.SPEL_PREFIX + NOT_SUPER_ADMIN // 如果不是超级管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + NOT_SUPER_ADMIN // 如果不是超级管理员，那么值是必须的
    )
    @OR(autoClose = true)
    @Eq
    @IsNull(condition = "#_isQuery && !isSuperAdmin && isContainsPublicData() ", desc = "如果是公共数据，允许包括非该租户的数据")
    @Eq(condition = "#_isQuery && !isSuperAdmin && isTenantShared()", value = "tenantShared", paramExpr = "true", desc = "如果有可共享的数据，允许包括非该租户的数据")
    //@Validator(expr = "isSuperAdmin || #isNotEmpty(#_fieldVal) " , promptInfo = "tenantId-不能为空")
    protected String tenantId;

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
