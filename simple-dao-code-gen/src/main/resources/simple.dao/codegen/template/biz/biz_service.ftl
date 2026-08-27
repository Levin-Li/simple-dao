package ${packageName};

import ${entityClassPackage}.${entityName}.*;
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
<#if selfOverridableMatchFields?has_content>
    /**
    * 获取最匹配的${entityTitle}。
    * <p>
    * 每个字段均匹配参数值或公共值（null），并按参数声明顺序优先返回精确匹配的数据。
    */
    @Operation(summary = "获取最匹配的" + E_${entityName}.BIZ_NAME)
    ${entityName}Info findBestMatch(
<#list selfOverridableMatchFields as field>
            ${field.typeName} ${field.name}<#if field_has_next>,</#if>
</#list>
    );

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
