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
//${(fields?size > 0) ? string('','//')}@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)

public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if pkField?exists>
    @Schema(title = ${pkField.schemaTitle}, required = true, requiredMode = REQUIRED)
    <#if pkField.typeName == 'String' >@NotBlank<#else>@NotNull</#if>
    @Eq(require = true)
    @Id//主键标识
    ${pkField.typeName} ${pkField.name};

    public ${className}() {
    }

    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }

    public ${className}(${pkField.typeName} ${pkField.name}, boolean autoForceUpdateField) {
        super(autoForceUpdateField);
        this.${pkField.name} = ${pkField.name};
    }

    public ${className} update${pkField.name?cap_first}WhenNotBlank(${pkField.typeName} ${pkField.name}){
        if(isNotBlank(${pkField.name})){
            this.${pkField.name} = ${pkField.name};
        }
        return this;
    }

    public static ${className} of(${pkField.typeName} ${pkField.name}, boolean autoForceUpdateField){
        return new ${className}(${pkField.name}, autoForceUpdateField);
    }

    @PostConstruct //@PreUpdate
    public void preUpdate() {
    //@todo 更新之前初始化数据

        super.preUpdate();

<#-- 简单防止树形节点的循环引用 -->
    <#if classModel.isType('com.levin.commons.dao.domain.TreeObject')>
        if(getParentId() != null && getParentId().equals(get${pkField.name?cap_first}())){
            throw new IllegalArgumentException("父节点不能设置为自己");
        }
    </#if>

    <#if classModel.isType('com.levin.commons.dao.domain.ConfidentialObject')>
        if (confidentialLevel != null
           && get_dataAccessLevel() != null
           && confidentialLevel > get_dataAccessLevel()) {
            throw new IllegalArgumentException("confidentialLevel great than " + get_dataAccessLevel());
        }
    </#if>

    }

</#if>
}
