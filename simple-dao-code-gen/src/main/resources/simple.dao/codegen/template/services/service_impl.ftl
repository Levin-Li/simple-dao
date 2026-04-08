package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.domain.*;
import com.levin.commons.utils.ObjectWrapperUtils;


import com.levin.commons.service.support.SpringCacheEventListener;
import ${modulePackageName}.cache.ModuleCacheService;

import jakarta.annotation.*;
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

import jakarta.persistence.PersistenceException;
import cn.hutool.core.lang.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.spring.context.annotation.*;
<#if !enableDubbo>//</#if>import org.apache.dubbo.config.annotation.*;

import ${entityClassPackage}.*;
import ${entityClassName};
import static ${entityClassPackage}.E_${entityName}.*;

import ${entityClassPackage}.${entityName}.*;

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

//*** 提示 *** 如果要注释缓存注解的代码可以在实体类上加上@jakarta.persistence.Cacheable(false)，然后重新生成代码
<#if !isCacheableEntity>//</#if>@CacheConfig(cacheNames = ${serviceName}.CACHE_NAME, cacheResolver = PLUGIN_PREFIX + "ModuleSpringCacheResolver")

// *** 提示 *** 请尽量不要修改本类，如果需要修改，请在Biz${className}业务类中重写业务逻辑

public class ${className} extends BaseService<${className}> implements ${serviceName} {

    private static final Logger log = LoggerFactory.getLogger(${className}.class);

    <#if enableDubbo>@DubboReference<#else>@Autowired</#if>
    ModuleCacheService moduleCacheService;

    @Autowired(required = false)
    DaoEventBus daoEventBus;

<#if isCacheableEntity>
    @PostConstruct
    public void init() {

        //启动先清除缓存
        //${serviceName?uncap_first}.clearAllCache();
        moduleCacheService.getCache(${serviceName}.CACHE_NAME).clear();

        SpringCacheEventListener.add(this.springCacheEventListener(),
               ${serviceName}.CACHE_NAME, ${serviceName}.CK_PREFIX + "*", SpringCacheEventListener.Action.Evict
        );
       
    }

</#if>

    public boolean handleEvent(boolean ok, EntityOption.Action action, Object id) {

        if (ok && action != null && daoEventBus != null) {
            daoEventBus.sendEvent(E_${entityName}.CLASS_NAME + "/" + action.name(), id);
        }

        return ok;
    }

    @Operation(summary = QUERY_ACTION)
    @Override
    public PagingData<${entityName}Info> query(Query${entityName}Req req, Paging paging, Object... queryObjs) {
        return simpleDao.findPagingDataByQueryObj(req, paging, queryObjs);
    }

    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    public PagingData<${entityName}Info> selectQuery(Query${entityName}Req req, Paging paging, String... columnNames){
        return simpleDao.forSelect(${entityName}Info.class, req, paging).select(columnNames).findPaging(${entityName}Info.class, paging);
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
    //如果要注释缓存注解的代码可以在实体类上加上@jakarta.persistence.Cacheable(false)，然后重新生成代码
    <#if !pkField?exists || !isCacheableEntity>//</#if>@Cacheable(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#${pkField.name})", key = CK_PREFIX_EXPR + "#${pkField.name}") //默认允许空值缓存 unless = "#result == null ",
    public ${entityName}Info findById(${pkField.typeName} ${pkField.name}) {
        return simpleDao.selectFrom(${entityName}.class).eq(E_${entityName}.${pkField.name}, ${pkField.name}).findUnique(${entityName}Info.class);
    }

    @Operation(summary = VIEW_DETAIL_ACTION, description = "注意性能, 该方法将不会使用缓存")
    @Override
    public ${entityName}Info findById(${entityName}IdReq req) {

        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");

        return simpleDao.findUnique(req);
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
        handleEvent(true, EntityOption.Action.Create, entity.get${pkField.name?cap_first}());
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
        return reqList.stream().map(req -> getSelfProxy().create(req)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Operation(summary = "唯一" + UPDATE_ACTION, description = "有且仅有一条数据被更新，否则将抛出异常")
    public void updateUnique(Update${entityName}Req req, Object... queryObjs) {
        Assert.isTrue(getSelfProxy().update(req, queryObjs), BIZ_NAME + "(" + req.get${pkField.name?cap_first}() + ")" + "-更新未成功");
    }

    @Operation(summary = UPDATE_ACTION)
    @Override
    <#if !pkField?exists || !isCacheableEntity>//</#if>@CacheEvict(condition = "@${cacheSpelUtilsBeanName}.isNotEmpty(#req.${pkField.name}) && #result", key = CK_PREFIX_EXPR + "#req.${pkField.name}")//, beforeInvocation = true
    @Transactional
    public boolean update(Update${entityName}Req req, Object... queryObjs) {
        Assert.${(pkField.typeClsName == 'java.lang.String') ? string('notBlank','notNull')}(req.get${pkField.name?cap_first}(), BIZ_NAME + " ${pkField.name} 不能为空");
        return handleEvent(simpleDao.singleUpdateByQueryObj(req, queryObjs), EntityOption.Action.Update, req.get${pkField.name?cap_first}());
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
        return handleEvent(simpleDao.singleDeleteByQueryObj(req), EntityOption.Action.Delete, req.get${pkField.name?cap_first}());
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

    /**
     * 加载所有数据
     * @param wrapper2Readonly
     * @param exDaoConsumer
     */
    protected List<${entityName}Info> loadAll(boolean wrapper2Readonly, Consumer<SelectDao<${entityName}>> exDaoConsumer){

       SelectDao<${entityName}> dao = simpleDao.selectFrom(${entityName}.class)

             //最大缓存记录5万
           .setSafeModeMaxLimit(-1).disableSafeMode().limit(-1, 5_0000);

      if(exDaoConsumer != null){
        exDaoConsumer.accept(dao);
      }

      List<${entityName}Info> result = dao

        <#if classModel.isType('com.levin.commons.dao.domain.SortableObject')>
            //排序码排序
            .orderBy(E_${entityName}.orderCode)
        </#if>

        <#if classModel.findFirstAttr('createTime','addTime','occurTime')??>
            //时间倒序
            .orderBy(E_${entityName}.${classModel.findFirstAttr('createTime','addTime','occurTime')})
        </#if>
        .find(${entityName}Info.class);

      //转为只读对象
      if(wrapper2Readonly){
        result = result.stream().map(ObjectWrapperUtils::wrapper2Readonly).collect(Collectors.toUnmodifiableList());
      }

     return result;
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
                        log.info("发生缓存Evict事件({})，试图清除租户({})的${entityTitle}缓存列表", key, tenantId);
                    }

                    //试图清除租户的[${entityTitle}]缓存
                    cache.evict("T@" + null2Empty(tenantId));
                    cache.evict("${entityName}List");
                };
    }

	<#if classModel.isType('com.levin.commons.dao.domain.MultiTenantPublicObject')>
    /**
    * 加载租户的缓存${entityTitle}列表，自动把公共数据合并进去
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * tenantId 为 null 时加载公共${entityTitle}
    *
    * @param tenantId 可为null，为 null 时仅加载公共${entityTitle}
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

                    //从公共数据中去除本租户已经有的数据
                    publicDataList.removeIf(m1 -> {
                                String key = simpleDao.getAttrValues(m1, attrs).stream().map(String::valueOf).collect(Collectors.joining(":"));
                                //字符串
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
                loadAll(true, dao ->
                        dao.isNull(!StringUtils.hasText(tenantId), ${entityName}::getTenantId)
                        .eq(StringUtils.hasText(tenantId), ${entityName}::getTenantId, tenantId)
                 )
        );


        if(dataList == null) {
           clearCacheListByTenant(tenantId);
        }

        return filter != null ? dataList.stream().filter(filter).collect(Collectors.toList()) : Collections.unmodifiableList(dataList);
    }

    @Override
    public void clearCacheListByTenant(String tenantId){
        getSelfProxy().clearCache("T@" + null2Empty(tenantId));
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

        List<${entityName}Info> dataList = getSelfProxy().getCache("${entityName}List", (key) -> loadAll(true, null) );

        if(dataList == null) {
           clearCacheList();
        }

        return filter != null ? dataList.stream().filter(filter).collect(Collectors.toList()) : Collections.unmodifiableList(dataList);
    }

    @Override
    public void clearCacheList(){
        getSelfProxy().clearCache("${entityName}List");
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
