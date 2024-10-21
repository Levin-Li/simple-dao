package ${packageName};

import static ${modulePackageName}.ModuleOption.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;

//import org.springframework.cache.annotation.*;
//import org.springframework.dao.*;
//import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.*;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import javax.validation.*;
import javax.validation.constraints.*;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;

import ${modulePackageName}.*;
import ${modulePackageName}.entities.*;
import static ${modulePackageName}.entities.EntityConst.*;


/**
 * ${entityTitle}-服务接口
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Tag(name = E_${entityName}.BIZ_NAME, description = E_${entityName}.BIZ_NAME + MAINTAIN_ACTION)
public interface ${className} {

    String BIZ_NAME = E_${entityName}.BIZ_NAME;

    String CACHE_NAME = ModuleOption.ID + CACHE_DELIM + E_${entityName}.SIMPLE_CLASS_NAME;

    //缓存key前缀
    String CK_PREFIX = E_${entityName}.CACHE_KEY_PREFIX;

    //缓存key前缀Spel表达式
    String CK_PREFIX_EXPR = E_${entityName}.CACHE_KEY_PREFIX_EXPR;

    String SERVICE_NAME = "${className}";

    String SERVICE_BEAN_NAME = PLUGIN_PREFIX + SERVICE_NAME;

    /**
    * 获取实体类
    */
    default Class<?> getEntityClass() {
        return ${entityName}.class;
    }

    /**
     * 查询记录
     *
     * @param req
     * @param paging 分页设置，可空
     * @param queryObjs 扩展的查询对象，可以是 Consumer<SelectDao<?>> 对象
     * @return defaultPagingData 分页数据
     */
    @Operation(summary = QUERY_ACTION)
    PagingData<${entityName}Info> query(@NotNull Query${entityName}Req req, Paging paging, Object... queryObjs);

    /**
     * 指定选择列查询
     *
     * @param req
     * @param paging 分页设置，可空
     * @param selectColumnNames 列名
     * @return defaultPagingData 分页数据
     */
    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    PagingData<${entityName}Info> selectQuery(@NotNull Query${entityName}Req req, Paging paging, String... selectColumnNames);

    /**
    * 指定选择列查询
    *
    * @param req
    * @param paging 分页设置，可空
    * @param selectColumns 列名
    * @return defaultPagingData 分页数据
    */
    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    default PagingData<${entityName}Info> selectQuery(@NotNull Query${entityName}Req req, Paging paging, PFunction<${entityName},?>... selectColumns){
        return selectQuery(req, paging, Stream.of(selectColumns).filter(Objects::nonNull).map(PFunction::get).toArray(String[]::new));
    }

    /**
     * 统计记录数
     *
     * @param req
     * @param queryObjs 扩展的查询对象，可以是 Consumer<SelectDao<?>> 对象
     * @return record count
     */
    @Operation(summary = STAT_ACTION)
    int count(@NotNull Query${entityName}Req req, Object... queryObjs);

<#if pkField?exists>
    /**
     * 通过主键查找记录，建议在服务内部调用，不要在控制器中调用
     * @param ${pkField.name} 主键ID
     * @return data 数据详情
     */
    @Operation(summary = VIEW_DETAIL_ACTION)
    ${entityName}Info findById(@NotNull ${pkField.typeName} ${pkField.name});

    /**
    * 通过主键查找记录，同时可能注入其它过滤条件（如租户过滤，部门过滤，人员过滤），试图增加数据安全性
    * @param req
    * @return data 数据详情
    */
    @Operation(summary = VIEW_DETAIL_ACTION)
    ${entityName}Info findById(@NotNull ${entityName}IdReq req);
</#if>

    /**
     * 查询并返回第一条数据
     *
     * @param req
     * @param queryObjs 扩展的查询对象，可以是 Consumer<SelectDao<?>> 对象
     * @return data 第一条数据
     */
    @Operation(summary = QUERY_ACTION)
    ${entityName}Info findOne(@NotNull Query${entityName}Req req, Object... queryObjs);

    /**
     * 查询并返回唯一一条数据
     * 如果有多余1条数据，将抛出异常
     *
     * @param req
     * @return data
     * @throws RuntimeException 多条数据时抛出异常
     */
    @Operation(summary = QUERY_ACTION)
    ${entityName}Info findUnique(Query${entityName}Req req);

    /**
     * 创建记录，返回主键ID
     * @param req
     * @return pkId 主键ID
     */
    @Operation(summary = CREATE_ACTION)
<#if pkField?exists>
    ${pkField.typeName} create(@NotNull Create${entityName}Req req);
<#else>
    boolean create(@NotNull Create${entityName}Req req);
</#if>

    /**
     * 创建记录，返回主键ID列表
     * @param reqList
     * @return pkId 主键ID列表
     */
    @Operation(summary = BATCH_CREATE_ACTION)
<#if pkField?exists>
    List<${pkField.typeName}> batchCreate(@NotNull List<Create${entityName}Req> reqList);
<#else>
    List<Boolean> batchCreate(@NotNull List<Create${entityName}Req> reqList);
</#if>

    /**
    * 唯一更新
    * 有且仅有一条数据被更新，否则将抛出异常
    *
    * @param req
    * @param queryObjs
    */
    @Operation(summary = "唯一" + UPDATE_ACTION, description = "有且仅有一条数据被更新，否则将抛出异常")
    void updateUnique(@NotNull Update${entityName}Req req, Object... queryObjs);

    /**
     * 更新记录，并返回更新是否成功
     *
     * @param req
     * @param queryObjs 附加的查询条件或是更新内容，如可以是 Consumer<UpdateDao<?>> 对象
     * @return boolean 是否成功
     */
    @Operation(summary = UPDATE_ACTION)
    boolean update(@NotNull Update${entityName}Req req, Object... queryObjs);

    /**
     * 批量无ID更新记录，并返回更新记录数，请小心使用
     *
     * @param setReq
     * @param whereReq
     * @param queryObjs 附加的查询条件或是更新内容，如可以是 Consumer<UpdateDao<?>> 对象
     * @return int 记录数
     */
    @Operation(summary = UPDATE_ACTION)
    int batchUpdate(@NotNull SimpleUpdate${entityName}Req setReq, Query${entityName}Req whereReq, Object... queryObjs);

    /**
     * 批量更新记录，并返回更新记录数
     *
     * @param reqList
     * @return num 更新记录数
     */
    @Operation(summary = BATCH_UPDATE_ACTION)
    int batchUpdate(@NotNull List<Update${entityName}Req> reqList);

    /**
     * 删除记录，并返回删除是否成功
     * @param req
     * @return boolean 删除是否成功
     */
    @Operation(summary = DELETE_ACTION)
    boolean delete(@NotNull ${entityName}IdReq req);

    /**
     * 批量删除记录，并返回删除记录数
     * @param req
     * @return num 删除记录数
     */
    @Operation(summary = BATCH_DELETE_ACTION)
    int batchDelete(@NotNull Delete${entityName}Req req);

    /**
     * 批量删除记录，并返回删除记录数
     * @param req
     * @return num 删除记录数
     */
    @Operation(summary = BATCH_DELETE_ACTION)
    int batchDelete(Query${entityName}Req req, Object... queryObjs);

    /**
     * 有效数据过滤器
     *
     * @return
     */
    default Predicate<${entityName}Info> defaultEffectiveDataFilter() {
        return info -> {
            return info != null
                         <#if classModel.isType('com.levin.commons.dao.domain.EnableObject')>
                          //启用的
                          && Boolean.TRUE.equals(info.getEnable())
                         </#if>
                         <#if classModel.isType('com.levin.commons.dao.domain.StatefulObject')>
                         //状态正常的
                         //info.getState() == State.NORMAL
                         </#if>
                         <#if classModel.isType('com.levin.commons.dao.domain.ExpiredObject')>
                         //未过期的
                         && (info.getExpiredTime() == null || info.getExpiredTime().after(new Date()))
                         </#if>
            ;
        };
    }

<#if isCacheableEntity>

    <#if isMultiTenantObject>
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
    List<${entityName}Info> loadCacheList(String tenantId, Predicate<${entityName}Info> filter);

        <#else>
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
    List<${entityName}Info> loadCacheListByTenant(String tenantId, Predicate<${entityName}Info> filter);

        </#if>
    <#else>
    /**
    * 加载缓存${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * @return
    */
    List<${entityName}Info> loadCacheList(Predicate<${entityName}Info> filter);

    </#if>
    /**
     * 获取缓存
     *
     * @param keySuffix 缓存Key后缀，不包含前缀
     * @return 缓存数据
     */
    @Operation(summary = GET_CACHE_ACTION, description = "通常是主键ID")
    default <T> T getCacheByKeySuffix(@NotNull String keySuffix) {
        Assert.hasText(keySuffix, "keySuffix is empty");
        return getCache(CK_PREFIX + keySuffix, null);
    }

    /**
    * 获取缓存
    *
    * @param key 缓存Key
    * @param valueLoader 缓存没有，则从加载函数加载
    * @return 缓存数据
    */
    @Operation(summary = GET_CACHE_ACTION, description = "完整的缓存Key")
    <T> T getCache(@NotNull String key, Function<String,T> valueLoader);

    /**
     * 清除缓存
     *
     * @param keySuffix 缓存Key后缀，不包含前缀
     */
    @Operation(summary = CLEAR_CACHE_ACTION, description = "通常是主键ID")
    default void clearCacheByKeySuffix(@NotNull String keySuffix) {
        Assert.hasText(keySuffix, "keySuffix is empty");
        clearCache(CK_PREFIX + keySuffix);
    }

    /**
     * 清除缓存
     *
     * @param key 缓存Key
     */
    @Operation(summary = CLEAR_CACHE_ACTION, description = "完整的缓存Key")
    void clearCache(@NotNull String key);

    /**
     * 清除所有缓存
     */
    @Operation(summary = CLEAR_CACHE_ACTION, description = "清除所有缓存")
    void clearAllCache();

</#if>
}
