package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->
import static ${modulePackageName}.entities.EntityConst.*;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.*;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.*;
/////////////////////////////////////////////////////
import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import ${entityClassPackage}.*;
import static ${entityClassPackage}.E_${entityName}.*;
////////////////////////////////////
<#list importList as imp>
import ${imp};
</#list>
////////////////////////////////////

/**
 * ${entityTitle}
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 请不要修改和删除此行内容。
 * 代码生成哈希校验码：[], 请不要修改和删除此行内容。
 */
@Schema(title = BIZ_NAME)
@Data
@Accessors(chain = true)
@NoArgsConstructor
<#if pkField?exists>
@EqualsAndHashCode(of = {"${pkField.name}"})
</#if>
@ToString(exclude = {<#list fields as field><#if field.lazy>"${field.name}"<#if field?has_next>,</#if></#if></#list>})
@FieldNameConstants
<#if isMultiTenantObject>@JsonIgnoreProperties(tenantId)</#if>
public class ${className} implements Serializable {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>

   <#if field.lazy!>
    //@Fetch //默认不加载，请通过查询对象控制
   </#if>
   <#list field.annotations as annotation>
    ${annotation}
   </#list>
<#--    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if> ${field.required!?string(', required = true, requiredMode = REQUIRED', '')})-->
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if>)
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};
</#list>

}
