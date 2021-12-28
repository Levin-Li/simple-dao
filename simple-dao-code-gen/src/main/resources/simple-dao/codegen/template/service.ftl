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
public interface ${className} {

    String BIZ_NAME = E_${entityName}.BIZ_NAME;

    @Operation(tags = {BIZ_NAME}, summary = CREATE_ACTION)
<#if pkField?exists>
    ${pkField.typeName} create(Create${entityName}Req req);
<#else>
    boolean create(Create${entityName}Req req);
</#if>

    @Operation(tags = {BIZ_NAME}, summary = BATCH_CREATE_ACTION)
<#if pkField?exists>
    List<${pkField.typeName}> batchCreate(List<Create${entityName}Req> reqList);
<#else>
    List<Boolean> batchCreate(List<Create${entityName}Req> reqList);
</#if>

<#if pkField?exists>
    @Operation(tags = {BIZ_NAME}, summary = VIEW_DETAIL_ACTION)
    ${entityName}Info findById(${pkField.typeName} ${pkField.name});
</#if>

    @Operation(tags = {BIZ_NAME}, summary = UPDATE_ACTION)
    int update(Update${entityName}Req req);

    //尽量不用调用批量删除，会导致缓存清空
    @Operation(tags = {BIZ_NAME}, summary = BATCH_UPDATE_ACTION)
    List<Integer> batchUpdate(List<Update${entityName}Req> reqList);

    @Operation(tags = {BIZ_NAME}, summary = DELETE_ACTION)
    int delete(Delete${entityName}Req req);

    @Operation(tags = {BIZ_NAME}, summary = QUERY_ACTION)
    PagingData<${entityName}Info> query(Query${entityName}Req req , Paging paging);

    @Operation(tags = {BIZ_NAME}, summary = QUERY_ACTION)
    ${entityName}Info findOne(Query${entityName}Req req);
}
