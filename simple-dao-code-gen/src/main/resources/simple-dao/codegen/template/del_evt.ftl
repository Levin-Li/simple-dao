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

////////////////////////////////////
//自动导入列表
<#list importList as imp>
    import ${imp};
</#list>
////////////////////////////////////

/**
 *  删除${desc}
 *  //Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "删除${desc}")
@Data

<#if pkField?exists>
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
</#if>

@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)
public class ${className} implements ServiceReq {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#if pkField?exists>
    @Schema(description = "${pkField.desc}")
    private ${pkField.typeName} ${pkField.name};

    @Schema(description = "${pkField.desc}集合")
    @In(E_${entityName}.${pkField.name})
    @Validator(expr = "${pkField.name} != null || ( ${pkField.name}s != null &&  ${pkField.name}s.length > 0)" , promptInfo = "删除${desc}必须指定ID")
    private ${pkField.typeName}[] ${pkField.name}s;


    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }

    public ${className}(${pkField.typeName}... ${pkField.name}s) {
        this.${pkField.name}s = ${pkField.name}s;
    }
</#if>
}
