package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.dao.codegen.external.ExternalEntity;
import com.alibaba.fastjson2.JSONObject;
import com.levin.commons.service.domain.InjectVar;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        assertTrue(source.contains("import com.levin.commons.dao.util.CycleAvoidingMappingContext;"), source);
        assertFalse(source.contains("import com.example.services.commons.mapper.CycleAvoidingMappingContext;"), source);
        assertTrue(source.contains("@BeforeMapping"), source);
        assertTrue(source.contains("cycleContext.getMappedInstance(source, targetType)"), source);
        assertTrue(source.contains("cycleContext.storeMappedInstance(source, target, targetType)"), source);
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
    void sharedJsonObjectMappingShouldOfferBothDirections() throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/commons/mapper/JsonObjectMapping.java")
                .process(Map.of("modulePackageName", "com.example"), output);

        String source = output.toString();
        assertTrue(source.contains("JSONObject map(String json)"), source);
        assertTrue(source.contains("String map(JSONObject value)"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
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
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void nestedEntitiesShouldDelegateToTheirOwnMappers() throws Exception {
        assertTrue(ServiceModelCodeGenerator.nestedEntityTypes(AccountEntity.class).contains(OrgEntity.class));
        assertFalse(ServiceModelCodeGenerator.nestedEntityTypes(AccountEntity.class).contains(ExternalEntity.class));
        assertFalse(ServiceModelCodeGenerator.nestedEntityTypes(FixtureEntity.class).contains(FixtureEntity.class));

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
                "fields", Collections.emptyList()), output);

        String source = output.toString();
        assertTrue(source.contains("OrgEntityMapper.INSTANCE"), source);
        assertTrue(source.contains(".toInfo(entity, allowLazyLoading, cycleContext)"), source);
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
        assertFalse(source.contains("extends JsonObjectMapping,"), source);
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
                "needsJsonArrayMapping", true,
                "fields", Collections.singletonList(legacyField)), output);

        String source = output.toString();
        assertTrue(source.contains("interface FixtureEntityMapper extends JsonArrayMapping"), source);
        assertFalse(source.contains("default List<String> fromJsonArray"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    @Test
    void sharedJsonArrayMappingShouldOfferBothDirections() throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/commons/mapper/JsonArrayMapping.java")
                .process(Map.of("modulePackageName", "com.example"), output);

        String source = output.toString();
        assertTrue(source.contains("List<String> fromJsonArray(String json)"), source);
        assertTrue(source.contains("String toJsonArray(List<String> values)"), source);
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
        @ManyToOne
        ExternalEntity external;
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

    static class BalanceInfo {
        String currency;
    }
}
