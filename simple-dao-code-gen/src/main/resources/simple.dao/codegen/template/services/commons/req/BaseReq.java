package ${modulePackageName}.services.commons.req;


import cn.hutool.core.lang.Assert;
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

import javax.persistence.Column;
import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;


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

    public static final String IS_SAAS_ADMIN = " (#" + InjectConst.IS_SAAS_ADMIN + "?:false) ";

    public static final String IS_SAAS_USER = " (#" + InjectConst.IS_SAAS_USER + "?:false) ";

    public static final String IS_TENANT_ADMIN = " (#" + InjectConst.IS_TENANT_ADMIN + "?:false) ";

    public static final String NOT_SUPER_ADMIN = " !" + IS_SUPER_ADMIN;
    
    public static final String NOT_SAAS_ADMIN = " !" + IS_SAAS_ADMIN;

    public static final String NOT_SAAS_USER = " !" + IS_SAAS_USER;

    public static final String NOT_TENANT_ADMIN = " !" + IS_TENANT_ADMIN;

    public static final String NOT_SUPER_ADMIN_AND_NOT_SAAS_ADMIN = " (" + NOT_SUPER_ADMIN + " && " + NOT_SAAS_ADMIN + ") ";

    public static final String NOT_SUPER_SAAS_TENANT_ADMIN = " (" + NOT_SUPER_ADMIN + " && " + NOT_SAAS_ADMIN + " && " + NOT_TENANT_ADMIN + ") ";

    /////////////////////////////////////////////////////////////////////

    @InjectVar(value = InjectConst.IS_WEB_CONTEXT, isRequired = "false")
    @Ignore
    protected boolean isWebContext = true;


    ///////////////////////////////////////////////////

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SUPER_ADMIN)
    @Ignore
    protected boolean isSuperAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SAAS_ADMIN)
    @Ignore
    protected boolean isSaasAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SAAS_USER)
    @Ignore
    protected boolean isSaasUser = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_TENANT_ADMIN)
    @Ignore
    protected boolean isTenantAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + "(#canVisitPersonalData?:false)")
    @Ignore
    protected boolean canVisitPersonalData = false;
    ///////////////////////////////////////////////////////////////////////

    @InjectVar(value = InjectConst.IP_ADDR, isRequired = "false")
    @Schema(title = "IP地址", hidden = true)
    @Ignore
    protected String ipAddr;

    @Schema(title = "操作员ID", hidden = true)
    @InjectVar(value = InjectConst.USER_ID, isRequired = "false")
    @Ignore
    protected String operatorId;

    @Schema(title = "操作员名称", hidden = true)
    @InjectVar(value = InjectConst.USER_NAME, isRequired = "false")
    @Ignore
    protected String operatorName;

    @Schema(title = "允许默认排序")
    @Ignore
    protected boolean enableDefaultOrderBy = true;

    ////////////////////////////////////////////////////////////////////

    @Schema(title = "是否是web请求", hidden = true)
    public boolean isWebContext() {
        return this.isWebContext;
    }

    @Schema(title = "是否允许默认排序", hidden = true)
    public boolean isEnableDefaultOrderBy() {
        return this.enableDefaultOrderBy;
    }

    @Schema(title = "是否可访问个人数据", description = "是否可以访问个人的数据", hidden = true)
    public boolean isCanVisitPersonalData() {
        return this.canVisitPersonalData;
    }

    @Schema(title = "是否超级管理员", hidden = true)
    public boolean isSuperAdmin() {
        return this.isSuperAdmin;
    }

    @Schema(title = "是否SAAS管理员", hidden = true)
    public boolean isSaasAdmin() {
        return this.isSaasAdmin;
    }

    @Schema(title = "是否SAAS用户", hidden = true)
    public boolean isSaasUser() {
        return this.isSaasUser;
    }

    @Schema(title = "是否租户管理员", hidden = true)
    public boolean isTenantAdmin() {
        return this.isTenantAdmin;
    }

    @Schema(title = "是否管理员",description = "超级管理员，SAAS管理员，租户管理员", hidden = true)
    public boolean isAdmin() {
        return isSuperAdmin || isSaasAdmin || isTenantAdmin;
    }

    /**
     * 是否强制更新字段
     * @param fieldName
     * @return
     */
    protected boolean isForceUpdateField(String fieldName) {
        return false;
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
     * 简单的防止SQL注检查
     * @param statements
     */
    protected <T extends BaseReq> T checkSQLInject(String... statements) {

        for (String statement : statements) {
            if (!StringUtils.hasText(statement)) {
                continue;
            }
            //简单的防止SQL注检查
            Assert.isTrue(Stream.of(" from ", " where ", " set ").noneMatch(statement.toLowerCase()::contains), "不支持的语句：{}", statement);

            Assert.isTrue(Stream.of(" select ", " insert ", " update ", " delete ").noneMatch((" " + statement.toLowerCase())::contains), "不支持的语句：{}", statement);
            Assert.isTrue(Stream.of("(select ", "(insert ", "(update ", "(delete ").noneMatch(statement.toLowerCase()::contains), "不支持的语句：{}", statement);
            Assert.isTrue(Stream.of("'select ", "'insert ", "'update ", "'delete ").noneMatch(statement.toLowerCase()::contains), "不支持的语句：{}", statement);
        }

        return (T) this;
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
