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
            assertExtends(output, "SimpleUpdateSampleReq.java", "extends SampleBaseRequest<SimpleUpdateSampleReq>");
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
            assertExtends(output, "SimpleQueryUserOrderReq.java", "extends UserOrderBaseRequest<T>");
            assertExtends(output, "QueryUserOrderReq.java", "extends SimpleQueryUserOrderReq<QueryUserOrderReq>");
            try (var generated = Files.walk(output)) {
                Path baseFile = generated.filter(path -> path.getFileName().toString().equals("UserOrderBaseRequest.java"))
                        .findFirst().orElseThrow();
                String source = Files.readString(baseFile);
                assertTrue(source.contains("@DaoLogicGroup(name = \"visible\", logicOp = DaoLogicGroup.LogicOp.OR,\n"
                        + "        minConditions = 1, unless = \"1=2\", remark = \"用户订单可见范围 @CopyToGenCode @*\")"), source);
                assertEquals(1, source.split("@DaoLogicGroup\\(", -1).length - 1, source);
                assertTrue(source.contains("@In(value = E_UserOrder.afterSalesOrgId, group = \"visible\")"), source);
                assertTrue(source.contains("@Eq(value = E_UserOrder.salesUserId, group = \"visible\")"), source);
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
                        + " protected boolean isNotBlank(Object value) { return value != null; } }"));
        sources.add(write(stubs, "probe/services/commons/req/MultiTenantOrgReq.java",
                "package probe.services.commons.req;"
                        + " public class MultiTenantOrgReq<T extends MultiTenantOrgReq<T>> extends BaseReq {}"));
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
            assertEquals(1, base.getDeclaredAnnotationsByType(com.levin.commons.dao.DaoLogicGroup.class).length);
            List<String> sourceFields = List.of("afterSalesOrgId", "afterSalesUserId",
                    "customerOrgId", "customerUserId", "preSalesOrgId", "preSalesUserId",
                    "salesOrgId", "salesUserId");
            List<String> generatedFields = List.of("inAfterSalesOrgIdList", "eqAfterSalesUserId",
                    "inCustomerOrgIdList", "eqCustomerUserId", "inPreSalesOrgIdList", "eqPreSalesUserId",
                    "inSalesOrgIdList", "eqSalesUserId");
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
                    assertEquals(sourceField, field.getAnnotation(In.class).value());
                    assertEquals("visible", field.getAnnotation(In.class).group());
                } else {
                    assertEquals(String.class, field.getType());
                    assertEquals(sourceField, field.getAnnotation(Eq.class).value());
                    assertEquals("visible", field.getAnnotation(Eq.class).group());
                }
                InjectVar inject = field.getAnnotation(InjectVar.class);
                assertEquals(org ? "accessibleOrgIds" : "currentUserId", inject.value());
                assertEquals("true", inject.isOverride());
                assertEquals("false", inject.isRequired());
                field.setAccessible(true);
                conditions.add(field);
            }

            Map<String, Object> context = Map.of("accessibleOrgIds", List.of("D1", "D2"),
                    "currentUserId", "U1");
            List<String> expected = List.of("D1", "D2", "U1", "D1", "D2", "U1",
                    "D1", "D2", "U1", "D1", "D2", "U1");
            SimpleVariableInjector injector = new SimpleVariableInjector() {};
            for (Class<?> requestType : List.of(simple, query)) {
                Object request = requestType.getConstructor().newInstance();
                for (Field field : conditions) injector.injectValue(request, field, context);
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
                    "currentUserId", "USER-X");
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
                        Map.of("currentUserId", "USER-X"), null));
                assertEquals(userOnly, executeUserOrderQuery(connection, requestType, conditions,
                        Map.of("accessibleOrgIds", List.of(), "currentUserId", "USER-X"), null));
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
        for (Field condition : conditions) injector.injectValue(update, condition, context);
        Field scopeField = base.getDeclaredField("eqSalesUserId");
        scopeField.setAccessible(true);
        assertEquals("U1", scopeField.get(update));
        assertEquals("NEW-U", updateField.get(update));

        UpdateDaoImpl<UserOrder> updateDao = new UpdateDaoImpl<>(miniDao(), false, UserOrder.class, "r");
        updateDao.appendByQueryObj(update);
        updateDao.limit(0, 10);
        List<?> updateParams = updateDao.genFinalParamList();
        String updateSql = updateDao.genFinalStatement();
        assertTrue(updateSql.contains("salesUserId"), updateSql);
        List<String> expectedUpdate = new ArrayList<>();
        expectedUpdate.add("NEW-U");
        expectedUpdate.addAll(expectedScope);
        expectedUpdate.add("ORDER-1");
        assertEquals(expectedUpdate, updateParams.stream().filter(String.class::isInstance).toList());
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
