package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import org.springframework.dao.*;

import javax.persistence.PersistenceException;
import cn.hutool.core.lang.*;
import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;

//import org.apache.dubbo.config.spring.context.annotation.*;
import org.apache.dubbo.config.annotation.*;

import ${entityClassPackage}.*;
import ${entityClassName};

import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

import ${modulePackageName}.*;
import ${modulePackageName}.services.*;

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
 * ${entityTitle}-服务实现
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

@Service(PLUGIN_PREFIX + "${serviceName}")
@DubboService

@ConditionalOnMissingBean({${serviceName}.class}) //默认只有在无对应服务才启用
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${serviceName}", matchIfMissing = true)
@Slf4j

//@Valid只能用在controller， @Validated可以用在其他被spring管理的类上。
//@Validated
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
@CacheConfig(cacheNames = {ID + CACHE_DELIM + E_${entityName}.SIMPLE_CLASS_NAME})
public class ${className} extends BaseService implements ${serviceName} {

    protected ${serviceName} getSelfProxy(){
        return getSelfProxy(${serviceName}.class);
    }

    @Operation(summary = CREATE_ACTION)
    @Transactional(rollbackFor = {RuntimeException.class})
    @Override
<#if pkField?exists>
    public ${pkField.typeName} create(Create${entityName}Req req){
<#else>
    public boolean create(Create${entityName}Req req){
</#if>
    <#list fields as field>
        <#if !field.notUpdate && field.uk>
        //long ${field.name}Cnt = simpleDao.selectFrom(${entityName}.class)
        //        .select(E_${entityName}.${field.name})
        //        .eq(E_${entityName}.${field.name}, req.get${field.name?cap_first}())
        //        .count();
        //Assert.isTrue(${field.name}Cnt <= 0, () -> new EntityExistsException("${field.title}已经存在"));
        //
        </#if>
    </#list>
        //保存自动先查询唯一约束，并给出错误信息
        ${entityName} entity = simpleDao.create(req, true);
<#if pkField?exists>
        return entity.get${pkField.name?cap_first}();
<#else>
        return entity != null;
</#if>
    }

    @Operation(summary = BATCH_CREATE_ACTION)
    //@Transactional(rollbackFor = {PersistenceException.class, DataAccessException.class})
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
<#if pkField?exists>
    public List<${pkField.typeName}> batchCreate(List<Create${entityName}Req> reqList){
    <#else>
    public List<Boolean> batchCreate(List<Create${entityName}Req> reqList){
</#if>
        return reqList.stream().map(this::create).collect(Collectors.toList());
    }

    @Operation(summary = UPDATE_ACTION)
    @Override
    //@CacheEvict(condition = "#isNotEmpty(#req.${pkField.name})", key = E_${entityName}.CACHE_KEY_PREFIX + "#req.${pkField.name}")
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean update(Update${entityName}Req req) {
        Assert.notNull(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleUpdateByQueryObj(req);
    }

    @Operation(summary = UPDATE_ACTION)
    @Override
    public int update(SimpleUpdate${entityName}Req setReq, Query${entityName}Req whereReq){
       return simpleDao.updateByQueryObj(setReq, whereReq);
    }

    @Operation(summary = BATCH_UPDATE_ACTION)
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public int batchUpdate(List<Update${entityName}Req> reqList){
        //@Todo 优化批量提交
        return reqList.stream().map(req -> getSelfProxy().update(req)).mapToInt(n -> n ? 1 : 0).sum();
    }

    @Operation(summary = DELETE_ACTION)
    @Override
    //@CacheEvict(condition = "#isNotEmpty(#req.${pkField.name})", key = E_${entityName}.CACHE_KEY_PREFIX + "#req.${pkField.name}")
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean delete(${entityName}IdReq req) {
        Assert.notNull(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleDeleteByQueryObj(req);
    }

    @Operation(summary = BATCH_DELETE_ACTION)
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public int batchDelete(Delete${entityName}Req req){
        //@Todo 优化批量提交
        return Stream.of(req.get${pkField.name?cap_first}List())
            .map(${pkField.name} -> simpleDao.copy(req, new ${entityName}IdReq().set${pkField.name?cap_first}(${pkField.name})))
            .map(idReq -> getSelfProxy().delete(idReq))
            .mapToInt(n -> n ? 1 : 0)
            .sum();
    }

    @Operation(summary = QUERY_ACTION)
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging) {
        return simpleDao.findPagingDataByQueryObj(req, paging);
    }

    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    public PagingData<Simple${entityName}Info> simpleQuery(Query${entityName}Req req, Paging paging){
        return simpleDao.findPagingDataByQueryObj(Simple${entityName}Info.class, req, paging);
    }

    @Operation(summary = STAT_ACTION)
    @Override
    public PagingData<Stat${entityName}Req.Result> stat(Stat${entityName}Req req , Paging paging){
        return simpleDao.findPagingDataByQueryObj(req, paging);
    }

    @Override
    @Operation(summary = STAT_ACTION)
    public int count(Query${entityName}Req req){
        return (int) simpleDao.countByQueryObj(req);
    }

<#if pkField?exists>
    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    //Srping 4.3提供了一个sync参数。是当缓存失效后，为了避免多个请求打到数据库,系统做了一个并发控制优化，同时只有一个线程会去数据库取数据其它线程会被阻塞。
    //@Cacheable(condition = "#isNotEmpty(#${pkField.name})", unless = "#result == null ", key = E_${entityName}.CACHE_KEY_PREFIX + "#${pkField.name}")
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        return findById(new ${entityName}IdReq().set${pkField.name?cap_first}(${pkField.name}));
    }

    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    //只更新缓存
    //@CachePut(unless = "#result == null" , condition = "#isNotEmpty(#req.${pkField.name})" , key = E_${entityName}.CACHE_KEY_PREFIX + "#req.${pkField.name}")
    public ${entityName}Info findById(${entityName}IdReq req) {
        Assert.notNull(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.findUnique(req);
    }
</#if>

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findOne(Query${entityName}Req req){
        return simpleDao.findOneByQueryObj(req);
    }

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findUnique(Query${entityName}Req req){
        return simpleDao.findUnique(req);
    }

    @Override
    @Operation(summary = CLEAR_CACHE_ACTION, description = "缓存Key通常是ID")
    @CacheEvict(condition = "#isNotEmpty(#key)", key = E_${entityName}.CACHE_KEY_PREFIX + "#key")
    public void clearCache(Object key) {
    }

}
