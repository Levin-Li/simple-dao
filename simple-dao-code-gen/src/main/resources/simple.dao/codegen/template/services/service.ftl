package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
//import org.springframework.cache.annotation.*;
import java.util.*;
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
     * 更新记录，并返回更新是否成功
     *
     * @param req
     * @return boolean 是否成功
     */
    @Operation(summary = UPDATE_ACTION)
    boolean update(@NotNull Update${entityName}Req req);

    /**
     * 更新记录，并返回更新记录数
     *
     * @param setReq
     * @param whereReq
     * @return int 记录数
     */
    @Operation(summary = UPDATE_ACTION)
    int update(@NotNull SimpleUpdate${entityName}Req setReq, Query${entityName}Req whereReq);

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
     * 查询记录
     *
     * @param req
     * @param paging 分页设置，可空
     * @return pagingData 分页数据
     */
    @Operation(summary = QUERY_ACTION)
    PagingData<${entityName}Info> query(@NotNull Query${entityName}Req req, Paging paging);

    /**
     * 指定选择列查询
     *
     * @param req
     * @param paging 分页设置，可空
     * @return pagingData 分页数据
     */
    @Operation(summary = QUERY_ACTION + "-指定列", description = "通常用于字段过多的情况，提升性能")
    PagingData<Simple${entityName}Info> simpleQuery(@NotNull Query${entityName}Req req, Paging paging);

    /**
     * 简单统计
     *
     * @param req
     * @param paging 分页设置，可空
     * @return pagingData 分页数据
     */
    @Operation(summary = STAT_ACTION)
    PagingData<Stat${entityName}Req.Result> stat(@NotNull Stat${entityName}Req req, Paging paging);

    /**
     * 统计记录数
     *
     * @param req
     * @return record count
     */
    @Operation(summary = STAT_ACTION)
    int count(@NotNull Query${entityName}Req req);

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
     * @return data 第一条数据
     */
    @Operation(summary = QUERY_ACTION)
    ${entityName}Info findOne(@NotNull Query${entityName}Req req);

    /**
     * 查询并返回唯一一条数据
     * 如果有多余1条数据，将抛出异常
     * @param req
     * @return data
     */
    @Operation(summary = QUERY_ACTION)
    ${entityName}Info findUnique(Query${entityName}Req req);

    /**
     * 清除缓存
     * @param key 缓存Key
     */
    @Operation(summary = CLEAR_CACHE_ACTION,  description = "缓存Key通常是主键ID")
    void clearCache(@NotNull Object key);

}
