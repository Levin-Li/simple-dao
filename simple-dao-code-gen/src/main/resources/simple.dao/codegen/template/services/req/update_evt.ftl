package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->
import static ${modulePackageName}.entities.EntityConst.*;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
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

import javax.validation.constraints.*;
import javax.annotation.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

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
 * 更新${entityTitle}
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Schema(title = UPDATE_ACTION + BIZ_NAME)
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
public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if pkField?exists>
    @Schema(title = ${pkField.schemaTitle}, required = true, requiredMode = REQUIRED)
    @NotNull
    @Eq(require = true)
    ${pkField.typeName} ${pkField.name};

</#if>
<#if classModel.isType('com.levin.commons.dao.domain.EditableObject')>
    @Schema(description = "可编辑条件" , hidden = true)
    @Eq(condition = "!#" + InjectConsts.IS_SUPER_ADMIN)
    final boolean eqEditable = true;

</#if>
<#-- 字段分组，参考 CRUD枚举，UPDATE_fields 表示更新分组 -->
<#list UPDATE_fields as field>
    <#if !field.notUpdate && (!field.lazy || field.baseType) && field.baseType && !field.jpaEntity >
    <#list field.annotations as annotation>
        <#if !(annotation?string)?contains("@NotNull")>
    ${annotation}
        </#if>
    </#list>
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if> ${field.hidden?string(' , hidden = true', '')})
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};

    </#if>
</#list>
<#-- 字段分组，参考 CRUD枚举，默认是 CRUD.DEFAULT 分组，没有前缀 -->
<#list fields as field>
    <#if !field.notUpdate && (!field.lazy || field.baseType) && field.baseType && !field.jpaEntity >
    <#list field.annotations as annotation>
    <#if !(annotation?string)?contains("@NotNull")>
    ${annotation}
    </#if>
    </#list>
    <#if field.optimisticLock>
    @Eq(desc = "乐观锁更新条件")
    @Update(incrementMode = true, paramExpr = "1", condition = "", desc = "乐观锁版本号 + 1")
    </#if>
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if>${field.hidden?string(' , hidden = true', '')})
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};

    </#if>
</#list>

<#if pkField?exists>
    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }

    public ${className} update${pkField.name?cap_first}WhenNotBlank(${pkField.typeName} ${pkField.name}){
        if(isNotBlank(${pkField.name})){
        this.${pkField.name} = ${pkField.name};
        }
        return this;
    }

</#if>
    @PostConstruct
    public void preUpdate() {
        //@todo 更新之前初始化数据
<#list fields as field>
    <#if classModel.isDefaultUpdateTime(field.name)>

        if(get${field.name?cap_first}() == null){
            set${field.name?cap_first}(<#if field.typeName =='Date'>new ${field.typeName}()<#else>${field.typeName}.now()</#if>);
        }
    </#if>
</#list>
    }
}
