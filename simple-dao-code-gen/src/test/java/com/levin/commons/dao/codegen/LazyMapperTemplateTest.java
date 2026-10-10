package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.dao.codegen.external.ExternalEntity;
import com.levin.commons.dao.codegen.external.IncompleteExternalEntity;
import com.levin.commons.dao.codegen.external.MappedExternalEntity;
import com.levin.commons.dao.codegen.external.NoInstanceExternalEntity;
import com.alibaba.fastjson2.JSONObject;
import com.levin.commons.service.domain.InjectVar;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.example.value.ExternalValue;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.MappedSuperclass;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LazyMapperTemplateTest {

    @Test
    void generatedMapperShouldGuardOnlyLazyPropertiesAndKeepSingleArgumentEntry() throws Exception {
        List<FieldModel> fields = Arrays.stream(FixtureEntity.class.getDeclaredFields())
                .map(this::fieldModel).collect(Collectors.toList());
        fields.add(new FieldModel(FixtureEntity.class).setName("orgName").setType(String.class));
        fields.add(fieldModel(FixtureEntity.class.getDeclaredField("parent")).setName("renamedParent"));
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl").process(Map.of(
                "packageName", "com.example.services",
                "modulePackageName", "com.example",
                "entityClassPackage", "com.example.entities",
                "entityClassName", "com.example.entities.FixtureEntity",
                "entityName", "FixtureEntity",
                "entityTitle", "测试实体",
                "importList", Collections.emptyList(),
                "nestedInfoCollectionMappings", ServiceModelCodeGenerator.nestedInfoCollectionMappings(
                        FixtureEntity.class, Set.of(FixtureEntity.class)),
                "fields", fields), output);

        String source = output.toString();
        assertTrue(source.contains("@Mapping(target = \"children\", conditionExpression"), source);
        assertTrue(source.contains("@Mapping(target = \"parent\", conditionExpression"), source);
        assertTrue(source.contains("@Mapping(target = \"lazyName\", conditionExpression"), source);
        assertFalse(source.contains("@Mapping(target = \"eagerParent\""), source);
        assertFalse(source.contains("@Mapping(target = \"name\""), source);
        assertFalse(source.contains("@Mapping(target = \"orgName\""), source);
        assertFalse(source.contains("@Mapping(target = \"renamedParent\""), source);
        assertTrue(source.contains("default FixtureEntityInfo toInfo(FixtureEntity entity)"), source);
        assertTrue(source.contains("default FixtureEntityInfo toInfo(FixtureEntity entity, boolean allowLazyLoading)"), source);
        assertTrue(source.contains("return toInfo(entity, allowLazyLoading, cycleContext)"), source);
        assertTrue(source.contains("@Context CycleAvoidingMappingContext cycleContext"), source);
        assertTrue(source.contains("return toInfo(info, cycleContext)"), source);
        assertTrue(source.contains("finally {\n            cycleContext.clear();"), source);
        assertTrue(source.contains("import com.levin.commons.dao.support.CycleAvoidingMappingContext;"), source);
        assertFalse(source.contains("import com.levin.commons.dao.util.CycleAvoidingMappingContext;"), source);
        assertFalse(source.contains("import com.example.services.commons.mapper.CycleAvoidingMappingContext;"), source);
        assertTrue(source.contains("@BeforeMapping"), source);
        assertTrue(source.contains("cycleContext.getMappedInstance(source, targetType)"), source);
        assertTrue(source.contains("cycleContext.storeMappedInstance(source, target, targetType)"), source);
        assertTrue(source.contains("copyInfoList0(List<"), source);
        assertFalse(source.contains("extends JsonObjectMapping"), source);
        assertFalse(source.contains("extends JsonArrayMapping"), source);
        assertFalse(source.contains("fromJsonArray"), source);
        assertFalse(source.contains("toJsonArray"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void shouldNotPackageModuleSpecificCycleContextTemplate() {
        assertNull(ServiceModelCodeGenerator.class.getResource(
                "/simple.dao/codegen/template/services/commons/mapper/CycleAvoidingMappingContext.java"));
    }

    @Test
    void shouldNotPackageModuleSpecificJsonMappingTemplates() {
        assertNull(ServiceModelCodeGenerator.class.getResource(
                "/simple.dao/codegen/template/services/commons/mapper/JsonObjectMapping.java"));
        assertNull(ServiceModelCodeGenerator.class.getResource(
                "/simple.dao/codegen/template/services/commons/mapper/JsonArrayMapping.java"));
    }

    @Test
    void nestedEntityJsonFieldsShouldEnableInheritedMapping() throws Exception {
        assertFalse(ServiceModelCodeGenerator.needsJsonObjectMapping(FixtureEntity.class));
        assertFalse(ServiceModelCodeGenerator.needsJsonArrayMapping(FixtureEntity.class));
        assertTrue(ServiceModelCodeGenerator.needsJsonObjectMapping(AccountEntity.class));
        assertTrue(ServiceModelCodeGenerator.needsJsonArrayMapping(AccountEntity.class));

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl").process(Map.of(
                "packageName", "com.example.services",
                "modulePackageName", "com.example",
                "entityClassPackage", "com.example.entities",
                "entityClassName", "com.example.entities.FixtureEntity",
                "entityName", "FixtureEntity",
                "entityTitle", "测试实体",
                "importList", Collections.emptyList(),
                "jsonTargetTypes", List.of("com.alibaba.fastjson2.JSONObject", "java.util.List<java.lang.String>"),
                "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("interface FixtureEntityMapper {"), source);
        assertFalse(source.contains("extends Json"), source);
        assertTrue(source.contains("com.alibaba.fastjson2.JSONObject fromJsonType0(String json)"), source);
        assertTrue(source.contains("java.util.List<java.lang.String> fromJsonType1(String json)"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void nestedEntitiesShouldDelegateToTheirOwnMappers() throws Exception {
        Set<Class<?>> generatedEntities = Set.of(AccountEntity.class, OrgEntity.class, TeamEntity.class);
        List<Class<?>> nestedTypes = ServiceModelCodeGenerator.nestedEntityTypes(AccountEntity.class, generatedEntities);
        List<Class<?>> nestedInfoTypes = ServiceModelCodeGenerator.nestedInfoTypes(AccountEntity.class, generatedEntities);
        assertTrue(nestedTypes.contains(OrgEntity.class));
        assertTrue(nestedTypes.contains(TeamEntity.class));
        assertTrue(nestedTypes.contains(MappedExternalEntity.class));
        assertFalse(nestedTypes.contains(ExternalEntity.class));
        assertFalse(nestedTypes.contains(NoInstanceExternalEntity.class));
        assertTrue(nestedTypes.contains(IncompleteExternalEntity.class));
        assertTrue(nestedInfoTypes.contains(MappedExternalEntity.class));
        assertFalse(nestedInfoTypes.contains(ExternalEntity.class));
        assertFalse(nestedInfoTypes.contains(NoInstanceExternalEntity.class));
        assertFalse(nestedInfoTypes.contains(IncompleteExternalEntity.class));
        assertFalse(ServiceModelCodeGenerator.nestedEntityTypes(FixtureEntity.class,
                Set.of(FixtureEntity.class)).contains(FixtureEntity.class));
        List<Map<String, Object>> collectionMappings = ServiceModelCodeGenerator.nestedInfoCollectionMappings(
                AccountEntity.class, generatedEntities);
        assertTrue(collectionMappings.stream().anyMatch(mapping -> mapping.get("entityType") == OrgEntity.class
                && "List".equals(mapping.get("kind"))));
        assertTrue(collectionMappings.stream().anyMatch(mapping -> mapping.get("entityType") == OrgEntity.class
                && "Set".equals(mapping.get("kind"))));
        assertTrue(collectionMappings.stream().anyMatch(mapping -> mapping.get("entityType") == OrgEntity.class
                && "Collection".equals(mapping.get("kind"))));
        assertTrue(collectionMappings.stream().anyMatch(mapping -> mapping.get("entityType") == OrgEntity.class
                && "Array".equals(mapping.get("kind"))));
        assertTrue(collectionMappings.stream().anyMatch(mapping -> mapping.get("entityType") == TeamEntity.class
                && "List".equals(mapping.get("kind"))));
        assertTrue(collectionMappings.stream().anyMatch(mapping -> mapping.get("entityType") == MappedExternalEntity.class
                && "List".equals(mapping.get("kind"))));
        assertFalse(collectionMappings.stream().anyMatch(mapping -> mapping.get("entityType") == ExternalEntity.class
                || mapping.get("entityType") == IncompleteExternalEntity.class));

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl").process(Map.of(
                "packageName", "com.example.services",
                "modulePackageName", "com.example",
                "entityClassPackage", "com.example.entities",
                "entityName", "AccountEntity",
                "entityTitle", "测试实体",
                "nestedEntityTypes", nestedTypes,
                "nestedInfoTypes", nestedInfoTypes,
                "nestedInfoCollectionMappings", collectionMappings,
                "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("OrgEntityMapper.INSTANCE"), source);
        assertTrue(source.contains("TeamEntityMapper.INSTANCE"), source);
        assertTrue(source.contains("MappedExternalEntityMapper.INSTANCE"), source);
        assertTrue(source.contains("IncompleteExternalEntityMapper.INSTANCE"), source);
        assertFalse(source.contains("IncompleteExternalEntityInfo info,"), source);
        assertTrue(source.contains(".toInfo(entity, allowLazyLoading, cycleContext)"), source);
        assertTrue(source.contains("OrgEntityInfo mapNested("), source);
        assertTrue(source.contains(".toInfo(info, cycleContext)"), source);
        assertTrue(source.contains("copyInfoList"), source);
        assertTrue(source.contains("copyInfoSet"), source);
        assertTrue(source.contains("copyInfoCollection"), source);
        assertTrue(source.contains("copyInfoArray"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void generatedMapperShouldInheritOnlyJsonObjectMappingWhenArrayIsNotNeeded() throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl").process(Map.of(
                "packageName", "com.example.services",
                "modulePackageName", "com.example",
                "entityClassPackage", "com.example.entities",
                "entityClassName", "com.example.entities.FixtureEntity",
                "entityName", "FixtureEntity",
                "entityTitle", "测试实体",
                "importList", Collections.emptyList(),
                "jsonTargetTypes", List.of("com.alibaba.fastjson2.JSONObject"),
                "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("interface FixtureEntityMapper {"), source);
        assertTrue(source.contains("com.alibaba.fastjson2.JSONObject fromJsonType0(String json)"), source);
        assertFalse(source.contains("extends JsonObjectMapping"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void generatedMapperShouldInheritJsonArrayConversionWhenNeeded() throws Exception {
        FieldModel legacyField = new FieldModel(FixtureEntity.class)
                .setName("permissionList")
                .setType(String.class)
                .setTypeName("List<String>");
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl").process(Map.of(
                "packageName", "com.example.services",
                "modulePackageName", "com.example",
                "entityClassPackage", "com.example.entities",
                "entityClassName", "com.example.entities.FixtureEntity",
                "entityName", "FixtureEntity",
                "entityTitle", "测试实体",
                "importList", Collections.emptyList(),
                "jsonTargetTypes", List.of("java.util.List<java.lang.String>"),
                "fields", Collections.singletonList(legacyField)), output);

        String source = output.toString();
        assertTrue(source.contains("interface FixtureEntityMapper {"), source);
        assertTrue(source.contains("java.util.List<java.lang.String> fromJsonType0(String json)"), source);
        assertFalse(source.contains("extends JsonArrayMapping"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void nestedJsonPojoFieldShouldGenerateOnlyExactTypeMethods() throws Exception {
        assertTrue(ServiceModelCodeGenerator.jsonPojoTypes(FixtureEntity.class).isEmpty());
        assertTrue(ServiceModelCodeGenerator.jsonPojoTypes(AccountEntity.class).contains(BalanceInfo.class));

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl")
                .process(Map.of(
                        "packageName", "com.example.services",
                        "modulePackageName", "com.example",
                        "entityClassPackage", "com.example.entities",
                        "entityClassName", "com.example.entities.FixtureEntity",
                        "entityName", "FixtureEntity",
                        "entityTitle", "测试实体",
                        "importList", Collections.emptyList(),
                        "jsonTargetTypes", List.of(BalanceInfo.class.getCanonicalName()),
                        "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains(BalanceInfo.class.getCanonicalName() + " fromJsonType0(String json)"), source);
        assertTrue(source.contains("String toJsonType0("), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void nestedTypedJsonListShouldGenerateExactElementConversion() throws Exception {
        assertTrue(ServiceModelCodeGenerator.jsonListTargetTypes(FixtureEntity.class).isEmpty());
        assertTrue(ServiceModelCodeGenerator.jsonListTargetTypes(AccountEntity.class)
                .contains("java.util.List<" + BalanceInfo.class.getCanonicalName() + ">"));
        assertTrue(ServiceModelCodeGenerator.jsonListTargetTypes(AccountEntity.class)
                .contains("java.util.List<java.util.List<" + BalanceInfo.class.getCanonicalName() + ">>"));
        assertFalse(ServiceModelCodeGenerator.jsonListTargetTypes(AccountEntity.class)
                .contains("java.util.List<java.lang.String>"));
        assertTrue(ServiceModelCodeGenerator.jsonTargetTypes(AccountEntity.class)
                .contains("java.util.Map<java.lang.String,java.util.List<"
                        + BalanceInfo.class.getCanonicalName() + ">>"));

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl")
                .process(Map.of(
                        "packageName", "com.example.services",
                        "modulePackageName", "com.example",
                        "entityClassPackage", "com.example.entities",
                        "entityClassName", "com.example.entities.FixtureEntity",
                        "entityName", "FixtureEntity",
                        "entityTitle", "测试实体",
                        "importList", Collections.emptyList(),
                        "jsonTargetTypes", List.of(
                                "java.util.List<" + BalanceInfo.class.getCanonicalName() + ">",
                                "java.util.List<java.util.List<" + BalanceInfo.class.getCanonicalName() + ">>",
                                "java.util.Map<java.lang.String,java.util.List<" + BalanceInfo.class.getCanonicalName() + ">>"),
                        "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("java.util.List<" + BalanceInfo.class.getCanonicalName() + "> fromJsonType0(String json)"), source);
        assertTrue(source.contains("new com.alibaba.fastjson2.TypeReference<java.util.List<" + BalanceInfo.class.getCanonicalName() + ">>()"), source);
        assertTrue(source.contains("String toJsonType0(java.util.List<" + BalanceInfo.class.getCanonicalName() + "> value)"), source);
        assertTrue(source.contains("java.util.List<java.util.List<" + BalanceInfo.class.getCanonicalName() + ">> fromJsonType1(String json)"), source);
        assertTrue(source.contains("java.util.Map<java.lang.String,java.util.List<"
                + BalanceInfo.class.getCanonicalName() + ">> fromJsonType2(String json)"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void inheritedUnresolvedCollectionShouldBeIgnoredOnlyWithoutConcreteField() throws Exception {
        assertTrue(ServiceModelCodeGenerator.ignoredUnresolvedCollectionProperties(GenericListEntity.class)
                .contains("computedList"));
        assertFalse(ServiceModelCodeGenerator.ignoredUnresolvedCollectionProperties(GenericListEntity.class)
                .contains("storedList"));
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl")
                .process(Map.of(
                        "packageName", "com.example.services",
                        "modulePackageName", "com.example",
                        "entityClassPackage", "com.example.entities",
                        "entityClassName", "com.example.entities.FixtureEntity",
                        "entityName", "FixtureEntity",
                        "entityTitle", "测试实体",
                        "importList", Collections.emptyList(),
                        "ignoredUnresolvedCollectionProperties", List.of("computedList"),
                        "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("@Mapping(target = \"computedList\", ignore = true)"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void mutableValueObjectsShouldGetOneCopyMethodAndExplicitOverload() throws Exception {
        List<FieldModel> infoFields = infoFieldModels(OrgEntity.class);
        List<Class<?>> copyTypes = ServiceModelCodeGenerator.valueCopyTypes(infoFields);
        assertTrue(copyTypes.contains(ValueObject.class));
        assertTrue(copyTypes.contains(ExternalValue.class));
        assertTrue(ServiceModelCodeGenerator.valueCopyFields(infoFields, copyTypes).stream()
                .anyMatch(field -> "externalValues".equals(field.get("name"))
                        && "copyValueMap".equals(field.get("copyMethod"))));

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl")
                .process(Map.of(
                        "packageName", "com.example.services",
                        "modulePackageName", "com.example",
                        "entityClassPackage", "com.example.entities",
                        "entityClassName", "com.example.entities.FixtureEntity",
                        "entityName", "FixtureEntity",
                        "entityTitle", "测试实体",
                        "importList", Collections.emptyList(),
                        "valueCopyModels", ServiceModelCodeGenerator.valueCopyModels(copyTypes),
                        "fields", Collections.emptyList()), output);

        String source = output.toString();
        int valueIndex = copyTypes.indexOf(ValueObject.class);
        int externalIndex = copyTypes.indexOf(ExternalValue.class);
        assertTrue(source.contains(ValueObject.class.getCanonicalName() + " copyValue" + valueIndex + "("), source);
        assertTrue(source.contains(ExternalValue.class.getCanonicalName() + " copyValue" + externalIndex + "("), source);
        assertFalse(source.contains("copyValueList" + valueIndex + "("), source);
        assertFalse(source.contains("copyValueSet" + valueIndex + "("), source);
        assertTrue(source.contains("@Mapping(target = \"occurred\", ignore = true)"), source);
        assertTrue(source.contains("@org.mapstruct.AfterMapping"), source);
        assertTrue(source.contains("copyDynamicValue(source.getOccurred(), cycleContext"), source);
        assertTrue(source.contains("copyDynamicValue(source.getStatus(), cycleContext"), source);
        assertTrue(source.contains("copyValueAuto" + valueIndex), source);
        assertTrue(source.contains("copyDynamicValue(source.getExtParams(), cycleContext"), source);
        assertTrue(source.contains("@Mapping(target = \"next\", qualifiedByName = \"copyValue" + valueIndex + "\")"), source);
        assertTrue(source.contains("@Mapping(target = \"peers\", qualifiedByName = \"copyValue" + valueIndex + "\")"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void mappedSuperclassGenericAssociationsResolveToConcreteEntity() {
        List<FieldModel> infoFields = infoFieldModels(MappedTreeEntity.class);
        List<Class<?>> copyTypes = ServiceModelCodeGenerator.valueCopyTypes(infoFields);
        assertFalse(copyTypes
                .contains(MappedTreeBase.class));
        assertTrue(ServiceModelCodeGenerator.valueCopyModels(copyTypes).isEmpty());
        assertTrue(ServiceModelCodeGenerator.nestedInfoCollectionMappings(MappedTreeEntity.class,
                        Set.of(MappedTreeEntity.class)).stream()
                .anyMatch(mapping -> mapping.get("entityType") == MappedTreeEntity.class
                        && "List".equals(mapping.get("kind"))));
        IllegalStateException unresolved = assertThrows(IllegalStateException.class,
                () -> ServiceModelCodeGenerator.valueCopyTypes(infoFieldModels(RawMappedTreeEntity.class)));
        assertTrue(unresolved.getMessage().contains(MappedTreeBase.class.getName()));
    }

    @Test
    void valueCopyMetadataUsesSuppliedInfoFieldModelsOnly() {
        List<FieldModel> selected = infoFieldModels(OrgEntity.class).stream()
                .filter(field -> "externalValue".equals(field.getName()))
                .collect(Collectors.toList());
        assertTrue(ServiceModelCodeGenerator.valueCopyTypes(selected).contains(ExternalValue.class));
        assertFalse(ServiceModelCodeGenerator.valueCopyTypes(selected).contains(ValueObject.class));
        FieldModel derived = new FieldModel(OrgEntity.class).setField(selected.get(0).getField())
                .setResolvableType(selected.get(0).getResolvableType()).setName("derivedExternalValue");
        assertTrue(ServiceModelCodeGenerator.valueCopyTypes(List.of(derived)).isEmpty());
    }

    @Test
    void sameSimpleNameFromDifferentPackagesShouldNotEraseCollectionMethods() throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl").process(Map.of(
                "packageName", "com.example.services",
                "modulePackageName", "com.example",
                "entityClassPackage", "com.example.entities",
                "entityName", "Root",
                "entityTitle", "Root",
                "nestedInfoCollectionMappings", List.of(
                        Map.of("kind", "List", "entityType", Map.of("simpleName", "User",
                                "package", Map.of("name", "com.example.a.entities"))),
                        Map.of("kind", "List", "entityType", Map.of("simpleName", "User",
                                "package", Map.of("name", "com.example.b.entities")))),
                "fields", List.of()), output);
        String source = output.toString();
        assertTrue(source.contains("copyInfoList0(List<"), source);
        assertTrue(source.contains("copyInfoList1(List<"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    private FieldModel fieldModel(Field field) {
        return new FieldModel(FixtureEntity.class).setField(field)
                .setName(field.getName()).setType(field.getType());
    }

    private List<FieldModel> infoFieldModels(Class<?> entityClass) {
        List<FieldModel> fields = new ArrayList<>();
        ReflectionUtils.doWithFields(entityClass, field -> {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                fields.add(new FieldModel(entityClass).setField(field)
                        .setResolvableType(ResolvableType.forField(field, entityClass))
                        .setName(field.getName()));
            }
        });
        return fields;
    }

    static class FixtureEntity {
        String name;
        @Basic(fetch = FetchType.LAZY)
        String lazyName;
        @OneToMany
        List<FixtureEntity> children;
        @ManyToOne(fetch = FetchType.LAZY)
        FixtureEntity parent;
        @ManyToOne
        FixtureEntity eagerParent;
    }

    @Entity
    static class AccountEntity {
        @ManyToOne
        OrgEntity org;
        @ManyToOne
        ExternalEntity external;
        List<OrgEntity> orgs;
        Set<OrgEntity> peers;
        Collection<OrgEntity> all;
        OrgEntity[] relatedArray;
        TeamEntity team;
        List<TeamEntity> teams;
        List<ExternalEntity> externals;
        MappedExternalEntity mappedExternal;
        List<MappedExternalEntity> mappedExternals;
        IncompleteExternalEntity incompleteExternal;
        NoInstanceExternalEntity noInstanceExternal;
    }

    @Entity
    static class OrgEntity {
        ExternalValue externalValue;
        Map<String, ExternalValue> externalValues;
        @InjectVar(domain = "dao", expectBaseType = JSONObject.class)
        String exInfo;
        @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {String.class})
        String permissionList;
        @InjectVar(domain = "dao", expectBaseType = BalanceInfo.class)
        String balanceInfo;
        @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {BalanceInfo.class})
        String balanceHistory;
        @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {List.class, BalanceInfo.class})
        String nestedBalanceHistory;
        @InjectVar(domain = "dao", expectBaseType = Map.class, expectGenericTypes = {String.class, List.class, BalanceInfo.class})
        String balancesByRegion;
        @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {ValueObject.class})
        String values;
    }

    @MappedSuperclass
    abstract static class MappedTreeBase<T extends MappedTreeBase<T>> {
        T parent;
        List<T> children;
    }

    @Entity
    static class MappedTreeEntity extends MappedTreeBase<MappedTreeEntity> {
    }

    @Entity
    @SuppressWarnings("rawtypes")
    static class RawMappedTreeEntity extends MappedTreeBase {
    }

    @Entity
    static class TeamEntity {
        String name;
    }

    public static class BalanceInfo {
        public BalanceInfo() {}
        String currency;
    }

    public static class ValueObject {
        public ValueObject() {}
        ValueObject next;
        List<ValueObject> peers;
        String occurred;
        String status;
        Map<String, Object> extParams;

        public ValueObject getNext() { return next; }
        public void setNext(ValueObject next) { this.next = next; }
        public List<ValueObject> getPeers() { return peers; }
        public void setPeers(List<ValueObject> peers) { this.peers = peers; }
        public String getOccurred() { return occurred; }
        public void setOccurred(java.util.Date occurred) { this.occurred = occurred.toString(); }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public void setStatus(java.util.Date status) { this.status = status.toString(); }
        public Map<String, Object> getExtParams() { return extParams; }
        public void setExtParams(Map<String, Object> extParams) { this.extParams = extParams; }
    }

    interface GenericLists {
        default <T> List<T> getComputedList() { return null; }
        default <T> List<T> getStoredList() { return null; }
    }

    static class GenericListEntity implements GenericLists {
        List<String> storedList;
    }
}
