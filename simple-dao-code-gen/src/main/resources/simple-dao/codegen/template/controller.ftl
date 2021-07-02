package ${packageName};


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
     * 分页查询
     *
     * @param req  Query${entityName}Req
     * @return  ApiResp<PagingData<${entityName}Info>>
     */
    @GetMapping("/query")
    @Operation(summary = "查询${entityName}", description = "${desc}")
    public ApiResp<PagingData<${entityName}Info>> query(Query${entityName}Req req , SimplePaging paging) {
        return ApiResp.ok(${serviceName?uncap_first}.query(req,paging));
    }


    /**
     * 新增保存
     *
     * @param req   Create${entityName}Evt
     * @return ApiResp
     */
    @PutMapping("/create")
    @Operation(summary = "创建${entityName}", description = "${desc}")
    public ApiResp<Long> create(Create${entityName}Req req) {
        return ${serviceName?uncap_first}.create(req);
    }



    /**
    * 详情
    *
    * @param ${pkField.name} ${pkField.typeName}
    */
    @GetMapping("/{id}")
    @Operation(summary = "详情${entityName}", description = "${desc}")
    public ApiResp<${entityName}Info> detail(@PathVariable ${pkField.typeName} ${pkField.name}) {
        return ApiResp.ok(${serviceName?uncap_first}.findById(${pkField.name}));
     }


    /**
     * 修改保存
     */
     @PostMapping("/edit")
     @Operation(summary = "编辑${entityName}", description = "${desc}")
     public ApiResp<Void> edit(Edit${entityName}Req req) {
         return ${serviceName?uncap_first}.edit(req);
    }


    /**
     * 删除
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除${entityName}", description = "${desc}")
    public ApiResp<Void> delete(Delete${entityName}Req req) {
        return ${serviceName?uncap_first}.delete(req);
    }


}
