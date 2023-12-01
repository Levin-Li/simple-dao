package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import org.springframework.dao.*;

import javax.persistence.PersistenceException;
import cn.hutool.core.lang.*;
import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.annotation.*;

import ${entityClassPackage}.*;
import ${entityClassName};

import ${servicePackageName}.*;
import static ${servicePackageName}.${serviceName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

import ${modulePackageName}.*;
import ${modulePackageName}.services.*;

<#list fields as field>
    <#if (field.lzay)??>
import ${field.classType.package.name}.${field.classType.simpleName};
    </#if>
    <#if (field.infoClassName)??>
import ${field.infoClassName};
    </#if>
</#list>

////////////////////////////////////
//自动导入列表
<#list importList as imp>
import ${imp};
</#list>
////////////////////////////////////

/**
 *  ${entityTitle}-业务服务实现类
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

// 事务隔离级别
// Propagation.REQUIRED：默认的事务传播级别，它表示如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。
// Propagation.SUPPORTS：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。
// Propagation.MANDATORY：（mandatory：强制性）如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。
// Propagation.REQUIRES_NEW：表示创建一个新的事务，如果当前存在事务，则把当前事务挂起。也就是说不管外部方法是否开启事务，Propagation.REQUIRES_NEW 修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。
// Propagation.NOT_SUPPORTED：以非事务方式运行，如果当前存在事务，则把当前事务挂起。
// Propagation.NEVER：以非事务方式运行，如果当前存在事务，则抛出异常。
// Propagation.NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于 PROPAGATION_REQUIRED。

<#if enableDubbo>@DubboService<#else>@Service(PLUGIN_PREFIX + "Biz${serviceName}")</#if>

@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "Biz${serviceName}", havingValue = "true", matchIfMissing = true)
@Slf4j

//@Valid只能用在controller，@Validated可以用在其他被spring管理的类上。
//@Validated
@Tag(name = E_${entityName}.BIZ_NAME + "-业务服务", description = "")
@CacheConfig(cacheNames = {ID + CACHE_DELIM + E_${entityName}.SIMPLE_CLASS_NAME})
public class ${className} extends BaseService implements Biz${serviceName} {

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    ${serviceName} ${serviceName?uncap_first};

    protected ${className} getSelfProxy(){
        return getSelfProxy(${className}.class);
    }

    @Operation(summary = CREATE_ACTION)
    @Transactional
    //@Override
    <#if pkField?exists>
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#result)", key = CK_PREFIX + "#result") //创建也清除缓存，防止空值缓存的情况
    public ${pkField.typeName} create(Create${entityName}Req req){
    <#else>
    public boolean create(Create${entityName}Req req){
    </#if>
        return ${serviceName?uncap_first}.create(req);
    }

<#if pkField?exists>
    @Operation(summary = VIEW_DETAIL_ACTION)
    //@Override
    //Spring 缓存变量可以使用Spring 容器里面的bean名称，SpEL支持使用@符号来引用Bean。
    <#if !pkField?exists || !isCacheableEntity>//</#if>@Cacheable(unless = "#result == null ", condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#${pkField.name})", key = CK_PREFIX + "#${pkField.name}")
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        return ${serviceName?uncap_first}.findById(${pkField.name});
    }

    //调用本方法会导致不会对租户ID经常过滤，如果需要调用方对租户ID进行核查
    @Operation(summary = VIEW_DETAIL_ACTION)
    //@Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@Cacheable(unless = "#result == null" , condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name})" , key = CK_PREFIX + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if>
    public ${entityName}Info findById(${entityName}IdReq req) {
        return ${serviceName?uncap_first}.findById(req);
    }
</#if>

    @Operation(summary = UPDATE_ACTION)
    //@Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX + "#req.${pkField.name}")//, beforeInvocation = true
    @Transactional
    public boolean update(Update${entityName}Req req) {
        return ${serviceName?uncap_first}.update(req);
    }


    @Operation(summary = DELETE_ACTION)
    //@Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if> , beforeInvocation = true
    @Transactional
    public boolean delete(${entityName}IdReq req) {
        return ${serviceName?uncap_first}.delete(req);
    }

    //@Override
    @Operation(summary = CLEAR_CACHE_ACTION, description = "缓存Key通常是ID")
    @CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#key)", key = CK_PREFIX + "#key")
    public void clearCache(Object key) {
        ${serviceName?uncap_first}.clearCache(key);
    }

}
