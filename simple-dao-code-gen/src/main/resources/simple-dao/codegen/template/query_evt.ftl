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

import ${servicePackageName}.info.*;
import ${entityClassName};

import ${entityClassPackage}.*;


////////////////////////////////////
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
<#--@EqualsAndHashCode(callSuper = true)-->
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS
, resultClass = ${entityName}Info.class)
public class ${className} implements ServiceReq  {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>

    <#if field.typeName == 'Date'>
    @Schema(description = "最小${field.desc}")
    @Gte(E_${entityName}.${field.name})
    private ${field.typeName} min${field.name?cap_first};

    @Schema(description = "最大${field.desc}")
    @Lte(E_${entityName}.${field.name})
    private ${field.typeName} max${field.name?cap_first};

    <#elseif field.baseType>
    @Schema(description = "${field.desc}")
    private ${field.typeName} ${field.name};
    <#if field.contains>
    @Schema(description = "${field.desc}")
    @Contains(E_${entityName}.${field.name})
    private ${field.typeName} ${field.name}Contains;
    </#if>
    <#elseif field.lazy!>
    @Schema(description = "加载${field.desc}")
    @Fetch(attrs = E_${entityName}.${field.name}, condition = "#_val == true")
    private Boolean load${field.name?cap_first};
    </#if>

</#list>

    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }
}
