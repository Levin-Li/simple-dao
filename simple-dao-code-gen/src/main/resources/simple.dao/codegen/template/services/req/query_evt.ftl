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

import javax.validation.constraints.*;
import javax.annotation.*;

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
 * 查询${entityTitle}
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Schema(title = QUERY_ACTION + BIZ_NAME)
@Data
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS, resultClass = ${entityName}Info.class)
public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>
    @Schema(title = "是否包含平台公共数据")
    @Ignore
    boolean isContainsPublicData = true;

</#if>
<#if classModel.isType('com.levin.commons.dao.domain.OrganizedPublicObject')>
    @Schema(title = "是否包含组织公共数据")
    @Ignore
    boolean isContainsOrgPublicData = true;

</#if>
    @Ignore
    @Schema(title = "排序字段")
    String orderBy;

    public ${className} setOrderBy(String orderBy) {
        //要防止SQL注
        return checkSQLInject(this.orderBy = orderBy);
    }

    //@Ignore
    @Schema(title = "排序方向")
    @SimpleOrderBy(expr = "orderBy + ' ' + orderDir", condition = "#isNotEmpty(orderBy) && #isNotEmpty(orderDir)", remark = "生成排序表达式")
<#if classModel.isType('com.levin.commons.dao.domain.SortableObject')>
    @OrderBy(value = E_${entityName}.orderCode, condition = "#isEmpty(orderBy) || #isEmpty(orderDir)", order = Integer.MAX_VALUE - 10000, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "默认按顺序排序")
</#if>
<#if classModel.findFirstAttr('createTime','addTime','occurTime')??>
    @OrderBy(value = E_${entityName}.${classModel.findFirstAttr('createTime','addTime','occurTime')}, condition = "#isEmpty(orderBy) || #isEmpty(orderDir)", order = Integer.MAX_VALUE - 10000, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "默认按时间排序")
</#if>
    OrderBy.Type orderDir;

    @Schema(title = "查询的字段列表", description = "逗号隔开，默认查询所有的字段")
    @Select(value = C.FIELD_VALUE, condition = "#_isQuery && #isNotEmpty(#_fieldVal)")
    String[] selectColumns;

    public ${className} setSelectColumns(String... selectColumns) {
        //要防止SQL注
        return checkSQLInject(this.selectColumns = selectColumns);
    }

<#if pkField?exists>
    @Schema(title = ${pkField.schemaTitle} + "集合")
    @In(E_${entityName}.${pkField.name})
    List<${pkField.typeName}> ${pkField.name}List;

</#if>

<#list fields as field>

    <#list field.annotations as annotation>
        <#if annotation?contains('PrimitiveArrayJsonConverter.class')>
    @OR(autoClose = true)
    @Contains
    @InjectVar(domain = "dao", converter = JsonStrLikeConverter.class, isRequired = "false")
        <#else>
    ${annotation}
        </#if>
    </#list>
    <#-- 如果是日期类型 -->
    <#if field.typeName == 'Date'>
    @Schema(title = ${field.schemaTitle} , description = ${field.schemaTitle} + "大于等于字段值")
    @Gte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} gte${field.name?cap_first};

    @Schema(title = ${field.schemaTitle} , description = ${field.schemaTitle} + "小于等于字段值")
    @Lte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} lte${field.name?cap_first};

    @Schema(title = ${field.schemaTitle} + "-日期范围"<#if field.desc != ''> , description = ${field.schemaDesc}</#if>)
    @Between
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}String between${field.name?cap_first};

    <#-- 基本类型 -->
    <#elseif field.baseType>
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if>)
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};
    <#-- 模糊匹配 -->
    <#if field.contains && field.typeName = 'String'>

    @Schema(title = "模糊匹配-" + ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if>)
    @${field.extras.nameSuffix}
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.extras.nameSuffix?uncap_first}${field.name?cap_first};
    </#if>
    </#if>
    <#if field.lazy!>

    @Schema(title = "是否加载" + ${field.schemaTitle})
    @Fetch(attrs = E_${entityName}.${field.name}, condition = "#_val == true")
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
