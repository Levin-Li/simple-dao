package ${packageName};


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.validation.annotation.*;
import org.springframework.util.*;
import javax.validation.*;
import java.util.*;
import javax.annotation.*;

import javax.servlet.http.*;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.annotation.*;

import com.levin.commons.rbac.ResAuthorize;
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


// POST: 创建一个新的资源，如用户资源，部门资源; PATCH: 修改资源的某个属性; PUT: 更新资源当中包含的全部属性; DELETE: 删除某项资源; GET: 获取某个资源的详情

// 在数学计算或者计算机科学中，幂等性（idempotence）是指相同操作或资源在一次或多次请求中具有同样效果的作用。幂等性是在分布式系统设计中具有十分重要的地位。
// http协议明确规定，put、get、delete请求都是具有幂等性的，而post为非幂等性的。 所以一般插入新数据的时候使用post方法，更新数据库时用put方法

// Spring mvc 参数验证说明
// @Valid 只能用在controller, @Validated 可以用在其他被spring管理的类上。注意只有 @Valid 才支持对象嵌套验证，示例如下：
// @Valid
// @NotNull(groups = AdvanceInfo.class)
// private UserAddress userAddress;

/**
* ${entityTitle}控制器
*
* @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
*
*/

//生成的控制器
<#--<#if isCreateBizController>//</#if>@RestController(PLUGIN_PREFIX + "${className}")-->
@RestController(PLUGIN_PREFIX + "${className}")
<#if isCreateBizController>//</#if>@RequestMapping(API_PATH + "${entityName}") //${entityName?lower_case}

<#if isCreateBizController>//</#if>@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", havingValue = "true",  matchIfMissing = true)

//默认需要权限访问，默认从父类继承
@ResAuthorize(domain = ID, type = ${entityCategory} + "-")

//类注解
<#if isCreateBizController>//默认生成控制器类，@Tag的name属性关联权限的资源标识</#if>
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
@Validated //@Valid
@CRUD

@Slf4j
public<#if isCreateBizController> abstract</#if> class ${className} extends BaseController{

    protected static final String BIZ_NAME = E_${entityName}.BIZ_NAME;

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    protected ${serviceName} ${serviceName?uncap_first};

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    protected Biz${serviceName} biz${serviceName};

    /**
     * 分页列表查找
     *
     * @param req Query${entityName}Req
     * @return  ApiResp<PagingData<${entityName}Info>>
     */
    @GetMapping({"list"})
    @Operation(summary = QUERY_LIST_ACTION, description = QUERY_ACTION + " " + BIZ_NAME)
    @CRUD.ListTable
    public ApiResp<PagingData<${entityName}Info>> list(@Form @Valid Query${entityName}Req req, SimplePaging paging) {

        req = checkRequest(QUERY_LIST_ACTION, req);

        return ApiResp.ok(checkResponse(QUERY_LIST_ACTION, ${serviceName?uncap_first}.query(req, paging)));
    }

    /**
     * 新增
     *
     * @param req Create${entityName}Evt
     * @return ApiResp
     */
    @PostMapping({"create", ""})
    @Operation(summary = CREATE_ACTION, description = CREATE_ACTION + " " + BIZ_NAME)
    @CRUD.Op(recordRefType = CRUD.RecordRefType.None)
<#if pkField?exists>
    public ApiResp<${pkField.typeName}> create(@RequestBody @Valid Create${entityName}Req req) {
<#else>
    public ApiResp<Boolean> create(@RequestBody @Valid Create${entityName}Req req) {
</#if>

        req = checkRequest(CREATE_ACTION, req);

   <#if pkField?exists>
        return ApiResp.ok(${serviceName?uncap_first}.create(req));
    <#else>
        return ${serviceName?uncap_first}.create(req) ? ApiResp.ok() : ApiResp.error(CREATE_ACTION + " " + BIZ_NAME + "失败");
    </#if>
    }

<#if pkField?exists>
    /**
     * 查看详情
     *
     * @param req Query${entityName}ByIdReq
     */
    @GetMapping({"retrieve", "{${pkField.name}}", ""})
    @Operation(summary = VIEW_DETAIL_ACTION, description = VIEW_DETAIL_ACTION + " " + BIZ_NAME + "-1, 路径变量参数优先")
    @CRUD.Op
    public ApiResp<${entityName}Info> retrieve(@NotNull @Valid ${entityName}IdReq req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {

         req.update${pkField.name?cap_first}WhenNotBlank(${pkField.name});

         req = checkRequest(VIEW_DETAIL_ACTION, req);

         ${entityName}Info info = ${serviceName?uncap_first}.findById(req);
         Assert.notNull(info, "记录不存在");
         // 租户校验，因为数据可能是从缓存加载的
         <#if !isMultiTenantObject>//</#if>Assert.isTrue(!StringUtils.hasText(req.getTenantId()) || req.getTenantId().equals(info.getTenantId()), "非法访问，租户不匹配");

         return ApiResp.ok(checkResponse(VIEW_DETAIL_ACTION, info));
     }

    /**
     * 更新
     * @param req Update${entityName}Req
     */
    @PutMapping({"update", "{${pkField.name}}", ""})
    @Operation(summary = UPDATE_ACTION, description = UPDATE_ACTION + " " + BIZ_NAME + "-1, 路径变量参数优先")
    @CRUD.Op
    public ApiResp<Boolean> update(@RequestBody @Valid Update${entityName}Req req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {

        req.update${pkField.name?cap_first}WhenNotBlank(${pkField.name});

        req = checkRequest(UPDATE_ACTION, req);

        return ApiResp.ok(assertTrue(${serviceName?uncap_first}.update(req), UPDATE_ACTION + BIZ_NAME + "失败"));
    }

    /**
     * 删除
     * @param req ${entityName}IdReq
     */
    @DeleteMapping({"delete", "{${pkField.name}}", ""})
    @Operation(summary = DELETE_ACTION, description = DELETE_ACTION  + "(Query方式) " + BIZ_NAME + "-1, 路径变量参数优先")
    @CRUD.Op
    public ApiResp<Boolean> delete(@Valid ${entityName}IdReq req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {

        req.update${pkField.name?cap_first}WhenNotBlank(${pkField.name});

        req = checkRequest(DELETE_ACTION, req);

        return ApiResp.ok(assertTrue(${serviceName?uncap_first}.delete(req), DELETE_ACTION + BIZ_NAME + "失败"));
    }

    /**
     * 删除
     * @param req ${entityName}IdReq
     */
    @DeleteMapping(value = {"{${pkField.name}}", ""}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = DELETE_ACTION, description = DELETE_ACTION + " " + BIZ_NAME + "-2, 路径变量参数优先")
    public ApiResp<Boolean> delete2(@RequestBody @Valid ${entityName}IdReq req, @PathVariable(required = false) ${pkField.typeName} ${pkField.name}) {

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
    @PostMapping("batchCreate")
    @Operation(summary = BATCH_CREATE_ACTION, description = BATCH_CREATE_ACTION + " " + BIZ_NAME)
<#if pkField?exists>
    public ApiResp<List<${pkField.typeName}>> batchCreate(@RequestBody @Valid List<Create${entityName}Req> reqList) {
<#else>
    public ApiResp<List<Boolean>> batchCreate(@RequestBody List<Create${entityName}Req> reqList) {
</#if>

        reqList = checkRequest(BATCH_CREATE_ACTION, reqList);

        return ApiResp.ok(${serviceName?uncap_first}.batchCreate(reqList));
    }

    /**
     * 批量更新
     */
    @PutMapping("batchUpdate")
    @Operation(summary = BATCH_UPDATE_ACTION, description = BATCH_UPDATE_ACTION + " " + BIZ_NAME)
    public ApiResp<Integer> batchUpdate(@RequestBody @Valid List<Update${entityName}Req> reqList) {

        reqList = checkRequest(BATCH_UPDATE_ACTION, reqList);

        return ApiResp.ok(assertTrue(${serviceName?uncap_first}.batchUpdate(reqList), BATCH_UPDATE_ACTION + BIZ_NAME + "失败"));
    }

    /**
     * 批量删除
     * @param req Delete${entityName}Req
     */
    @DeleteMapping({"batchDelete"})
    @Operation(summary = BATCH_DELETE_ACTION, description = BATCH_DELETE_ACTION + " " + BIZ_NAME)
    @CRUD.Op(recordRefType = CRUD.RecordRefType.Multiple)
    public ApiResp<Integer> batchDelete(@NotNull @Valid Delete${entityName}Req req) {

        req = checkRequest(BATCH_DELETE_ACTION, req);

        return ApiResp.ok(assertTrue(${serviceName?uncap_first}.batchDelete(req), BATCH_DELETE_ACTION + BIZ_NAME + "失败"));
    }

    /**
     * 批量删除2
     * @param req @RequestBody Delete${entityName}Req
     */
    @DeleteMapping(value = {"batchDelete"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = BATCH_DELETE_ACTION, description = BATCH_DELETE_ACTION + " " + BIZ_NAME)
    public ApiResp<Integer> batchDelete2(@RequestBody @Valid Delete${entityName}Req req) {

        req = checkRequest(BATCH_DELETE_ACTION, req);

        return batchDelete(req);
    }
}
