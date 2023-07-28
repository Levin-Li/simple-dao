package ${packageName};

//import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
/////////////////////////////////////////////////////
import javax.validation.constraints.*;
import javax.annotation.*;
import lombok.*;
import lombok.experimental.*;
import java.util.*;

///////////////////////////////////////////////////////
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
 * 新增${entityTitle}
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Schema(title = CREATE_ACTION + BIZ_NAME)
@Data
@Accessors(chain = true)
@ToString
//@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
@Builder
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)
public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>
<#--    <#if (field.baseType && !field.pk && (!field.lazy || field.baseType) && !field.autoGenValue)>-->
    <#if (field.baseType && (!field.lazy || field.baseType) && !field.autoGenValue)>
<#--    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if> ${field.baseEntityField?string(', hidden = true', '')} ${(field.required && !field.baseEntityField)?string(', required = true, requiredMode = REQUIRED', '')})-->
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if> ${field.baseEntityField?string(', hidden = true', '')})
    <#list field.annotations as annotation>
    ${field.baseEntityField?string('//', '')}${annotation}
    </#list>
<#--    <#if (field.baseEntityField && field.name =='creator')>-->
<#--    @InjectVar(InjectConsts.USER_ID)-->
<#--    </#if>-->
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};

    </#if>
</#list>

    @PostConstruct
    public void prePersist() {
       //@todo 保存之前初始化数据，比如时间，初始状态等
<#list fields as field>
    <#if field.typeName == 'String' && (field.name == 'sn' || field.name == 'uuid')>

        if(get${field.name?cap_first}() == null){
            set${field.name?cap_first}(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
        }
    </#if>
    <#if field.name == 'createTime' || field.name == 'addTime' || field.name == 'occurTime' >

        if(get${field.name?cap_first}() == null){
            set${field.name?cap_first}(<#if field.typeName =='Date'>new ${field.typeName}()<#else>${field.typeName}.now()</#if>);
        }
    </#if>
</#list>
    }
}
