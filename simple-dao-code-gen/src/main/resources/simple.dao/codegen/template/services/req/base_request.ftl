package ${packageName};

import java.util.List;
import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.service.domain.InjectVar;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import ${modulePackageName}.services.commons.req.*;
import ${entityClassPackage}.*;
<#list daoLogicGroupClassImports![] as groupImport>
import ${groupImport};
</#list>
<#if reqExtendClassImport?has_content>
import ${reqExtendClassImport};
</#if>
<#list importList as imp>
import ${imp};
</#list>

/**
 * ${entityTitle}请求对象的公共父类。
 * 每轮代码生成都会重新生成本类，请勿手工修改。
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 */
<#list daoLogicGroupClassAnnotations![] as groupAnnotation>
${groupAnnotation}
</#list>
public abstract class ${className}<T extends ${className}<T>> extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

<#list daoLogicGroupFields![] as logicField>
    @Schema(title = ${logicField.titleLiteral}, hidden = true)
    @JsonIgnore
    @InjectVar(value = ${logicField.injectVarLiteral},
            isOverride = ${logicField.injectOverrideLiteral},
            isRequired = ${logicField.injectRequiredLiteral})
    @com.levin.commons.dao.annotation.update.Update(condition = "false")
    @${logicField.filterAnnotationName}(value = E_${entityName}.${logicField.sourceFieldName}, group = ${logicField.groupLiteral})
    <#list classModel.rbacAnnotations(logicField.sourceFieldName) as groupAnnotation>
    ${groupAnnotation}
    </#list>
    ${logicField.queryTypeName} ${logicField.queryFieldName};



</#list>
<#if hasRoleScopeFields!false>
<#list daoLogicGroupFields![] as f><#if f.scopeRole?string == 'ORG'>
    protected void check${f.sourceFieldName?cap_first}(String value) {
        checkUpdateOrgTarget(E_${entityName}.L_${f.sourceFieldName}, value, ${f.queryFieldName});
    }
<#elseif f.scopeRole?string == 'OWNER' || f.scopeRole?string == 'USER'>
    protected void check${f.sourceFieldName?cap_first}(String value) {
        checkUpdateOwnerTarget(E_${entityName}.L_${f.sourceFieldName}, value);
    }
</#if></#list>
</#if>
}
