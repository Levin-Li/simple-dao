package ${packageName};

<#--import com.oak.api.model.ApiBaseReq;-->
import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.service.domain.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

import ${entityClassName};
import ${entityClassPackage}.*;

<#list fields as field>
    <#if !field.baseType && field.enums>
import ${field.classType.name};
        <#list field.imports as imp>
import ${imp};
        </#list>
    </#if>
</#list>

/**
 *  删除${desc}
 *  ${.now}
 */
@Schema(description = "删除${desc}")
@Data
@AllArgsConstructor
@Builder
<#--@EqualsAndHashCode(callSuper = true)-->
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)
public class ${className} implements ServiceReq {

    private static final long serialVersionUID = ${serialVersionUID}L;

    @Schema(description = "${pkField.desc}")
    private ${pkField.type} ${pkField.name};

    @Schema(description = "${pkField.desc}集合")
    @In(E_${entityName}.${pkField.name})
    @Validator(expr = "${pkField.name} == null && ( ${pkField.name}s ==null || ${pkField.name}s.length == 0)",promptInfo = "${pkField.desc}必须指定")
    private ${pkField.type}[] ${pkField.name}s;


    public ${className}(${pkField.type} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }

    public ${className}(${pkField.type}... ${pkField.name}s) {
        this.${pkField.name}s = ${pkField.name}s;
    }

}
