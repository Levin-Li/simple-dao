package ${packageName};

import static ${modulePackageName}.ModuleOption.*;

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
import javax.validation.constraints.*;

import ${modulePackageName}.*;
import ${entityClassPackage}.*;
import ${servicePackageName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;


//Auto gen by simple-dao-codegen ${.now}

// POST: 创建一个新的资源，如用户资源，部门资源
// PATCH: 修改资源的某个属性
// PUT: 更新资源当中包含的全部属性
// DELETE: 删除某项资源
// GET: 获取某个资源的详情

// 在数学计算或者计算机科学中，幂等性（idempotence）是指相同操作或资源在一次或多次请求中具有同样效果的作用。幂等性是在分布式系统设计中具有十分重要的地位。

// http协议明确规定，put、get、delete请求都是具有幂等性的，而post为非幂等性的。
// 所以一般插入新数据的时候使用post方法，更新数据库时用put方法
// @Valid只能用在controller。@Validated可以用在其他被spring管理的类上。

@RestController(PLUGIN_PREFIX + "${className}")
@ConditionalOnProperty(value = PLUGIN_PREFIX + "${className}", havingValue = "false", matchIfMissing = true)
@RequestMapping(API_PATH + "${entityName?lower_case}")

@Tag(name = "${desc}", description = "${desc}管理")
@Slf4j @Valid
public class ${className} {

    //请求级别变量
    @Autowired
    HttpServletResponse httpResponse;

    //请求级别变量
    @Autowired
    HttpServletRequest httpRequest;

    @Autowired
    ${serviceName} ${serviceName?uncap_first};

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
    @PostMapping("/create")
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
    public ApiResp<${entityName}Info> retrieve(@PathVariable @NotNull ${pkField.typeName} ${pkField.name}) {
        return ApiResp.ok(${serviceName?uncap_first}.findById(${pkField.name}));
     }
</#if>

    /**
     * 更新
     */
     @PutMapping("/update")
     @Operation(tags = {"${desc}"}, summary = "更新${desc}", description = "更新${desc}(${entityName})")
     public ApiResp<Void> update(Update${entityName}Req req) {
         return ${serviceName?uncap_first}.update(req) > 0 ? ApiResp.ok() : ApiResp.error("更新${desc}失败");
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
