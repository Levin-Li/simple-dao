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

import org.springframework.format.annotation.*;

import javax.validation.constraints.*;
import javax.annotation.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;
import java.io.Serializable;

import ${servicePackageName}.info.*;
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
 *  统计${desc}
 *  @Author Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = STAT_ACTION + E_${entityName}.BIZ_NAME)
@Data
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS,
     //连接统计
    //joinOptions = { @JoinOption(entityClass = XXX.class,alias = E_XXX.ALIAS,joinColumn = E_XXX.joinColumn)},
    resultClass = ${className}.Result.class
)
public class ${className} extends ${isMultiTenantObject ? string('MultiTenantReq','BaseReq')}{

    private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>

    <#list field.annotations as annotation>
    //${annotation}
    </#list>
    <#if field.typeName == 'Date'>
    // @DateTimeFormat(iso = ISO.DATE_TIME) // Spring mvc 默认的时间格式：yyyy/MM/dd HH:mm:ss
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "大于等于${field.desc}，默认的时间格式：yyyy/MM/dd HH:mm:ss")
    @Gte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} gte${field.name?cap_first};

    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "小于等于${field.desc}，默认的时间格式：yyyy/MM/dd HH:mm:ss")
    @Lte
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} lte${field.name?cap_first};

    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '-日期范围\", ', '')}description = "${field.desc}-日期范围，格式：yyyyMMdd-yyyyMMdd，大于等于且小余等于")
    @Between(paramDelimiter = "-", patterns = {"yyyyMMdd"})
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}String between${field.name?cap_first};
    <#-- 如果是基本类型 -->
    <#elseif field.baseType>
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "${field.desc}")
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};
    <#if field.contains>
    <#-- 模糊匹配 -->
    @Schema(${(field.title!?trim!?length > 0)?string('title = \"' + field.title!?trim + '\", ', '')}description = "模糊匹配 - ${field.desc}")
    @${field.extras.nameSuffix}
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.extras.nameSuffix?uncap_first}${field.name?cap_first};
    </#if>
    </#if>
</#list>

<#if pkField?exists>
    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }
</#if>

    //
    //@Schema(description = "是否按状态分组统计")
    //@CtxVar //增加当前字段名称和字段值到环境变量中
    //@Ignore
    //private boolean isGroupByStatus;

    //@Schema(description = "是否按日期分组统计")
    //@CtxVar //增加当前字段名称和字段值到环境变量中
    //@Ignore //
    //private boolean isGroupByDate;


    @PostConstruct
    public void preStat() {
    //@todo 统计之前初始化数据
    }

    @Schema(description = "${desc}统计结果")
    @Data
    @Accessors(chain = true)
    @FieldNameConstants
    public static class Result
            implements Serializable {

        //@Schema(description = "状态分组统计")
        //@GroupBy(condition = "#isGroupByStatus")
        //Status status;

        //@Schema(description = "时间分组统计")
        //@GroupBy(condition = "#isGroupByDate", value = "date_format(" + E_${entityName}.createDate + ",'%Y-%m-%d')", orderBy = @OrderBy(type = OrderBy.Type.Asc))
        //String createDate;

        @Schema(description = "记录数")
        @Count
        Integer cnt;

        //@Schema(description = "分类记录数")
        //@Count(fieldCases = {@Case(column = E_${entityName}.status, whenOptions = {@Case.When(whenExpr = "OFF", thenExpr = "1")}, elseExpr = "NULL")})
        //Integer caseCnt;

        //@Schema(description = "累计" , havingOp=Op.Gt)
        //@Sum
        //Double sumGmv;

    }

}
