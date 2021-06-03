package ${packageName};

<#--import com.oak.api.model.ApiBaseReq;-->
import io.swagger.v3.oas.annotations.media.Schema;

/////////////////////////////////////////////////////
import javax.validation.constraints.*;
import lombok.*;
import lombok.experimental.*;
import java.util.*;

///////////////////////////////////////////////////////
import com.levin.commons.service.domain.*;
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
//自动导入列表
<#list importList as imp>
    import ${imp};
</#list>
////////////////////////////////////


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

    <#if ( field.baseType && !field.pk && !field.lazy && !field.autoIdentity)>
    @Schema(description = "${field.desc}")
    <#list field.annotations as annotation>
    ${annotation}
    </#list>
    private ${field.typeName} ${field.name};

    </#if>
</#list>


}
