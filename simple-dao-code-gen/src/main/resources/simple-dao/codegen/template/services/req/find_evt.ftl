package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

<#--import com.oak.api.model.ApiBaseReq;-->
import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.service.domain.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;
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
 * 查找${desc}
 * Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "查找${desc}")
@Data
@NoArgsConstructor
<#if pkField?exists>
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
</#if>
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS, resultClass = ${entityName}Info.class)
public class ${className} extends ${isMultiTenantObject ? string('MultiTenantReq','BaseReq')} {
    private static final long serialVersionUID = ${serialVersionUID}L;

<#if pkField?exists>
    @Schema(description = "${pkField.desc}")
    @NotNull
    @Eq(require = true)
    private ${pkField.typeName} ${pkField.name};
</#if>

}
