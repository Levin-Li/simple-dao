package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->
import static ${modulePackageName}.entities.EntityConst.*;

import io.swagger.v3.oas.annotations.media.Schema;
import com.levin.commons.dao.annotation.Ignore;

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
import com.levin.commons.service.support.*;

import org.springframework.format.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.*;
import jakarta.annotation.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * 查询${entityTitle}
 * <p>
 * 这个一个生成的代码,  请务必不要修改这个类文件, 否则会导致后续无法再生成代码.
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Schema(title = QUERY_ACTION + BIZ_NAME)
@Data
//${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
//@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS, resultClass = ${entityName}Info.class)
public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if pkField?exists>
    @Schema(title = ${pkField.schemaTitle} + "集合")
    @In(E_${entityName}.${pkField.name})
    @Id//主键标识
    List<${pkField.typeName}> ${pkField.name}List;

</#if>
<#-- 注解宏 -->
<#macro FieldAnnotationList field keyword = '' ignoreInjectVar = false>
    <#list field.annotations as annotation>
    <#-- 兼容旧版本 -->
        <#if annotation?contains('PrimitiveArrayJsonConverter.class') && !ignoreInjectVar>
    @OR(autoClose = true)
    @InjectVar(domain = "dao", converter = JsonStrLikeConverter.class, isRequired = "false")
    @Contains
        <#elseif (keyword != '' &&  annotation?trim?starts_with(keyword)) || annotation?trim?starts_with('@PrimitiveValue') || annotation?trim?starts_with('@Ignore')  || annotation?trim?starts_with('@Eq')|| annotation?trim?starts_with('@Id') || annotation?trim?starts_with('@Version') || annotation?trim?starts_with('@Max') || annotation?trim?starts_with('@Size')>
           <#if annotation?trim?starts_with('@Eq') && field.isJsonColumn() && field.isSimpleCollectionType() >
    @OR(autoClose = true)
    @ForceSplitCondition
           </#if>
    ${annotation}
        </#if>
    </#list>
</#macro>
<#--  注解宏结束 -->
<#list fields as field>

   <#-- 是否有between注解 -->
    <#if field.hasBetweenAnnotation() || field.isDateTimeType() >
    @Schema(title = ${field.schemaTitle} + "范围", description = <#if field.desc != ''>${field.schemaDesc}<#else>${field.schemaTitle} + "-范围"</#if>)
    @Between
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}String between${field.name?cap_first};

    </#if>
    <#-- 如果是日期类型 -->
    <#if field.isDateTimeType()>
    @Schema(title = ${field.schemaTitle} + "开始" , description = <#if field.desc != ''>${field.schemaDesc}<#else>${field.schemaTitle} + "大于等于"</#if>)
    @Gte
    <@FieldAnnotationList field = field/>
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} gte${field.name?cap_first};

    @Schema(title = ${field.schemaTitle} + "结束", description = <#if field.desc != ''>${field.schemaDesc}<#else>${field.schemaTitle} + "小于等于"</#if>)
    @Lte
    <@FieldAnnotationList field = field/>
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} lte${field.name?cap_first};

    <#-- 非关联对象 -->
    <#elseif !field.hasJpaJoinColumn()>
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''>, description = ${field.schemaDesc}</#if>)
    <@FieldAnnotationList field = field  keyword='@Options'/>
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};
    <#-- 模糊匹配 -->
    <#if field.contains>

    @Schema(title = ${field.schemaTitle}, description = <#if field.desc != ''>${field.schemaDesc}<#else>${field.schemaTitle} + "-模糊匹配"</#if>)
    <@FieldAnnotationList field = field ignoreInjectVar = true />
        <#if field.isIterable()>
    @OR(autoClose = true)
    @ForceSplitCondition
        </#if>
    @${field.extras.nameSuffix}(<#if field.isJsonColumn() && field.isSimpleCollectionType()>jsonPath="$[*]"</#if>)
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.extras.nameSuffix?uncap_first}${field.name?cap_first};
    </#if>
    </#if>
    <#if field.enumerable!>  <#-- 可枚举的 -->

    <@FieldAnnotationList field = field keyword='@Options'/>
    @Schema(title = ${field.schemaTitle}, description = <#if field.desc != ''>${field.schemaDesc}<#else>${field.schemaTitle} + "-包含匹配"</#if>)
    @In
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}List<${field.typeName}> in${field.name?cap_first};

    <@FieldAnnotationList field = field keyword='@Options'/>
    @Schema(title = ${field.schemaTitle}, description = <#if field.desc != ''>${field.schemaDesc}<#else>${field.schemaTitle} + "-不包含匹配"</#if>)
    @NotIn
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}List<${field.typeName}> notIn${field.name?cap_first};
    </#if>
    <#if field.lazy!>

    @Schema(title = "是否加载" + ${field.schemaTitle})
    @Fetch(attrs = E_${entityName}.${field.name}, condition = "#_fieldVal == true")
    Boolean load${field.name?cap_first};
    </#if>
    <#-- 字段结束 -->
</#list>

<#if pkField?exists>
    <#-- 构造函数-->
    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }

</#if>
    <#-- 查询前的动作 -->
    @PostConstruct
    public void preQuery() {
        //@todo 查询之前初始化数据
    }
}
