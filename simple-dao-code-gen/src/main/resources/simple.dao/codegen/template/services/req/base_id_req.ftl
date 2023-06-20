package ${packageName};

import static ${modulePackageName}.entities.EntityConst.*;


import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.service.domain.*;
import com.levin.commons.service.support.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import javax.annotation.*;
import javax.validation.constraints.*;

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
*  ${desc} 主键通用请求
*  //Auto gen by simple-dao-codegen ${.now}
*/

@Schema(title =  BIZ_NAME + " 主键通用查询")
@Data

<#if pkField?exists>
    ${(fields?size > 0) ? string('','//')}@AllArgsConstructor
</#if>

@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS, resultClass = ${entityName}Info.class)
public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if pkField?exists>
    @Schema(title = ${pkField.schemaTitle} , required = true, requiredMode = Schema.RequiredMode.REQUIRED)
    @Eq(require = true)
    //@NotNull
    protected ${pkField.typeName} ${pkField.name};

    public ${className} update${pkField.name?cap_first}WhenNotBlank(${pkField.typeName} ${pkField.name}){
        if(isNotBlank(${pkField.name})){
            this.${pkField.name} = ${pkField.name};
        }
        return this;
    }

</#if>

    @PostConstruct
    public void preQuery() {
        //@todo ID 查询之前初始化数据
    }
}