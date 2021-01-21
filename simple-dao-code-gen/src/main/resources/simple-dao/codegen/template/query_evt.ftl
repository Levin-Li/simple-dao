package ${packageName};

<#--import com.oak.api.model.ApiBaseQueryReq;-->
import io.swagger.v3.oas.annotations.media.Schema;

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

import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

import ${entityClassName};

<#list fields as field>
    <#if !field.baseType && field.enums>
import ${field.classType.name};
        <#list field.imports as imp>
import ${imp};
        </#list>
    </#if>
</#list>

/**
 *  查询${desc}
 *  ${.now}
 */
@Schema(description = "查询${desc}")
@Data
@AllArgsConstructor
@Builder
<#--@EqualsAndHashCode(callSuper = true)-->
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS, resultClass = ${entityName}Info.class)
public class ${className} extends PagingQueryReq {

<#list fields as field>
    <#if field.type=='Date'>
    @Schema(description = "最小${field.desc}")
    @Gte(E_${entityName}.${field.name})
    private ${field.type} min${field.name?cap_first};

    @Schema(description = "最大${field.desc}")
    @Lte(E_${entityName}.${field.name})
    private ${field.type} max${field.name?cap_first};

    <#elseif !field.complex>
    @Schema(description = "${field.desc}")
    private ${field.type} ${field.name};

    <#if field.like>
    @Schema(description = "${field.desc}")
    @Contains(E_${entityName}.${field.name})
    private ${field.type} ${field.name}Like;

    </#if>
    <#elseif field.lazy!>
    @Schema(description = "加载${field.desc}")
    @Fetch(value = E_${entityName}.${field.name}, condition = "#_val==true")
    private Boolean load${field.name?cap_first};

    </#if>
</#list>
    public ${className}() {
    }

    public ${className}(${pkField.type} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }
}
