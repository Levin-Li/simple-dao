package com.levin.commons.dao.codegen;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.rbac.RbacDataAttrGroup;
import com.levin.commons.rbac.RbacDataAttrGroupMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.levin.commons.rbac.RbacDataAttrMetadata;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class RbacDataAttrGroupCodegenTest {
    @TempDir Path dir;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void compiledEntityGroupsMustSurviveAllModelsAndDerivedQueryFields(boolean enumScope) throws Exception {
        Path entity = source("input/probe/entities/Contract.java", """
                package probe.entities;
                import com.levin.commons.rbac.RbacDataAttrGroup;
                import io.swagger.v3.oas.annotations.media.Schema;
                @RbacDataAttrGroup(name="财务", attrList={"amount"}, confidentialLevel=100)
                @RbacDataAttrGroup(name="日期", attrList={"day"})
                @jakarta.persistence.Entity
                @jakarta.persistence.Access(jakarta.persistence.AccessType.FIELD)
                public class Contract extends Parent implements com.levin.commons.dao.domain.MultiTenantObject {
                    @RbacDataAttrGroup @Schema(title="租户身份") public String tenantId;
                    public <T extends java.io.Serializable> T getTenantId() { return (T)tenantId; }
                    @jakarta.persistence.Id @RbacDataAttrGroup(name="记录标识") public String id;
                    @Schema(title="金额") public java.math.BigDecimal amount;
                    @RbacDataAttrGroup @RbacDataAttrGroup(name="付款") @Schema(title="银行账号") public String account;
                    @RbacDataAttrGroup(name="财务") public String label;
                    @Schema(title="日期") public java.time.LocalDate day;
                    @Schema(title="附件") @RbacDataAttrGroup(name="附件") public String attachment;
                    @RbacDataAttrGroup(name="财务", confidentialLevel=300, remark="结算\\\"说明")
                    public java.math.BigDecimal getAmount() { return amount; }
                    public String plain;
                    public enum Kind { CUSTOMER, SUPPLIER }
                    %s @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING) public Kind kind;
                }
                @jakarta.persistence.MappedSuperclass
                class Parent {
                    @RbacDataAttrGroup(name="继承") @Schema(title="父级备注") public String inherited;
                    %s @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING) public Contract.Kind inheritedKind;
                }
                """.formatted(enumScope ? "@com.levin.commons.rbac.RbacDataAttr" : "",
                        enumScope ? "@com.levin.commons.rbac.RbacDataAttr" : ""));
        Path entityClasses = dir.resolve("entity-classes");
        compile(List.of(entity), entityClasses, System.getProperty("java.class.path"));
        Path jar = dir.resolve("entity-without-sources.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar));
             var paths = Files.walk(entityClasses)) {
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                output.putNextEntry(new JarEntry(entityClasses.relativize(file).toString()));
                Files.copy(file, output);
                output.closeEntry();
            }
        }
        Files.delete(entity);
        try (URLClassLoader entityLoader = new URLClassLoader(new java.net.URL[]{jar.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> sourceType = entityLoader.loadClass("probe.entities.Contract");
            Field sourceMap = ServiceModelCodeGenerator.class.getDeclaredField("srcFileCompilationMap");
            sourceMap.setAccessible(true);
            sourceMap.set(null, new HashMap<>());
            ServiceModelCodeGenerator.moduleName("probe");
            ServiceModelCodeGenerator.modulePackageName("probe");
            List<Path> generated = new ArrayList<>();
            Map<String, String> templates = new LinkedHashMap<>();
            templates.put("CreateContractReq", ServiceModelCodeGenerator.CREATE_EVT_FTL);
            templates.put("SimpleUpdateContractReq", ServiceModelCodeGenerator.SIMPLE_UPDATE_EVT_FTL);
            templates.put("UpdateContractReq", ServiceModelCodeGenerator.UPDATE_EVT_FTL);
            templates.put("SimpleQueryContractReq", ServiceModelCodeGenerator.SIMPLE_QUERY_EVT_FTL);
            templates.put("QueryContractReq", ServiceModelCodeGenerator.QUERY_EVT_FTL);
            templates.put("ContractIdReq", ServiceModelCodeGenerator.BASE_ID_EVT_FTL);
            templates.put("DeleteContractReq", ServiceModelCodeGenerator.DEL_EVT_FTL);
            templates.put("ContractInfo", ServiceModelCodeGenerator.INFO_FTL);
            for (var template : templates.entrySet()) {
                String name = template.getKey();
                String action = name.contains("Info") ? "info" : name.contains("Query") ? "query" : name.contains("Update") ? "update" : name.contains("Delete") || name.contains("IdReq") ? "delete" : "create";
                Method build = ServiceModelCodeGenerator.class.getDeclaredMethod("buildFieldModel", Class.class, Map.class, boolean.class, String.class);
                build.setAccessible(true);
                @SuppressWarnings("unchecked") List<FieldModel> fields = (List<FieldModel>) build.invoke(null, sourceType, Map.of(), !action.equals("info"), action);
                fields.forEach(field -> {
                    field.setSchemaDescUseConstRef(false);
                    if (field.getName().equals("account")) field.setEnumerable(true);
                    if (field.getName().equals("attachment")) field.setLazy(true);
                    if (field.getName().equals("label")) {
                        field.setContains(true);
                        field.getExtras().put("nameSuffix", "Contains");
                    }
                });
                Method base = ServiceModelCodeGenerator.class.getDeclaredMethod("getBaseInfo", Class.class, List.class, String.class, String.class);
                base.setAccessible(true);
                String pkg = name.contains("Info") ? "probe.services.info" : "probe.services.req";
                @SuppressWarnings("unchecked") Map<String, Object> params = (Map<String, Object>) base.invoke(null, sourceType, fields, pkg, name);
                params.put("servicePackageName", "probe.services");
                params.put("infoExtendClass", "BaseInfo");
                params.put("reqExtendClass", name.equals("UpdateContractReq") ? "SimpleUpdateContractReq" : name.equals("QueryContractReq") ? "SimpleQueryContractReq<QueryContractReq>" : "BaseReq");
                Path output = dir.resolve("generated/" + pkg.replace('.', '/') + "/" + name + ".java");
                ServiceModelCodeGenerator.genFileByTemplate(template.getValue(), params, output.toString());
                String first = Files.readString(output).replaceAll("@time: .*?, 代码", "@time: stable, 代码");
                ServiceModelCodeGenerator.genFileByTemplate(template.getValue(), params, output.toString());
                assertEquals(first, Files.readString(output).replaceAll("@time: .*?, 代码", "@time: stable, 代码"));
                generated.add(output);
            }
            generated.add(source("generated/probe/entities/E_Contract.java", "package probe.entities; public class E_Contract { public static final String ALIAS=\"c\", tenantId=\"tenantId\", tenantName=\"tenantName\", id=\"id\", amount=\"amount\", account=\"account\", label=\"label\", day=\"day\", attachment=\"attachment\", inherited=\"inherited\", plain=\"plain\", kind=\"kind\", inheritedKind=\"inheritedKind\"; }"));
            generated.add(source("generated/probe/entities/EntityConst.java", "package probe.entities; public class EntityConst { public static final String CREATE_ACTION=\"新增\", UPDATE_ACTION=\"修改\", QUERY_ACTION=\"查询\", DELETE_ACTION=\"删除\", BIZ_NAME=\"合同\"; }"));
            generated.add(source("generated/probe/services/commons/req/BaseReq.java", "package probe.services.commons.req; public class BaseReq { public String tenantId; public <T extends java.io.Serializable> T getTenantId() {return (T)tenantId;} protected <T> T checkSQLInject(Object... values) {return (T)this;} protected boolean isNotBlank(Object value) {return value != null;} }"));
            generated.add(source("generated/probe/services/commons/info/BaseInfo.java", "package probe.services.commons.info; public class BaseInfo { public String tenantId; public String tenantName; public <T extends java.io.Serializable> T getTenantId() {return (T)tenantId;} }"));
            generated.add(source("generated/com/levin/commons/dao/support/Probe.java", "package com.levin.commons.dao.support; public class Probe {}"));
            Path compiled = dir.resolve("models");
            compile(generated, compiled, System.getProperty("java.class.path") + java.io.File.pathSeparator + jar);
            try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{compiled.toUri().toURL()}, entityLoader)) {
                for (String name : templates.keySet()) {
                    Class<?> model = loader.loadClass((name.contains("Info") ? "probe.services.info." : "probe.services.req.") + name);
                    assertEquals(sourceType, model.getAnnotation(TargetOption.class).entityClass());
                    assertEquals(!enumScope, RbacDataAttrMetadata.forType(sourceType).isEmpty());
                    assertTrue(RbacDataAttrMetadata.forType(model).isEmpty(), "range annotations belong only to the source entity");
                    assertTrue(Arrays.stream(model.getDeclaredFields()).noneMatch(f -> f.getName().startsWith("allowed")), name);
                    if (name.equals("SimpleQueryContractReq")) {
                        assertTrue(RbacDataAttrGroupMetadata.forType(model).getGroupsForAttribute("amount").isEmpty(), "source columns stay on TargetOption, not phantom DTO fields");
                        assertFalse(RbacDataAttrGroupMetadata.forType(model).getGroupsForAttribute("tenantId").isEmpty());
                        continue;
                    }
                    if (name.equals("ContractIdReq") || name.equals("DeleteContractReq")) {
                        String idProperty = name.equals("ContractIdReq") ? "id" : "idList";
                        assertFalse(RbacDataAttrGroupMetadata.forType(model).getGroupsForAttribute(idProperty).isEmpty(), name);
                        assertFalse(RbacDataAttrGroupMetadata.forType(model).getGroupsForAttribute("tenantId").isEmpty(), name);
                        continue;
                    }
                    for (String attr : List.of("amount", "account", "inherited")) {
                        if (name.equals("UpdateContractReq")) continue; // inherited metadata is verified separately below
                        List<RbacDataAttrGroupMetadata.Group> groups = RbacDataAttrGroupMetadata.forType(model).getGroupsForAttribute(attr);
                        assertFalse(groups.isEmpty(), name + ":" + attr);
                    }
                    assertTrue(RbacDataAttrGroupMetadata.forType(model).getGroupsForAttribute("plain").isEmpty());
                    assertFalse(RbacDataAttrGroupMetadata.forType(model).getGroupsForAttribute("tenantId").isEmpty(), name);
                }
                Class<?> query = loader.loadClass("probe.services.req.QueryContractReq");
                for (String attr : List.of("betweenDay", "gteDay", "lteDay", "day", "inAccount", "notInAccount", "containsLabel", "loadAttachment")) {
                    assertFalse(RbacDataAttrGroupMetadata.forType(query).getGroupsForAttribute(attr).isEmpty(), attr);
                }
                Class<?> info = loader.loadClass("probe.services.info.ContractInfo");
                assertFalse(RbacDataAttrGroupMetadata.forType(info).getGroupsForAttribute("tenantName").isEmpty());
                assertEquals(300, RbacDataAttrGroupMetadata.forType(info).getGroups().get("财务").getConfidentialLevel());
                assertEquals(Set.of("银行账号", "付款"), Arrays.stream(info.getDeclaredField("account").getAnnotationsByType(RbacDataAttrGroup.class)).map(RbacDataAttrGroup::name).collect(java.util.stream.Collectors.toSet()));
                Class<?> update = loader.loadClass("probe.services.req.UpdateContractReq");
                assertFalse(RbacDataAttrGroupMetadata.forType(update).getGroupsForAttribute("account").isEmpty());
            }
        }
    }

    @Test
    void rawRepeatableAnnotationsMustNotBypassNormalizationThroughCopyWhitelist() {
        List<String> previous = new ArrayList<>(ServiceModelCodeGenerator.keepAnnotationList());
        try {
            ServiceModelCodeGenerator.keepAnnotationList("*");
            for (String annotation : List.of("@RbacDataAttrGroup", "@RbacDataAttrGroup(name=\"财务\")",
                    "@RbacDataAttrGroups({})", "@com.levin.commons.rbac.RbacDataAttrGroup$RbacDataAttrGroups({})")) {
                assertFalse(ServiceModelCodeGenerator.isMatch(annotation, "info"), annotation);
            }
        } finally {
            ServiceModelCodeGenerator.keepAnnotationList(previous.toArray(new String[0]));
        }
    }

    @Test
    void unannotatedAndInheritedCommonPropertiesMustUseNormalizedRules() {
        assertTrue(new ClassModel(Plain.class).rbacAnnotations("name").isEmpty());
        ClassModel model = new ClassModel(Tenant.class);
        assertEquals(1, model.rbacInheritedAnnotations(false).size());
        assertTrue(model.rbacInheritedAnnotations(false).get(0).contains("attrList = {\"tenantId\"}"));
        assertTrue(model.rbacInheritedAnnotations(true).get(0).contains("\"tenantName\""));
    }

    static class Plain { String name; }
    static class Tenant implements com.levin.commons.dao.domain.MultiTenantObject {
        @RbacDataAttrGroup @io.swagger.v3.oas.annotations.media.Schema(title="租户身份") String tenantId;
        public <T extends java.io.Serializable> T getTenantId() { return null; }
    }

    private Path source(String name, String content) throws Exception {
        Path path = dir.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path;
    }

    private void compile(List<Path> sources, Path output, String classpath) throws Exception {
        Files.createDirectories(output);
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        try (var manager = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, manager, null, List.of("-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor", "-classpath", classpath, "-d", output.toString()), null, manager.getJavaFileObjectsFromPaths(sources)).call());
        }
    }
}
