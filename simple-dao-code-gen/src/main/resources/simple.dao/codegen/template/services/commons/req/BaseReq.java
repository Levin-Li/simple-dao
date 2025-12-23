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


import javax.persistence.Column;
import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;


/**
 * 基本查询对象
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 */
@Schema(title = "基本请求对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class BaseReq implements ServiceReq {

    public static final String IS_WEB_CONTEXT = " (#" + InjectConst.IS_WEB_CONTEXT + "?:false) ";

    public static final String IS_SUPER_ADMIN = " (#" + InjectConst.IS_SUPER_ADMIN + "?:false) ";

    public static final String IS_SAAS_ADMIN = " (#" + InjectConst.IS_SAAS_ADMIN + "?:false) ";

    public static final String IS_SAAS_USER = " (#" + InjectConst.IS_SAAS_USER + "?:false) ";

    public static final String IS_TENANT_ADMIN = " (#" + InjectConst.IS_TENANT_ADMIN + "?:false) ";

    public static final String CAN_VISIT_PERSONAL_DATA = " (#canVisitPersonalData?:false)";
    public static final String CAN_NOT_VISIT_PERSONAL_DATA = " !" + CAN_VISIT_PERSONAL_DATA;

    public static final String NOT_SUPER_ADMIN = " !" + IS_SUPER_ADMIN;

    public static final String NOT_SAAS_ADMIN = " !" + IS_SAAS_ADMIN;

    public static final String NOT_SAAS_USER = " !" + IS_SAAS_USER;

    public static final String NOT_TENANT_ADMIN = " !" + IS_TENANT_ADMIN;

    public static final String NOT_SUPER_ADMIN_AND_NOT_SAAS_ADMIN = " (" + NOT_SUPER_ADMIN + " && " + NOT_SAAS_ADMIN + ") ";

    public static final String NOT_SUPER_SAAS_TENANT_ADMIN = " (" + NOT_SUPER_ADMIN + " && " + NOT_SAAS_ADMIN + " && " + NOT_TENANT_ADMIN + ") ";

    /////////////////////////////////////////////////////////////////////

    @InjectVar(value = InjectConst.IS_WEB_CONTEXT, isRequired = "false")
    @Ignore
    @CtxVar
    protected boolean isWebContext = true;

    ///////////////////////////////////////////////////

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SUPER_ADMIN)
    @Ignore
    @CtxVar
    protected boolean isSuperAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SAAS_ADMIN)
    @Ignore
    @CtxVar
    protected boolean isSaasAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SAAS_USER)
    @Ignore
    @CtxVar
    protected boolean isSaasUser = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_TENANT_ADMIN)
    @Ignore
    @CtxVar
    protected boolean isTenantAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + CAN_VISIT_PERSONAL_DATA)
    @Ignore
    @CtxVar
    protected boolean canVisitPersonalData = false;
    ///////////////////////////////////////////////////////////////////////

    @Schema(title = "跟踪标识", hidden = true)
    @Ignore
    protected String traceId = java.util.UUID.randomUUID().toString().replace("-", "");

    @Ignore
    @Schema(title = "客户端类型", hidden = true)
    @InjectVar(value = InjectConst.USER_AGENT, isRequired = "false")
    protected String _userAgent;

    @Ignore
    @Schema(title = "域名", hidden = true)
    @InjectVar(value = InjectConst.DOMAIN, isRequired = "false")
    protected String _domain;

    @InjectVar(value = InjectConst.IP_ADDR, isRequired = "false")
    @Schema(title = "IP地址", hidden = true)
    @Ignore
    protected String _ipAddr;

    @Schema(title = "操作员动作",description = "一般对应控制器的方法或是描述", hidden = true)
    @InjectVar(value = InjectConst.OPERATOR_ACTION, isRequired = "false")
    @Ignore
    protected String _operatorAction;

    @Schema(title = "操作员ID", hidden = true)
    @InjectVar(value = InjectConst.USER_ID, isRequired = "false")
    @Ignore
    protected String _operatorId;

    @Schema(title = "操作员名称", hidden = true)
    @InjectVar(value = InjectConst.USER_NAME, isRequired = "false")
    @Ignore
    protected String _operatorName;

    @Schema(title = "允许默认排序")
    @Ignore
    @CtxVar
    protected boolean enableDefaultOrderBy = true;

    ////////////////////////////////////////////////////////////////////

    @Ignore
    @Schema(title = "是否是web请求", hidden = true)
    public boolean isWebContext() {
        return this.isWebContext;
    }

    @Ignore
    @Schema(title = "是否允许默认排序", hidden = true)
    public boolean isEnableDefaultOrderBy() {
        return this.enableDefaultOrderBy;
    }

    @Ignore
    @Schema(title = "是否可访问个人数据", description = "是否可以访问个人的数据", hidden = true)
    public boolean isCanVisitPersonalData() {
        return this.canVisitPersonalData;
    }

    @Ignore
    @Schema(title = "是否超级管理员", hidden = true)
    public boolean isSuperAdmin() {
        return this.isSuperAdmin;
    }

    @Ignore
    @Schema(title = "是否SAAS管理员", hidden = true)
    public boolean isSaasAdmin() {
        return this.isSaasAdmin;
    }

    @Ignore
    @Schema(title = "是否SAAS用户", hidden = true)
    public boolean isSaasUser() {
        return this.isSaasUser;
    }

    @Ignore
    @Schema(title = "是否租户管理员", hidden = true)
    public boolean isTenantAdmin() {
        return this.isTenantAdmin;
    }

    @Ignore
    @Schema(title = "是否管理员", description = "超级管理员，SAAS管理员，租户管理员", hidden = true)
    @CtxVar
    public boolean isAdmin() {
        return isSuperAdmin() || isSaasAdmin() || isTenantAdmin();
    }

    @Schema(title = "是否是敏感数据", description = "敏感数据需要根据级别进行过滤", hidden = true)
    @CtxVar
    public boolean isConfidentialObject() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////
    @Schema(title = "数据访问级别", hidden = true)
    @InjectVar(value = InjectConst.DATA_ACCESS_LEVEL
            , isOverride = InjectVar.SPEL_PREFIX + "" + NOT_SUPER_ADMIN // 如果不是超管 那么覆盖必须的
            , isRequired = InjectVar.SPEL_PREFIX + "" + NOT_SUPER_ADMIN // 如果不是超管 那么值是必须的
    )
    @Lte(value = "confidentialLevel", condition = "#isNotEmpty(#_fieldVal) && !(isSuperAdmin()) && isConfidentialObject()",  desc = "数据机密级别小于用户的数据访问级别的都可见")
    protected Integer dataAccessLevel;

    /**
     * 是否强制更新字段
     *
     * @param fieldName
     * @return
     */
    protected boolean isForceUpdateField(String fieldName) {
        return false;
    }

    /**
     * 是否非空
     *
     * @param value
     * @return
     */
    protected boolean isNotBlank(Object value) {
        return value != null
                && (!(value instanceof CharSequence) || StringUtils.hasText((CharSequence) value));
    }

    protected <T extends BaseReq> T checkSQLInject(String... statements) {

        if(statements == null){
            return (T) this;
        }

        return checkSQLInject(Arrays.asList(statements));
    }

    /**
     * 简单的防止SQL注检查
     *
     * @param statements
     */
    protected <T extends BaseReq> T checkSQLInject(Iterable<String> statements) {

        if(statements == null){
            return (T) this;
        }

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

}
