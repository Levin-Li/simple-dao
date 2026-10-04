package ${packageName};

import static ${modulePackageName}.ModuleOption.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

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

import ${entityClassPackage}.${entityName}.*;

import static ${modulePackageName}.entities.EntityConst.*;


/**
 * ${entityTitle}-Mapper接口
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Mapper(unmappedTargetPolicy =  ReportingPolicy.IGNORE)
public interface ${entityName}Mapper {

    ${entityName}Mapper INSTANCE = Mappers.getMapper(${entityName}Mapper.class);

    @Named("defaultEntityToInfo")
    default ${entityName}Info toInfo(${entityName} entity) {
        return toInfo(entity, false);
    }

<#list fields as field>
<#if field.loadCheckRequired>
    @Mapping(target = "${field.name}", conditionExpression = "java(com.levin.commons.dao.util.HibernateLazyPropertyUtil.shouldMap(entity, \"${field.name}\", allowLazyLoading))")
</#if>
</#list>
    ${entityName}Info toInfo(${entityName} entity, @Context boolean allowLazyLoading);

    ${entityName}Info toInfo(${entityName}Info info);

    /** 旧版实体使用 JSON 字符串存储简单集合。 */
    default List<String> fromJsonArray(String json) {
        return json == null ? null : com.alibaba.fastjson2.JSON.parseArray(json, String.class);
    }

    default String toJsonArray(List<String> values) {
        return values == null ? null : com.alibaba.fastjson2.JSON.toJSONString(values);
    }

    ${entityName}Info toInfo(Create${entityName}Req req);

    ${entityName}Info toInfo(Update${entityName}Req req);

    ${entityName} toEntity(Create${entityName}Req req);

    ${entityName} toEntity(Update${entityName}Req req);

    Create${entityName}Req toCreateReq(${entityName} entity);

    Create${entityName}Req toCreateReq(${entityName}Info info);

    Create${entityName}Req toCreateReq(Update${entityName}Req req);

    Update${entityName}Req toUpdateReq(${entityName}Info info);

    Update${entityName}Req toUpdateReq(Create${entityName}Req req);

    ${entityName}IdReq toIdReq(Update${entityName}Req req);

    ${entityName}IdReq toIdReq(Query${entityName}Req req);

}
