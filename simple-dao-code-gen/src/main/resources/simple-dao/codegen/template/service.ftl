package ${packageName};


import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;


/**
 *  ${desc}服务
 *  ${.now}
 *  @author auto gen by oaknt
 */
public interface ${className} {

    @Schema(description = "创建${desc}")
    ApiResp<${pkField.type}> create(Create${entityName}Req req);

    @Schema(description = "编辑${desc}")
    ApiResp<Void> edit(Edit${entityName}Req req);

    @Schema(description = "删除${desc}")
    ApiResp<Void> delete(Delete${entityName}Req req);

    @Schema(description = "通过ID查找${desc}")
    ${entityName}Info findById(${pkField.type} ${pkField.name});

    @Schema(description = "分页查找${desc}")
    PagingData<${entityName}Info> query(Query${entityName}Req req);

}
