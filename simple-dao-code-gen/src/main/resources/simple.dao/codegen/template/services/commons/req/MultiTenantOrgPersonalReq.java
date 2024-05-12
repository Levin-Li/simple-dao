package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.*;
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
 * 多租户个人查询对象
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Schema(title = "多租户多部门个人查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
@ToString(callSuper = true)
public class MultiTenantOrgPersonalReq<T extends MultiTenantOrgPersonalReq<T>>
        extends MultiTenantOrgReq<T>
        implements PersonalObject {

    @InjectVar(value = InjectConst.USER_ID
            , isOverride = InjectVar.SPEL_PREFIX + NOT_SUPER_SAAS_TENANT_ADMIN // 如果不是超管 不是SAAS管理员 也不是 租户管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + NOT_SUPER_SAAS_TENANT_ADMIN // 如果不是超管 不是SAAS管理员 也不是 租户管理员，那么值是必须的
    )
    @Schema(title = "拥有者Id" , hidden = true)
    @Eq
    protected String ownerId;

    /**
     * 设置个人ID
     * @param ownerId
     * @return
     */
    public T setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return (T) this;
    }
}
