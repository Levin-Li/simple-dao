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

import static ${modulePackageName}.*;
import static ${modulePackageName}.entities.EntityConst.*;

/**
 *  ${desc}服务
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)

@CacheConfig(cacheNames = {ModuleOption.ID})
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
    @Cacheable(condition = "#id != null", unless = "#result == null ", key = E_User.CACHE_KEY_PREFIX + "#id")
    ${entityName}Info findById(${pkField.typeName} ${pkField.name});
</#if>

    @Operation(tags = {BIZ_NAME}, summary = UPDATE_ACTION + BIZ_NAME)
    @CacheEvict(condition = "#req.id != null", key = E_User.CACHE_KEY_PREFIX + "#req.id")
    int update(Update${entityName}Req req);

    @Operation(tags = {BIZ_NAME}, summary = BATCH_UPDATE_ACTION + BIZ_NAME)
    List<Integer> batchUpdate(List<Update${entityName}Req> reqList);

    @Operation(tags = {BIZ_NAME}, summary = DELETE_ACTION + BIZ_NAME)
    @CacheEvict(condition = "#req.id != null", key = E_User.CACHE_KEY_PREFIX + "#req.id")
    int delete(Delete${entityName}Req req);

    @Operation(tags = {BIZ_NAME}, summary = QUERY_ACTION + BIZ_NAME)
    PagingData<${entityName}Info> query(Query${entityName}Req req , Paging paging);

}
