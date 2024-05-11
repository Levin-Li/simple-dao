package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.domain.*;

import javax.annotation.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.core.annotation.*;

import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.*;

import com.levin.commons.service.support.SpringCacheEventListener;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
//import org.springframework.dao.*;

import javax.persistence.PersistenceException;
import cn.hutool.core.lang.*;
import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.annotation.*;

import ${entityClassPackage}.*;
import ${entityClassName};

import ${servicePackageName}.*;
import ${bizBoPackageName}.*;
import static ${servicePackageName}.${serviceName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

import ${modulePackageName}.*;
import ${modulePackageName}.services.*;
import ${modulePackageName}.cache.*;

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
 *  ${entityTitle}-业务服务实现类
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

// 事务隔离级别
// Propagation.REQUIRED：默认的事务传播级别，它表示如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。
// Propagation.SUPPORTS：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。
// Propagation.MANDATORY：（mandatory：强制性）如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。
// Propagation.REQUIRES_NEW：表示创建一个新的事务，如果当前存在事务，则把当前事务挂起。也就是说不管外部方法是否开启事务，Propagation.REQUIRES_NEW 修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。
// Propagation.NOT_SUPPORTED：以非事务方式运行，如果当前存在事务，则把当前事务挂起。
// Propagation.NEVER：以非事务方式运行，如果当前存在事务，则抛出异常。
// Propagation.NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于 PROPAGATION_REQUIRED。

<#if enableDubbo>@DubboService<#else>@Service(PLUGIN_PREFIX + "Biz${serviceName}")</#if>

@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "Biz${serviceName}", havingValue = "true", matchIfMissing = true)
@Slf4j

//@Valid只能用在controller，@Validated可以用在其他被spring管理的类上。
//@Validated
@Tag(name = E_${entityName}.BIZ_NAME + "-业务服务", description = "")

//*** 提示 *** 如果要注释缓存注解的代码可以在实体类上加上@javax.persistence.Cacheable(false)，然后重新生成代码
<#if !isCacheableEntity>//</#if>@CacheConfig(cacheNames = ${serviceName}.CACHE_NAME, cacheResolver = PLUGIN_PREFIX + "ModuleSpringCacheResolver")

public class ${className} extends BaseService<${className}> implements Biz${serviceName} {

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    ${serviceName} ${serviceName?uncap_first};

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    ModuleCacheService moduleCacheService;

    /** 参考示例

    @Operation(summary = UPDATE_ACTION)
    //@Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX_EXPR + "#req.${pkField.name}")//, beforeInvocation = true
    @Transactional
    public boolean update(Update${entityName}Req req) {
        return ${serviceName?uncap_first}.update(req);
    }

    */

    /**
    * 统计
    *
    * @param req
    * @param paging 分页设置，可空
    * @return Stat${entityName}Req.Result
    */
    @Operation(summary = STAT_ACTION)
    public Stat${entityName}Req.Result stat(Stat${entityName}Req req, Paging paging){

         //回调构造更多的查询条件
         Consumer<SelectDao<${entityName}>> callback = dao -> {
          dao
          //.eq(${entityName}::getId, req.getId())

          //OR 语句
          .or()
             // .isNull(${entityName}::getEnable)
             // .eq(${entityName}::getEnable, true)
          .end() //OR 语句结束

          .setSafeModeMaxLimit(-1);
          };

        return simpleDao.findOneByQueryObj(req, paging, callback);
    }

}
