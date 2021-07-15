package ${packageName};


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.*;
import javax.validation.*;


import javax.servlet.http.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;

import ${modulePackageName}.*;
import ${entityClassPackage}.*;
import ${servicePackageName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;


//Auto gen by simple-dao-codegen ${.now}

// http协议明确规定，put、get、delete请求都是具有幂等性的，而post为非幂等性的。所以一般插入新数据的时候使用post方法，更新数据库时用put方法

@RestController("${packageName}.${className}")
@RequestMapping(ModuleOption.API_PATH + "${entityName?lower_case}")
@Tag(name = "${desc}", description = "${desc}管理")
@Slf4j
////@Valid只能用在controller。@Validated可以用在其他被spring管理的类上。
@Valid

@ConditionalOnProperty(value = "plugin.${packageName}.${className}", havingValue = "false", matchIfMissing = true)
public class ${className} {

    //请求级别变量
    @Autowired
    HttpServletResponse httpResponse;

    //请求级别变量
    @Autowired
    HttpServletRequest httpRequest;

    @Autowired
    private ${serviceName} ${serviceName?uncap_first};

    /**
     * 分页查找
     *
     * @param req  Query${entityName}Req
     * @return  ApiResp<PagingData<${entityName}Info>>
     */
    @GetMapping("/query")
    @Operation(tags = {"${desc}"}, summary = "分页查找${desc}", description = "分页查找${desc}(${entityName})")
    public ApiResp<PagingData<${entityName}Info>> query(Query${entityName}Req req , SimplePaging paging) {
        return ApiResp.ok(${serviceName?uncap_first}.query(req,paging));
    }

    /**
     * 新增
     *
     * @param req   Create${entityName}Evt
     * @return ApiResp
     */
    @PutMapping("/create")
    @Operation(tags = {"${desc}"}, summary = "新增${desc}", description = "新增${desc}(${entityName})")

<#if pkField?exists>
    public ApiResp<${pkField.typeName}> create(Create${entityName}Req req) {
<#else>
    public ApiResp<Boolean> create(Create${entityName}Req req) {
</#if>
   <#if pkField?exists>
        return ApiResp.ok(${serviceName?uncap_first}.create(req));
    <#else>
        return ${serviceName?uncap_first}.create(req) ? ApiResp.ok():ApiResp.error("新增${desc}失败");
    </#if>
    }


<#if pkField?exists>
    /**
    * 查看详情
    *
    * @param ${pkField.name} ${pkField.typeName}
    */
    @GetMapping("/{id}")
    @Operation(tags = {"${desc}"}, summary = "查看详情${desc}", description = "查看详情${desc}(${entityName})")
    public ApiResp<${entityName}Info> detail(@PathVariable @NotNull ${pkField.typeName} ${pkField.name}) {
        return ApiResp.ok(${serviceName?uncap_first}.findById(${pkField.name}));
     }
</#if>

    /**
     * 更新
     */
     @PostMapping("/edit")
     @Operation(tags = {"${desc}"}, summary = "更新${desc}", description = "更新${desc}(${entityName})")
     public ApiResp<Void> edit(Edit${entityName}Req req) {
         return ${serviceName?uncap_first}.edit(req) > 0 ? ApiResp.ok() : ApiResp.error("更新${desc}失败");
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    @Operation(tags = {"${desc}"}, summary = "删除${desc}", description = "删除${desc}(${entityName})")
    public ApiResp<Void> delete(Delete${entityName}Req req) {
        return ${serviceName?uncap_first}.delete(req) > 0 ? ApiResp.ok() : ApiResp.error("删除${desc}失败");
    }


}
