package ${packageName};

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

import ${entityClassPackage}.*;

<#list fields as field>
    <#if !field.baseType && field.enums>
import ${field.classType.name};
        <#list field.imports as imp>
import ${imp};
        </#list>
    </#if>
</#list>


/**
 *  创建${desc}
 *  //Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "创建${desc}")
@Data
@Accessors(chain = true)
@ToString
<#--@EqualsAndHashCode(callSuper = true)-->
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ${className} implements ServiceReq {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>
    <#if (!field.notUpdate && !field.hasDefValue && !field.complex) || (field.identity?? && !field.identity)>
    @Schema(description = "${field.desc}")
    <#list field.annotations as annotation>
    ${annotation}
    </#list>
    private ${field.type} ${field.name};

    </#if>
</#list>


}
