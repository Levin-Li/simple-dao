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

import javax.annotation.*;
import javax.validation.constraints.*;

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
 * 删除${entityTitle}
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 请不要修改和删除此行内容。
 * 代码生成哈希校验码：[], 请不要修改和删除此行内容。
 */
@Schema(title = DELETE_ACTION + BIZ_NAME)
@Data

<#if pkField?exists>
${(fields?size > 0) ? string('','//')}//@AllArgsConstructor
</#if>

@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)
public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if classModel.isType('com.levin.commons.dao.domain.EditableObject')>
    @Schema(description = "可编辑条件" , hidden = true)
    @Eq(condition = "!#" + InjectConsts.IS_SUPER_ADMIN)
    final boolean eqEditable = true;

</#if>
<#if pkField?exists>

    @Schema(title = ${pkField.schemaTitle} + "集合", required = true, requiredMode = REQUIRED)
    @In(value = E_${entityName}.${pkField.name})
    @NotEmpty
    private ${pkField.typeName}[] ${pkField.name}List;

    public ${className}(${pkField.typeName}... ${pkField.name}List) {
        this.${pkField.name}List = ${pkField.name}List;
    }

    public ${className} set${pkField.name?cap_first}List(${pkField.typeName}... ${pkField.name}List) {
        this.${pkField.name}List = ${pkField.name}List;
        return this;
    }

</#if>

    @PostConstruct
    public void preDelete() {
        //@todo 删除之前初始化数据
    }

}
