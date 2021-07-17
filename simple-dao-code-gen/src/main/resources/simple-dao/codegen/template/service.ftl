package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;


/**
 *  ${desc}服务
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
public interface ${className} {

    @Schema(description = "新增${desc}")
<#if pkField?exists>
    ${pkField.typeName} create(Create${entityName}Req req);
<#else>
    boolean create(Create${entityName}Req req);
</#if>

<#if pkField?exists>
    @Schema(description = "通过ID找回${desc}")
    ${entityName}Info findById(${pkField.typeName} ${pkField.name});
</#if>

    @Schema(description = "更新${desc}")
    int update(Update${entityName}Req req);

    @Schema(description = "删除${desc}")
    int delete(Delete${entityName}Req req);

    @Schema(description = "分页查找${desc}")
    PagingData<${entityName}Info> query(Query${entityName}Req req , Paging paging);

}
