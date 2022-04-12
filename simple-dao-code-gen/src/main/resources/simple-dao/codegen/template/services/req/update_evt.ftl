package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

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
import javax.annotation.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

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
 *  更新${desc}
 *  Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "更新${desc}")
@Data
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)
//默认更新注解
@Update
public class ${className} extends ${isMultiTenantObject ? string('MultiTenantReq','BaseReq')} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if pkField?exists>
    @Schema(description = "${pkField.desc}" , required = true)
    @NotNull
    @Eq(require = true)
    ${pkField.typeName} ${pkField.name};
</#if>

<#if classModel.isType('com.levin.commons.dao.domain.EditableObject')>
    @Schema(description = "可编辑条件" , hidden = true)
    @Eq(condition ="!#user.isSuperAdmin()")
    final boolean eqEditable = true;
</#if>

<#list UPDATE_fields as field>
    <#if !field.notUpdate && !field.lazy && field.baseType && !field.jpaEntity >
    <#list field.annotations as annotation>
        <#if !(annotation?string)?contains("@NotNull")>
    ${annotation}
        </#if>
    </#list>
    @Schema(description = "${field.desc}"${field.hidden?string(' , hidden = true', '')})
    ${field.modifiersPrefix} ${field.typeName} ${field.name};
    </#if>
</#list>

<#list fields as field>
    <#if !field.notUpdate && !field.lazy && field.baseType && !field.jpaEntity >
    <#list field.annotations as annotation>
    <#if !(annotation?string)?contains("@NotNull")>
    ${annotation}
    </#if>
    </#list>
    @Schema(description = "${field.desc}"${field.hidden?string(' , hidden = true', '')})
    ${field.modifiersPrefix} ${field.typeName} ${field.name};

    </#if>
</#list>

<#if pkField?exists>
    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }
</#if>
    @PostConstruct
    public void preUpdate() {
        //@todo 更新之前初始化数据
<#list fields as field>
    <#if field.name == 'updateTime'>

        if(getUpdateTime() == null){
            setUpdateTime(new Date());
        }
    </#if>
    <#if field.name == 'lastUpdateTime'>

        if(getLastUpdateTime() == null){
            setLastUpdateTime(new Date());
        }
    </#if>
</#list>
    }

}
