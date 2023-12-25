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
public class MultiTenantOrgReq<T extends MultiTenantOrgReq>
        extends MultiTenantReq<T>
        implements OrganizedObject {

    //注入当前用户有权限的机构ID列表
    @InjectVar(value = InjectConst.ORG_ID_LIST
            , isOverride = InjectVar.SPEL_PREFIX + NOT_SUPER_ADMIN_AND_NOT_TENANT_ADMIN // 如果不是超管 也不是 租户管理员, 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + NOT_SUPER_ADMIN_AND_NOT_TENANT_ADMIN // 如果不是超管 也不是 租户管理员，那么值是必须的
    )
    @Schema(title = "机构ID列表", description = "过滤出指定的机构的数据")
    @In(InjectConst.ORG_ID)
    @Validator(expr= NOT_SUPER_ADMIN_AND_NOT_TENANT_ADMIN + " || !isNotEmpty(#_fieldVal) ")
    protected List<String> orgIdList;

    /**
     * 设置部门ID
     * @param orgId
     * @return
     */
    public T setOrgId(String orgId) {
        this.orgId = orgId;
        return (T) this;
    }

}
