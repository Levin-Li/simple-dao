package ${modulePackageName}.services.commons.req;

import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.*;
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


/**
 * 多租户多组织个人请求对象。
 *
 * <p>在租户、组织范围之外叠加个人拥有者范围。不安全上下文下，无个人访问权限用户只能操作
 * {@code ownerId} 等于当前用户 ID 的数据；非管理员删除同样只能删除自己的数据；修改拥有者归属仅管理员允许。</p>
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
        extends MultiTenantOrgReq<T> implements PersonalObject{

    /** 拥有者范围列表，优先于单个 ownerId；不得在受限个人视角扩大到其他用户。 */
    @Schema(title = "拥有者ID列表", description = "拥有者范围列表，优先于ownerId，用于查询、更新和删除条件", hidden = true)
    @In(value = "ownerId", condition = "ownerIdListCondition(#_isDelete)")
    protected Collection<String> ownerIdList;

    /** 单个拥有者 ID；受限个人视角必须等于当前用户，更新归属仅管理员允许。 */
    @Schema(title = "拥有者Id" , hidden = true)
    @Eq(condition = "ownerIdCondition(#_isQuery, #_isDelete)" , desc = "个人范围或用户主动指定的单拥有者查询条件")
    @Update(condition = "isPersonalObject() && (#_isUpdate) && isAdmin()  && (#isNotEmpty(#_fieldVal) || isForceUpdateField(#_fieldName))", desc = "只有管理员才能变更数据的拥有者")
    protected String ownerId;

    /**
     * 设置拥有者ID列表
     * @param ownerIdList
     * @return
     */
    public T setOwnerIdList(Collection<String> ownerIdList) {
        this.ownerIdList = ownerIdList;
        return (T) this;
    }

    /**
     * 设置个人ID
     * @param ownerId
     * @return
     */
    public T setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return (T) this;
    }

    protected void checkOwnerScope(boolean isDeleteAction) {
        // 外部调用中，查看权限不等于删除他人数据的权限。
        if (!isUnsafeContext()) return;
        boolean mustBeCurrentUser = !isCanVisitPersonalData() || (isDeleteAction && !isAdmin());
        if (!mustBeCurrentUser) return;
        if (ownerId == null || ownerId.isBlank() || !ownerId.equals(get_currentUserId()))
            throw new IllegalStateException("非法的越界访问：只能操作当前用户的个人数据");
        if (ownerIdList != null && ownerIdList.stream().anyMatch(id -> !ownerId.equals(id)))
            throw new IllegalStateException("非法的越界访问：拥有者列表不能扩大个人数据范围");
    }

    public boolean ownerIdListCondition(boolean isDeleteAction) {
        if (!isPersonalObject()) return false;
        checkOwnerScope(isDeleteAction);
        return ownerIdList != null && !ownerIdList.isEmpty();
    }

    public boolean ownerIdCondition(boolean isQueryAction, boolean isDeleteAction) {
        if (!isPersonalObject()) return false;
        checkOwnerScope(isDeleteAction);
        return ownerId != null && !ownerId.isBlank() && (ownerIdList == null || ownerIdList.isEmpty())
                && (!isCanVisitPersonalData() || isQueryAction || isDeleteAction);
    }
}
