package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.*;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.*;

/////////////////////////////////////////////////////
import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import ${entityClassPackage}.*;

////////////////////////////////////
<#list importList as imp>
import ${imp};
</#list>
////////////////////////////////////

/**
* ${desc}
* @Author Auto gen by simple-dao-codegen ${.now}
*/
@Schema(description ="${desc}")
@Data
@Accessors(chain = true)
@NoArgsConstructor
<#if pkField?exists>
@EqualsAndHashCode(of = {"${pkField.name}"})
</#if>
@ToString(exclude = {<#list fields as field><#if field.lazy>"${field.name}"<#if field?has_next>,</#if></#if></#list>})
@FieldNameConstants
public class ${className} implements Serializable {

   private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>

   <#if field.lazy>
   //@Fetch //默认不加载，请通过查询对象控制
   </#if>
   <#list field.annotations as annotation>
   ${annotation}
   </#list>
   @Schema(description = "${field.desc}")
   private ${field.typeName} ${field.name};

</#list>

}
