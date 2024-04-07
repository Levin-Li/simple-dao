package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import java.util.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import org.springframework.validation.annotation.*;

import ${entityClassPackage}.*;
import ${entityClassName};
import ${bizBoPackageName}.*;
import ${servicePackageName}.*;
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
 *  ${entityTitle}-业务服务
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

@Tag(name = E_${entityName}.BIZ_NAME + "-业务服务", description = "")
public interface ${className} {

<#if isCacheableEntity && isMultiTenantObject>

<#if classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>
    /**
    * 加载租户的缓存${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * tenantId 为 null 时加载公共${entityTitle}
    *
    * @param userPrincipal 操作者
    * @param tenantId 可为null，为 null 时加载公共${entityTitle}
    * @return
    */
    List<${entityName}Info> loadCacheList(Serializable userPrincipal, String tenantId);

<#else>
    /**
    * 加载租户的缓存${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * tenantId 为 null 时加载公共${entityTitle}
    *
    * @param userPrincipal 操作者
    * @param tenantId 可为null，为 null 时加载公共${entityTitle}
    * @return
    */
    List<${entityName}Info> loadCacheListByTenant(Serializable userPrincipal, String tenantId);
</#if>

<#elseif isCacheableEntity>

    /**
    * 加载${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * @return
    */
    List<${entityName}Info> load${entityName}List();
</#if>

    /**
    * 统计
    *
    * @param req
    * @param paging 分页设置，可空
    * @return Stat${entityName}Req.Result
    */
    @Operation(summary = STAT_ACTION)
    Stat${entityName}Req.Result stat(Stat${entityName}Req req, Paging paging);
}
