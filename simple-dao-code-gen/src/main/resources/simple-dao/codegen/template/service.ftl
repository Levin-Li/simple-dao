package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;

import java.util.*;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;

import static ${entityClassPackage}.EntityOption.*;

/**
 *  ${desc}服务
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
public interface ${className} {

    String ENTITY_NAME = E_${entityName}.BIZ_NAME;

    @Operation(tags = {ENTITY_NAME}, summary = CREATE_ACTION + ENTITY_NAME)
<#if pkField?exists>
    ${pkField.typeName} create(Create${entityName}Req req);
<#else>
    boolean create(Create${entityName}Req req);
</#if>

    @Operation(tags = {ENTITY_NAME}, summary = BATCH_CREATE_ACTION + ENTITY_NAME)
<#if pkField?exists>
    List<${pkField.typeName}> batchCreate(List<Create${entityName}Req> reqList);
<#else>
    List<Boolean> batchCreate(List<Create${entityName}Req> reqList);
</#if>

<#if pkField?exists>
    @Operation(tags = {ENTITY_NAME}, summary = VIEW_DETAIL_ACTION + ENTITY_NAME)
    ${entityName}Info findById(${pkField.typeName} ${pkField.name});
</#if>

    @Operation(tags = {ENTITY_NAME}, summary = UPDATE_ACTION + ENTITY_NAME)
    int update(Update${entityName}Req req);

    @Operation(tags = {ENTITY_NAME}, summary = BATCH_UPDATE_ACTION + ENTITY_NAME)
    List<Integer> batchUpdate(List<Update${entityName}Req> reqList);

    @Operation(tags = {ENTITY_NAME}, summary = DELETE_ACTION + ENTITY_NAME)
    int delete(Delete${entityName}Req req);

    @Operation(tags = {ENTITY_NAME}, summary = QUERY_ACTION + ENTITY_NAME)
    PagingData<${entityName}Info> query(Query${entityName}Req req , Paging paging);

}
