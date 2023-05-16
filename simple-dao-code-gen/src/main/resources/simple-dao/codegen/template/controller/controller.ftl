package ${packageName};

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.*;
import javax.validation.*;
import java.util.*;
import javax.annotation.*;

import javax.servlet.http.*;

import com.levin.commons.dao.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.ui.annotation.*;
import javax.validation.constraints.*;

import ${modulePackageName}.controller.*;
import ${modulePackageName}.*;
import ${entityClassPackage}.*;

import ${bizServicePackageName}.*;

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

//生成的控制器默认不开启，请手动取消注释
//@RestController(PLUGIN_PREFIX + "${className}")
//@RequestMapping(API_PATH + "${entityName}") //${entityName?lower_case}

@Slf4j
//@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", matchIfMissing = true)

//默认需要权限访问
//@ResAuthorize(domain = ID, type = TYPE_NAME)

//类注解
//@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
@Valid
@CRUD
public abstract class ${className} extends BaseController{

    protected static final String BIZ_NAME = E_${entityName}.BIZ_NAME;

    @Autowired
    ${serviceName} ${serviceName?uncap_first};

    @Autowired
    Biz${serviceName} biz${serviceName};

    /**
     * 分页列表查找
     *
     * @param req Query${entityName}Req
     * @return  ApiResp<PagingData<${entityName}Info>>
     */
    @GetMapping("/queryList")
    @Operation(summary = QUERY_LIST_ACTION, description = QUERY_ACTION + " " + BIZ_NAME)
    @CRUD.ListTable
    public ApiResp<PagingData<${entityName}Info>> queryList(@Form Query${entityName}Req req, SimplePaging paging) {
        return ApiResp.ok(${serviceName?uncap_first}.query(req,paging));
    }

     /**
      * 简单统计
      *
      * @param req Query${entityName}Req
      * @return  ApiResp<PagingData<Stat${entityName}Req.Result>>
      */
     //@GetMapping("/stat") //默认不开放
     @Operation(summary = STAT_ACTION, description = STAT_ACTION + " " + BIZ_NAME)
     public ApiResp<PagingData<Stat${entityName}Req.Result>> stat(Stat${entityName}Req req, SimplePaging paging) {
         return ApiResp.ok(${serviceName?uncap_first}.stat(req,paging));
     }

    /**
     * 新增
     *
     * @param req Create${entityName}Evt
     * @return ApiResp
     */
    @PostMapping
    @Operation(summary = CREATE_ACTION, description = CREATE_ACTION + " " + BIZ_NAME)
    @CRUD.Op(recordRefType = CRUD.RecordRefType.None)
<#if pkField?exists>
    public ApiResp<${pkField.typeName}> create(@RequestBody Create${entityName}Req req) {
<#else>
    public ApiResp<Boolean> create(@RequestBody Create${entityName}Req req) {
</#if>
   <#if pkField?exists>
        return ApiResp.ok(${serviceName?uncap_first}.create(req));
    <#else>
        return ${serviceName?uncap_first}.create(req) ? ApiResp.ok():ApiResp.error(CREATE_ACTION + " " + BIZ_NAME + "失败");
    </#if>
    }

<#if pkField?exists>
    /**
    * 查看详情
    *
    * @param req Query${entityName}ByIdReq
    */
    @GetMapping({"","{${pkField.name}}"})
    @Operation(summary = VIEW_DETAIL_ACTION, description = VIEW_DETAIL_ACTION + " " + BIZ_NAME)
    @CRUD.Op
    public ApiResp<${entityName}Info> retrieve(@NotNull ${entityName}IdReq req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {
         req.update${pkField.name?cap_first}WhenNotBlank(${pkField.name});
         return ApiResp.ok(${serviceName?uncap_first}.findById(req));
     }

    /**
     * 更新
     * @param req Update${entityName}Req
     */
     @PutMapping({"","{${pkField.name}}"})
     @Operation(summary = UPDATE_ACTION + "(RequestBody方式)", description = UPDATE_ACTION + " " + BIZ_NAME + ", 路径变量参数优先")
     @CRUD.Op
     public ApiResp<Boolean> update(@RequestBody Update${entityName}Req req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {
         req.update${pkField.name?cap_first}WhenNotBlank(${pkField.name});
         return ApiResp.ok(checkResult(${serviceName?uncap_first}.update(req), UPDATE_ACTION));
    }

    /**
     * 删除
     * @param req ${entityName}IdReq
     */
    @DeleteMapping({"","{${pkField.name}}"})
    @Operation(summary = DELETE_ACTION, description = DELETE_ACTION  + "(Query方式) " + BIZ_NAME + ", 路径变量参数优先")
    @CRUD.Op
    public ApiResp<Boolean> delete(${entityName}IdReq req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {
        req.update${pkField.name?cap_first}WhenNotBlank(${pkField.name});
        return ApiResp.ok(checkResult(${serviceName?uncap_first}.delete(req), DELETE_ACTION));
    }

    /**
     * 删除
     * @param req ${entityName}IdReq
     */
    @DeleteMapping(value = {"","{${pkField.name}}"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = DELETE_ACTION + "(RequestBody方式)", description = DELETE_ACTION + " " + BIZ_NAME + ", 路径变量参数优先")
    public ApiResp<Boolean> delete2(@RequestBody ${entityName}IdReq req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {
        req.update${pkField.name?cap_first}WhenNotBlank(${pkField.name});
        return delete(req, ${pkField.name});
    }
</#if>

    //////////////////////////////////////以下是批量操作//////////////////////////////////////

    /**
     * 批量新增
     *
     * @param reqList List<Create${entityName}Evt>
     * @return ApiResp
     */
    @PostMapping("/batchCreate")
    @Operation(summary = BATCH_CREATE_ACTION, description = BATCH_CREATE_ACTION + " " + BIZ_NAME)
<#if pkField?exists>
    public ApiResp<List<${pkField.typeName}>> batchCreate(@RequestBody List<Create${entityName}Req> reqList) {
<#else>
    public ApiResp<List<Boolean>> batchCreate(@RequestBody List<Create${entityName}Req> reqList) {
</#if>
        return ApiResp.ok(${serviceName?uncap_first}.batchCreate(reqList));
    }

    /**
     * 批量更新
     */
     @PutMapping("/batchUpdate")
     @Operation(summary = BATCH_UPDATE_ACTION, description = BATCH_UPDATE_ACTION + " " + BIZ_NAME)
     public ApiResp<Integer> batchUpdate(@RequestBody List<Update${entityName}Req> reqList) {
        return ApiResp.ok(checkResult(${serviceName?uncap_first}.batchUpdate(reqList), BATCH_UPDATE_ACTION));
    }

    /**
     * 批量删除
     * @param req Delete${entityName}Req
     */
    @DeleteMapping({"/batchDelete"})
    @Operation(summary = BATCH_DELETE_ACTION, description = BATCH_DELETE_ACTION + " " + BIZ_NAME)
    @CRUD.Op(recordRefType = CRUD.RecordRefType.Multiple)
    public ApiResp<Integer> batchDelete(@NotNull Delete${entityName}Req req) {
        return ApiResp.ok(checkResult(${serviceName?uncap_first}.batchDelete(req), BATCH_DELETE_ACTION));
    }

     /**
     * 批量删除2
     * @param req @RequestBody Delete${entityName}Req
     */
    @DeleteMapping(value = {"/batchDelete"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = BATCH_DELETE_ACTION, description = BATCH_DELETE_ACTION + " " + BIZ_NAME)
    public ApiResp<Integer> batchDelete2(@RequestBody Delete${entityName}Req req) {
        return batchDelete(req);
    }

    /**
     * 检查结果
     * @param n
     * @param action
     * @return
     */
    protected int checkResult(int n, String action) {
        Assert.isTrue(n > 0, action + BIZ_NAME + "失败");
        return n;
    }

    /**
     * 检查结果
     * @param ok
     * @param action
     * @return
     */
    protected boolean checkResult(boolean ok, String action) {
        Assert.isTrue(ok, action + BIZ_NAME + "失败");
        return ok;
    }

}
