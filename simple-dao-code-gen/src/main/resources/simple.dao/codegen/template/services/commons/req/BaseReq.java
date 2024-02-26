package ${modulePackageName}.services.commons.req;


import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.domain.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.service.support.*;

import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;

import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 *  基本查询对象
 *  @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *  
 */
@Schema(title = "基本查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class BaseReq implements ServiceReq {

    public static final String IS_WEB_CONTEXT = " (#" + InjectConst.IS_WEB_CONTEXT + "?:false) ";

    public static final String IS_SUPER_ADMIN = " (#" + InjectConst.IS_SUPER_ADMIN + "?:false) ";
    public static final String IS_TENANT_ADMIN = " (#" + InjectConst.IS_TENANT_ADMIN + "?:false) ";

    public static final String NOT_SUPER_ADMIN = " !" + IS_SUPER_ADMIN;

    public static final String NOT_TENANT_ADMIN = " !" + IS_TENANT_ADMIN;

    public static final String NOT_SUPER_ADMIN_AND_NOT_TENANT_ADMIN = " (" + NOT_SUPER_ADMIN + " && " + NOT_TENANT_ADMIN + ") ";

    @Schema(title = "是否超级管理员", hidden = true)
    @InjectVar(InjectVar.SPEL_PREFIX + IS_SUPER_ADMIN)
    @Ignore
    protected boolean isSuperAdmin = false;

    @Schema(title = "是否租户管理员", hidden = true)
    @InjectVar(InjectVar.SPEL_PREFIX + IS_TENANT_ADMIN)
    @Ignore
    protected boolean isTenantAdmin = false;

    @Schema(title = "操作员ID", hidden = true)
    @InjectVar(value = InjectConst.USER_ID, isRequired = "false")
    @Ignore
    protected String operatorId;

    @Schema(title = "操作员名称", hidden = true)
    @InjectVar(value = InjectConst.USER_NAME, isRequired = "false")
    @Ignore
    protected String operatorName;

    @Schema(title = "是否超级管理员", hidden = true)
    public boolean isSuperAdmin() {
        return this.isSuperAdmin;
    }

    @Schema(title = "是否租户管理员", hidden = true)
    public boolean isTenantAdmin() {
        return this.isTenantAdmin;
    }

    /**
     * 是否非空
     * @param value
     * @return
     */
    protected boolean isNotBlank(Object value){
        return value != null
                && (!(value instanceof CharSequence) || StringUtils.hasText((CharSequence) value));
    }

    /**
     * 强制转换
     * @param <T>
     * @return
     */
    public <T extends BaseReq> T cast() {
        return (T) this;
    }

}
