package ${packageName};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import javax.validation.constraints.*;

import ${entityClassPackage}.*;

<#list fields as field>
    <#if !field.baseType && field.enums>
 import ${field.classType.name};
    </#if>
    <#if (field.infoClassName)??>
 import ${field.infoClassName};
    </#if>
    <#list field.imports as imp>
 import ${imp};
    </#list>
</#list>


import java.io.Serializable;
import java.util.Date;


/**
* ${desc}
* ${.now}
*/
@Schema(description ="${desc}")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(of = {"${pkField.name}"})
@ToString(exclude = {<#list fields as field><#if field.lazy?default(false)>"${field.name}${field.excessSuffix!}"<#if field?has_next>,</#if></#if></#list>})
@FieldNameConstants
public class ${className} implements Serializable {

   private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>
  <#if field.complex>
   <#if (field.lazy)??>
   @Fetch(value = "${field.name}")
   </#if>
   @Schema(description = "${field.desc}")
   private ${field.excessReturnType} ${field.name}${field.excessSuffix};
 
  <#else>
   @Schema(description = "${field.desc}")
   private ${field.type} ${field.name};

  </#if>
</#list>

}
