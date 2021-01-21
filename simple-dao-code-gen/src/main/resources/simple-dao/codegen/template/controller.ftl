package ${packageName};


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;

import ${entityClassPackage}.*;
import ${servicePackageName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;



@RestController
@RequestMapping("/${entityName?lower_case}")
@Tag(name = "${desc}", description = "${desc}管理")
@Slf4j
public class ${className} {


    @Autowired
    private ${serviceName} ${serviceName?uncap_first};



    /**
     * 分页数据
     *
     * @param req  Query${entityName}Req
     * @return  ApiResp<PagingData<${entityName}Info>>
     */
    @GetMapping("/query")
    @Operation(summary = "查询${entityName}", description = "${desc}")
    public ApiResp<PagingData<${entityName}Info>> query(Query${entityName}Req req) {
        return ApiResp.ok(${serviceName?uncap_first}.query(req));
    }



    /**
     * 新增保存
     *
     * @param req   Create${entityName}Evt
     * @return ApiResp
     */
    @PostMapping("/create")
    @Operation(summary = "创建${entityName}", description = "${desc}")
    //@ApiLog(value = "#JSON.toJSONString(req)")
    public ApiResp<Long> create(Create${entityName}Req req) {
        return ${serviceName?uncap_first}.create(req);
    }



    /**
    * 详情
    *
    * @param ${pkField.name} ${pkField.type}
    */
    @GetMapping("/{id}")
    @Operation(summary = "详情${entityName}", description = "${desc}")
    public ApiResp<${entityName}Info> detail(@PathVariable ${pkField.type} ${pkField.name}) {
        return ApiResp.ok(${serviceName?uncap_first}.findById(${pkField.name}));
     }


    /**
     * 修改保存
     */
     @PutMapping("/edit")
     @Operation(summary = "编辑${entityName}", description = "${desc}")
     public ApiResp<Void> edit(Edit${entityName}Req req) {
         return ${serviceName?uncap_first}.edit(req);
    }


    /**
     * 删除
     */
    @GetMapping("/delete")
    @Operation(summary = "删除${entityName}", description = "${desc}")
    public ApiResp<Void> delete(Delete${entityName}Req req) {
        return ${serviceName?uncap_first}.delete(req);
    }


}
