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
import javax.annotation.*;

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
 *  编辑${desc}
 *  Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "编辑${desc}")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
<#--@EqualsAndHashCode(callSuper = true)-->
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)
//默认更新注解
@Update
public class ${className} implements ServiceReq {

    private static final long serialVersionUID = ${serialVersionUID}L;

    @Schema(description = "${pkField.desc}")
    @NotNull
    @Eq(require = true)
    private ${pkField.typeName} ${pkField.name};

<#list fields as field>
    <#if !field.notUpdate && !field.lazy && field.baseType && !field.jpaEntity >
    <#list field.annotations as annotation>
    <#if !(annotation?string)?contains("@NotNull")>
    ${annotation}
    </#if>
    </#list>
    @Schema(description = "${field.desc}")
    private ${field.typeName} ${field.name};

    </#if>
</#list>


    public ${className}(${pkField.typeName} ${pkField.name}) {
        this.${pkField.name} = ${pkField.name};
    }


    @PostConstruct
    public void preUpdate() {
    //更新之前初始化数据

    }

}
