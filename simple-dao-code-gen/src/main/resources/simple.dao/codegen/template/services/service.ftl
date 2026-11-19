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
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;

import ${entityClassPackage}.*;
import ${packageName}.req.*;
import ${packageName}.info.*;

import ${modulePackageName}.*;
import ${modulePackageName}.entities.*;

import ${entityClassPackage}.${entityName}.*;

import static ${modulePackageName}.entities.EntityConst.*;

////////////////////////////////////
//自动导入列表
<#list importList as imp>
import ${imp};
</#list>
////////////////////////////////////

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
     * 按查询对象分页查询。主请求、分页参数和可变参数中的附加条件会交给同一个 DAO 查询处理。
     *
     * <p>{@code req} 是必传的主查询对象，通常为生成的 {@code Query${entityName}Req}；
     * {@code paging} 接受 {@code Paging} 实现（通常为 {@code SimplePaging}），传 {@code null}
     * 时使用默认的 {@code SimplePaging}。分页请放在独立的 {@code paging} 参数中。</p>
     * <p>{@code queryObjs} 可同时包含多个普通的注解查询对象和
     * {@code Consumer<SelectDao<${entityName}>>} 条件回调，用于扩展主请求的筛选条件。
     * 回调必须使用明确的 {@code Consumer<SelectDao<${entityName}>>} 类型，不能把未授权的
     * 客户端查询对象、租户或组织范围直接当作可信条件。</p>
     * <p>示例：</p>
     * <pre>{@code
     * SimplePaging page = new SimplePaging();
     * page.setPageIndex(1);
     * page.setPageSize(20);
     * page.setRequireTotals(true);
     * Query${entityName}Req extraFilter = new Query${entityName}Req(); // 按需设置额外的注解查询字段
     * Consumer<SelectDao<${entityName}>> scope = dao -> dao.eq(E_${entityName}.id, allowedId);
     * service.query(req, page, extraFilter, scope);
     * req.selectColumns(E_${entityName}.id);
     * service.query(req, page, extraFilter, scope); // 同时指定查询列
     * }</pre>
     *
     * @param req 必传的主查询对象
     * @param paging 分页设置，可空；通常使用 SimplePaging
     * @param queryObjs 附加的普通查询对象和/或 Consumer&lt;SelectDao&lt;${entityName}&gt;&gt; 回调
     * @return 分页数据
     */
    @Operation(summary = QUERY_ACTION)
    PagingData<${entityName}Info> query(@NotNull SimpleQuery${entityName}Req<?> req, Paging paging, Object... queryObjs);

    /**
     * 只追加一个 DAO 条件回调的便捷入口；分页及主请求与上面的 {@code query} 相同。
     * 需要同时传普通附加查询对象和回调时，使用 {@code query(req, paging, extraFilter, callback)}。
     */
    @Operation(summary = QUERY_ACTION)
    default PagingData<${entityName}Info> query(@NotNull SimpleQuery${entityName}Req<?> req, Paging paging,
                                                 Consumer<SelectDao<${entityName}>> queryCallback) {
        return query(req, paging, (Object) queryCallback);
    }

    /**
     * 按主查询对象查询指定列；此签名的可变参数只表示列名，不接受附加查询对象或 DAO 回调。
     * 需要扩展筛选条件时，先用 {@code req.selectColumns(...)} 指定服务端允许的列，
     * 再调用 {@code query(req, paging, extraFilter, callback)}。
     *
     * @param req 必传的主查询对象
     * @param paging 分页设置，可空
     * @param selectColumnNames 服务端确认的列名
     * @return 分页数据
     */
    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    PagingData<${entityName}Info> selectQuery(@NotNull SimpleQuery${entityName}Req<?> req, Paging paging, String... selectColumnNames);

    /**
    * 指定选择列查询
    *
    * @param req
    * @param paging 分页设置，可空
    * @param selectColumns 列名
    * @return defaultPagingData 分页数据
    */
    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    default PagingData<${entityName}Info> selectQuery(@NotNull SimpleQuery${entityName}Req<?> req, Paging paging, LambdaMethodAttr<${entityName},?>... selectColumns){
        return selectQuery(req, paging, Stream.of(selectColumns).filter(Objects::nonNull).map(LambdaMethodAttr::get).toArray(String[]::new));
    }

    /**
     * 统计记录数
     *
     * @param req
     * @param queryObjs 扩展的查询对象，可以是 Consumer<SelectDao<?>> 对象
     * @return record count
     */
    @Operation(summary = STAT_ACTION)
    int count(@NotNull SimpleQuery${entityName}Req<?> req, Object... queryObjs);

    /**
     * 统计记录，并通过回调追加 DAO 查询条件。
     */
    @Operation(summary = STAT_ACTION)
    default int count(@NotNull SimpleQuery${entityName}Req<?> req,
                      Consumer<SelectDao<${entityName}>> queryCallback) {
        return count(req, (Object) queryCallback);
    }

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
    @Operation(summary = VIEW_DETAIL_ACTION, description = "注意性能, 该方法将不会使用缓存")
    ${entityName}Info findById(@NotNull ${entityName}IdReq req);
</#if>

<#if classModel.uniqueKeyModels?has_content>
    /** 查询指定唯一属性组合是否启用查找缓存。 */
    boolean isUniqueFindCacheEnabled(String... propertyNames);

<#list classModel.uniqueKeyModels as uniqueKey>
    <#assign uniqueFields = classModel.findFields(uniqueKey.propertyNames)>
    @Operation(summary = VIEW_DETAIL_ACTION)
    ${entityName}Info findBy${uniqueKey.methodSuffix}(<#list uniqueFields as field><#if field.required>@NotNull </#if>${field.typeName} ${field.name}<#if field_has_next>, </#if></#list>);
</#list>
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

<#if selfOverridableMatchFields?has_content>
    /**
    * 获取最匹配的${entityTitle}。
    * <p>
    * 每个字段均匹配参数值或公共值（null），并按参数声明顺序优先返回精确匹配的数据。
    */
    @Operation(summary = "获取最匹配的" + E_${entityName}.BIZ_NAME)
    ${entityName}Info findBestMatch(
<#list selfOverridableMatchFields as field>
            <#if field.required>@NotNull </#if>${field.typeName} ${field.name}<#if field_has_next>,</#if>
</#list>, Object... exQueryObjects);

</#if>
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
     * 批量更新记录，并通过回调动态追加 UpdateDao 条件或更新内容。
     * 无条件更新由 DAO 层安全机制保护。
     */
    @Operation(summary = UPDATE_ACTION)
    default int batchUpdate(@NotNull SimpleUpdate${entityName}Req setReq,
                            Consumer<UpdateDao<${entityName}>>... updateCallbacks) {
        return batchUpdate(setReq, null, (Object[]) updateCallbacks);
    }

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

    /**
    * 清除租户${entityTitle}缓存
    *
    * @param tenantId 租户ID
    */
    @Operation(summary = CLEAR_CACHE_ACTION +"-租户", description = "用租户ID，清除租户${entityTitle}缓存列表")
    void clearCacheListByTenant(String tenantId);

    <#else>
    /**
    * 加载缓存${entityTitle}列表
    *
    * 注意：数据量大的数据，请不要使用缓存，将导致缓存爆满
    *
    * @return
    */
    List<${entityName}Info> loadCacheList(Predicate<${entityName}Info> filter);

    @Operation(summary = CLEAR_CACHE_ACTION +"-默认列表", description = "清除${entityTitle}缓存列表")
    void clearCacheList();
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
