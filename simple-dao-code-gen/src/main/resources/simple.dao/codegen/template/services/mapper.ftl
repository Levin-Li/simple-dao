package ${packageName};

import static ${modulePackageName}.ModuleOption.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MapMapping;
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
import com.levin.commons.dao.support.CycleAvoidingMappingContext;

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
<#list ignoredUnresolvedCollectionProperties![] as property>
    @Mapping(target = "${property}", ignore = true)
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

<#list ignoredUnresolvedCollectionProperties![] as property>
    @Mapping(target = "${property}", ignore = true)
</#list>
<#list valueCopyFields![] as field>
    @Mapping(target = "${field.name}", qualifiedByName = "${field.copyMethod}")
</#list>
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

<#list nestedInfoTypes![] as nestedEntityType>

    default ${nestedEntityType.package.name?replace("entities", "services")}.${nestedEntityType.simpleName?lower_case}.info.${nestedEntityType.simpleName}Info mapNested(
            ${nestedEntityType.package.name?replace("entities", "services")}.${nestedEntityType.simpleName?lower_case}.info.${nestedEntityType.simpleName}Info info,
            @Context CycleAvoidingMappingContext cycleContext) {
        return ${nestedEntityType.package.name?replace("entities", "services")}.${nestedEntityType.simpleName?lower_case}.${nestedEntityType.simpleName}Mapper.INSTANCE
                .toInfo(info, cycleContext);
    }

</#list>

<#list nestedInfoCollectionMappings![] as mapping>
<#assign infoType = mapping.entityType.package.name?replace("entities", "services") + "." + mapping.entityType.simpleName?lower_case + ".info." + mapping.entityType.simpleName + "Info">
<#if mapping.kind == "Array">
    ${infoType}[] copyInfoArray${mapping?index}(${infoType}[] infos,
            @Context CycleAvoidingMappingContext cycleContext);
<#else>
    ${mapping.kind}<${infoType}> copyInfo${mapping.kind}${mapping?index}(${mapping.kind}<${infoType}> infos,
            @Context CycleAvoidingMappingContext cycleContext);
</#if>

</#list>

<#list jsonTargetTypes![] as targetType>
    default ${targetType} fromJsonType${targetType?index}(String json) {
        return com.levin.commons.dao.support.MapperJsonUtils.fromJson(json,
                new com.alibaba.fastjson2.TypeReference<${targetType}>() {}.getType());
    }

    default String toJsonType${targetType?index}(${targetType} value) {
        return com.levin.commons.dao.support.MapperJsonUtils.toJson(value);
    }

</#list>

<#list valueCopyModels![] as model>
    @Named("copyValue${model.index}")
<#list model.overloadedProperties as property>
    @Mapping(target = "${property.name}", expression = "java((${property.typeName}) ${property.mappingExpression})")
</#list>
<#list model.inaccessibleProperties as property>
    @Mapping(target = "${property.name}", ignore = true)
</#list>
<#list model.qualifiedProperties as property>
    @Mapping(target = "${property.name}", qualifiedByName = "${property.copyMethod}")
</#list>
    ${model.typeName} copyValue${model.index}(${model.typeName} source,
            @Context CycleAvoidingMappingContext cycleContext);

    default ${model.typeName} copyValueAuto${model.index}(${model.typeName} source,
            @Context CycleAvoidingMappingContext cycleContext) {
        return copyValue${model.index}(source, cycleContext);
    }

<#if model.inaccessibleProperties?has_content>
    @org.mapstruct.AfterMapping
    default void copyValueFields${model.index}(${model.typeName} source,
            @MappingTarget ${model.typeName} target,
            @Context CycleAvoidingMappingContext cycleContext) {
<#list model.inaccessibleProperties as property>
        java.lang.reflect.Field field${property?index} = org.springframework.util.ReflectionUtils.findField(
                ${model.typeName}.class, "${property.name}");
        org.springframework.util.ReflectionUtils.makeAccessible(field${property?index});
        org.springframework.util.ReflectionUtils.setField(field${property?index}, target,
                ${property.copyExpression});
</#list>
    }
</#if>

</#list>

<#list mapCopyModels![] as mapModel>
    @Named("copyMap${mapModel.index}")
    @MapMapping(<#if mapModel.keyCopyIndex gte 0>keyQualifiedByName = "copyValue${mapModel.keyCopyIndex}"<#if mapModel.valueCopyIndex gte 0>, </#if></#if><#if mapModel.valueCopyIndex gte 0>valueQualifiedByName = "copyValue${mapModel.valueCopyIndex}"</#if>)
    ${mapModel.rawTypeName}<${mapModel.keyTypeName}, ${mapModel.valueTypeName}> copyMap${mapModel.index}(
            ${mapModel.rawTypeName}<${mapModel.keyTypeName}, ${mapModel.valueTypeName}> source,
            @Context CycleAvoidingMappingContext cycleContext);

</#list>

    @Named("copyValueMap")
    @SuppressWarnings("unchecked")
    default <K, V, M extends Map<K, V>> M copyValueMap(M source,
            @Context CycleAvoidingMappingContext cycleContext) {
        if (source == null) return null;
        M target;
        try {
            target = (M) source.getClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            target = (M) new LinkedHashMap<K, V>();
        }
        target.putAll(source);
        return target;
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
