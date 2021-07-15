package ${packageName};


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

    @Schema(description = "更新${desc}")
    int edit(Edit${entityName}Req req);

    @Schema(description = "删除${desc}")
    int delete(Delete${entityName}Req req);

<#if pkField?exists>
    @Schema(description = "通过ID查找${desc}")
    ${entityName}Info findById(${pkField.typeName} ${pkField.name});
</#if>

   /**
    * 分页查询
    *
    * @param req     查询对象
    * @param paging  分页参数，如果 req 参数本身也是Paging对象，那么 paging 参数将无效
    * @return
    */
    @Schema(description = "分页查找${desc}")
    PagingData<${entityName}Info> query(Query${entityName}Req req , Paging paging);

}
