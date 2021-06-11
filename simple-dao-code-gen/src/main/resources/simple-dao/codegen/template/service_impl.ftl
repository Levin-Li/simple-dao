package ${packageName};

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import org.springframework.util.*;
import java.util.Date;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import ${entityClassPackage}.*;
import ${entityClassName};

import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

<#list fields as field>
    <#if (field.lzay)??>
import ${field.classType.package.name}.${field.classType.simpleName};
    </#if>
    <#if (field.infoClassName)??>
import ${field.infoClassName};
    </#if>
</#list>

////////////////////////////////////
//自动导入列表
<#list importList as imp>
    import ${imp};
</#list>
////////////////////////////////////

/**
 *  ${desc}服务实现
 *
 *@author auto gen by simple-dao-codegen ${.now}
 *
 */
@Service("${packageName}.${serviceName}")
@Slf4j
public class ${className} implements ${serviceName} {

    @Autowired
    private SimpleDao simpleDao;

    @Schema(description = "创建${desc}")
    @Override
    public  ApiResp<${pkField.typeName}> create(Create${entityName}Req req) {

    <#list fields as field>
        <#if !field.notUpdate && field.uk>
        long ${field.name}C = simpleDao.selectFrom(${entityName}.class)
                .eq("${field.name}", req.get${field.name?cap_first}())
                .count();
        if (${field.name}C > 0) {
            return ApiResp.error("${field.desc}已被使用");
        }
        </#if>
    </#list>

        ${entityName} entity = (${entityName}) simpleDao.create(req);

        return ApiResp.ok(entity.get${pkField.name?cap_first}());
    }

    @Schema(description = "编辑${desc}")
    @Override
    public ApiResp<Void> edit(Edit${entityName}Req req) {

        return simpleDao.updateByQueryObj(req) > 0 ? ApiResp.ok() : ApiResp.error("更新${desc}失败");

    }

    @Schema(description = "删除${desc}")
    @Override
    public ApiResp<Void> delete(Delete${entityName}Req req) {

        return simpleDao.deleteByQueryObj(req) > 0 ? ApiResp.ok() : ApiResp.error("删除${desc}失败");
    }

    @Schema(description = "通过ID查找${desc}")
    @Override
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {

        return simpleDao.findOneByQueryObj(new Query${entityName}Req().set${pkField.name?cap_first}(${pkField.name}));
    }

    @Schema(description = "分页查找${desc}")
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging) {

      return simpleDao.findPagingDataByQueryObj(req, paging);

    }

}
