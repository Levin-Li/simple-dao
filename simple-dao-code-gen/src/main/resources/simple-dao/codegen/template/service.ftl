package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import org.springframework.cache.annotation.*;
import java.util.*;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;

import ${modulePackageName}.*;
import ${modulePackageName}.entities.*;
import static ${modulePackageName}.entities.EntityConst.*;


/**
 *  ${desc}-服务接口
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)

@CacheConfig(cacheNames = {ModuleOption.ID_PREFIX + E_${entityName}.SIMPLE_CLASS_NAME})
public interface ${className} {

    String BIZ_NAME = E_${entityName}.BIZ_NAME;

    @Operation(tags = {BIZ_NAME}, summary = CREATE_ACTION + BIZ_NAME)
<#if pkField?exists>
    ${pkField.typeName} create(Create${entityName}Req req);
<#else>
    boolean create(Create${entityName}Req req);
</#if>

    @Operation(tags = {BIZ_NAME}, summary = BATCH_CREATE_ACTION + BIZ_NAME)
<#if pkField?exists>
    List<${pkField.typeName}> batchCreate(List<Create${entityName}Req> reqList);
<#else>
    List<Boolean> batchCreate(List<Create${entityName}Req> reqList);
</#if>

<#if pkField?exists>
    @Operation(tags = {BIZ_NAME}, summary = VIEW_DETAIL_ACTION + BIZ_NAME)
    //Srping 4.3提供了一个sync参数。是当缓存失效后，为了避免多个请求打到数据库,系统做了一个并发控制优化，同时只有一个线程会去数据库取数据其它线程会被阻塞。
    @Cacheable(sync = false, condition = "#${pkField.name} != null", unless = "#result == null ", key = E_User.CACHE_KEY_PREFIX + "#${pkField.name}")
    ${entityName}Info findById(${pkField.typeName} ${pkField.name});
</#if>

    @Operation(tags = {BIZ_NAME}, summary = UPDATE_ACTION + BIZ_NAME)
    @CacheEvict(condition = "#req.${pkField.name} != null", key = E_User.CACHE_KEY_PREFIX + "#req.${pkField.name}")
    int update(Update${entityName}Req req);

    //尽量不用调用批量删除，会导致缓存清空
    @Operation(tags = {BIZ_NAME}, summary = BATCH_UPDATE_ACTION + BIZ_NAME)
    @CacheEvict(condition = "#reqList != null && #reqList.size() > 0", allEntries = true)
    List<Integer> batchUpdate(List<Update${entityName}Req> reqList);

    @Operation(tags = {BIZ_NAME}, summary = DELETE_ACTION + BIZ_NAME)
    @Caching(evict = {  //尽量不用调用批量删除，会导致缓存清空
        @CacheEvict(condition = "#req.${pkField.name} != null", key = E_User.CACHE_KEY_PREFIX + "#req.${pkField.name}"),
        @CacheEvict(condition = "#req.${pkField.name}List != null && #req.${pkField.name}List.length > 0", allEntries = true),
    })
    int delete(Delete${entityName}Req req);

    @Operation(tags = {BIZ_NAME}, summary = QUERY_ACTION + BIZ_NAME)
    PagingData<${entityName}Info> query(Query${entityName}Req req , Paging paging);

}
