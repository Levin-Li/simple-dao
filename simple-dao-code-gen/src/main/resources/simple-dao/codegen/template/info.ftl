package ${packageName};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.*;
import com.levin.commons.service.domain.Desc;

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

<#list fields as field>
  <#if field.complex>
   <#if (field.lazy)??>  @Schema(description = "${field.name}")</#if>
   @Schema(description = "${field.desc}")
   private ${field.excessReturnType} ${field.name}${field.excessSuffix};
 
  <#else>
   @Schema(description = "${field.desc}")
   private ${field.type} ${field.name};

  </#if>
</#list>

}
