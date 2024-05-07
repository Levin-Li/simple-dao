package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.domain.*;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
//import org.springframework.dao.*;

import javax.persistence.PersistenceException;
import cn.hutool.core.lang.*;
import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.spring.context.annotation.*;
<#if !enableDubbo>//</#if>import org.apache.dubbo.config.annotation.*;

import ${entityClassPackage}.*;
import ${entityClassName};
import static ${entityClassPackage}.E_${entityName}.*;

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
 * ${entityTitle}-服务实现
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

<#if enableDubbo>@DubboService<#else>@Service(${serviceName}.SERVICE_BEAN_NAME)</#if>

@ConditionalOnProperty(name = ${serviceName}.SERVICE_BEAN_NAME, havingValue = "true", matchIfMissing = true)
//@Slf4j

//@Valid只能用在controller， @Validated可以用在其他被spring管理的类上。
//@Validated
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)

//*** 提示 *** 如果要注释缓存注解的代码可以在实体类上加上@javax.persistence.Cacheable(false)，然后重新生成代码
<#if !isCacheableEntity>//</#if>@CacheConfig(cacheNames = ${serviceName}.CACHE_NAME, cacheResolver = PLUGIN_PREFIX + "ModuleSpringCacheResolver")

// *** 提示 *** 请尽量不要修改本类，如果需要修改，请在Biz${className}业务类中重写业务逻辑

public class ${className} extends BaseService<${className}> implements ${serviceName} {

<#--    protected ${serviceName} getSelfProxy(){-->
<#--        //return getSelfProxy(${serviceName}.class);-->
<#--        return getSelfProxy(${className}.class);-->
<#--    }-->

    @Operation(summary = QUERY_ACTION)
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging, Object... queryObjs) {
        return simpleDao.findPagingDataByQueryObj(req, paging, queryObjs);
    }

    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    public PagingData<${entityName}Info> selectQuery(Query${entityName}Req req, Paging paging, String... columnNames){
        return simpleDao.forSelect(${entityName}Info.class, req, paging).select(columnNames).findPaging(null, paging);
    }

    @Override
    @Operation(summary = STAT_ACTION)
    public int count(Query${entityName}Req req, Object... queryObjs){
        return (int) simpleDao.countByQueryObj(req, queryObjs);
    }

<#if pkField?exists>
    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    //Spring 缓存变量可以使用Spring 容器里面的bean名称，SpEL支持使用@符号来引用Bean。
    //调用本方法会导致不会对租户ID进行过滤，如果需要调用方对租户ID进行核查
    //如果要注释缓存注解的代码可以在实体类上加上@javax.persistence.Cacheable(false)，然后重新生成代码
    <#if !pkField?exists || !isCacheableEntity>//</#if>@Cacheable(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#${pkField.name})", key = CK_PREFIX_EXPR + "#${pkField.name}") //默认允许空值缓存 unless = "#result == null ",
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        return simpleDao.selectFrom(${entityName}.class).eq(E_${entityName}.${pkField.name}, ${pkField.name}).findUnique(${entityName}Info.class);
    }

    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>//@Cacheable(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name})" , key = CK_PREFIX_EXPR + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if>  //默认允许空值缓存 unless = "#result == null ",
    public ${entityName}Info findById(${entityName}IdReq req) {

        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        //return simpleDao.findUnique(req);

        ${entityName}Info info = getSelfProxy().findById(req.get${pkField.name?cap_first}());

        boolean passed = false;

        <#if isMultiTenantObject>
        ///////////////////////租户检查///////////////////
        //如果有租户标识
        if (hasText(info.getTenantId())) {

            if (!hasText(req.getTenantId())
                    || info.getTenantId().equals(req.getTenantId())) {
                //如果请求对象中没有租户标识，或是租户标识相等，则返回
                passed = true;
            } else if (req instanceof MultiTenantSharedObject
                    && ((MultiTenantSharedObject) req).isTenantShared()) {
                //如果是租户主动共享的的数据
                passed = true;
            }

        }
        <#if isMultiTenantPublicObject>
        else if (req.isContainsPublicData()) {
            passed = true;
        }
        </#if>

        Assert.isTrue(passed, "租户ID不匹配({})", req.getTenantId());
        ///////////////////////租户检查///////////////////
        </#if>

        <#if isOrganizedObject>
         passed = false;
        ///////////////////////部门检查///////////////////
        //如果有组织标识
        if (hasText(info.getOrgId())) {
            if (isEmpty(req.getOrgIdList())
                    || req.getOrgIdList().contains(info.getOrgId())) {
                //如果请求对象中没有组织标识，或是组织标识相等，则返回
                passed = true;
            } else if (req instanceof OrganizedSharedObject
                    && ((OrganizedSharedObject) req).isOrgShared()) {
                //如果是组织主动共享的的数据
                passed = true;
            }
        }
        <#if isOrganizedPublicObject>
        else if (req.isContainsOrgPublicData()) {
            passed = true;
        }
        </#if>

        Assert.isTrue(passed || req.isTenantAdmin(), "组织ID不匹配({})", req.getOrgId());
        ///////////////////////部门检查///////////////////
        </#if>

       <#if isPersonalObject>
        passed = false;
        ///////////////////////私有检查///////////////////
       // if (req instanceof PersonalObject) {
            if (!hasText(info.getOwnerId())
                    || !hasText(req.getOwnerId())
                    || info.getOwnerId().equals(req.getOwnerId())) {
                passed = true;
            }
        //}
        Assert.isTrue(passed, "拥有者ID不匹配({})", req.getOwnerId());
        ///////////////////////私有检查///////////////////
        </#if>

        return info;
    }
</#if>

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findOne(Query${entityName}Req req, Object... queryObjs){
        return simpleDao.findOneByQueryObj(req, queryObjs);
    }

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findUnique(Query${entityName}Req req){
        //记录超过一条时抛出异常 throws IncorrectResultSizeDataAccessException
        return simpleDao.findUnique(req);
    }

    /**
    * 创建记录，返回主键ID
    * @param req
    * @return pkId 主键ID
    */
    @Operation(summary = CREATE_ACTION)
    @Transactional
    @Override
<#if pkField?exists>
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#result)", key = CK_PREFIX_EXPR + "#result") //创建也清除缓存，防止空值缓存的情况
    public ${pkField.typeName} create(Create${entityName}Req req){
<#else>
    public boolean create(Create${entityName}Req req){
</#if>
        <#if classModel.isType('com.levin.commons.dao.domain.OrganizedObject')>

        Assert.isTrue(req.getOrgIdList() == null
            || req.getOrgIdList().isEmpty()
            || req.getOrgIdList().contains(req.getOrgId()), "orgId 超出可选范围");

        </#if>
        //dao支持保存前先自动查询唯一约束，并给出错误信息
        ${entityName} entity = simpleDao.create(req, true);
<#if pkField?exists>
        return entity.get${pkField.name?cap_first}();
<#else>
        return entity != null;
</#if>
    }

    @Operation(summary = BATCH_CREATE_ACTION)
    @Transactional
    @Override
<#if pkField?exists>
    public List<${pkField.typeName}> batchCreate(List<Create${entityName}Req> reqList){
    <#else>
    public List<Boolean> batchCreate(List<Create${entityName}Req> reqList){
</#if>
        return reqList.stream().map(this::create).collect(Collectors.toList());
    }

    @Operation(summary = UPDATE_ACTION)
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX_EXPR + "#req.${pkField.name}")//, beforeInvocation = true
    @Transactional
    public boolean update(Update${entityName}Req req, Object... queryObjs) {
        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleUpdateByQueryObj(req, queryObjs);
    }

    @Operation(summary = UPDATE_ACTION)
    @Override
    @Transactional
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(allEntries = true, condition = "#result > 0")
    public int batchUpdate(SimpleUpdate${entityName}Req setReq, Query${entityName}Req whereReq, Object... queryObjs){
       return simpleDao.updateByQueryObj(setReq, whereReq, queryObjs);
    }

    @Operation(summary = BATCH_UPDATE_ACTION)
    @Transactional
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>//@CacheEvict(allEntries = true, condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#reqList)  && #result > 0")
    public int batchUpdate(List<Update${entityName}Req> reqList){
        //@Todo 优化批量提交
        return reqList.stream().map(req -> getSelfProxy().update(req)).mapToInt(n -> n ? 1 : 0).sum();
    }

    @Operation(summary = DELETE_ACTION)
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX_EXPR + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if> , beforeInvocation = true
    @Transactional
    public boolean delete(${entityName}IdReq req) {
        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleDeleteByQueryObj(req);
    }

    @Operation(summary = BATCH_DELETE_ACTION)
    @Transactional
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>//@CacheEvict(allEntries = true, condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}List) && #result > 0")
    public int batchDelete(Delete${entityName}Req req){
        //@Todo 优化批量提交
        return Stream.of(req.get${pkField.name?cap_first}List())
            .map(${pkField.name} -> simpleDao.copy(req, new ${entityName}IdReq().set${pkField.name?cap_first}(${pkField.name})))
            .map(idReq -> getSelfProxy().delete(idReq))
            .mapToInt(n -> n ? 1 : 0)
            .sum();
    }

    @Operation(summary = BATCH_DELETE_ACTION)
    @Transactional
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(allEntries = true, condition = "#result > 0")
    public int batchDelete(Query${entityName}Req req, Object... queryObjs){
        return simpleDao.deleteByQueryObj(req, queryObjs);
    }

    /**
    * 获取缓存
    *
    * @param key 缓存Key
    * @param valueLoader 缓存没有，则从加载函数加载
    * @return 缓存数据
    */
    @Operation(summary = GET_CACHE_ACTION, description = "通常是主键ID")
    @Cacheable(unless = "#valueLoader == null ", condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#key)", key = "#key")  //默认允许空值缓存 unless = "#result == null ",
    public <T> T getCache(String key, Function<String,T> valueLoader){
        Assert.notBlank(key, "key is empty");
        return valueLoader == null ? null : valueLoader.apply(key);
    }

    /**
    * 清除缓存
    * @param key 缓存Key
    */
    @Override
    @Operation(summary = CLEAR_CACHE_ACTION, description = "完整的缓存Key")
    @CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#key)", key = "#key")
    public void clearCache(String key) {
        Assert.notBlank(key, "key is empty");
    }

    /**
    * 清除[${serviceName}.CACHE_NAME]缓存中的所有缓存
    *
    */
    @Override
    @Operation(summary = CLEAR_CACHE_ACTION,  description = "清除所有缓存")
    @CacheEvict(allEntries = true)
    public void clearAllCache() {
    }

}
