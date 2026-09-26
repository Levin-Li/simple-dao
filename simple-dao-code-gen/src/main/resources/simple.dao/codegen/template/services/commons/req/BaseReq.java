package ${modulePackageName}.services.commons.req;


import cn.hutool.core.lang.Assert;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.*;
import com.levin.commons.rbac.ConfidentialLevel;
import com.levin.commons.service.domain.*;
import com.levin.commons.service.support.*;

import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.dao.*;


import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    /**
     * 允许普通实体字段、关联属性路径，以及单段 MySQL 风格反引号转义字段。
     */
    private static final Pattern SAFE_FIELD_PATH = Pattern.compile(
            "^(?:[A-Za-z_][A-Za-z0-9_]*|`[A-Za-z_][A-Za-z0-9_]*`)(?:\\.(?:[A-Za-z_][A-Za-z0-9_]*|`[A-Za-z_][A-Za-z0-9_]*`))*$");

    public static final String IS_UNSAFE_CONTEXT = " (#" + InjectConst.IS_UNSAFE_CONTEXT + "?:false) ";

    public static final String IS_TOP_SUPER_ADMIN = " (#" + InjectConst.IS_TOP_SUPER_ADMIN + "?:false) ";

    public static final String IS_SUPER_ADMIN = " (#" + InjectConst.IS_SUPER_ADMIN + "?:false) ";

    public static final String IS_SAAS_ADMIN = " (#" + InjectConst.IS_SAAS_ADMIN + "?:false) ";

    public static final String IS_PLATFORM_USER = " (#" + InjectConst.IS_PLATFORM_USER + "?:false) ";

    public static final String IS_TENANT_USER = " (#" + InjectConst.IS_TENANT_USER + "?:false) ";


    public static final String IS_TENANT_ADMIN = " (#" + InjectConst.IS_TENANT_ADMIN + "?:false) ";


   /// /////////////////////////////////////////////////////////////////////
    public static final String NOT_TOP_SUPER_ADMIN = " !" + IS_TOP_SUPER_ADMIN;

    public static final String NOT_SUPER_ADMIN = " !" + IS_SUPER_ADMIN;

    public static final String NOT_SAAS_ADMIN = " !" + IS_SAAS_ADMIN;

    public static final String NOT_PLATFORM_USER = " !" + IS_PLATFORM_USER;

    public static final String NOT_TENANT_USER = " !" + IS_TENANT_USER;

    public static final String NOT_TENANT_ADMIN = " !" + IS_TENANT_ADMIN;

    /// //////////////////////////////////////////////////////////////////////////////////

    public static final String NOT_SUPER_ADMIN_AND_NOT_SAAS_ADMIN = " (" + NOT_SUPER_ADMIN + " && " + NOT_SAAS_ADMIN + ") ";

    public static final String NOT_SUPER_SAAS_TENANT_ADMIN = " (" + NOT_SUPER_ADMIN + " && " + NOT_SAAS_ADMIN + " && " + NOT_TENANT_ADMIN + ") ";

    /////////////////////////////////////////////////////////////////////

    /**
     * 是否为外部、不可信调用链。
     * <p>true 表示 Controller、RPC、消息等外部入口，必须执行越权、越范围和危险操作校验；false 表示
     * 内部可信调用，权限责任由调用方承担。</p>
     */
    @InjectVar(value = InjectConst.IS_UNSAFE_CONTEXT, isRequired = "true")
    @Ignore
    protected boolean isUnsafeContext = false;

    ///////////////////////////////////////////////////
    @InjectVar(InjectVar.SPEL_PREFIX + IS_TOP_SUPER_ADMIN)
    @Ignore
    protected boolean isTopSuperAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SUPER_ADMIN)
    @Ignore
    protected boolean isSuperAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_SAAS_ADMIN)
    @Ignore
    protected boolean isSaasAdmin = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_PLATFORM_USER)
    @Ignore
    protected boolean isPlatformUser = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_TENANT_USER)
    @Ignore
    protected boolean isTenantUser = false;

    @InjectVar(InjectVar.SPEL_PREFIX + IS_TENANT_ADMIN)
    @Ignore
    protected boolean isTenantAdmin = false;

    @Schema(title = "跟踪标识", hidden = true)
    @Ignore
    protected String traceId = java.util.UUID.randomUUID().toString().replace("-", "");

    /// //////////////////////////////////////////////////////////////////////////////////////////////
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
    @InjectVar(value = InjectConst.OPERATOR_ACTION, isRequired = "false",remark = "一般对应控制器的方法或是描述")
    @Ignore
    protected String _operatorAction;

    /// //////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 当前登录用户 ID。
     *
     * <p>替代原 {@code _operatorId} 属性；该值始终表示当前用户身份，不能作为可由请求参数切换的
     * 数据范围条件。</p>
     */
    @Schema(title = "当前用户ID", hidden = true)
    @InjectVar(remark = "用户ID是必须覆盖也必须注入,如果当前请求未登录,那就是匿名用户ID")
    @Ignore
    protected String _currentUserId;

    /**
     * 当前登录用户名称。
     *
     * <p>替代原 {@code _operatorName} 属性；该值表示当前用户身份，不因请求的数据范围切换而改变。</p>
     */
    @Schema(title = "当前用户名称", hidden = true)
    @InjectVar(remark = "用户名称也是必须的,如果当前请求未登录，那就是匿名用户名称")
    @Ignore
    protected String _currentUserName;

    /**
     * 当前登录用户所属组织 ID。
     *
     * <p>它表示用户身份上下文，不等同于请求用于筛选或更新的 {@code orgId}/{@code orgIdList}。</p>
     */
    @Schema(title = "当前用户组织ID", hidden = true)
    @InjectVar(isRequired = "false")
    @Ignore
    protected String _currentUserOrgId;

    /**
     * 当前登录用户所属租户 ID。
     *
     * <p>它表示用户身份上下文，不等同于请求实际采用的 {@code tenantId}；平台用户选择租户视角时，
     * 后者可以变化，前者保持当前登录用户的值。</p>
     */
    @Schema(title = "当前用户租户ID", hidden = true)
    @InjectVar(isRequired = "false")
    @Ignore
    protected String _currentUserTenantId;

    /// ///////////////////////////////////////////////////////////////////////

    @Schema(title = "允许默认排序")
    @Ignore
    protected boolean enableDefaultOrderBy = true;

    ////////////////////////////////////////////////////////////////////

    @Ignore
    @Schema(title = "是否是不安全的上下文", hidden = true)
    public boolean isUnsafeContext() {
        return this.isUnsafeContext;
    }

    @Ignore
    @Schema(title = "是否允许默认排序", hidden = true)
    public boolean isEnableDefaultOrderBy() {
        return this.enableDefaultOrderBy;
    }

    @Ignore
    @Schema(title = "是否Top超级管理员", hidden = true)
    public boolean isTopSuperAdmin() {
        return isPlatformUser() && this.isTopSuperAdmin;
    }

    @Ignore
    @Schema(title = "是否超级管理员", hidden = true)
    public boolean isSuperAdmin() {
        return isTopSuperAdmin() || this.isSuperAdmin;
    }

    @Ignore
    @Schema(title = "是否SAAS管理员", hidden = true)
    public boolean isSaasAdmin() {
        return isPlatformUser() && this.isSaasAdmin;
    }

    @Ignore
    @Schema(title = "是否平台用户", hidden = true)
    public boolean isPlatformUser() {
        return this.isPlatformUser;
    }

    @Ignore
    @Schema(title = "是否租户用户", hidden = true)
    public boolean isTenantUser() {
        return this.isTenantUser;
    }

    @Ignore
    @Schema(title = "是否租户管理员", hidden = true)
    public boolean isTenantAdmin() {
        return this.isTenantUser() && this.isTenantAdmin;
    }

    @Ignore
    @Schema(title = "是否管理员", description = "超级管理员，SAAS管理员，租户管理员", hidden = true)
    public boolean isAdmin() {
        return isSuperAdmin() || isSaasAdmin() || isTenantAdmin();
    }

    @Schema(title = "是否是敏感数据", description = "敏感数据需要根据级别进行过滤", hidden = true)
    public boolean isConfidentialObject() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////

    @Schema(title = "数据访问级别", hidden = true)
    @InjectVar(value = InjectConst.CONFIDENTIAL_DATA_ACCESS_LEVEL

            , isOverride = InjectVar.SPEL_PREFIX + "" + NOT_TOP_SUPER_ADMIN // 如果不是TOP超管 那么覆盖必须的

            // 用户可以没有数据访问级别
           // , isRequired = InjectVar.SPEL_PREFIX + "" + NOT_TOP_SUPER_ADMIN // 如果不是TOP超管 那么值是必须的
              , isRequired = "false"
    )
    @OR(autoClose = true)
    @Lte(value = "confidentialLevel",    condition = "isUnsafeContext() && isConfidentialObject() && !isTopSuperAdmin() && #isNotEmpty(#_fieldVal) ",  desc = "数据机密级别小于用户的数据访问级别的都可见")
    @IsNull(value = "confidentialLevel", condition = "isUnsafeContext() && isConfidentialObject() && !isTopSuperAdmin() ", desc = "保密等级未定义的数据")
    protected Integer _confidentialDataAccessLevel;

    @Ignore
    @Schema(title = "是否能访问个人数据", description = "", hidden = true)
    public boolean isCanVisitPersonalData() {
        return isTopSuperAdmin()
                || (_confidentialDataAccessLevel != null
                && _confidentialDataAccessLevel >= ConfidentialLevel.PERSON_PRIVATE.code());
    }

    @Schema(title = "领域ID", hidden = true, description = "领域ID，未显式指定时从请求上下文自动注入")
    @InjectVar(isRequired = "false" , isOverride = InjectVar.SPEL_PREFIX + "" + NOT_TOP_SUPER_ADMIN) // 如果不是TOP超管 那么覆盖必须的

    @OR(autoClose = true, condition = "isDomainObject()", desc = "只有实现了领域对象[DomainObject]接口，才加入这个条件")
    @Eq(condition = "#isNotEmpty(#_fieldVal) && #_fieldVal != '_OnlyEmptyDomainId_'") // domainId 为空 查询条件
    @IsNull(condition = "#_isQuery && isContainsEmptyDomain(#_fieldVal)", desc = "查询结果包含(domainId为NULL的数据)")
    protected String domainId;

    @Schema(title = "是否包含DomainId为空的数据", hidden = true)
    public boolean isContainsEmptyDomain(String domainId){
        return domainId != null; //默认情况下, 有指定领域ID的话,也要顺便把无领域ID的数据也一起查出来; 如果仅仅只要查询领域ID为空的数据，则可以填 _OnlyEmptyDomainId_
    }

    public <T extends BaseReq> T setDomainId(String domainId) {
        this.domainId = domainId;
        return (T) this;
    }

    /** 将当前请求整体导出为 DAO 上下文变量，供后续处理对象跨类引用。 */
    @CtxVar(varName = "_req")
    public <T extends BaseReq> T getRequestContext() {
        return (T)this;
    }

    public boolean isDomainObject(){
        return (this instanceof DomainObject);
    }

    public boolean isMultiTenantObject(){
        return (this instanceof MultiTenantObject);
    }

    public boolean isOrganizedObject(){
        return (this instanceof OrganizedObject);
    }

    public boolean isPersonalObject(){
        return (this instanceof PersonalObject);
    }

    /**
     * 是否强制更新字段
     *
     * @param fieldName
     * @return
     */
    public boolean isForceUpdateField(String fieldName) {
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
     * 字段路径白名单检查，用于排序字段与选择字段。
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
            Assert.isTrue(SAFE_FIELD_PATH.matcher(statement).matches(), "不支持的字段路径：{}", statement);

            // 旧的 SQL 关键字黑名单检查保留在此，字段路径白名单已覆盖该场景。
//            String normalized = statement.toLowerCase(Locale.ROOT);
//            Assert.isTrue(Stream.of(" from ", " where ", " set ").noneMatch(normalized::contains), "不支持的语句：{}", statement);
//            Assert.isTrue(Stream.of(" select ", " insert ", " update ", " delete ").noneMatch((" " + normalized)::contains), "不支持的语句：{}", statement);
//            Assert.isTrue(Stream.of("(select ", "(insert ", "(update ", "(delete ").noneMatch(normalized::contains), "不支持的语句：{}", statement);
//            Assert.isTrue(Stream.of("'select ", "'insert ", "'update ", "'delete ").noneMatch(normalized::contains), "不支持的语句：{}", statement);
        }

        return (T) this;
    }

}
