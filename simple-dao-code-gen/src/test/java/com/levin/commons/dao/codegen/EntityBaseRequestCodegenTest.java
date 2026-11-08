package com.levin.commons.dao.codegen;

import tools.jackson.databind.ObjectMapper;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.support.SelectDaoImpl;
import com.levin.commons.dao.support.UpdateDaoImpl;
import com.levin.commons.dao.support.DeleteDaoImpl;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.SimpleVariableInjector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.tools.ToolProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityBaseRequestCodegenTest {

    @Test
    void generatedRequestsCompileWithActualCommonsBase(@TempDir Path output) throws Exception {
        generateUserOrderSourcesOnly(output);
        Path stub = output.resolve("stubs/probe/services/commons/req/BaseReq.java");
        Files.createDirectories(stub.getParent());
        com.levin.commons.plugins.Utils.copyAndReplace(output.toString(), true,
                "simple.dao/codegen/template/services/commons/req/BaseReq.java", stub.toFile(),
                Map.of("modulePackageName", "probe"));
        List<Path> sources = new ArrayList<>();
        try (var files = Files.walk(output.resolve("services"))) {
            sources.addAll(files.filter(path -> path.toString().endsWith(".java")).toList());
        }
        try (var files = Files.walk(output.resolve("stubs"))) {
            sources.addAll(files.filter(path -> path.toString().endsWith(".java")).toList());
        }
        // Use a distinct package to avoid the DAO's name-keyed reflection cache retaining
        // fields from the earlier test's separate class loader.
        for (Path source : sources) {
            Files.writeString(source, Files.readString(source).replace("probe.", "probe_actual."));
        }
        Path classes = output.resolve("actual-commons-classes");
        Files.createDirectories(classes);
        try (var manager = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null)) {
            assertTrue(ToolProvider.getSystemJavaCompiler().getTask(null, manager, null,
                    List.of("-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                            "-classpath", System.getProperty("java.class.path"), "-d", classes.toString()),
                    null, manager.getJavaFileObjectsFromPaths(sources)).call());
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> requestType = loader.loadClass("probe_actual.services.userorder.req.SimpleUpdateUserOrderReq");
            Class<?> commons = loader.loadClass("probe_actual.services.commons.req.BaseReq");
            Object request = requestType.getConstructor().newInstance();
            var check = commons.getDeclaredMethod("checkUpdateOrgTarget", String.class, String.class, java.util.Collection.class);
            check.setAccessible(true);
            Field unsafe = commons.getDeclaredField("isUnsafeContext");
            unsafe.setAccessible(true);
            Field all = commons.getDeclaredField("isCanAccessAllOrg");
            all.setAccessible(true);
            check.invoke(request, "销售部门", "outside", List.of("allowed"));
            unsafe.setBoolean(request, true);
            check.invoke(request, "销售部门", null, null);
            check.invoke(request, "销售部门", "allowed", List.of("allowed"));
            var denied = assertThrows(java.lang.reflect.InvocationTargetException.class,
                    () -> check.invoke(request, "销售部门", "outside", List.of("allowed")));
            assertTrue(denied.getCause().getMessage().contains("销售部门"));
            all.setBoolean(request, true);
            check.invoke(request, "销售部门", "outside", null);
            verifyActualCommonsUpdates(loader, commons, unsafe);
        }
    }

    /** Generate sources without running the other test or loading its classes. */
    private static void generateUserOrderSourcesOnly(Path output) throws Exception {
        String oldModule = ServiceModelCodeGenerator.moduleName("probe");
        String oldPackage = ServiceModelCodeGenerator.modulePackageName("probe");
        Class<?> oldEntity = ServiceModelCodeGenerator.entityClass(UserOrder.class);
        Field sourceMap = ServiceModelCodeGenerator.class.getDeclaredField("srcFileCompilationMap");
        sourceMap.setAccessible(true);
        Object oldSourceMap = sourceMap.get(null);
        Path sourceRoot = output.resolve("entity-source");
        Path copiedSource = sourceRoot.resolve("com/levin/commons/dao/codegen/UserOrder.java");
        Files.createDirectories(copiedSource.getParent());
        Path original = Path.of("src/test/java/com/levin/commons/dao/codegen/UserOrder.java");
        if (!Files.exists(original)) original = Path.of("simple-dao-code-gen").resolve(original);
        Files.copy(original, copiedSource);
        sourceMap.set(null, ServiceModelCodeGenerator.parseSrcFile(sourceRoot.toFile()));
        try {
            Method buildFields = ServiceModelCodeGenerator.class.getDeclaredMethod("buildFieldModel",
                    Class.class, Map.class, boolean.class, String.class);
            buildFields.setAccessible(true);
            @SuppressWarnings("unchecked") var fields = (List<com.levin.commons.dao.codegen.model.FieldModel>)
                    buildFields.invoke(null, UserOrder.class, new HashMap<>(), false, "info");
            Method buildRequests = ServiceModelCodeGenerator.class.getDeclaredMethod("buildRequests",
                    Class.class, Map.class, String.class, Map.class, List.class);
            buildRequests.setAccessible(true);
            buildRequests.invoke(null, UserOrder.class, new HashMap<>(), output.resolve("services").toString(),
                    new HashMap<>(Map.of("reqExtendClass", "BaseReq")), fields);
            Path stubs = output.resolve("stubs");
            write(stubs, "probe/entities/EntityConst.java", "package probe.entities; public class EntityConst {"
                    + " public static final String CREATE_ACTION=\"创建\", QUERY_ACTION=\"查询\", UPDATE_ACTION=\"更新\", DELETE_ACTION=\"删除\"; }");
            write(stubs, "probe/services/userorder/info/UserOrderInfo.java",
                    "package probe.services.userorder.info; public class UserOrderInfo {}");
        } finally {
            sourceMap.set(null, oldSourceMap);
            ServiceModelCodeGenerator.entityClass(oldEntity);
            ServiceModelCodeGenerator.modulePackageName(oldPackage);
            ServiceModelCodeGenerator.moduleName(oldModule);
        }
    }

    private static void verifyActualCommonsUpdates(ClassLoader loader, Class<?> commons, Field unsafe) throws Exception {
        Class<?> entityBase = loader.loadClass("probe_actual.services.userorder.req.UserOrderBaseRequest");
        Class<?> updateType = loader.loadClass("probe_actual.services.userorder.req.UpdateUserOrderReq");
        Class<?> simpleType = loader.loadClass("probe_actual.services.userorder.req.SimpleUpdateUserOrderReq");
        Class<?> queryType = loader.loadClass("probe_actual.services.userorder.req.QueryUserOrderReq");
        List<Field> scopes = java.util.Arrays.stream(entityBase.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(InjectVar.class)).toList();
        var injector = new SimpleVariableInjector() {};
        Map<String, Object> context = Map.of("accessibleOrgIds", List.of("D1", "D2"), "accessibleOwnerIds", List.of("U1"));
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:actual_" + UUID.randomUUID().toString().replace("-", ""))) {
            try (var sql = connection.createStatement()) {
                sql.execute("CREATE TABLE user_order (id VARCHAR(40) PRIMARY KEY, tenantId VARCHAR(40), salesOrgId VARCHAR(40), salesUserId VARCHAR(40), customerOrgId VARCHAR(40), customerUserId VARCHAR(40), afterSalesOrgId VARCHAR(40), afterSalesUserId VARCHAR(40), preSalesOrgId VARCHAR(40), preSalesUserId VARCHAR(40))");
                sql.execute("INSERT INTO user_order (id,tenantId,salesOrgId) VALUES ('a','A','D1'),('b','B','D1')");
            }
            Object single = new ObjectMapper().readValue("{\"id\":\"a\",\"salesOrgId\":\"D2\"}", updateType);
            injector.injectValue(single, unsafe, Map.of("isUnsafeContext", true));
            assertTrue(unsafe.getBoolean(single));
            injectVisibilityConditions(single, scopes, injector, context);
            UpdateDaoImpl<UserOrder> singleDao = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            singleDao.appendByQueryObj(single).eq("tenantId", "A");
            assertEquals(1, executeGeneratedUpdate(connection, singleDao));

            Object set = new ObjectMapper().readValue("{\"salesOrgId\":\"D2\"}", simpleType);
            Object where = queryType.getConstructor().newInstance();
            injector.injectValue(set, unsafe, Map.of("isUnsafeContext", true));
            injector.injectValue(where, unsafe, Map.of("isUnsafeContext", true));
            injectVisibilityConditions(set, scopes, injector, context);
            injectVisibilityConditions(where, scopes, injector, context);
            UpdateDaoImpl<UserOrder> batch = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            batch.appendByQueryObj(set).appendByQueryObj(where).eq("tenantId", "A");
            assertEquals(1, executeGeneratedUpdate(connection, batch));
            try (var sql = connection.createStatement(); ResultSet rows = sql.executeQuery("SELECT salesOrgId FROM user_order WHERE id='b'")) {
                assertTrue(rows.next());
                assertEquals("D1", rows.getString(1));
            }
            Object outside = new ObjectMapper().readValue("{\"id\":\"a\",\"salesOrgId\":\"outside\"}", updateType);
            injector.injectValue(outside, unsafe, Map.of("isUnsafeContext", true));
            injectVisibilityConditions(outside, scopes, injector, context);
            assertThrows(RuntimeException.class, () -> new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r").appendByQueryObj(outside));

            Object missing = new ObjectMapper().readValue("{\"salesOrgId\":\"D2\"}", simpleType);
            injector.injectValue(missing, unsafe, Map.of("isUnsafeContext", true));
            assertThrows(RuntimeException.class, () -> new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r").appendByQueryObj(missing));
        }
    }

    @Test
    void everyRequestExceptCreateUsesRegeneratedEntityBase(@TempDir Path output) throws Exception {
        String services = output.resolve("services").toString();
        String oldModule = ServiceModelCodeGenerator.moduleName("probe");
        String oldPackage = ServiceModelCodeGenerator.modulePackageName("probe");
        Class<?> oldEntity = ServiceModelCodeGenerator.entityClass(Sample.class);
        Field sourceMap = ServiceModelCodeGenerator.class.getDeclaredField("srcFileCompilationMap");
        sourceMap.setAccessible(true);
        Object oldSourceMap = sourceMap.get(null);
        sourceMap.set(null, new HashMap<>());
        try {
            Method buildFields = ServiceModelCodeGenerator.class.getDeclaredMethod("buildFieldModel",
                    Class.class, java.util.Map.class, boolean.class, String.class);
            buildFields.setAccessible(true);
            @SuppressWarnings("unchecked") var infoFields = (java.util.List<com.levin.commons.dao.codegen.model.FieldModel>)
                    buildFields.invoke(null, Sample.class, new HashMap<>(), false, "info");
            Method buildRequests = ServiceModelCodeGenerator.class.getDeclaredMethod("buildRequests",
                    Class.class, java.util.Map.class, String.class, java.util.Map.class, java.util.List.class);
            buildRequests.setAccessible(true);
            buildRequests.invoke(null, Sample.class, new HashMap<>(), services,
                    new HashMap<>(java.util.Map.of("reqExtendClass", "BaseReq")), infoFields);
            assertExtends(output, "SampleBaseRequest.java", "extends BaseReq");
            assertExtends(output, "SimpleQuerySampleReq.java", "extends SampleBaseRequest<T>");
            assertExtends(output, "QuerySampleReq.java", "extends SimpleQuerySampleReq<QuerySampleReq>");
            assertExtends(output, "SimpleUpdateSampleReq.java", "extends BaseReq");
            assertExtends(output, "UpdateSampleReq.java", "extends SimpleUpdateSampleReq");
            assertExtends(output, "DeleteSampleReq.java", "extends SampleBaseRequest<DeleteSampleReq>");
            assertExtends(output, "SampleIdReq.java", "extends SampleBaseRequest<SampleIdReq>");
            assertExtends(output, "CreateSampleReq.java", "extends BaseReq");
            Path compiled = compileGeneratedRequests(output, services, Sample.class);
            verifyInheritedGroupCondition(compiled);

            Path multi = output.resolve("multi");
            buildRequests.invoke(null, Sample.class, new HashMap<>(), multi.toString(),
                    new HashMap<>(java.util.Map.of("reqExtendClass", "MultiTenantOrgReq<")), infoFields);
            assertExtends(multi, "SampleBaseRequest.java", "extends MultiTenantOrgReq<T>");
            assertExtends(multi, "SimpleQuerySampleReq.java", "extends SampleBaseRequest<T>");
            assertExtends(multi, "CreateSampleReq.java", "extends MultiTenantOrgReq<CreateSampleReq>");
            compileGeneratedRequests(multi, multi.toString(), Sample.class);
        } finally {
            sourceMap.set(null, oldSourceMap);
            ServiceModelCodeGenerator.entityClass(oldEntity);
            ServiceModelCodeGenerator.modulePackageName(oldPackage);
            ServiceModelCodeGenerator.moduleName(oldModule);
        }
    }

    @Test
    void userOrderGenerationPlacesAllEightScopeFieldsInBaseRequest(@TempDir Path output) throws Exception {
        String services = output.resolve("services").toString();
        String oldModule = ServiceModelCodeGenerator.moduleName("probe");
        String oldPackage = ServiceModelCodeGenerator.modulePackageName("probe");
        Class<?> oldEntity = ServiceModelCodeGenerator.entityClass(UserOrder.class);
        Field sourceMap = ServiceModelCodeGenerator.class.getDeclaredField("srcFileCompilationMap");
        sourceMap.setAccessible(true);
        Object oldSourceMap = sourceMap.get(null);
        Path sourceRoot = output.resolve("entity-source");
        Path copiedSource = sourceRoot.resolve("com/levin/commons/dao/codegen/UserOrder.java");
        Files.createDirectories(copiedSource.getParent());
        Path userOrderSource = Path.of("src/test/java/com/levin/commons/dao/codegen/UserOrder.java");
        if (!Files.exists(userOrderSource))
            userOrderSource = Path.of("simple-dao-code-gen").resolve(userOrderSource);
        Files.copy(userOrderSource, copiedSource);
        sourceMap.set(null, ServiceModelCodeGenerator.parseSrcFile(sourceRoot.toFile()));
        try {
            Method buildFields = ServiceModelCodeGenerator.class.getDeclaredMethod("buildFieldModel",
                    Class.class, java.util.Map.class, boolean.class, String.class);
            buildFields.setAccessible(true);
            @SuppressWarnings("unchecked") var infoFields = (List<com.levin.commons.dao.codegen.model.FieldModel>)
                    buildFields.invoke(null, UserOrder.class, new HashMap<>(), false, "info");
            Method buildRequests = ServiceModelCodeGenerator.class.getDeclaredMethod("buildRequests",
                    Class.class, java.util.Map.class, String.class, java.util.Map.class, java.util.List.class);
            buildRequests.setAccessible(true);
            buildRequests.invoke(null, UserOrder.class, new HashMap<>(), services,
                    new HashMap<>(Map.of("reqExtendClass", "BaseReq")), infoFields);
            assertExtends(output, "UserOrderBaseRequest.java", "extends BaseReq");
            assertExtends(output, "SimpleUpdateUserOrderReq.java", "extends UserOrderBaseRequest<SimpleUpdateUserOrderReq>");
            assertExtends(output, "SimpleQueryUserOrderReq.java", "extends UserOrderBaseRequest<T>");
            assertExtends(output, "QueryUserOrderReq.java", "extends SimpleQueryUserOrderReq<QueryUserOrderReq>");
            try (var generated = Files.walk(output)) {
                Path baseFile = generated.filter(path -> path.getFileName().toString().equals("UserOrderBaseRequest.java"))
                        .findFirst().orElseThrow();
                String source = Files.readString(baseFile);
                assertFalse(source.contains("@DaoLogicGroup("), source);
                assertTrue(source.contains("checkAfterSalesOrgId"), source);
            }
            Path review = Path.of("target/generated-scope-review");
            Files.createDirectories(review);
            try (var generated = Files.walk(Path.of(services))) {
                for (Path file : generated.filter(path -> path.getFileName().toString().matches(
                        "(UserOrderBaseRequest|SimpleUpdateUserOrderReq|UpdateUserOrderReq|QueryUserOrderReq)\\.java"))
                        .toList()) {
                    Files.copy(file, review.resolve(file.getFileName()),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
            verifyUserOrderConditions(compileGeneratedRequests(output, services, UserOrder.class));
        } finally {
            sourceMap.set(null, oldSourceMap);
            ServiceModelCodeGenerator.entityClass(oldEntity);
            ServiceModelCodeGenerator.modulePackageName(oldPackage);
            ServiceModelCodeGenerator.moduleName(oldModule);
        }
    }

    private static void assertExtends(Path output, String filename, String declaration) throws Exception {
        try (var files = Files.walk(output)) {
            Path file = files.filter(path -> path.getFileName().toString().equals(filename)).findFirst().orElseThrow();
            assertTrue(Files.readString(file).contains(declaration), file + " should contain " + declaration);
        }
    }

    private static Path compileGeneratedRequests(Path output, String services, Class<?> entity) throws Exception {
        Path stubs = output.resolve("stubs");
        List<Path> sources = new ArrayList<>();
        sources.add(write(stubs, "probe/entities/EntityConst.java",
                "package probe.entities; public class EntityConst {"
                        + " public static final String CREATE_ACTION=\"创建\", QUERY_ACTION=\"查询\","
                        + " UPDATE_ACTION=\"更新\", DELETE_ACTION=\"删除\""
                        + (entity == Sample.class ? ", BIZ_NAME=\"样例\"" : "") + "; }"));
        sources.add(write(stubs, "probe/services/commons/req/BaseReq.java",
                "package probe.services.commons.req; public class BaseReq implements java.io.Serializable {"
                        + " protected <T> T checkSQLInject(Object... values) { return (T)this; }"
                        + " protected boolean isNotBlank(Object value) { return value != null; }"
                        + " protected void checkUpdateOrgTarget(String title, String value, java.util.Collection<?> values) { if (!isUnsafeContext() || isCanAccessAllOrg() || value == null || value.isBlank()) return; if (values == null || !values.contains(value)) throw new IllegalArgumentException(\"更新 \"+title+\"：组织ID不在授权范围内\"); }"
                        + " public boolean isUnsafeContext() { return true; }"
                        + " @com.levin.commons.dao.annotation.Ignore public boolean admin;"
                        + " public boolean isAdmin() { return admin; }"
                        + " @com.levin.commons.dao.annotation.Ignore public boolean allOrg;"
                        + " public boolean isCanAccessAllOrg() { return allOrg; }"
                        + " public String get_currentUserId() { return \"U1\"; }"
                        + " protected void checkUpdateOwnerTarget(String title, String ownerId) { if (!isUnsafeContext() || isAdmin() || ownerId == null || ownerId.isBlank()) return; if (get_currentUserId() == null || get_currentUserId().isBlank()) throw new IllegalArgumentException(\"更新 \" + title + \"：缺少当前用户ID\"); }"
                        + " @com.levin.commons.dao.CtxVar(varName=\"_req\")"
                        + " public BaseReq getRequestContext() { return this; }"
                        + " public boolean isForceUpdateField(String name) { return false; } }"));
        sources.add(write(stubs, "probe/services/commons/req/MultiTenantOrgReq.java",
                "package probe.services.commons.req;"
                        + " public class MultiTenantOrgReq<T extends MultiTenantOrgReq<T>> extends BaseReq {"
                        + " protected java.util.Collection<String> orgIdList = java.util.List.of(\"D1\",\"D2\");"
                        + " protected String orgId;"
                        + " protected void checkOrgScopeParam() {}"
                        + " public boolean isCanAccessAllOrg() { return false; } }"));
        String entityName = entity.getSimpleName();
        String serviceName = entityName.toLowerCase(Locale.ROOT);
        sources.add(write(stubs, "probe/services/" + serviceName + "/info/" + entityName + "Info.java",
                "package probe.services." + serviceName + ".info; public class " + entityName + "Info {}"));
        try (var generated = Files.walk(Path.of(services))) {
            sources.addAll(generated.filter(path -> path.toString().endsWith(".java")).toList());
        }
        Path compiled = output.resolve("compiled");
        Files.createDirectories(compiled);
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        try (var manager = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, manager, null,
                    List.of("-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                            "-classpath", System.getProperty("java.class.path"),
                            "-d", compiled.toString()), null,
                    manager.getJavaFileObjectsFromPaths(sources)).call());
        }
        return compiled;
    }

    private static void verifyInheritedGroupCondition(Path compiled) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[]{compiled.toUri().toURL()},
                EntityBaseRequestCodegenTest.class.getClassLoader())) {
            Class<?> simple = loader.loadClass("probe.services.sample.req.SimpleQuerySampleReq");
            Class<?> query = loader.loadClass("probe.services.sample.req.QuerySampleReq");
            Class<?> base = loader.loadClass("probe.services.sample.req.SampleBaseRequest");
            Field condition = base.getDeclaredField("eqName");
            assertThrows(NoSuchFieldException.class, () -> simple.getDeclaredField("eqName"));
            assertThrows(NoSuchFieldException.class, () -> query.getDeclaredField("eqName"));
            assertEquals("name", condition.getAnnotation(Eq.class).value());
            assertEquals("visible", condition.getAnnotation(Eq.class).group());
            InjectVar injection = condition.getAnnotation(InjectVar.class);
            assertEquals("name", injection.value());
            assertEquals("true", injection.isOverride());
            assertEquals("false", injection.isRequired());
            assertEquals(1, com.levin.commons.dao.util.QueryAnnotationUtil.getNonStaticFields(query).stream()
                    .filter(field -> field.getName().equals("eqName")).count());

            condition.setAccessible(true);
            SimpleVariableInjector injector = new SimpleVariableInjector() {};
            for (Class<?> requestType : List.of(simple, query)) {
                Object request = requestType.getConstructor().newInstance();
                injector.injectValue(request, condition, Map.of("name", "chosen"));
                assertEquals("chosen", condition.get(request));
                SelectDaoImpl<Sample> dao = new SelectDaoImpl<>(miniDao(), false, Sample.class, "r");
                dao.appendByQueryObj(request);
                List<?> params = dao.genFinalParamList();
                String statement = dao.genFinalStatement();
                assertTrue(statement.contains("r.name ="), statement);
                assertEquals(List.of("chosen"), params.stream().filter(String.class::isInstance).toList());
                assertEquals(params, dao.genFinalParamList());

                Object emptyRequest = requestType.getConstructor().newInstance();
                SelectDaoImpl<Sample> emptyDao = new SelectDaoImpl<>(miniDao(), false, Sample.class, "r");
                emptyDao.appendByQueryObj(emptyRequest);
                assertTrue(emptyDao.genFinalStatement().contains("1=2"));
                assertTrue(emptyDao.genFinalParamList().stream().noneMatch(String.class::isInstance));
            }
        }
    }

    private static void verifyUserOrderConditions(Path compiled) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[]{compiled.toUri().toURL()},
                EntityBaseRequestCodegenTest.class.getClassLoader())) {
            Class<?> base = loader.loadClass("probe.services.userorder.req.UserOrderBaseRequest");
            Class<?> simple = loader.loadClass("probe.services.userorder.req.SimpleQueryUserOrderReq");
            Class<?> query = loader.loadClass("probe.services.userorder.req.QueryUserOrderReq");
            assertEquals(0, base.getDeclaredAnnotationsByType(com.levin.commons.dao.DaoLogicGroup.class).length);
            assertEquals(1, simple.getDeclaredAnnotationsByType(com.levin.commons.dao.DaoLogicGroup.class).length);
            List<String> sourceFields = List.of("afterSalesOrgId", "afterSalesUserId",
                    "customerOrgId", "customerUserId", "preSalesOrgId", "preSalesUserId",
                    "salesOrgId", "salesUserId");
            List<String> generatedFields = List.of("inAfterSalesOrgIdList", "inAfterSalesUserIdList",
                    "inCustomerOrgIdList", "inCustomerUserIdList", "inPreSalesOrgIdList", "inPreSalesUserIdList",
                    "inSalesOrgIdList", "inSalesUserIdList");
            List<Field> conditions = new ArrayList<>();
            for (int index = 0; index < sourceFields.size(); index++) {
                String sourceField = sourceFields.get(index);
                boolean org = sourceField.endsWith("OrgId");
                String generatedName = generatedFields.get(index);
                Field field = base.getDeclaredField(generatedName);
                assertThrows(NoSuchFieldException.class, () -> simple.getDeclaredField(generatedName));
                assertThrows(NoSuchFieldException.class, () -> query.getDeclaredField(generatedName));
                assertEquals(1, com.levin.commons.dao.util.QueryAnnotationUtil.getNonStaticFields(query).stream()
                        .filter(candidate -> candidate.getName().equals(generatedName)).count());
                if (org) {
                    assertEquals(List.class, field.getType());
                    assertEquals(sourceField, simple.getDeclaredField("scopeAlias" + Character.toUpperCase(generatedName.charAt(0)) + generatedName.substring(1)).getAnnotation(In.class).value());
                } else {
                    assertEquals(List.class, field.getType());
                    assertEquals(sourceField, simple.getDeclaredField("scopeAlias" + Character.toUpperCase(generatedName.charAt(0)) + generatedName.substring(1)).getAnnotation(In.class).value());
                }
                InjectVar inject = field.getAnnotation(InjectVar.class);
                assertEquals(org ? "accessibleOrgIds" : "accessibleOwnerIds", inject.value());
                assertEquals("true", inject.isOverride());
                assertEquals("false", inject.isRequired());
                field.setAccessible(true);
                conditions.add(field);
            }

            Map<String, Object> context = Map.of("accessibleOrgIds", List.of("D1", "D2"),
                    "accessibleOwnerIds", List.of("U1"));
            List<String> expected = List.of("D1", "D2", "U1", "D1", "D2", "U1",
                    "D1", "D2", "U1", "D1", "D2", "U1");
            SimpleVariableInjector injector = new SimpleVariableInjector() {};
            for (Class<?> requestType : List.of(simple, query)) {
                Object request = requestType.getConstructor().newInstance();
                conditions.get(0).set(request, List.of("FORGED"));
                for (Field field : conditions) injector.injectValue(request, field, context);
                assertEquals(List.of("D1", "D2"), conditions.get(0).get(request));
                SelectDaoImpl<UserOrder> dao = new SelectDaoImpl<>(miniDao(), false, UserOrder.class, "r");
                dao.appendByQueryObj(request);
                List<?> params = dao.genFinalParamList();
                String statement = dao.genFinalStatement();
                assertEquals(expected, params.stream().filter(String.class::isInstance).toList());
                int previous = -1;
                for (String field : sourceFields) {
                    int position = statement.indexOf("r." + field);
                    assertTrue(position > previous, statement);
                    previous = position;
                }
                assertEquals(params, dao.genFinalParamList());

                SelectDaoImpl<UserOrder> empty = new SelectDaoImpl<>(miniDao(), false, UserOrder.class, "r");
                empty.appendByQueryObj(requestType.getConstructor().newInstance());
                assertTrue(empty.genFinalStatement().contains("1=2"));
                assertTrue(empty.genFinalParamList().stream().noneMatch(String.class::isInstance));
            }
            verifyUserOrderMutations(loader, base, conditions, context, expected);
            verifyGeneratedUserOrderSql(simple, query, conditions, sourceFields);
        }
    }

    private static void verifyGeneratedUserOrderSql(Class<?> simple, Class<?> query,
                                                     List<Field> conditions, List<String> columns) throws Exception {
        String dbName = "user_order_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName)) {
            String definitions = String.join(", ", columns.stream().map(column -> column + " VARCHAR(40)").toList());
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE TABLE user_order (id VARCHAR(40) PRIMARY KEY, " + definitions + ")");
            }
            String insertSql = "INSERT INTO user_order (id, " + String.join(", ", columns) + ") VALUES ("
                    + String.join(", ", Collections.nCopies(columns.size() + 1, "?")) + ")";
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                for (int row = 0; row <= columns.size(); row++) {
                    insert.setString(1, row == columns.size() ? "outside" : "case-" + row);
                    for (int column = 0; column < columns.size(); column++) {
                        String value = row == column ? (column % 2 == 0 ? "ORG-B" : "USER-X") : "OUTSIDE";
                        insert.setString(column + 2, value);
                    }
                    insert.addBatch();
                }
                insert.executeBatch();
            }

            Map<String, Object> both = Map.of("accessibleOrgIds", List.of("ORG-A", "ORG-B"),
                    "accessibleOwnerIds", List.of("USER-X"));
            Set<String> all = Set.of("case-0", "case-1", "case-2", "case-3",
                    "case-4", "case-5", "case-6", "case-7");
            Set<String> orgOnly = Set.of("case-0", "case-2", "case-4", "case-6");
            Set<String> userOnly = Set.of("case-1", "case-3", "case-5", "case-7");
            for (Class<?> requestType : List.of(simple, query)) {
                assertEquals(all, executeUserOrderQuery(connection, requestType, conditions, both, null));
                assertEquals(orgOnly, executeUserOrderQuery(connection, requestType, conditions,
                        Map.of("accessibleOrgIds", List.of("ORG-A", "ORG-B")), null));
                assertEquals(orgOnly, executeUserOrderQuery(connection, requestType, conditions,
                        Map.of("accessibleOrgIds", List.of("ORG-B")), null));
                assertEquals(userOnly, executeUserOrderQuery(connection, requestType, conditions,
                        Map.of("accessibleOwnerIds", List.of("USER-X")), null));
                assertEquals(userOnly, executeUserOrderQuery(connection, requestType, conditions,
                        Map.of("accessibleOrgIds", List.of(), "accessibleOwnerIds", List.of("USER-X")), null));
                assertTrue(executeUserOrderQuery(connection, requestType, conditions, Map.of(), null).isEmpty());
                assertTrue(executeUserOrderQuery(connection, requestType, conditions,
                        Map.of("accessibleOrgIds", List.of()), null).isEmpty());
                assertEquals(Set.of("case-0"), executeUserOrderQuery(connection, requestType, conditions, both, "case-0"));
                assertTrue(executeUserOrderQuery(connection, requestType, conditions, both, "outside").isEmpty());
            }
        }
    }

    private static Set<String> executeUserOrderQuery(Connection connection, Class<?> requestType,
                                                      List<Field> conditions, Map<String, Object> context,
                                                      String id) throws Exception {
        Object request = requestType.getConstructor().newInstance();
        SimpleVariableInjector injector = new SimpleVariableInjector() {};
        for (Field condition : conditions) injector.injectValue(request, condition, context);
        SelectDaoImpl<UserOrder> dao = new SelectDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
        dao.setCanChangeNativeQL(false);
        dao.appendByQueryObj(request);
        if (id != null) dao.eq("id", id);
        List<?> params = dao.genFinalParamList();
        String sql = dao.genFinalStatement();
        List<String> bound = params.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        assertEquals(sql.chars().filter(ch -> ch == '?').count(), bound.size(), sql);
        assertEquals(params, dao.genFinalParamList());
        Set<String> result = new LinkedHashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < bound.size(); i++) statement.setString(i + 1, bound.get(i));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(rows.getString("id"));
            }
        }
        return result;
    }

    private static MiniDao nativeMiniDao() {
        return (MiniDao) Proxy.newProxyInstance(EntityBaseRequestCodegenTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getParamPlaceholder")) return "?";
                    if (method.getName().equals("getSafeModeMaxLimit")) return 10;
                    if (method.getName().equals("getNamingStrategy"))
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    if (method.getName().equals("getTableName")) return "user_order";
                    if (method.getName().equals("getColumnName")) return args[1];
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
    }

    private static void verifyUserOrderMutations(ClassLoader loader, Class<?> base, List<Field> conditions,
                                                 Map<String, Object> context, List<String> expectedScope) throws Exception {
        Class<?> updateType = loader.loadClass("probe.services.userorder.req.UpdateUserOrderReq");
        Class<?> deleteType = loader.loadClass("probe.services.userorder.req.DeleteUserOrderReq");
        Class<?> idType = loader.loadClass("probe.services.userorder.req.UserOrderIdReq");
        Class<?> createType = loader.loadClass("probe.services.userorder.req.CreateUserOrderReq");
        assertTrue(base.isAssignableFrom(updateType));
        assertEquals(1, updateType.getDeclaredAnnotationsByType(com.levin.commons.dao.DaoLogicGroup.class).length);
        assertTrue(base.isAssignableFrom(deleteType));
        assertTrue(base.isAssignableFrom(idType));
        assertFalse(base.isAssignableFrom(createType));

        SimpleVariableInjector injector = new SimpleVariableInjector() {};
        Object idRequest = idType.getConstructor(String.class).newInstance("ORDER-1");
        for (Field condition : conditions) injector.injectValue(idRequest, condition, context);
        SelectDaoImpl<UserOrder> idDao = new SelectDaoImpl<>(miniDao(), false, UserOrder.class, "r");
        idDao.appendByQueryObj(idRequest);
        List<?> idParams = idDao.genFinalParamList();
        String idSql = idDao.genFinalStatement();
        assertTrue(idSql.contains("salesUserId"), idSql);
        List<String> expectedId = new ArrayList<>(expectedScope);
        expectedId.add("ORDER-1");
        assertEquals(expectedId, idParams.stream().filter(String.class::isInstance).toList());
        assertEquals(idParams, idDao.genFinalParamList());

        Object update = new ObjectMapper().readValue("{\"id\":\"ORDER-1\",\"salesUserId\":\"NEW-U\"}", updateType);
        Field updateField = updateType.getSuperclass().getDeclaredField("salesUserId");
        updateField.setAccessible(true);
        assertEquals("NEW-U", updateField.get(update));
        injectVisibilityConditions(update, conditions, injector, context);
        injectUpdateGuards(update, injector, context);
        Field scopeField = base.getDeclaredField("inSalesUserIdList");
        scopeField.setAccessible(true);
        assertEquals(List.of("U1"), scopeField.get(update));
        assertEquals("NEW-U", updateField.get(update));

        UpdateDaoImpl<UserOrder> updateDao = new UpdateDaoImpl<>(miniDao(), false, UserOrder.class, "r");
        updateDao.appendByQueryObj(update);
        updateDao.limit(0, 10);
        List<?> updateParams = updateDao.genFinalParamList();
        String updateSql = updateDao.genFinalStatement();
        assertTrue(updateSql.contains("salesUserId"), updateSql);
        List<?> boundUpdate = updateParams.stream().filter(String.class::isInstance).toList();
        assertTrue(boundUpdate.containsAll(List.of("NEW-U", "U1", "ORDER-1")), boundUpdate.toString());
        assertEquals(updateParams, updateDao.genFinalParamList());

        Object delete = deleteType.getConstructor().newInstance();
        deleteType.getMethod("setIdList", String[].class).invoke(delete, (Object) new String[]{"ORDER-1"});
        for (Field condition : conditions) injector.injectValue(delete, condition, context);
        DeleteDaoImpl<UserOrder> deleteDao = new DeleteDaoImpl<>(miniDao(), false, UserOrder.class, "r");
        deleteDao.appendByQueryObj(delete);
        deleteDao.limit(0, 10);
        List<?> deleteParams = deleteDao.genFinalParamList(false);
        Method physicalDelete = DeleteDaoImpl.class.getDeclaredMethod("genFinalStatement", boolean.class);
        physicalDelete.setAccessible(true);
        String deleteSql = (String) physicalDelete.invoke(deleteDao, false);
        assertTrue(deleteSql.contains("salesUserId"), deleteSql);
        List<String> expectedDelete = new ArrayList<>(expectedScope);
        expectedDelete.add("ORDER-1");
        assertEquals(expectedDelete, deleteParams.stream().filter(String.class::isInstance).toList());
        assertEquals(deleteParams, deleteDao.genFinalParamList(false));

        verifyGeneratedUpdatesOnH2(loader, updateType, conditions, context);
    }

    private static void verifyGeneratedUpdatesOnH2(ClassLoader loader, Class<?> updateType,
                                                   List<Field> conditions, Map<String, Object> context) throws Exception {
        Class<?> simpleType = loader.loadClass("probe.services.userorder.req.SimpleUpdateUserOrderReq");
        Class<?> queryType = loader.loadClass("probe.services.userorder.req.QueryUserOrderReq");
        String dbName = "user_order_update_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName)) {
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE TABLE user_order (id VARCHAR(40) PRIMARY KEY, "
                        + "salesOrgId VARCHAR(40), salesUserId VARCHAR(40), "
                        + "customerOrgId VARCHAR(40), customerUserId VARCHAR(40), "
                        + "afterSalesOrgId VARCHAR(40), afterSalesUserId VARCHAR(40), "
                        + "preSalesOrgId VARCHAR(40), preSalesUserId VARCHAR(40))");
                statement.execute("INSERT INTO user_order (id,salesOrgId,salesUserId) "
                        + "VALUES ('own','OTHER','U1'),('foreign','D1','OTHER')");
            }
            SimpleVariableInjector injector = new SimpleVariableInjector() {};
            for (String id : List.of("own", "foreign")) {
                Object request = new ObjectMapper().readValue(
                        "{\"id\":\"" + id + "\",\"salesUserId\":\"NEW-U\"}", updateType);
                injectVisibilityConditions(request, conditions, injector, context);
                injectUpdateGuards(request, injector, context);
                UpdateDaoImpl<UserOrder> builder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
                builder.appendByQueryObj(request);
                assertEquals(id.equals("own") ? 1 : 0, executeGeneratedUpdate(connection, builder), id);
            }
            try (var statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT salesUserId FROM user_order WHERE id='foreign'")) {
                assertTrue(rows.next());
                assertEquals("OTHER", rows.getString(1));
            }

            try (var statement = connection.createStatement()) {
                statement.execute("UPDATE user_order SET salesUserId='U1' WHERE id='own'");
            }
            Object setRequest = new ObjectMapper().readValue("{\"salesUserId\":\"BATCH-U\"}", simpleType);
            injectUpdateGuards(setRequest, injector, context);
            Object whereRequest = queryType.getConstructor().newInstance();
            for (Field condition : conditions) injector.injectValue(whereRequest, condition, context);
            Field salesScope = conditions.stream().filter(f -> f.getName().equals("inSalesOrgIdList")).findFirst().orElseThrow();
            Field customerScope = conditions.stream().filter(f -> f.getName().equals("inCustomerOrgIdList")).findFirst().orElseThrow();
            salesScope.set(whereRequest, List.of("SALES"));
            customerScope.set(whereRequest, List.of("CUSTOMER"));
            var salesCheck = salesScope.getDeclaringClass().getDeclaredMethod("checkSalesOrgId", String.class);
            var customerCheck = customerScope.getDeclaringClass().getDeclaredMethod("checkCustomerOrgId", String.class);
            salesCheck.setAccessible(true);
            customerCheck.setAccessible(true);
            salesCheck.invoke(whereRequest, "SALES");
            customerCheck.invoke(whereRequest, "CUSTOMER");
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> salesCheck.invoke(whereRequest, "CUSTOMER"));
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> customerCheck.invoke(whereRequest, "SALES"));
            for (Field condition : conditions) injector.injectValue(whereRequest, condition, context);
            Field idList = queryType.getDeclaredField("idList");
            idList.setAccessible(true);
            idList.set(whereRequest, List.of("own", "foreign"));
            injectVisibilityConditions(setRequest, conditions, injector, context);
            UpdateDaoImpl<UserOrder> batch = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            batch.appendByQueryObj(setRequest).appendByQueryObj(whereRequest);
            assertEquals(1, executeGeneratedUpdate(connection, batch));
            try (var statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT id,salesUserId FROM user_order ORDER BY id")) {
                assertTrue(rows.next());
                assertEquals("foreign", rows.getString(1));
                assertEquals("OTHER", rows.getString(2));
                assertTrue(rows.next());
                assertEquals("own", rows.getString(1));
                assertEquals("BATCH-U", rows.getString(2));
            }

            Object orgSet = new ObjectMapper().readValue("{\"salesOrgId\":\"D2\"}", simpleType);
            injectUpdateGuards(orgSet, injector, context);
            UpdateDaoImpl<UserOrder> noDelegate = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            assertThrows(RuntimeException.class, () -> noDelegate.appendByQueryObj(orgSet), "未绑定组织范围必须拒绝");
            injectVisibilityConditions(orgSet, conditions, injector, context);
            UpdateDaoImpl<UserOrder> orgBatch = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            orgBatch.appendByQueryObj(orgSet).appendByQueryObj(whereRequest);
            assertEquals(1, executeGeneratedUpdate(connection, orgBatch));
            try (var statement = connection.createStatement()) { statement.execute("UPDATE user_order SET salesOrgId='D1' WHERE id='own'"); }
            Object clearOrg = updateType.getConstructor(String.class, boolean.class).newInstance("own", true);
            simpleType.getMethod("setSalesOrgId", String.class).invoke(clearOrg, (Object) null);
            injectVisibilityConditions(clearOrg, conditions, injector, context);
            injectUpdateGuards(clearOrg, injector, context);
            UpdateDaoImpl<UserOrder> clearBuilder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            clearBuilder.appendByQueryObj(clearOrg);
            assertEquals(1, executeGeneratedUpdate(connection, clearBuilder));

            try (var statement = connection.createStatement()) {
                statement.execute("INSERT INTO user_order (id,salesOrgId,customerOrgId,salesUserId) "
                        + "VALUES ('two-orgs','D1','OTHER','U1')");
            }
            Object mixedOldScope = new ObjectMapper().readValue("{\"id\":\"two-orgs\","
                    + "\"salesOrgId\":\"D2\",\"customerOrgId\":\"D2\"}", updateType);
            injectVisibilityConditions(mixedOldScope, conditions, injector, context);
            injectUpdateGuards(mixedOldScope, injector, context);
            UpdateDaoImpl<UserOrder> mixedOldBuilder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            mixedOldBuilder.appendByQueryObj(mixedOldScope);
            assertEquals(1, executeGeneratedUpdate(connection, mixedOldBuilder),
                    "目标组织在可信范围内时允许更新");

            try (var statement = connection.createStatement()) {
                statement.execute("UPDATE user_order SET customerOrgId='D1' WHERE id='two-orgs'");
            }
            Object bothAuthorized = new ObjectMapper().readValue("{\"id\":\"two-orgs\","
                    + "\"salesOrgId\":\"D2\",\"customerOrgId\":\"D2\"}", updateType);
            injectVisibilityConditions(bothAuthorized, conditions, injector, context);
            injectUpdateGuards(bothAuthorized, injector, context);
            UpdateDaoImpl<UserOrder> bothBuilder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            bothBuilder.appendByQueryObj(bothAuthorized);
            assertEquals(1, executeGeneratedUpdate(connection, bothBuilder));
            try (var statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT salesOrgId,customerOrgId FROM user_order WHERE id='two-orgs'")) {
                assertTrue(rows.next());
                assertEquals("D2", rows.getString(1));
                assertEquals("D2", rows.getString(2));
            }

            Object deniedNewOrg = new ObjectMapper().readValue("{\"id\":\"two-orgs\","
                    + "\"salesOrgId\":\"OUTSIDE\"}", updateType);
            injectVisibilityConditions(deniedNewOrg, conditions, injector, context);
            injectUpdateGuards(deniedNewOrg, injector, context);
            UpdateDaoImpl<UserOrder> deniedBuilder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            assertThrows(RuntimeException.class, () -> deniedBuilder.appendByQueryObj(deniedNewOrg),
                    "新组织不能超出可信组织范围");

            assertThrows(NoSuchFieldException.class, () -> simpleType.getDeclaredField("oldOrgGuardSalesOrgId"));
            Class<?> entityBase = simpleType.getSuperclass();
            assertThrows(NoSuchMethodException.class,
                    () -> entityBase.getDeclaredMethod("checkUpdateOwnerTarget", String.class, String.class));
            var ownerCheck = entityBase.getSuperclass().getDeclaredMethod("checkUpdateOwnerTarget", String.class, String.class);
            assertTrue(java.lang.reflect.Modifier.isProtected(ownerCheck.getModifiers()));
            var check = salesScope.getDeclaringClass().getDeclaredMethod("checkSalesOrgId", String.class);
            check.setAccessible(true);
            assertTrue(java.lang.reflect.Modifier.isProtected(check.getModifiers()));
            check.invoke(whereRequest, (Object) null);
            var denied = assertThrows(java.lang.reflect.InvocationTargetException.class,
                    () -> check.invoke(whereRequest, "OUTSIDE"));
            assertEquals("更新 销售部门ID：组织ID不在授权范围内", denied.getCause().getMessage());


            Map<String, Object> personalView = Map.of("accessibleOrgIds", List.of(),
                    "accessibleOwnerIds", List.of("OTHER"));
            Object viewer = new ObjectMapper().readValue(
                    "{\"id\":\"foreign\",\"salesUserId\":\"VIEWER-U\"}", updateType);
            injectVisibilityConditions(viewer, conditions, injector, personalView);
            injectUpdateGuards(viewer, injector, personalView);
            UpdateDaoImpl<UserOrder> viewBuilder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            viewBuilder.appendByQueryObj(viewer);
            assertEquals(0, executeGeneratedUpdate(connection, viewBuilder),
                    "仅有个人查看范围不能转交他人的负责人字段");

            Object administrator = new ObjectMapper().readValue(
                    "{\"id\":\"foreign\",\"salesUserId\":\"ADMIN-U\"}", updateType);
            injectVisibilityConditions(administrator, conditions, injector, context);
            injectUpdateGuards(administrator, injector, context);
            loader.loadClass("probe.services.commons.req.BaseReq").getField("admin")
                    .setBoolean(administrator, true);
            UpdateDaoImpl<UserOrder> adminBuilder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
            adminBuilder.appendByQueryObj(administrator);
            assertEquals(1, executeGeneratedUpdate(connection, adminBuilder),
                    "管理员可转交当前不属于自己的负责人字段");

            try (var statement = connection.createStatement()) {
                statement.execute("INSERT INTO user_order (id,salesUserId) VALUES ('no-org','OTHER')");
            }
            for (String id : List.of("foreign", "no-org")) {
                Object allOrgRequest = new ObjectMapper().readValue(
                        "{\"id\":\"" + id + "\",\"salesOrgId\":\"ANY-ORG\"}", updateType);
                injectVisibilityConditions(allOrgRequest, conditions, injector, Map.of());
                injectUpdateGuards(allOrgRequest, injector, Map.of());
                loader.loadClass("probe.services.commons.req.BaseReq").getField("allOrg")
                        .setBoolean(allOrgRequest, true);
                UpdateDaoImpl<UserOrder> allOrgBuilder = new UpdateDaoImpl<>(nativeMiniDao(), true, UserOrder.class, "r");
                allOrgBuilder.setContext(new HashMap<>(Map.of("isCanAccessAllOrg", true)));
                allOrgBuilder.appendByQueryObj(allOrgRequest);
                assertEquals(1, executeGeneratedUpdate(connection, allOrgBuilder), id);
            }
        }
    }

    private static int executeGeneratedUpdate(Connection connection, UpdateDaoImpl<UserOrder> builder) throws Exception {
        builder.limit(0, 10);
        String sql = builder.genFinalStatement();
        List<String> values = builder.genFinalParamList().stream()
                .filter(value -> value == null || value instanceof String).map(value -> (String) value).toList();
        assertEquals(sql.chars().filter(ch -> ch == '?').count(), values.size(), sql);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.size(); index++) statement.setString(index + 1, values.get(index));
            return statement.executeUpdate();
        }
    }

    private static void injectUpdateGuards(Object request, SimpleVariableInjector injector,
                                           Map<String, Object> context) throws Exception {
        Map<String, Object> trusted = new HashMap<>(context);
        trusted.put("_currentUserId", "U1");
        for (Class<?> type = request.getClass(); type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getName().startsWith("oldOrgGuard") || field.getName().startsWith("oldOwnerGuard")) {
                    injector.injectValue(request, field, trusted);
                }
            }
        }
    }

    private static void injectVisibilityConditions(Object request, List<Field> conditions,
                                                   SimpleVariableInjector injector,
                                                   Map<String, Object> context) throws Exception {
        for (Field condition : conditions) {
            Field target = condition.getDeclaringClass().isInstance(request)
                    ? condition : request.getClass().getDeclaredField(condition.getName());
            injector.injectValue(request, target, context);
        }
    }

    private static MiniDao miniDao() {
        return (MiniDao) Proxy.newProxyInstance(EntityBaseRequestCodegenTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getParamPlaceholder")) return ":?";
                    if (method.getName().equals("getSafeModeMaxLimit")) return 10;
                    if (method.getName().equals("getNamingStrategy"))
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
    }

    private static Path write(Path root, String name, String content) throws Exception {
        Path file = root.resolve(name);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return file;
    }
}
