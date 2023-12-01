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

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import org.springframework.dao.*;

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
@Slf4j

//@Valid只能用在controller， @Validated可以用在其他被spring管理的类上。
//@Validated
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
@CacheConfig(cacheNames = {ID + CACHE_DELIM + E_${entityName}.SIMPLE_CLASS_NAME})
public class ${className} extends BaseService implements ${serviceName} {

    protected ${serviceName} getSelfProxy(){
        //return getSelfProxy(${serviceName}.class);
        return getSelfProxy(${className}.class);
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
    <#if pkField?exists && !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#result)", key = CK_PREFIX + "#result") //创建也清除缓存，防止空值缓存的情况
    public ${pkField.typeName} create(Create${entityName}Req req){
<#else>
    public boolean create(Create${entityName}Req req){
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
    //@Transactional(rollbackFor = {PersistenceException.class, DataAccessException.class})
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
    <#if pkField?exists && !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX + "#req.${pkField.name}")//, beforeInvocation = true
    @Transactional
    public boolean update(Update${entityName}Req req) {
        Assert.notNull(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleUpdateByQueryObj(req);
    }

    @Operation(summary = UPDATE_ACTION)
    @Override
    @Transactional
    <#if pkField?exists && !isCacheableEntity>//</#if>@CacheEvict(allEntries = true, condition = "#result > 0")
    public int update(SimpleUpdate${entityName}Req setReq, Query${entityName}Req whereReq){
       return simpleDao.updateByQueryObj(setReq, whereReq);
    }

    @Operation(summary = BATCH_UPDATE_ACTION)
    @Transactional
    @Override
    <#if pkField?exists && !isCacheableEntity>//</#if>//@CacheEvict(allEntries = true, condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#reqList)  && #result > 0")
    public int batchUpdate(List<Update${entityName}Req> reqList){
        //@Todo 优化批量提交
        return reqList.stream().map(req -> getSelfProxy().update(req)).mapToInt(n -> n ? 1 : 0).sum();
    }

    @Operation(summary = DELETE_ACTION)
    @Override
    <#if pkField?exists && !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if> , beforeInvocation = true
    @Transactional
    public boolean delete(${entityName}IdReq req) {
        Assert.notNull(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleDeleteByQueryObj(req);
    }

    @Operation(summary = BATCH_DELETE_ACTION)
    @Transactional
    @Override
    <#if pkField?exists && !isCacheableEntity>//</#if>//@CacheEvict(allEntries = true, condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}List) && #result > 0")
    public int batchDelete(Delete${entityName}Req req){
        //@Todo 优化批量提交
        return Stream.of(req.get${pkField.name?cap_first}List())
            .map(${pkField.name} -> simpleDao.copy(req, new ${entityName}IdReq().set${pkField.name?cap_first}(${pkField.name})))
            .map(idReq -> getSelfProxy().delete(idReq))
            .mapToInt(n -> n ? 1 : 0)
            .sum();
    }

    @Operation(summary = QUERY_ACTION)
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging) {
        return simpleDao.findPagingDataByQueryObj(req, paging);
    }

    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    public PagingData<Simple${entityName}Info> simpleQuery(Query${entityName}Req req, Paging paging){
        return simpleDao.findPagingDataByQueryObj(Simple${entityName}Info.class, req, paging);
    }

    @Operation(summary = STAT_ACTION)
    @Override
    public PagingData<Stat${entityName}Req.Result> stat(Stat${entityName}Req req , Paging paging){
        return simpleDao.findPagingDataByQueryObj(req, paging);
    }

    @Override
    @Operation(summary = STAT_ACTION)
    public int count(Query${entityName}Req req){
        return (int) simpleDao.countByQueryObj(req);
    }

<#if pkField?exists>
    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    //Spring 缓存变量可以使用Spring 容器里面的bean名称，SpEL支持使用@符号来引用Bean。
    <#if pkField?exists && !isCacheableEntity>//</#if>@Cacheable(unless = "#result == null ", condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#${pkField.name})", key = CK_PREFIX + "#${pkField.name}")
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        return findById(new ${entityName}IdReq().set${pkField.name?cap_first}(${pkField.name}));
    }

    //调用本方法会导致不会对租户ID经常过滤，如果需要调用方对租户ID进行核查
    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    <#if pkField?exists && !isCacheableEntity>//</#if>@Cacheable(unless = "#result == null" , condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name})" , key = CK_PREFIX + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if>
    public ${entityName}Info findById(${entityName}IdReq req) {
        Assert.notNull(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.findUnique(req);
    }
</#if>

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findOne(Query${entityName}Req req){
        return simpleDao.findOneByQueryObj(req);
    }

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findUnique(Query${entityName}Req req){
        return simpleDao.findUnique(req);
    }

    @Override
    @Operation(summary = CLEAR_CACHE_ACTION, description = "缓存Key通常是ID")
    @CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#key)", key = CK_PREFIX + "#key")
    public void clearCache(Object key) {
    }

}
