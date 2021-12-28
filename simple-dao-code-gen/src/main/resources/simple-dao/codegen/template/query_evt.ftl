package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

<#--import com.oak.api.model.ApiBaseQueryReq;-->
import io.swagger.v3.oas.annotations.media.Schema;

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

import javax.validation.constraints.*;
import javax.annotation.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

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
 *  查询${desc}
 *  @Author Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "查询${desc}")
@Data
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS
, resultClass = ${entityName}Info.class)
public class ${className} extends ${isMultiTenantObject ? string('MultiTenantReq','BaseReq')}{

    private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>

    <#if field.typeName == 'Date'>
    @Schema(description = "大于等于${field.desc}")
    @Gte
    private ${field.typeName} gte${field.name?cap_first};

    @Schema(description = "小于等于${field.desc}")
    @Lte
    private ${field.typeName} lte${field.name?cap_first};

    <#elseif field.baseType>
    @Schema(description = "${field.desc}")
    private ${field.typeName} ${field.name};
    <#if field.contains>
    @Schema(description = "${field.desc}")
    @Contains(E_${entityName}.${field.name})
    private ${field.typeName} ${field.name}Contains;
    </#if>
    <#elseif field.lazy!>
    @Schema(description = "是否加载${field.desc}")
    @Fetch(attrs = E_${entityName}.${field.name}, condition = "#_val == true")
    private Boolean load${field.name?cap_first};
    </#if>

</#list>

<#if pkField?exists>
    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }
</#if>

    @PostConstruct
    public void preQuery() {
        //@todo 查询之前初始化数据
    }

}
