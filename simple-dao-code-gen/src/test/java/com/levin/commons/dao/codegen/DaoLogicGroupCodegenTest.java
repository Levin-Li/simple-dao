package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.NotIn;
import com.levin.commons.dao.annotation.StartsWith;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.GenDaoLogicGroup;
import com.levin.commons.dao.codegen.model.DaoLogicGroupField;
import com.levin.commons.dao.codegen.model.FieldModel;
import freemarker.template.Configuration;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.ResolvableType;

import java.io.StringWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DaoLogicGroupCodegenTest {

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2", remark = "可见范围")
    static class Parent {
        @GenDaoLogicGroup(name = "visible", filterOp = In.class, injectVarName = "allowedOrgIds")
        String salesOrgId;
    }

    @DaoLogicGroup(name = "excluded", parent = "visible", logicOp = DaoLogicGroup.LogicOp.AND)
    static class Settlement extends Parent {
        @GenDaoLogicGroup(name = "visible", injectVarName = "viewerId")
        String salesUserId;

        @GenDaoLogicGroup(name = "excluded", filterOp = NotIn.class)
        String state;
    }

    static class Invalid {
        @GenDaoLogicGroup(name = " ") String orgId;
    }

    static class MixedGroup {
        @GenDaoLogicGroup(name = "same") String orgId;
        @GenDaoLogicGroup(name = "same") String userId;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class PrimitiveId {
        @GenDaoLogicGroup(name = "visible", filterOp = In.class) int orgId;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    @Entity
    static class ScalarPrimaryKey {
        @Id
        @GenDaoLogicGroup(name = "visible", filterOp = In.class, injectVarName = "allowedOrgIds")
        String id;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class CompositePrimaryKey {
        @EmbeddedId
        @GenDaoLogicGroup(name = "visible", filterOp = In.class)
        Object id;
    }

    @DaoLogicGroup(name = "optional", logicOp = DaoLogicGroup.LogicOp.OR)
    static class CustomInjection {
        @GenDaoLogicGroup(name = "optional", injectVarName = "orgScope",
                injectOverride = "false", injectRequired = "true")
        String orgId;
        @GenDaoLogicGroup(name = "optional", injectVarName = "userScope", injectRequired = "false")
        String userId;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class InvalidFilter {
        @GenDaoLogicGroup(name = "visible", filterOp = Ignore.class) String orgId;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class NameCollision {
        @GenDaoLogicGroup(name = "visible", filterOp = In.class) String orgId;
        String inOrgIdList;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class EqNameCollision {
        @GenDaoLogicGroup(name = "visible") String userId;
        String eqUserId;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR)
    static class AdditionalOperators {
        @GenDaoLogicGroup(name = "visible", filterOp = StartsWith.class) String code;
        @GenDaoLogicGroup(name = "visible", filterOp = Gte.class) Integer rank;
    }

    @DaoLogicGroup(name = "self", parent = "self", logicOp = DaoLogicGroup.LogicOp.OR)
    static class SelfParent { }

    @DaoLogicGroup(name = "a", parent = "b", logicOp = DaoLogicGroup.LogicOp.OR)
    @DaoLogicGroup(name = "b", parent = "a", logicOp = DaoLogicGroup.LogicOp.AND)
    static class Cycle { }

    @DaoLogicGroup(name = "a", parent = "b", logicOp = DaoLogicGroup.LogicOp.OR)
    @DaoLogicGroup(name = "b", parent = "c", logicOp = DaoLogicGroup.LogicOp.AND)
    @DaoLogicGroup(name = "c", parent = "a", logicOp = DaoLogicGroup.LogicOp.OR)
    static class ThreeLevelCycle { }

    @DaoLogicGroup(name = "child", parent = "missing", logicOp = DaoLogicGroup.LogicOp.OR)
    static class MissingParent { }

    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
    static class DuplicateDefinition { }

    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.OR)
    static class ParentDefinition { }

    @DaoLogicGroup(name = "same", logicOp = DaoLogicGroup.LogicOp.AND)
    static class ChildDuplicateDefinition extends ParentDefinition { }

    @Test
    void inheritedAnnotationsProduceTypedInjectedGroupedQueryFields() throws Exception {
        List<DaoLogicGroupField> groups = DaoLogicGroupField.forType(Settlement.class);
        assertEquals(3, groups.size());
        String source = renderBaseRequest(Settlement.class, groups);
        var type = StaticJavaParser.parse(source).getClassByName("SettlementBaseRequest").orElseThrow();

        assertFalse(type.getFieldByName("salesOrgId").isPresent());
        var salesOrg = type.getFieldByName("inSalesOrgIdList").orElseThrow();
        assertEquals("java.util.List<java.lang.String>", salesOrg.getVariable(0).getType().toString());
        assertFalse(salesOrg.toString().contains("@DaoLogicGroup"));
        assertTrue(salesOrg.toString().contains("@In(value = E_Settlement.salesOrgId, group = \"visible\")"));
        assertTrue(salesOrg.toString().contains("@InjectVar(value = \"allowedOrgIds\""));
        assertTrue(salesOrg.toString().contains("isOverride = \"true\""));
        assertTrue(salesOrg.toString().contains("isRequired = \"false\""));

        var salesUser = type.getFieldByName("eqSalesUserId").orElseThrow();
        assertTrue(salesUser.toString().contains("@Eq(value = E_Settlement.salesUserId, group = \"visible\")"));
        assertFalse(salesUser.toString().contains("@DaoLogicGroup"));

        var states = type.getFieldByName("notInStateList").orElseThrow();
        assertTrue(states.toString().contains("@NotIn(value = E_Settlement.state, group = \"excluded\")"));
        assertFalse(states.toString().contains("@DaoLogicGroup"));
        assertTrue(states.toString().contains("@InjectVar(value = \"state\""));
    }

    @Test
    void injectionOverrideAndRequirementCanBeConfiguredIndependently() throws Exception {
        String source = renderBaseRequest(CustomInjection.class, DaoLogicGroupField.forType(CustomInjection.class));
        var type = StaticJavaParser.parse(source).getClassByName("CustomInjectionBaseRequest").orElseThrow();
        String org = type.getFieldByName("eqOrgId").orElseThrow().toString();
        String user = type.getFieldByName("eqUserId").orElseThrow().toString();
        assertTrue(org.contains("@InjectVar(value = \"orgScope\""), org);
        assertTrue(org.contains("isOverride = \"false\""), org);
        assertTrue(org.contains("isRequired = \"true\""), org);
        assertTrue(user.contains("@InjectVar(value = \"userScope\""), user);
        assertTrue(user.contains("isOverride = \"true\""), user);
        assertTrue(user.contains("isRequired = \"false\""), user);
    }

    @Test
    void invalidGroupFailsBeforeGeneratingAnyFile() {
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(Invalid.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(MixedGroup.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(InvalidFilter.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(NameCollision.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(EqNameCollision.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(SelfParent.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(Cycle.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(ThreeLevelCycle.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(MissingParent.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(DuplicateDefinition.class));
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(ChildDuplicateDefinition.class));
        DaoLogicGroupField primitive = DaoLogicGroupField.forType(PrimitiveId.class).get(0);
        assertEquals("inOrgIdList", primitive.getQueryFieldName());
        assertEquals("java.util.List<java.lang.Integer>", primitive.getQueryTypeName());
        assertThrows(IllegalArgumentException.class, () -> DaoLogicGroupField.forType(CompositePrimaryKey.class));
    }

    @Test
    void scalarPrimaryKeyGeneratesTrustedGroupedInFilter(@TempDir Path output) throws Exception {
        String source = renderBaseRequest(ScalarPrimaryKey.class,
                DaoLogicGroupField.forType(ScalarPrimaryKey.class));
        var type = StaticJavaParser.parse(source).getClassByName("ScalarPrimaryKeyBaseRequest").orElseThrow();
        String filter = type.getFieldByName("inIdList").orElseThrow().toString();
        assertTrue(filter.contains("@In(value = E_ScalarPrimaryKey.id, group = \"visible\")"), filter);
        assertTrue(filter.contains("@InjectVar(value = \"allowedOrgIds\""), filter);

        ServiceModelCodeGenerator.moduleName("probe");
        ServiceModelCodeGenerator.modulePackageName("probe");
        Method buildEvt = ServiceModelCodeGenerator.class.getDeclaredMethod("buildEvt",
                Class.class, List.class, String.class, Map.class, String.class);
        buildEvt.setAccessible(true);
        Map<String, Object> params = new HashMap<>();
        params.put("reqExtendClass", "BaseReq");
        params.put("daoLogicGroupNames", List.of("id"));
        List<FieldModel> fields = fieldModels(ScalarPrimaryKey.class);
        FieldModel idField = fields.stream().filter(field -> "id".equals(field.getName())).findFirst().orElseThrow();
        idField.setPk(true);
        params.put("pkField", idField);
        Class<?> previous = ServiceModelCodeGenerator.entityClass(ScalarPrimaryKey.class);
        try {
            buildEvt.invoke(null, ScalarPrimaryKey.class, fields,
                    output.toString(), params, "query");
        } finally {
            ServiceModelCodeGenerator.entityClass(previous);
        }
        try (var generated = Files.walk(output)) {
            String query = Files.readString(generated
                    .filter(path -> path.getFileName().toString().equals("QueryScalarPrimaryKeyReq.java"))
                    .findFirst().orElseThrow());
            assertTrue(query.contains("this.idList = java.util.Collections.singletonList(id);"), query);
            assertFalse(query.contains("this.id = id;"), query);
        }
    }

    @Test
    void multiwordAndComparisonOperatorsUseTheirOwnPrefixes() throws Exception {
        String source = renderBaseRequest(AdditionalOperators.class, DaoLogicGroupField.forType(AdditionalOperators.class));
        var type = StaticJavaParser.parse(source).getClassByName("AdditionalOperatorsBaseRequest").orElseThrow();
        String code = type.getFieldByName("startsWithCode").orElseThrow().toString();
        String rank = type.getFieldByName("gteRank").orElseThrow().toString();
        assertTrue(code.contains("@StartsWith(value = E_AdditionalOperators.code, group = \"visible\")"), code);
        assertTrue(rank.contains("@Gte(value = E_AdditionalOperators.rank, group = \"visible\")"), rank);
    }

    @Test
    void sourceMarkerIsNotCopiedToGeneratedModels() {
        List<String> previous = new ArrayList<>(ServiceModelCodeGenerator.keepAnnotationList());
        try {
            ServiceModelCodeGenerator.keepAnnotationList("*");
            assertFalse(ServiceModelCodeGenerator.isMatch("@GenDaoLogicGroup(name=\"visible\")", "query"));
            assertFalse(ServiceModelCodeGenerator.isMatch(
                    "@com.levin.commons.dao.GenDaoLogicGroup(name=\"visible\")", "info"));
        } finally {
            ServiceModelCodeGenerator.keepAnnotationList(previous.toArray(new String[0]));
        }
    }

    @Test
    void generatorPlacesGroupConditionsOnlyInEntityBaseRequest(@TempDir Path output) throws Exception {
        ServiceModelCodeGenerator.moduleName("probe");
        ServiceModelCodeGenerator.modulePackageName("probe");
        List<FieldModel> fields = fieldModels(Settlement.class);
        Method buildEvt = ServiceModelCodeGenerator.class.getDeclaredMethod("buildEvt",
                Class.class, List.class, String.class, Map.class, String.class);
        buildEvt.setAccessible(true);
        Map<String, Object> params = new HashMap<>();
        params.put("reqExtendClass", "MultiTenantOrgReq<");
        Class<?> previous = ServiceModelCodeGenerator.entityClass(Settlement.class);
        try {
            buildEvt.invoke(null, Settlement.class, fields, output.toString(), params, "query");
        } finally {
            ServiceModelCodeGenerator.entityClass(previous);
        }
        try (var generated = Files.walk(output)) {
            Path query = generated.filter(path -> path.getFileName().toString().equals("QuerySettlementReq.java"))
                    .findFirst().orElseThrow();
            String source = Files.readString(query);
            var queryType = StaticJavaParser.parse(source).getClassByName("QuerySettlementReq").orElseThrow();
            assertFalse(queryType.getFieldByName("inSalesOrgIdList").isPresent(), source);
            assertFalse(queryType.getFieldByName("eqSalesUserId").isPresent(), source);
            assertFalse(queryType.getFieldByName("notInStateList").isPresent(), source);
            assertFalse(source.contains("@DaoLogicGroup(name ="), source);
        }
        Method buildBase = ServiceModelCodeGenerator.class.getDeclaredMethod("buildBaseRequest",
                Class.class, List.class, String.class, Map.class, String.class);
        buildBase.setAccessible(true);
        ServiceModelCodeGenerator.entityClass(Settlement.class);
        try {
            buildBase.invoke(null, Settlement.class, fields, output.toString(), params, "SettlementBaseRequest");
        } finally {
            ServiceModelCodeGenerator.entityClass(previous);
        }
        try (var generated = Files.walk(output)) {
            Path baseReq = generated.filter(path -> path.getFileName().toString().equals("SettlementBaseRequest.java"))
                    .findFirst().orElseThrow();
            String source = Files.readString(baseReq);
            assertTrue(source.contains("@DaoLogicGroup(name = \"visible\""), source);
            assertTrue(source.contains("parent = \"visible\""), source);
            assertEquals(2, source.split("@DaoLogicGroup\\(", -1).length - 1, source);
            var baseType = StaticJavaParser.parse(source).getClassByName("SettlementBaseRequest").orElseThrow();
            assertTrue(baseType.getFieldByName("inSalesOrgIdList").isPresent(), source);
            assertTrue(baseType.getFieldByName("eqSalesUserId").isPresent(), source);
            assertTrue(baseType.getFieldByName("notInStateList").isPresent(), source);
            assertTrue(source.contains("@In(value = E_Settlement.salesOrgId, group = \"visible\")"), source);
        }
        try (var generated = Files.walk(output)) {
            Path simple = generated.filter(path -> path.getFileName().toString().equals("SimpleQuerySettlementReq.java"))
                    .findFirst().orElseThrow();
            String source = Files.readString(simple);
            assertTrue(source.contains("extends MultiTenantOrgReq<T>"), source);
            var simpleType = StaticJavaParser.parse(source).getClassByName("SimpleQuerySettlementReq").orElseThrow();
            assertFalse(simpleType.getFieldByName("inSalesOrgIdList").isPresent(), source);
            assertFalse(simpleType.getFieldByName("eqSalesUserId").isPresent(), source);
            assertFalse(simpleType.getFieldByName("notInStateList").isPresent(), source);
        }
    }

    @Test
    void sourceClassAnnotationIsCopiedVerbatimToEntityBaseRequest(@TempDir Path output) throws Exception {
        Path sourceRoot = output.resolve("source");
        Path sourceFile = sourceRoot.resolve("com/levin/commons/dao/codegen/Sample.java");
        Files.createDirectories(sourceFile.getParent());
        Path sampleSource = Path.of("src/test/java/com/levin/commons/dao/codegen/Sample.java");
        if (!Files.exists(sampleSource))
            sampleSource = Path.of("simple-dao-code-gen").resolve(sampleSource);
        String sourceText = Files.readString(sampleSource)
                .replace("unless = \"1=2\"", "unless = \"1\" + \"=2\"");
        Files.writeString(sourceFile, sourceText);
        Method parse = ServiceModelCodeGenerator.class.getDeclaredMethod("parseSrcFile", File.class);
        parse.setAccessible(true);
        Field sourceMap = ServiceModelCodeGenerator.class.getDeclaredField("srcFileCompilationMap");
        sourceMap.setAccessible(true);
        Object previousMap = sourceMap.get(null);
        Class<?> previousEntity = ServiceModelCodeGenerator.entityClass(Sample.class);
        try {
            sourceMap.set(null, parse.invoke(null, sourceRoot.toFile()));
            ServiceModelCodeGenerator.moduleName("probe");
            ServiceModelCodeGenerator.modulePackageName("probe");
            Method buildBase = ServiceModelCodeGenerator.class.getDeclaredMethod("buildBaseRequest",
                    Class.class, List.class, String.class, Map.class, String.class);
            buildBase.setAccessible(true);
            buildBase.invoke(null, Sample.class, fieldModels(Sample.class), output.toString(),
                    new HashMap<>(Map.of("reqExtendClass", "BaseReq")), "SampleBaseRequest");
            try (var generated = Files.walk(output)) {
                Path baseReq = generated.filter(path -> path.getFileName().toString().equals("SampleBaseRequest.java"))
                        .findFirst().orElseThrow();
                String source = Files.readString(baseReq);
                assertTrue(source.contains("@DaoLogicGroup(name = \"visible\", logicOp = DaoLogicGroup.LogicOp.OR, minConditions = 1, unless = \"1\" + \"=2\")"), source);
            }
        } finally {
            sourceMap.set(null, previousMap);
            ServiceModelCodeGenerator.entityClass(previousEntity);
        }
    }

    private static String renderBaseRequest(Class<?> entity, List<DaoLogicGroupField> groups) throws Exception {
        List<FieldModel> fields = fieldModels(entity);
        Method base = ServiceModelCodeGenerator.class.getDeclaredMethod("getBaseInfo", Class.class, List.class, String.class, String.class);
        base.setAccessible(true);
        ServiceModelCodeGenerator.moduleName("probe");
        ServiceModelCodeGenerator.modulePackageName("probe");
        @SuppressWarnings("unchecked") Map<String, Object> params = (Map<String, Object>) base.invoke(null, entity, fields,
                "probe.services.req", entity.getSimpleName() + "BaseRequest");
        params.put("fields", fields);
        params.put("reqExtendClass", "BaseReq");
        params.put("servicePackageName", "probe.services");
        params.put("importList", Collections.emptySet());
        params.put("daoLogicGroupFields", groups);
        params.put("daoLogicGroupNames", groups.stream().map(DaoLogicGroupField::getSourceFieldName).toList());
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter writer = new StringWriter();
        cfg.getTemplate("simple.dao/codegen/template/services/req/base_request.ftl").process(params, writer);
        return writer.toString();
    }

    private static List<FieldModel> fieldModels(Class<?> entity) {
        List<FieldModel> fields = new ArrayList<>();
        for (Field field : com.levin.commons.dao.util.QueryAnnotationUtil.getNonStaticFields(entity)) {
            fields.add(new FieldModel(entity).setField(field).setName(field.getName())
                    .setResolvableType(ResolvableType.forField(field, entity))
                    .setType(field.getType()).setTypeName(field.getType().getSimpleName()));
        }
        return fields;
    }
}
