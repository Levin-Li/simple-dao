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
 *  查询${desc}
 *  @Author Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = QUERY_ACTION + BIZ_NAME)
@Data
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS, resultClass = ${entityName}Info.class)
public class ${className} extends ${isMultiTenantObject ? string('MultiTenantReq','BaseReq')}{

    private static final long serialVersionUID = ${serialVersionUID}L;

    @Ignore
    @Schema(description = "排序字段")
    String orderBy;

    //@Ignore
    @Schema(description = "排序方向-desc asc")
    @SimpleOrderBy(expr = "orderBy + ' ' + orderDir", condition = "orderBy != null && orderDir != null", remark = "生成排序表达式")
    OrderBy.Type orderDir;

<#list fields as field>

    <#list field.annotations as annotation>
    //${annotation}
    </#list>
    <#-- 如果是日期类型 -->
    <#if field.typeName == 'Date'>
    // @DateTimeFormat(iso = ISO.DATE_TIME) // Spring mvc 默认的时间格式：yyyy/MM/dd HH:mm:ss
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "大于等于" + ${field.schemaDesc} + "，默认的时间格式：yyyy/MM/dd HH:mm:ss")
    @Gte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} gte${field.name?cap_first};

    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "小于等于" + ${field.schemaDesc} + "，默认的时间格式：yyyy/MM/dd HH:mm:ss")
    @Lte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} lte${field.name?cap_first};

    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '-日期范围\", ', '')}description = ${field.schemaDesc} + "-日期范围，格式：yyyyMMdd-yyyyMMdd，大于等于且小余等于")
    @Between(paramDelimiter = "-", patterns = {"yyyyMMdd"})
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}String between${field.name?cap_first};
    <#-- 基本类型 -->
    <#elseif field.baseType>
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = ${field.schemaDesc})
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};
    <#if field.contains>
    <#-- 模糊匹配 -->
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "模糊匹配 - " + ${field.schemaDesc})
    @${field.extras.nameSuffix}
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.extras.nameSuffix?uncap_first}${field.name?cap_first};
    </#if>
    <#elseif field.lazy!>
    @Schema(description = "是否加载" + ${field.schemaDesc})
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
