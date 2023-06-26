package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->
import static ${modulePackageName}.entities.EntityConst.*;

import io.swagger.v3.oas.annotations.media.Schema;
import com.levin.commons.dao.annotation.Ignore;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.support.*;

import org.springframework.format.annotation.*;

import javax.validation.constraints.*;
import javax.annotation.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

import ${servicePackageName}.info.*;
import ${entityClassName};

import ${entityClassPackage}.*;
import static ${entityClassPackage}.E_${entityName}.*;
import ${modulePackageName}.services.commons.req.*;

////////////////////////////////////
//自动导入列表
<#list importList as imp>
    import ${imp};
</#list>
////////////////////////////////////

/**
 *  查询${entityTitle}
 *  @Author Auto gen by simple-dao-codegen ${.now}
 *  代码生成哈希校验码：[]
 */
@Schema(title = QUERY_ACTION + BIZ_NAME)
@Data
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS, resultClass = ${entityName}Info.class)
public class ${className} extends ${reqExtendClass}{

    private static final long serialVersionUID = ${serialVersionUID}L;

    @Ignore
    @Schema(title = "排序字段")
    String orderBy;

    //@Ignore
    @Schema(title = "排序方向")
    @SimpleOrderBy(expr = "orderBy + ' ' + orderDir", condition = "orderBy != null && orderDir != null", remark = "生成排序表达式")
    OrderBy.Type orderDir;

<#list fields as field>

    <#list field.annotations as annotation>
        <#if annotation?contains('PrimitiveArrayJsonConverter.class')>
    @OR(autoClose = true)
    @Contains
    @InjectVar(domain = "dao",  converter = JsonStrLikeConverter.class, isRequired = "false")
        <#else>
    ${annotation}
        </#if>
    </#list>
    <#-- 如果是日期类型 -->
    <#if field.typeName == 'Date'>
    @Schema(title = ${field.schemaTitle} , description = "大于等于" + ${field.schemaTitle})
    @Gte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} gte${field.name?cap_first};

    @Schema(title = ${field.schemaTitle} , description = "小于等于" + ${field.schemaTitle})
    @Lte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} lte${field.name?cap_first};

    //@Schema(title = ${field.schemaTitle} + "-日期范围"<#if field.desc != ''> , description = ${field.schemaDesc}</#if>)
    //@Between(paramDelimiter = "-", patterns = {"yyyyMMdd","yyyy-MM-dd"})
    //${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}String between${field.name?cap_first};

    <#-- 基本类型 -->
    <#elseif field.baseType>
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if>)
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};
    <#if field.contains && field.typeName = 'String'>  <#-- 模糊匹配 -->

    @Schema(title = "模糊匹配-" + ${field.schemaTitle})
    @${field.extras.nameSuffix}
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.extras.nameSuffix?uncap_first}${field.name?cap_first};
    </#if>
    <#elseif field.lazy!>
    @Schema(title = "是否加载" + ${field.schemaTitle})
    @Fetch(attrs = E_${entityName}.${field.name}, condition = "#_val == true")
    Boolean load${field.name?cap_first};
    </#if>
    <#-- 字段结束 -->
</#list>

<#if pkField?exists>
    <#-- 构造函数-->
    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }
</#if>
    <#-- 查询前的动作 -->
    @PostConstruct
    public void preQuery() {
        //@todo 查询之前初始化数据
    }

}
