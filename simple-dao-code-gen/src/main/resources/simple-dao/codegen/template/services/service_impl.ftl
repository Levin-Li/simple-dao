package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;



import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import java.util.*;
import java.util.stream.*;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.*;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;

import ${entityClassPackage}.*;
import ${entityClassName};

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
 *  ${desc}-服务实现
 *
 *@author auto gen by simple-dao-codegen ${.now}
 *
 */

//@Valid只能用在controller。@Validated可以用在其他被spring管理的类上。

@Service(PLUGIN_PREFIX + "${serviceName}")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX , name = "${serviceName}")
@Slf4j
//@Validated
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
@CacheConfig(cacheNames = {ModuleOption.ID_PREFIX + E_${entityName}.SIMPLE_CLASS_NAME})
public class ${className} extends BaseService implements ${serviceName} {

    @Autowired
    private SimpleDao simpleDao;

    protected ${serviceName} getSelfProxy(){
        return getSelfProxy(${serviceName}.class);
    }

    @Operation(tags = {BIZ_NAME}, summary = CREATE_ACTION)
    @Override
<#if pkField?exists>
    public ${pkField.typeName} create(Create${entityName}Req req){
<#else>
    public boolean create(Create${entityName}Req req){
</#if>
    <#list fields as field>
        <#if !field.notUpdate && field.uk>
        long ${field.name}Cnt = simpleDao.selectFrom(${entityName}.class)
                .eq("${field.name}", req.get${field.name?cap_first}())
                .count();
        if (${field.name}Cnt > 0) {
            throw new EntityExistsException("${field.desc}已经存在");
        }
        </#if>
    </#list>
        ${entityName} entity = simpleDao.create(req);
<#if pkField?exists>
        return entity.get${pkField.name?cap_first}();
<#else>
        return entity != null;
</#if>
    }

    @Operation(tags = {BIZ_NAME}, summary = BATCH_CREATE_ACTION)
    @Transactional(rollbackFor = Exception.class)
    @Override
<#if pkField?exists>
    public List<${pkField.typeName}> batchCreate(List<Create${entityName}Req> reqList){
    <#else>
    public List<Boolean> batchCreate(List<Create${entityName}Req> reqList){
</#if>
        return reqList.stream().map(this::create).collect(Collectors.toList());
    }

<#if pkField?exists>
    @Operation(tags = {BIZ_NAME}, summary = VIEW_DETAIL_ACTION)
    @Override
    //Srping 4.3提供了一个sync参数。是当缓存失效后，为了避免多个请求打到数据库,系统做了一个并发控制优化，同时只有一个线程会去数据库取数据其它线程会被阻塞。
    @Cacheable(sync = false, condition = "#${pkField.name} != null", unless = "#result == null ", key = E_${entityName}.CACHE_KEY_PREFIX + "#${pkField.name}")
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        Assert.notNull(${pkField.name}, BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.findOneByQueryObj(new Query${entityName}Req().set${pkField.name?cap_first}(${pkField.name}));
    }

    @Operation(tags = {BIZ_NAME}, summary = VIEW_DETAIL_ACTION)
    @Override
    //只更新缓存
    @CachePut(unless = "#result == null" , condition = "#req.${pkField.name} != null" , key = E_${entityName}.CACHE_KEY_PREFIX + "#req.${pkField.name}")
    public ${entityName}Info findById(Query${entityName}ByIdReq req) {
        return simpleDao.findOneByQueryObj(req);
    }
</#if>

    @Operation(tags = {BIZ_NAME}, summary = UPDATE_ACTION)
    @Override
    @CacheEvict(condition = "#req.${pkField.name} != null", key = E_${entityName}.CACHE_KEY_PREFIX + "#req.${pkField.name}")    
    public int update(Update${entityName}Req req) {
        return simpleDao.updateByQueryObj(req);
    }

    @Operation(tags = {BIZ_NAME}, summary = BATCH_UPDATE_ACTION)
    @Transactional(rollbackFor = Exception.class)
    @Override
    //@Caching(evict = {
        //@CacheEvict(condition = "#reqList != null && #reqList.size() > 0", allEntries = true)
    //})
    public List<Integer> batchUpdate(List<Update${entityName}Req> reqList){
        //@Todo 优化批量提交
        return reqList.stream().map(req -> getSelfProxy().update(req)).collect(Collectors.toList());
    }

    @Operation(tags = {BIZ_NAME}, summary = DELETE_ACTION)
    @Override
    @Caching(evict = {
        //尽量不用调用批量删除，会导致缓存清空
        @CacheEvict(condition = "#req.${pkField.name} != null", key = E_${entityName}.CACHE_KEY_PREFIX + "#req.${pkField.name}"),
        @CacheEvict(condition = "#req.${pkField.name}List != null && #req.${pkField.name}List.length > 0", allEntries = true),
    })                    
    public int delete(Delete${entityName}Req req) {
        return simpleDao.deleteByQueryObj(req);
    }

    @Operation(tags = {BIZ_NAME}, summary = QUERY_ACTION)
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging) {
        return simpleDao.findPagingDataByQueryObj(req, paging);
    }

    @Operation(tags = {BIZ_NAME}, summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findOne(Query${entityName}Req req){
        return simpleDao.findOneByQueryObj(req);
    }

}
