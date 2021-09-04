package ${packageName};

import static ${modulePackageName}.ModuleOption.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import java.util.*;
import java.util.stream.*;
import org.springframework.transaction.annotation.*;
import org.springframework.util.*;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.*;

import ${entityClassPackage}.*;
import ${entityClassName};

import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

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
 *  ${desc}服务实现
 *
 *@author auto gen by simple-dao-codegen ${.now}
 *
 */

//@Valid只能用在controller。@Validated可以用在其他被spring管理的类上。

@Service(PLUGIN_PREFIX + "${serviceName}")
@Slf4j
//@Validated
public class ${className} implements ${serviceName} {

    @Autowired
    private SimpleDao simpleDao;

    @Operation(tags = {ENTITY_NAME}, summary = "新增" + ENTITY_NAME)
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

    @Operation(tags = {ENTITY_NAME}, summary = "批量新增" + ENTITY_NAME)
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
    @Operation(tags = {ENTITY_NAME}, summary = "通过ID找回" + ENTITY_NAME)
    @Override
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        return simpleDao.findOneByQueryObj(new Query${entityName}Req().set${pkField.name?cap_first}(${pkField.name}));
    }
</#if>

    @Operation(tags = {ENTITY_NAME}, summary = "更新" + ENTITY_NAME)
    @Override
    public int update(Update${entityName}Req req) {
        return simpleDao.updateByQueryObj(req);
    }

    @Operation(tags = {ENTITY_NAME}, summary = "批量更新" + ENTITY_NAME)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Integer> batchUpdate(List<Update${entityName}Req> reqList){
        return reqList.stream().map(this::update).collect(Collectors.toList());
    }

    @Operation(tags = {ENTITY_NAME}, summary = "删除" + ENTITY_NAME)
    @Override
    public int delete(Delete${entityName}Req req) {
        return simpleDao.deleteByQueryObj(req);
    }

    @Operation(tags = {ENTITY_NAME}, summary = "分页查找" + ENTITY_NAME)
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging) {
        return simpleDao.findPagingDataByQueryObj(req, paging);
    }
}
