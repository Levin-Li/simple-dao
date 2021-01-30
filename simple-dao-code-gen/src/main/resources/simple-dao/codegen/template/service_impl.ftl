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
<#--import ${servicePackageName}.*;-->
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


/**
 *  ${desc}服务实现
 *  ${.now}
 *@author auto gen by oaknt
 *
 */
@Service
@Slf4j
public class ${className} implements ${serviceName} {


    @Autowired
    private SimpleDao simpleDao;

    @Override
    public  ApiResp<${pkField.type}> create(Create${entityName}Req req) {

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
        ${entityName} entity = new ${entityName}();
        BeanUtils.copyProperties(req, entity);

    <#list fields as field>
        <#if field.name == 'sn' && field.type == 'String'>
        String sn = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
        entity.setSn(sn);
        </#if>
        <#if field.name == 'addTime'>
        entity.setAddTime(new Date());
        </#if>
        <#if field.name == 'createTime'>
        entity.setCreateTime(new Date());
        </#if>
        <#if field.name == 'updateTime'>
        entity.setUpdateTime(new Date());
        </#if>
        <#if field.name == 'lastUpdateTime'>
        entity.setLastUpdateTime(new Date());
        </#if>
    </#list>

        simpleDao.create(entity);

        return ApiResp.ok(entity.get${pkField.name?cap_first}());
    }

    @Override
    public ApiResp<Void> edit(Edit${entityName}Req req) {

<#list fields as field>
    <#if !field.notUpdate && field.uk>
        if (StringUtils.hasText(req.get${field.name?cap_first}())) {
            long ${field.name}C = simpleDao.selectFrom(${entityName}.class)
                    .eq("${field.name}", req.get${field.name?cap_first}())
                    .where("${pkField.name} != :?", req.get${pkField.name?cap_first}())
                    .count();
            if (${field.name}C > 0) {
                return  ApiResp.error("${field.desc}已被使用");
            }
        }

    </#if>
</#list>
        ${entityName} entity = simpleDao.find(${entityName}.class, req.get${pkField.name?cap_first}());
        if (entity == null) {
            return  ApiResp.error("${desc}数据不存在");
        }

        UpdateDao<${entityName}> updateDao = simpleDao.updateTo(${entityName}.class).appendByQueryObj(req);

<#list fields as field>
    <#if field.name == 'updateTime'>
        updateDao.set(E_${entityName}.updateTime, new Date());
    </#if>
</#list>

        return updateDao.update() > 0 ? ApiResp.ok() : ApiResp.error("更新${desc}失败");
    }

    @Override
    public ApiResp<Void> delete(Delete${entityName}Req req) {

        //if (req.get${pkField.name?cap_first}() == null
        //        && (req.get${pkField.name?cap_first}s() == null || req.get${pkField.name?cap_first}s().length == 0)) {
        //    return  ApiResp.error("删除参数不能为空");
        // }

        boolean successful = false;

        try {
            successful =  simpleDao.deleteByQueryObj(req) > 0;
        } catch (Exception ex) {
            log.error("delete ${desc} [${entityName}] error" , ex);
        }

        return successful ? ApiResp.ok() : ApiResp.error("删除${desc}失败");
    }

    @Override
    public ${entityName}Info findById(${pkField.type} ${pkField.name}) {

        Query${entityName}Req queryReq = new Query${entityName}Req();
        queryReq.set${pkField.name?cap_first}(${pkField.name});

       // return query(queryReq).getFirst();

        return simpleDao.findOneByQueryObj(queryReq);
    }

    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req) {

      return PagingQueryHelper.findByPageOption(simpleDao, PagingData.class,req);

    }

<#--    @Override-->
<#--    @Deprecated-->
<#--    public Pagination<${entityName}Info> query(Query${entityName}Req req) {-->

<#--        return SimpleCommonDaoHelper.queryObject(simpleDao,${entityName}.class,${entityName}Info.class,req);-->

<#--    }-->
}
