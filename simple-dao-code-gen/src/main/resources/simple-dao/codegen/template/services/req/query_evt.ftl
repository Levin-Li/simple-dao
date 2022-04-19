package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

<#--import com.oak.api.model.ApiBaseQueryReq;-->
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
@Schema(description = "查询${desc}")
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

    <#if field.typeName == 'Date'>
    // @DateTimeFormat(iso = ISO.DATE_TIME) // Spring mvc 默认的时间格式：yyyy/MM/dd HH:mm:ss
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "大于等于${field.desc}，默认的时间格式：yyyy/MM/dd HH:mm:ss")
    @Gte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} gte${field.name?cap_first};

    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "小于等于${field.desc}，默认的时间格式：yyyy/MM/dd HH:mm:ss")
    @Lte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} lte${field.name?cap_first};

    <#elseif field.baseType>
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "${field.desc}")
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};
    <#if field.contains>
    <#-- 模糊匹配 -->

    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "模糊匹配 - ${field.desc}")
    @${field.extras.nameSuffix}
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.extras.nameSuffix?uncap_first}${field.name?cap_first};
    </#if>
    <#elseif field.lazy!>
    @Schema(description = "是否加载${field.desc}")
    @Fetch(attrs = E_${entityName}.${field.name}, condition = "#_val == true")
    Boolean load${field.name?cap_first};
    </#if>

</#list>

<#if pkField?exists>
    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }
</#if>

    @PostConstruct
    public void preQuery() {
        //@todo 查询之前初始化数据
    }

}
