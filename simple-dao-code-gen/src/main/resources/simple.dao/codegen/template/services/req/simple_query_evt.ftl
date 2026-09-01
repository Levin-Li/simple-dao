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
<#if classModel.isType('com.levin.commons.dao.domain.ConfidentialObject')>
    @Schema(title = "是否是机密数据")
    @Ignore
    @JsonIgnore
    final boolean isConfidentialObject = true;

</#if>
    @Ignore
    @Schema(title = "排序字段")
    String orderBy;

    //@Ignore
    @Schema(title = "排序方向")
    @SimpleOrderBy(expr = "(orderBy?:'') + ' ' + (orderDir?:'')", condition = "#isNotEmpty(orderBy)", remark = "生成排序表达式")
<#if classModel.isType('com.levin.commons.dao.domain.SortableObject')>
    @OrderBy(value = E_${entityName}.orderCode, condition = "#enableDefaultOrderBy && (#isEmpty(orderBy))", order = Integer.MAX_VALUE - 10000, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "默认按顺序排序")
</#if>
<#if classModel.findFirstAttr('createTime','addTime','occurTime')??>
    @OrderBy(value = E_${entityName}.${classModel.findFirstAttr('createTime','addTime','occurTime')}, condition = "#enableDefaultOrderBy && (#isEmpty(orderBy))", order = Integer.MAX_VALUE - 10000, scope = OrderBy.Scope.OnlyForNotGroupBy, desc = "默认按时间排序")
</#if>
    OrderBy.Type orderDir = OrderBy.Type.Desc;

    @Schema(title = "查询的字段列表", description = "逗号隔开，默认查询所有的字段")
    @Select(value = C.FIELD_VALUE, alias = C.BLANK_VALUE, condition = "#_isQuery && #isNotEmpty(#_fieldVal)")
    Set<String> selectColumns;

    public ${className} setOrderBy(String orderBy) {
        //要防止SQL注
        return checkSQLInject(this.orderBy = orderBy);
    }

    public ${className} setSelectColumns(Set<String> selectColumns) {
        //要防止SQL注
        return checkSQLInject(this.selectColumns = selectColumns);
    }

    public ${className} selectColumns(String... selectColumns) {
        return setSelectColumns(Stream.of(selectColumns).filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    <#-- 查询前的动作 -->
    @PostConstruct
    public void preQuery() {
        //@todo 查询之前初始化数据
    }
}
