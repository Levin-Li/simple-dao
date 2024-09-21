package ${packageName};

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.extensions.*;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.validation.annotation.*;
import org.springframework.util.*;
import javax.validation.*;
import java.util.*;
import javax.annotation.*;

import cn.hutool.core.lang.Assert;

import javax.servlet.http.*;

//import org.apache.dubbo.config.annotation.*;

import com.levin.commons.dao.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.ui.annotation.*;
import com.levin.commons.rbac.ResAuthorize;

import javax.validation.constraints.*;

import ${modulePackageName}.controller.*;
//import ${controllerPackageName}.*;

import ${modulePackageName}.*;
import ${entityClassPackage}.*;

import ${bizBoPackageName}.*;

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
* ${entityTitle}业务控制器
*
* @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
*
*/

//生成的控制器
@RestController(PLUGIN_PREFIX + "${className}")
@RequestMapping(API_PATH + "${entityName}") //${entityName?lower_case}

@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", havingValue = "true", matchIfMissing = true)

//默认需要登录
@ResAuthorize(domain = ID, type = ${entityCategory} + "-", onlyRequireAuthenticated = true)

//类注解，@Tag的name属性关联权限的资源标识
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION) //, extensions = @Extension(properties = @ExtensionProperty(name = "x-order", value = "${classModel.nextOrderNum}"))
@Validated //@Valid
@CRUD

@Slf4j

@DisableApiOperation({"批量*"}) //被禁止的操作，Swagger 文档将不显示这些方法，API也将被拦截，不可访问。支持*通配符匹配，eg. @DisableApiOperation({"批量*", "method:create", "method:update","method:delete*", "path:delete/*"})
public class ${className} extends BaseController{

    protected static final String BIZ_NAME = E_${entityName}.BIZ_NAME;

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    @Getter
    protected ${serviceName} ${serviceName?uncap_first};

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    @Getter
    protected Biz${serviceName} biz${serviceName};

    //@Autowired
    //AuthService authService;

    /**
    * 检查请求
    *
    * @param action
    * @param req
    * @return
    */
    @Override
    protected <T> T checkRequest(String action, T req) {
        return super.checkRequest(action, req);
    }

    /**
     * 分页列表查找
     *
     * @param req Query${entityName}Req
     * @return  ApiResp<PagingData<${entityName}Info>>
     */
    @GetMapping({"list"})
    @Operation(summary = QUERY_LIST_ACTION, description = QUERY_ACTION + " " + BIZ_NAME) //, extensions = @Extension(properties = @ExtensionProperty(name = "x-order", value = "${classModel.nextOrderNum}"))
    @CRUD.ListTable
    public ApiResp<PagingData<${entityName}Info>> list(@Form @Valid Query${entityName}Req req, SimplePaging paging) {

        req = checkRequest(QUERY_LIST_ACTION, req);

        return ApiResp.ok(checkResponse(QUERY_LIST_ACTION, get${serviceName}().query(req, paging)));
    }

}