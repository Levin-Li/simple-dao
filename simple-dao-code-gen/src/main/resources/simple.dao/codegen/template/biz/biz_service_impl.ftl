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

<#--<#if classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>   && classModel.hasAnno('') -->

<#if isCacheableEntity && isMultiTenantObject>
    @PostConstruct
    public void init() {

        //启动先清除缓存
        //${serviceName?uncap_first}.clearAllCache();
        moduleCacheService.getCache(${serviceName}.CACHE_NAME).clear();

//        规划缓存(N个，每个租户一个 + 公共一个)： 1. 公共${entityTitle}列表缓存 2. 租户${entityTitle}列表缓存

        //如果缓存发生删除事件，则删除对应的缓存
        SpringCacheEventListener.add((ctx, cache, action, key, value) -> {
                    if (ctx == null && value == null) {
                        if (key == null) {
                            return;
                        }
                        //
                        cache.clear();
                    }

                    String tenantId = (value instanceof MultiTenantObject) ?
                            ((MultiTenantObject) value).getTenantId() :
                            (String) Stream.of(ctx.getArgs()).filter(o -> o instanceof MultiTenantObject).findFirst().map(o -> ((MultiTenantObject) o).getTenantId()).orElse(null);

                    if (log.isDebugEnabled()) {
                        log.debug("试图清除租户的${entityTitle}列表缓存：{} {}-{}", tenantId, key, value);
                    }

                    //试图清除租户的角色缓存
                    cache.evict("T@" + tenantId);
                }
                ,  ${serviceName}.CACHE_NAME,  ${serviceName}.CK_PREFIX + "*", SpringCacheEventListener.Action.Evict);
    }

<#if classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>
    /**
    * 加载租户的缓存${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * tenantId 为 null 时加载公共${entityTitle}
    *
    * @param userPrincipal 操作者
    * @param tenantId 可为null，为 null 时加载公共${entityTitle}
    * @return
    */
    @Override
    public List<${entityName}Info> loadCacheList(Serializable userPrincipal, String tenantId) {

        List<${entityName}Info> selfDataList = loadCacheListByTenant(userPrincipal, tenantId);

        //如果当前不是加载公共数据
        if (StringUtils.hasText(tenantId)
                && MultiTenantPublicObject.class.isAssignableFrom(${entityName}.class)) { //如果支持公共数据

            selfDataList = new ArrayList<>(selfDataList);

            final boolean tenantSelfEmpty = selfDataList.isEmpty();

            //加载公共数据
            List<${entityName}Info> publicDataList = new ArrayList<>(loadCacheListByTenant(userPrincipal, null));

            final boolean isPublicEmpty = publicDataList.isEmpty();

            if (!isPublicEmpty && !tenantSelfEmpty) {

                SelfOverridableObject selfOverridable = AnnotatedElementUtils.findMergedAnnotation(${entityName}.class, SelfOverridableObject.class);

                if (selfOverridable != null
                        && selfOverridable.overrideColumnNames() != null
                        && selfOverridable.overrideColumnNames().length > 0) {

                    //去除重复的记录
                    List<String> attrs = Arrays.asList(selfOverridable.overrideColumnNames());

                    final List<${entityName}Info> finalSelfDataList = selfDataList;
                    publicDataList.removeIf(m1 -> {
                                String key = simpleDao.getAttrValues(m1, attrs).stream().map(String::valueOf).collect(Collectors.joining(":"));

                                return finalSelfDataList.stream().anyMatch(m2 ->
                                        key.equals(simpleDao.getAttrValues(m2, attrs).stream().map(String::valueOf).collect(Collectors.joining(":")))
                                );
                            }

                    );
                }
            }
            selfDataList.addAll(publicDataList);

            //去除空对象
            selfDataList.removeIf(Objects::isNull);

            //重新新排序
            if (!isPublicEmpty && !tenantSelfEmpty && SortableObject.class.isAssignableFrom(${entityName}.class)) {
                selfDataList.sort(Comparator.comparing(${entityName}Info::getOrderCode));
            }
        }

        return selfDataList;
    }
</#if>

    /**
     * 加载租户的缓存${entityTitle}列表
     *
     * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
     *
     * tenantId 为 null 时加载公共${entityTitle}
     *
     * @param userPrincipal 操作者
     * @param tenantId 可为null，为 null 时加载公共${entityTitle}
     * @return
     */
<#if !classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>@Override</#if>
    public List<${entityName}Info> loadCacheListByTenant(Serializable userPrincipal, String tenantId) {

        List<${entityName}Info> ${entityName?uncap_first}InfoList = ${serviceName?uncap_first}.getCache("T@" + tenantId, (key) ->
                simpleDao.selectFrom(${entityName}.class)

                        .isNull(!StringUtils.hasText(tenantId), ${entityName}::getTenantId)
                        .eq(StringUtils.hasText(tenantId), ${entityName}::getTenantId, tenantId)

                         <#if classModel.isType('com.levin.commons.dao.domain.EnableObject')>
                         //启用的
                         .isNullOrEq(E_${entityName}.enable, true)
                         </#if>

                         <#if classModel.isType('com.levin.commons.dao.domain.StatefulObject')>
                         //状态正常的
                         //.eq(E_${entityName}.state, xxStatus)
                         </#if>

                         <#if classModel.isType('com.levin.commons.dao.domain.ExpiredObject')>
                          //未过期的
                          .or()
                          .isNull(E_${entityName}.expiredTime)
                          .gte(E_${entityName}.expiredTime, new java.util.Date())
                          .end()
                         </#if>

                         <#if classModel.isType('com.levin.commons.dao.domain.SortableObject')>
                         //排序码排序
                         .orderBy(E_${entityName}.orderCode)
                         </#if>

                         <#if classModel.findFirstAttr('createTime','addTime','occurTime')??>
                         //时间倒序
                         .orderBy(E_${entityName}.${classModel.findFirstAttr('createTime','addTime','occurTime')})
                         </#if>
                        .find(${entityName}Info.class)
        );

         <#if classModel.isType('com.levin.commons.dao.domain.ExpiredObject')>
         return ${entityName?uncap_first}InfoList.stream().filter(r -> r.getExpiredDate() == null || r.getExpiredDate().after(new Date())).collect(Collectors.toList());
         <#else>
         //复制一个列表，防止列表被修改，因为有使用内存缓存
         return Collections.unmodifiableList(${entityName?uncap_first}InfoList);
         </#if>
    }
<#elseif isCacheableEntity>
    @PostConstruct
    public void init() {

        //启动先清除缓存
        //${serviceName?uncap_first}.clearAllCache();
        moduleCacheService.getCache(${serviceName}.CACHE_NAME).clear();

        SpringCacheEventListener.add(
                (ctx, cache, action, key, value) -> cache.evict("${entityName}List"),
               ${serviceName}.CACHE_NAME, ${serviceName}.CK_PREFIX + "*", SpringCacheEventListener.Action.Evict
        );

    }

    /**
    * 加载${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * @return
    */
    @Override
    public List<${entityName}Info> load${entityName}List() {
        return ${serviceName?uncap_first}.getCache("${entityName}List",
                (key) -> {
                    Consumer<SelectDao<?>> ex = dao -> dao.setSafeModeMaxLimit(-1).disableSafeMode();
                    //最多2万条记录
                    return ${serviceName?uncap_first}.query(new Query${entityName}Req().setEnable(true), new SimplePaging().setPageSize(2 * 10000), ex).getItems();
                }
        );
    }
</#if>

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
        return simpleDao.findOneByQueryObj(req, paging);
    }

}
