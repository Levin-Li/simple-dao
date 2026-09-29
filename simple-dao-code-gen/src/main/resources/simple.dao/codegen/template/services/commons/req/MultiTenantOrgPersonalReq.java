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
 * <p>在租户、组织范围之外叠加个人拥有者范围。无个人访问授权时强制从上下文注入拥有者，
 * 外部操作的旧记录只能属于本人；有授权时保留显式范围，未填可由上下文补值。</p>
 * <p>列表优先用于 WHERE。列表为空时，查询、删除及非管理员更新用单个 ownerId 筛选；
 * 管理员更新则只把它作为 SET 新归属，无个人授权的外部管理员必须用本人列表限定旧范围。
 * 有个人授权的非管理员也可在父级范围内操作其他拥有者的记录，但不能变更归属。
 * 注入覆盖判断直接读取上下文权限，不依赖 DTO 字段注入顺序。</p>
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
    @InjectVar(isOverride = InjectVar.SPEL_PREFIX + EXPR_NOT_IS_CAN_ACCESS_ALL_PERSONAL,
               isRequired = InjectVar.SPEL_PREFIX + EXPR_NOT_IS_CAN_ACCESS_ALL_PERSONAL)
    @In(value = "ownerId", condition = "ownerIdListCondition(#_isQuery, #_isDelete)")
    protected Collection<String> ownerIdList;

    /** 单值是列表为空时的筛选条件；管理员更新时仅作 SET 新归属，不能代替旧记录范围。 */
    @Schema(title = "拥有者Id" , hidden = true)
    @InjectVar(isOverride = InjectVar.SPEL_PREFIX + EXPR_NOT_IS_CAN_ACCESS_ALL_PERSONAL,
               isRequired = InjectVar.SPEL_PREFIX + EXPR_NOT_IS_CAN_ACCESS_ALL_PERSONAL)
    @Eq(condition = "ownerIdCondition(#_isQuery, #_isDelete)" , desc = "列表为空时，查询、删除及非管理员更新按单个拥有者筛选")
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

    /** 兼容查询/删除的校验入口；管理员更新须区分旧范围和新归属。 */
    protected void checkOwnerScope(boolean isDeleteAction) {
        checkOwnerScope(isDeleteAction, false);
    }

    protected void checkOwnerScope(boolean isDeleteAction, boolean isUpdateAction) {

        if (ownerIdList != null && ownerIdList.stream().anyMatch(id -> id == null || id.isBlank()))
            throw new IllegalStateException("非法的越界访问：拥有者列表不能包含空ID");

        if (!isUnsafeContext() || isCanAccessAllPersonal()) return;

        String currentUserId = get_currentUserId();

        if (currentUserId == null || currentUserId.isBlank())
            throw new IllegalStateException("非法的越界访问：必须提供当前用户ID");

        if (ownerIdList != null && !ownerIdList.isEmpty()) {
            // 列表优先：旧记录只能属于本人，不使用管理员更新的 SET 新值校验旧范围。
            if (ownerIdList.stream().anyMatch(id -> !currentUserId.equals(id)))
                throw new IllegalStateException("非法的越界访问：拥有者列表不能扩大个人数据范围");
            return;
        }

        if (isUpdateAction && isAdmin())
            throw new IllegalStateException("非法的越界访问：管理员更新必须指定旧记录的拥有者范围列表");

        if (ownerId == null || ownerId.isBlank() || !currentUserId.equals(ownerId))
            throw new IllegalStateException("非法的越界访问：只能操作当前用户的个人数据");

    }

    public boolean ownerIdListCondition(boolean isQueryAction, boolean isDeleteAction) {

        if (!isPersonalObject()) return false;

        checkOwnerScope(isDeleteAction, !isQueryAction && !isDeleteAction);

        return ownerIdList != null && !ownerIdList.isEmpty();
    }

    public boolean ownerIdCondition(boolean isQueryAction, boolean isDeleteAction) {

        if (!isPersonalObject()) return false;

        checkOwnerScope(isDeleteAction, !isQueryAction && !isDeleteAction);

        return ownerId != null && !ownerId.isBlank() && (ownerIdList == null || ownerIdList.isEmpty())
                && (isQueryAction || isDeleteAction || !isAdmin());
    }
}
