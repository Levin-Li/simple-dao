package ${packageName};

import com.wuxp.api.ApiResp;
import com.wuxp.api.model.Pagination;
import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.dao.support.*;

import ${packageName}.req.*;
import ${packageName}.info.*;


/**
 *  ${desc}服务
 *  ${.now}
 *  @author auto gen by oaknt
 */
public interface ${className} {

    @Schema(description = "创建${entityName}")
    ApiResp<${pkField.type}> create(Create${entityName}Req req);

    @Schema(description = "编辑${entityName}")
    ApiResp<Void> edit(Edit${entityName}Req req);

    @Schema(description = "删除${entityName}")
    ApiResp<Void> delete(Delete${entityName}Req req);

    @Schema(description = "通过ID查找${entityName}")
    ${entityName}Info findById(${pkField.type} ${pkField.name});

<#--    @Deprecated-->
<#--    @Schema(description = "分页查找${entityName}")-->
<#--    Pagination<${entityName}Info> query(Query${entityName}Req req);-->

    @Schema(description = "分页查找${entityName}")
    PagingData<${entityName}Info> query(Query${entityName}Req req);

}
