package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

import io.swagger.v3.oas.annotations.*;

import java.util.*;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;


/**
 *  ${desc}服务
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
public interface ${className} {

    String ENTITY_NAME ="${desc}";

    @Operation(tags = {ENTITY_NAME}, summary = "新增" + ENTITY_NAME)
<#if pkField?exists>
    ${pkField.typeName} create(Create${entityName}Req req);
<#else>
    boolean create(Create${entityName}Req req);
</#if>

    @Operation(tags = {ENTITY_NAME}, summary = "批量新增" + ENTITY_NAME)
<#if pkField?exists>
    List<${pkField.typeName}> batchCreate(List<Create${entityName}Req> reqList);
<#else>
    List<Boolean> batchCreate(List<Create${entityName}Req> reqList);
</#if>

<#if pkField?exists>
    @Operation(tags = {ENTITY_NAME}, summary = "通过ID找回" + ENTITY_NAME)
    ${entityName}Info findById(${pkField.typeName} ${pkField.name});
</#if>

    @Operation(tags = {ENTITY_NAME}, summary = "更新" + ENTITY_NAME)
    int update(Update${entityName}Req req);

    @Operation(tags = {ENTITY_NAME}, summary = "批量更新" + ENTITY_NAME)
    List<Integer> batchUpdate(List<Update${entityName}Req> reqList);

    @Operation(tags = {ENTITY_NAME}, summary = "删除" + ENTITY_NAME)
    int delete(Delete${entityName}Req req);

    @Operation(tags = {ENTITY_NAME}, summary = "分页查找" + ENTITY_NAME)
    PagingData<${entityName}Info> query(Query${entityName}Req req , Paging paging);

}
