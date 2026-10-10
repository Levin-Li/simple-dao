package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.alibaba.fastjson2.JSONObject;
import com.levin.commons.service.domain.InjectVar;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.io.StringWriter;
import java.lang.reflect.Field;
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

class LazyMapperTemplateTest {

    @Test
    void generatedMapperShouldGuardOnlyLazyPropertiesAndKeepSingleArgumentEntry() throws Exception {
        List<FieldModel> fields = Arrays.stream(FixtureEntity.class.getDeclaredFields())
                .map(this::fieldModel).collect(Collectors.toList());
        fields.add(new FieldModel(FixtureEntity.class).setField(FixtureEntity.class.getDeclaredField("name"))
                .setName("orgName").setType(String.class));
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
                "nestedInfoCollectionMappings", ServiceModelCodeGenerator.nestedInfoCollectionMappings(FixtureEntity.class),
                "fields", fields), output);

        String source = output.toString();
        assertTrue(source.contains("@Mapping(target = \"children\", conditionExpression"), source);
        assertTrue(source.contains("@Mapping(target = \"parent\", conditionExpression"), source);
        assertTrue(source.contains("@Mapping(target = \"lazyName\", conditionExpression"), source);
        assertFalse(source.contains("@Mapping(target = \"eagerParent\""), source);
        assertFalse(source.contains("@Mapping(target = \"name\""), source);
        assertFalse(source.contains("@Mapping(target = \"orgName\""), source);
        assertTrue(source.contains("default FixtureEntityInfo toInfo(FixtureEntity entity)"), source);
        assertTrue(source.contains("default FixtureEntityInfo toInfo(FixtureEntity entity, boolean allowLazyLoading)"), source);
        assertTrue(source.contains("return toInfo(entity, allowLazyLoading, cycleContext)"), source);
        assertTrue(source.contains("finally {\n            cycleContext.clear();"), source);
        assertTrue(source.contains("@Context CycleAvoidingMappingContext cycleContext"), source);
        assertTrue(source.contains("return toInfo(info, cycleContext)"), source);
        assertTrue(source.contains("import com.levin.commons.dao.support.CycleAvoidingMappingContext;"), source);
        assertFalse(source.contains("import com.levin.commons.dao.util.CycleAvoidingMappingContext;"), source);
        assertFalse(source.contains("import com.example.services.commons.mapper.CycleAvoidingMappingContext;"), source);
        assertTrue(source.contains("@BeforeMapping"), source);
        assertTrue(source.contains("cycleContext.getMappedInstance(source, targetType)"), source);
        assertTrue(source.contains("cycleContext.storeMappedInstance(source, target, targetType)"), source);
        assertTrue(source.contains("copyFixtureEntityInfoList(List<"), source);
        assertFalse(source.contains("extends JsonObjectMapping"), source);
        assertFalse(source.contains("extends JsonArrayMapping"), source);
        assertFalse(source.contains("fromJsonArray"), source);
        assertFalse(source.contains("toJsonArray"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
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
                "needsJsonObjectMapping", true,
                "needsJsonArrayMapping", true,
                "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("interface FixtureEntityMapper extends JsonObjectMapping, JsonArrayMapping"), source);
        assertTrue(source.contains("import com.levin.commons.dao.support.JsonObjectMapping;"), source);
        assertTrue(source.contains("import com.levin.commons.dao.support.JsonArrayMapping;"), source);
        assertFalse(source.contains("import com.example.services.commons.mapper.JsonObjectMapping;"), source);
        assertFalse(source.contains("import com.example.services.commons.mapper.JsonArrayMapping;"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void nestedEntitiesShouldDelegateToTheirOwnMappers() throws Exception {
        assertTrue(ServiceModelCodeGenerator.nestedEntityTypes(AccountEntity.class).contains(OrgEntity.class));
        assertTrue(ServiceModelCodeGenerator.nestedEntityTypes(AccountEntity.class).contains(TeamEntity.class));
        assertFalse(ServiceModelCodeGenerator.nestedEntityTypes(FixtureEntity.class).contains(FixtureEntity.class));
        List<Map<String, Object>> collectionMappings = ServiceModelCodeGenerator.nestedInfoCollectionMappings(AccountEntity.class);
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
                "nestedEntityTypes", ServiceModelCodeGenerator.nestedEntityTypes(AccountEntity.class),
                "nestedInfoCollectionMappings", collectionMappings,
                "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("OrgEntityMapper.INSTANCE"), source);
        assertTrue(source.contains("TeamEntityMapper.INSTANCE"), source);
        assertTrue(source.contains(".toInfo(entity, allowLazyLoading, cycleContext)"), source);
        assertTrue(source.contains("OrgEntityInfo mapNested("), source);
        assertTrue(source.contains(".toInfo(info, cycleContext)"), source);
        assertTrue(source.contains("copyOrgEntityInfoList(List<"), source);
        assertTrue(source.contains("copyOrgEntityInfoSet(Set<"), source);
        assertTrue(source.contains("copyOrgEntityInfoCollection(Collection<"), source);
        assertTrue(source.contains("copyOrgEntityInfoArray("), source);
        assertTrue(source.contains("copyTeamEntityInfoList(List<"), source);
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
                "needsJsonObjectMapping", true,
                "needsJsonArrayMapping", false,
                "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("interface FixtureEntityMapper extends JsonObjectMapping {"), source);
        assertTrue(source.contains("import com.levin.commons.dao.support.JsonObjectMapping;"), source);
        assertFalse(source.contains("extends JsonObjectMapping,"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void generatedMapperShouldInheritJsonArrayConversionWhenNeeded() throws Exception {
        FieldModel legacyField = new FieldModel(FixtureEntity.class)
                .setField(FixtureEntity.class.getDeclaredField("name"))
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
                "needsJsonArrayMapping", true,
                "fields", Collections.singletonList(legacyField)), output);

        String source = output.toString();
        assertTrue(source.contains("interface FixtureEntityMapper extends JsonArrayMapping"), source);
        assertTrue(source.contains("import com.levin.commons.dao.support.JsonArrayMapping;"), source);
        assertFalse(source.contains("default List<String> fromJsonArray"), source);
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
                        "jsonPojoTypes", Collections.singletonList(BalanceInfo.class),
                        "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("BalanceInfo fromJsonType0(String json)"), source);
        assertTrue(source.contains("String toJsonType0("), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    private FieldModel fieldModel(Field field) {
        return new FieldModel(FixtureEntity.class).setField(field)
                .setName(field.getName()).setType(field.getType());
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
        List<OrgEntity> orgs;
        Set<OrgEntity> peers;
        Collection<OrgEntity> all;
        OrgEntity[] relatedArray;
        TeamEntity team;
        List<TeamEntity> teams;
    }

    @Entity
    static class OrgEntity {
        @InjectVar(domain = "dao", expectBaseType = JSONObject.class)
        String exInfo;
        @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {String.class})
        String permissionList;
        @InjectVar(domain = "dao", expectBaseType = BalanceInfo.class)
        String balanceInfo;
    }

    @Entity
    static class TeamEntity {
        String name;
    }

    static class BalanceInfo {
        String currency;
    }
}
