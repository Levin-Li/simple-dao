package ${packageName};

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.*;
import javax.validation.*;
import java.util.*;

import javax.servlet.http.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;
import javax.validation.constraints.*;

import ${modulePackageName}.controller.*;
import ${modulePackageName}.*;
import ${entityClassPackage}.*;
import ${servicePackageName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

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
//默认需要权限访问
//@ResAuthorize(domain = ID, type = TYPE_NAME)
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
@Slf4j
@Valid
public class ${className} extends BaseController{

    private static final String BIZ_NAME = E_${entityName}.BIZ_NAME;

    @Autowired
    ${serviceName} ${serviceName?uncap_first};

    /**
     * 分页查找
     *
     * @param req  Query${entityName}Req
     * @return  ApiResp<PagingData<${entityName}Info>>
     */
    @GetMapping("/query")
    @Operation(tags = {BIZ_NAME}, summary = QUERY_ACTION)
    public ApiResp<PagingData<${entityName}Info>> query(Query${entityName}Req req , SimplePaging paging) {
        return ApiResp.ok(${serviceName?uncap_first}.query(req,paging));
    }

    /**
     * 新增
     *
     * @param req Create${entityName}Evt
     * @return ApiResp
     */
    @PostMapping
    @Operation(tags = {BIZ_NAME}, summary = CREATE_ACTION)
<#if pkField?exists>
    public ApiResp<${pkField.typeName}> create(@RequestBody Create${entityName}Req req) {
<#else>
    public ApiResp<Boolean> create(@RequestBody Create${entityName}Req req) {
</#if>
   <#if pkField?exists>
        return ApiResp.ok(${serviceName?uncap_first}.create(req));
    <#else>
        return ${serviceName?uncap_first}.create(req) ? ApiResp.ok():ApiResp.error(CREATE_ACTION + BIZ_NAME + "失败");
    </#if>
    }

    /**
     * 批量新增
     *
     * @param reqList List<Create${entityName}Evt>
     * @return ApiResp
     */
    @PostMapping("/batchCreate")
    @Operation(tags = {BIZ_NAME}, summary = BATCH_CREATE_ACTION)
<#if pkField?exists>
    public ApiResp<List<${pkField.typeName}>> batchCreate(@RequestBody List<Create${entityName}Req> reqList) {
<#else>
    public ApiResp<List<Boolean>> batchCreate(@RequestBody List<Create${entityName}Req> reqList) {
</#if>
        return ApiResp.ok(${serviceName?uncap_first}.batchCreate(reqList));
    }

<#if pkField?exists>
    /**
    * 查看详情
    *
    * @param req Query${entityName}ByIdReq
    */
    @GetMapping("/{${pkField.name}}")
    @Operation(tags = {BIZ_NAME}, summary = VIEW_DETAIL_ACTION)
    public ApiResp<${entityName}Info> retrieve(@PathVariable @NotNull Query${entityName}ByIdReq req) {

         return ApiResp.ok(${serviceName?uncap_first}.findById(req));

         //return ApiResp.ok(${serviceName?uncap_first}.findById(${pkField.name}));
     }
</#if>

    /**
     * 更新
     * @param req Update${entityName}Req
     */
     @PutMapping({""})
     @Operation(tags = {BIZ_NAME}, summary = UPDATE_ACTION)
     public ApiResp<Void> update(@RequestBody Update${entityName}Req req) {
         return ${serviceName?uncap_first}.update(req) > 0 ? ApiResp.ok() : ApiResp.error(UPDATE_ACTION + BIZ_NAME + "失败");
    }

    /**
     * 批量更新
     */
     @PutMapping("/batchUpdate")
     @Operation(tags = {BIZ_NAME}, summary = BATCH_UPDATE_ACTION)
     public ApiResp<List<Integer>> batchUpdate(@RequestBody List<Update${entityName}Req> reqList) {
        return ApiResp.ok(${serviceName?uncap_first}.batchUpdate(reqList));
    }

    /**
     * 删除
     * @param ${pkField.name} ${pkField.typeName}
     */
    //@DeleteMapping({"/{${pkField.name}}"})
    //@Operation(tags = {BIZ_NAME}, summary = DELETE_ACTION)
    //public ApiResp<Void> delete(@PathVariable @NotNull ${pkField.typeName} ${pkField.name}) {
    //    return ${serviceName?uncap_first}.delete(new Delete${entityName}Req().set${pkField.name?cap_first}(${pkField.name})) > 0
    //                                            ? ApiResp.ok() : ApiResp.error(DELETE_ACTION + BIZ_NAME + "失败");
    //}

    /**
     * 批量删除
     * @param req Delete${entityName}Req
     */
    @DeleteMapping({"/batchDelete"})
    @Operation(tags = {BIZ_NAME}, summary = BATCH_DELETE_ACTION)
    public ApiResp<Void> batchDelete(@NotNull Delete${entityName}Req req) {
        //new Delete${entityName}Req().set${pkField.name?cap_first}List(${pkField.name}List)
        return ${serviceName?uncap_first}.delete(req) > 0 ? ApiResp.ok() : ApiResp.error(DELETE_ACTION + BIZ_NAME + "失败");
    }  

}
