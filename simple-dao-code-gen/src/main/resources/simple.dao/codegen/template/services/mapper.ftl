package ${packageName};

import static ${modulePackageName}.ModuleOption.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.TargetType;
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
import com.levin.commons.dao.util.CycleAvoidingMappingContext;
<#if needsJsonObjectMapping!false>
import ${modulePackageName}.services.commons.mapper.JsonObjectMapping;
</#if>
<#if needsJsonArrayMapping!false>
import ${modulePackageName}.services.commons.mapper.JsonArrayMapping;
</#if>

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
<#assign mappingBases = []>
<#if needsJsonObjectMapping!false><#assign mappingBases = mappingBases + ["JsonObjectMapping"]></#if>
<#if needsJsonArrayMapping!false><#assign mappingBases = mappingBases + ["JsonArrayMapping"]></#if>
public interface ${entityName}Mapper<#if mappingBases?size gt 0> extends ${mappingBases?join(", ")}</#if> {

    ${entityName}Mapper INSTANCE = Mappers.getMapper(${entityName}Mapper.class);

    @Named("defaultEntityToInfo")
    default ${entityName}Info toInfo(${entityName} entity) {
        return toInfo(entity, false);
    }

    @Named("entryEntityToInfo")
    default ${entityName}Info toInfo(${entityName} entity, boolean allowLazyLoading) {
        CycleAvoidingMappingContext cycleContext = new CycleAvoidingMappingContext();
        try {
            return toInfo(entity, allowLazyLoading, cycleContext);
        } finally {
            cycleContext.clear();
        }
    }

<#list fields as field>
<#if field.loadCheckRequired>
    @Mapping(target = "${field.name}", conditionExpression = "java(com.levin.commons.dao.util.HibernateLazyPropertyUtil.shouldMap(entity, \"${field.name}\", allowLazyLoading))")
</#if>
</#list>
    ${entityName}Info toInfo(${entityName} entity, @Context boolean allowLazyLoading,
                             @Context CycleAvoidingMappingContext cycleContext);

    @Named("entryInfoCopy")
    default ${entityName}Info toInfo(${entityName}Info info) {
        CycleAvoidingMappingContext cycleContext = new CycleAvoidingMappingContext();
        try {
            return toInfo(info, cycleContext);
        } finally {
            cycleContext.clear();
        }
    }

    ${entityName}Info toInfo(${entityName}Info info, @Context CycleAvoidingMappingContext cycleContext);

    @BeforeMapping
    default <T> T getMappedInstance(Object source, @TargetType Class<T> targetType,
                                    @Context CycleAvoidingMappingContext cycleContext) {
        return cycleContext.getMappedInstance(source, targetType);
    }

    @BeforeMapping
    default void storeMappedInstance(Object source, @MappingTarget Object target,
                                     @TargetType Class<?> targetType,
                                     @Context CycleAvoidingMappingContext cycleContext) {
        cycleContext.storeMappedInstance(source, target, targetType);
    }

<#list nestedEntityTypes![] as nestedEntityType>
    default ${nestedEntityType.package.name?replace("entities", "services")}.${nestedEntityType.simpleName?lower_case}.info.${nestedEntityType.simpleName}Info mapNested(${nestedEntityType.canonicalName} entity,
            @Context boolean allowLazyLoading, @Context CycleAvoidingMappingContext cycleContext) {
        return ${nestedEntityType.package.name?replace("entities", "services")}.${nestedEntityType.simpleName?lower_case}.${nestedEntityType.simpleName}Mapper.INSTANCE
                .toInfo(entity, allowLazyLoading, cycleContext);
    }

</#list>

<#list jsonPojoTypes![] as jsonPojoType>
    default ${jsonPojoType.canonicalName} fromJsonType${jsonPojoType?index}(String json) {
        return json == null ? null : com.alibaba.fastjson2.JSON.parseObject(json, ${jsonPojoType.canonicalName}.class);
    }

    default String toJsonType${jsonPojoType?index}(${jsonPojoType.canonicalName} value) {
        return value == null ? null : com.alibaba.fastjson2.JSON.toJSONString(value);
    }

</#list>

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
