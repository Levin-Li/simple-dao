package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.domain.*;


import com.levin.commons.service.support.SpringCacheEventListener;
import ${modulePackageName}.cache.ModuleCacheService;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import org.slf4j.*;

import org.springframework.core.annotation.AnnotatedElementUtils;
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
//import org.springframework.dao.*;

import javax.persistence.PersistenceException;
import cn.hutool.core.lang.*;
import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.spring.context.annotation.*;
<#if !enableDubbo>//</#if>import org.apache.dubbo.config.annotation.*;

import ${entityClassPackage}.*;
import ${entityClassName};
import static ${entityClassPackage}.E_${entityName}.*;

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

<#if enableDubbo>@DubboService<#else>@Service(${serviceName}.SERVICE_BEAN_NAME)</#if>

@ConditionalOnProperty(name = ${serviceName}.SERVICE_BEAN_NAME, havingValue = "true", matchIfMissing = true)
//@Slf4j

//@Valid只能用在controller， @Validated可以用在其他被spring管理的类上。
//@Validated
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)

//*** 提示 *** 如果要注释缓存注解的代码可以在实体类上加上@javax.persistence.Cacheable(false)，然后重新生成代码
<#if !isCacheableEntity>//</#if>@CacheConfig(cacheNames = ${serviceName}.CACHE_NAME, cacheResolver = PLUGIN_PREFIX + "ModuleSpringCacheResolver")

// *** 提示 *** 请尽量不要修改本类，如果需要修改，请在Biz${className}业务类中重写业务逻辑

public class ${className} extends BaseService<${className}> implements ${serviceName} {

    private static final Logger log = LoggerFactory.getLogger(${className}.class);

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    ModuleCacheService moduleCacheService;


<#if isCacheableEntity>
    @PostConstruct
    public void init() {

        //启动先清除缓存
        //${serviceName?uncap_first}.clearAllCache();
        moduleCacheService.getCache(${serviceName}.CACHE_NAME).clear();

        SpringCacheEventListener.add( this.springCacheEventListener(),
               ${serviceName}.CACHE_NAME, ${serviceName}.CK_PREFIX + "*", SpringCacheEventListener.Action.Evict
        );
       
    }

</#if>

    @Operation(summary = QUERY_ACTION)
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging, Object... queryObjs) {
        return simpleDao.findPagingDataByQueryObj(req, paging, queryObjs);
    }

    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    public PagingData<${entityName}Info> selectQuery(Query${entityName}Req req, Paging paging, String... columnNames){
        return simpleDao.forSelect(${entityName}Info.class, req, paging).select(columnNames).findPaging(null, paging);
    }

    @Override
    @Operation(summary = STAT_ACTION)
    public int count(Query${entityName}Req req, Object... queryObjs){
        return (int) simpleDao.countByQueryObj(req, queryObjs);
    }

<#if pkField?exists>
    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    //Spring 缓存变量可以使用Spring 容器里面的bean名称，SpEL支持使用@符号来引用Bean。
    //调用本方法会导致不会对租户ID进行过滤，如果需要调用方对租户ID进行核查
    //如果要注释缓存注解的代码可以在实体类上加上@javax.persistence.Cacheable(false)，然后重新生成代码
    <#if !pkField?exists || !isCacheableEntity>//</#if>@Cacheable(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#${pkField.name})", key = CK_PREFIX_EXPR + "#${pkField.name}") //默认允许空值缓存 unless = "#result == null ",
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        return simpleDao.selectFrom(${entityName}.class).eq(E_${entityName}.${pkField.name}, ${pkField.name}).findUnique(${entityName}Info.class);
    }

    @Operation(summary = VIEW_DETAIL_ACTION)
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>//@Cacheable(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name})" , key = CK_PREFIX_EXPR + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if>  //默认允许空值缓存 unless = "#result == null ",
    public ${entityName}Info findById(${entityName}IdReq req) {

        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        <#if !isCacheableEntity>
        return simpleDao.findUnique(req);
        <#else>
        ${entityName}Info info = getSelfProxy().findById(req.get${pkField.name?cap_first}());

       if(info == null){
           return null;
       }

        if(req.isSuperAdmin()){
            return info;
        }

        boolean passed = false;

        <#if isMultiTenantObject>
        ///////////////////////租户检查///////////////////
        //如果有租户标识
        if (isNotEmpty(info.getTenantId())) {

            if (isEmpty(req.getTenantId())
                    || info.getTenantId().equals(req.getTenantId())) {
                //如果请求对象中没有租户标识，或是租户标识相等，则返回
                passed = true;
            }
            <#if isMultiTenantSharedObject>
            else if (info.isTenantShared()) {
                //如果是租户主动共享的的数据
                passed = true;
            }
            </#if>
        }
        <#if isMultiTenantPublicObject>
        else if (req.isContainsPublicData()) {
            passed = true;
        }
        </#if>

        Assert.isTrue(passed, "租户ID不匹配({})", req.getTenantId());
        ///////////////////////租户检查///////////////////
        </#if>

        if(req.isTenantAdmin()){
            return info;
        }

        <#if isOrganizedObject>
         passed = req.isAllOrgScope();
        ///////////////////////部门检查///////////////////
        //如果有组织标识
        if (!passed && isNotEmpty(info.getOrgId())) {
            if (isEmpty(req.getOrgIdList())
                    || req.getOrgIdList().contains(info.getOrgId())) {
                //如果请求对象中没有组织标识，或是组织标识相等，则返回
                passed = true;
            }
            <#if isOrganizedSharedObject>
            else if (info.isOrgShared()) {
                //如果是组织主动共享的的数据
                passed = true;
            }
            </#if>
        }
        <#if isOrganizedPublicObject>
        else if (!passed && req.isContainsOrgPublicData()) {
            passed = true;
        }
        </#if>

        Assert.isTrue(passed, "组织机构ID不匹配({})", req.getOrgId());
        ///////////////////////部门检查///////////////////
        </#if>

       <#if isPersonalObject>
        passed = false;
        ///////////////////////私有检查///////////////////
       // if (req instanceof PersonalObject) {
            if (isEmpty(info.getOwnerId())
                    || isEmpty(req.getOwnerId())
                    || info.getOwnerId().equals(req.getOwnerId())) {
                passed = true;
            }
        //}
        Assert.isTrue(passed, "拥有者ID不匹配({})", req.getOwnerId());
        ///////////////////////私有检查///////////////////
        </#if>

        return info;
      </#if>
    }
</#if>

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findOne(Query${entityName}Req req, Object... queryObjs){
        return simpleDao.findOneByQueryObj(req, queryObjs);
    }

    @Operation(summary = QUERY_ACTION)
    @Override
    public ${entityName}Info findUnique(Query${entityName}Req req){
        //记录超过一条时抛出异常 throws IncorrectResultSizeDataAccessException
        return simpleDao.findUnique(req);
    }

    /**
    * 创建记录，返回主键ID
    * @param req
    * @return pkId 主键ID
    */
    @Operation(summary = CREATE_ACTION)
    @Transactional
    @Override
<#if pkField?exists>
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#result)", key = CK_PREFIX_EXPR + "#result") //创建也清除缓存，防止空值缓存的情况
    public ${pkField.typeName} create(Create${entityName}Req req){
<#else>
    public boolean create(Create${entityName}Req req){
</#if>
        <#if classModel.isType('com.levin.commons.dao.domain.OrganizedObject')>
        Assert.isTrue(isEmpty(req.getOrgId()) || isEmpty(req.getOrgIdList()) || req.getOrgIdList().contains(req.getOrgId()), "orgId 超出可选范围");
        </#if>
        //dao支持保存前先自动查询唯一约束，并给出错误信息
        ${entityName} entity = simpleDao.create(req, true);
<#if pkField?exists>
        return entity.get${pkField.name?cap_first}();
<#else>
        return entity != null;
</#if>
    }

    @Operation(summary = BATCH_CREATE_ACTION)
    @Transactional
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
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX_EXPR + "#req.${pkField.name}")//, beforeInvocation = true
    @Transactional
    public boolean update(Update${entityName}Req req, Object... queryObjs) {
        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleUpdateByQueryObj(req, queryObjs);
    }

    @Operation(summary = UPDATE_ACTION)
    @Override
    @Transactional
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(allEntries = true, condition = "#result > 0")
    public int batchUpdate(SimpleUpdate${entityName}Req setReq, Query${entityName}Req whereReq, Object... queryObjs){
       return simpleDao.updateByQueryObj(setReq, whereReq, queryObjs);
    }

    @Operation(summary = BATCH_UPDATE_ACTION)
    @Transactional
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>//@CacheEvict(allEntries = true, condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#reqList)  && #result > 0")
    public int batchUpdate(List<Update${entityName}Req> reqList){
        //@Todo 优化批量提交
        return reqList.stream().map(req -> getSelfProxy().update(req)).mapToInt(n -> n ? 1 : 0).sum();
    }

    @Operation(summary = DELETE_ACTION)
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX_EXPR + "#req.${pkField.name}") //<#if isMultiTenantObject>#req.tenantId + </#if> , beforeInvocation = true
    @Transactional
    public boolean delete(${entityName}IdReq req) {
        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return simpleDao.singleDeleteByQueryObj(req);
    }

    @Operation(summary = BATCH_DELETE_ACTION)
    @Transactional
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>//@CacheEvict(allEntries = true, condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}List) && #result > 0")
    public int batchDelete(Delete${entityName}Req req){
        //@Todo 优化批量提交
        return Stream.of(req.get${pkField.name?cap_first}List())
            .map(${pkField.name} -> simpleDao.copy(req, new ${entityName}IdReq().set${pkField.name?cap_first}(${pkField.name})))
            .map(idReq -> getSelfProxy().delete(idReq))
            .mapToInt(n -> n ? 1 : 0)
            .sum();
    }

    @Operation(summary = BATCH_DELETE_ACTION)
    @Transactional
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(allEntries = true, condition = "#result > 0")
    public int batchDelete(Query${entityName}Req req, Object... queryObjs){
        return simpleDao.deleteByQueryObj(req, queryObjs);
    }

<#if isCacheableEntity>
////////////////////////////////////// 缓存支持  ///////////////////////////////////////
    <#if isMultiTenantObject>
    /**
     * 缓存事件监听器
     */
    protected SpringCacheEventListener springCacheEventListener() {

        //如果缓存发生删除事件，则删除对应的缓存
        return (ctx, cache, action, key, value) -> {

                    MultiTenantObject multiTenantObject = null;

                    if (value instanceof MultiTenantObject) {
                        multiTenantObject = (MultiTenantObject) value;
                    } else if (ctx != null && ctx.getArgs() != null) {
                        multiTenantObject = (MultiTenantObject) Stream.of(ctx.getArgs()).filter(o -> o instanceof MultiTenantObject).findFirst().orElse(null);
                    }

                    //如果没有找到租户对象
                    if (multiTenantObject == null) {

                        if (isNotEmpty(key)) {

                            if (log.isInfoEnabled()) {
                                log.info("发生缓存Evict事件({})，但是无法获取租户ID，将清楚所有的缓存", key);
                            }

                            cache.clear();
                        } else {
                            if (log.isInfoEnabled()) {
                                log.info("发生缓存Evict事件：value:{},action:{}，但是无法获取租户ID，并且也无Key将将忽略这个事件", value, action);
                            }
                        }

                        return;
                    }

                    String tenantId = multiTenantObject.getTenantId();

                    //如果没有租户ID
                    if (!StringUtils.hasText(tenantId)) {

                        // 是否是超级管理员
                        final boolean isSuperAdmin = ctx != null && ctx.getArgs() != null && Stream.of(ctx.getArgs()).filter(o -> o instanceof ServiceReq).anyMatch(o -> ((ServiceReq) o).isSuperAdmin());

                        if (isSuperAdmin) {
                            //超级管理员，允许不指定租户ID进行操作
                            ${entityName}Info entityInfo = getSelfProxy().findById(key.toString().substring(${serviceName}.CK_PREFIX.length()));

                            tenantId = entityInfo != null ? entityInfo.getTenantId() : null;
                        }
                    }

                    if (log.isInfoEnabled()) {
                        log.info("发生缓存Evict事件({})，试图清除租户({})的缓存列表", key, tenantId);
                    }

                    //试图清除租户的角色缓存
                    cache.evict("T@" + null2Empty(tenantId));
                };
    }

	<#if classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>
    /**
    * 加载租户的缓存${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * tenantId 为 null 时加载公共${entityTitle}
    *
    * @param tenantId 可为null，为 null 时加载公共${entityTitle}
    * @return
    */
    @Override
    public List<${entityName}Info> loadCacheList(String tenantId, Predicate<${entityName}Info> filter) {

        List<${entityName}Info> selfDataList = loadCacheListByTenant(tenantId, filter);

        //如果当前不是加载公共数据
        if (StringUtils.hasText(tenantId)
                // && MultiTenantPublicObject.class.isAssignableFrom(${entityName}.class)
        ) { //如果支持公共数据

            selfDataList = new ArrayList<>(selfDataList);

            final boolean tenantSelfEmpty = selfDataList.isEmpty();

            //加载公共数据
            List<${entityName}Info> publicDataList = new ArrayList<>(loadCacheListByTenant(null, filter));

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
            if (!isPublicEmpty && !tenantSelfEmpty
                   // && SortableObject.class.isAssignableFrom(${entityName}.class)
            ) {
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
     * @param tenantId 可为null，为 null 时加载公共${entityTitle}
     * @return
     */
	<#if !classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>@Override</#if>
    public List<${entityName}Info> loadCacheListByTenant(String tenantId, Predicate<${entityName}Info> filter) {

        List<${entityName}Info> dataList = getSelfProxy().getCache("T@" + null2Empty(tenantId), (key) ->
                simpleDao.selectFrom(${entityName}.class)

                        .isNull(!StringUtils.hasText(tenantId), ${entityName}::getTenantId)
                        .eq(StringUtils.hasText(tenantId), ${entityName}::getTenantId, tenantId)

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

        return filter != null ? dataList.stream().filter(filter).collect(Collectors.toList()) : Collections.unmodifiableList(dataList);
    }

    <#else>

    /**
     * 缓存事件监听器
     */
    protected SpringCacheEventListener springCacheEventListener() {
        return (ctx, cache, action, key, value) -> cache.evict("${entityName}List");
    }

    /**
    * 加载${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * @return
    */
    @Override
    public List<${entityName}Info> loadCacheList(Predicate<${entityName}Info> filter) {

        List<${entityName}Info> dataList = getSelfProxy().getCache("${entityName}List",
                (key) -> {
                    Consumer<SelectDao<?>> ex = dao -> dao 
                         <#if classModel.isType('com.levin.commons.dao.domain.SortableObject')>
                         //排序码排序
                         .orderBy(E_${entityName}.orderCode)
                         </#if>
                         <#if classModel.findFirstAttr('createTime','addTime','occurTime')??>
                         //时间倒序
                         .orderBy(E_${entityName}.${classModel.findFirstAttr('createTime','addTime','occurTime')})
                         </#if>
			             .setSafeModeMaxLimit(-1)
                         .disableSafeMode();

                    //最多2万条记录
                    return getSelfProxy().query(new Query${entityName}Req().setEnable(true), new SimplePaging().setPageSize(2 * 10000), ex).getItems();
                }
        );

        return filter != null ? dataList.stream().filter(filter).collect(Collectors.toList()) : Collections.unmodifiableList(dataList);
    }
   </#if>
    /**
     * 获取缓存
     *
     * @param keySuffix 缓存Key后缀，不包含前缀
     * @return 缓存数据
     */
    @Override
    public <T> T getCacheByKeySuffix(String keySuffix) {
        Assert.notBlank(keySuffix, "keySuffix is empty");
        return getSelfProxy().getCache(CK_PREFIX + keySuffix, null);
    }

    /**
     * 清除缓存
     *
     * @param keySuffix 缓存Key后缀，不包含前缀
     */
    @Override
    public void clearCacheByKeySuffix(String keySuffix) {
        Assert.notBlank(keySuffix, "keySuffix is empty");
        getSelfProxy().clearCache(CK_PREFIX + keySuffix);
    }
                        
    /**
    * 获取缓存
    *
    * @param key 缓存Key
    * @param valueLoader 缓存没有，则从加载函数加载
    * @return 缓存数据
    */
    @Operation(summary = GET_CACHE_ACTION, description = "完整的缓存key")
    @Cacheable(unless = "#valueLoader == null ", condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#key)", key = "#key")  //默认允许空值缓存 unless = "#result == null ",
    public <T> T getCache(String key, Function<String,T> valueLoader){
        Assert.notBlank(key, "key is empty");
        return valueLoader == null ? null : valueLoader.apply(key);
    }

    /**
    * 清除缓存
    * @param key 缓存Key
    */
    @Override
    @Operation(summary = CLEAR_CACHE_ACTION, description = "缓存Key，完整的缓存key")
    @CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#key)", key = "#key")
    public void clearCache(String key) {
        Assert.notBlank(key, "key is empty");
    }

    /**
    * 清除[${serviceName}.CACHE_NAME]缓存中的所有缓存
    *
    */
    @Override
    @Operation(summary = CLEAR_CACHE_ACTION,  description = "清除所有缓存")
    @CacheEvict(allEntries = true)
    public void clearAllCache() {
    }
////////////////////////////////////// 缓存支持  ///////////////////////////////////////

</#if>
}
