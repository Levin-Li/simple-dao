package com.levin.commons.dao.codegen;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.support.UpdateDaoImpl;
import com.levin.commons.dao.support.SelectDaoImpl;
import com.levin.commons.dao.support.DeleteDaoImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.ToolProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Executes the rendered request classes against Hibernate and H2. */
class GeneratedScopeJpaExecutionTest {

    @Test
    void handwrittenBaseRequestSubclassKeepsVisibilityForAllActions(@TempDir Path output) throws Exception {
        generateRequests(output);
        var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:handwritten_scope_jpa;DB_CLOSE_DELAY=-1")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .build();
        try (SessionFactory factory = new MetadataSources(registry).addAnnotatedClass(UserOrder.class)
                .buildMetadata().buildSessionFactory();
             URLClassLoader loader = new URLClassLoader(new URL[]{output.resolve("compiled").toUri().toURL()},
                     getClass().getClassLoader())) {
            try (Session session = factory.openSession()) {
                session.beginTransaction();
                for (String id : List.of("visible", "hidden")) {
                    UserOrder row = new UserOrder();
                    row.id = id;
                    row.salesOrgId = id.equals("visible") ? "D1" : "OTHER";
                    row.salesUserId = "OTHER";
                    session.persist(row);
                }
                session.getTransaction().commit();
            }
            Class<?> customType = loader.loadClass("probe_jpa.services.userorder.req.CustomUserOrderReq");
            Object visible = customType.getConstructor().newInstance();
            field(customType, "id").set(visible, "visible");
            field(customType, "note").set(visible, "updated");
            injectScope(visible, customType.getSuperclass(), null);
            assertEquals(1, select(factory, visible));
            assertEquals(1, execute(factory, visible));

            Object hidden = customType.getConstructor().newInstance();
            field(customType, "id").set(hidden, "hidden");
            field(customType, "note").set(hidden, "blocked");
            injectScope(hidden, customType.getSuperclass(), null);
            assertEquals(0, select(factory, hidden));
            assertEquals(0, execute(factory, hidden));
            assertEquals(0, delete(factory, hidden));

            Object empty = customType.getConstructor().newInstance();
            field(customType, "id").set(empty, "hidden");
            field(customType, "note").set(empty, "blocked");
            SelectDaoImpl<UserOrder> emptyQuery = new SelectDaoImpl<>(miniDao(), false, UserOrder.class, "r");
            emptyQuery.appendByQueryObj(empty);
            assertTrue(emptyQuery.genFinalStatement().contains("1=2"));
            assertEquals(0, select(factory, empty));
            assertEquals(0, execute(factory, empty));
            assertEquals(0, delete(factory, empty));
            try (Session session = factory.openSession()) {
                assertEquals("updated", session.find(UserOrder.class, "visible").note);
                assertEquals(null, session.find(UserOrder.class, "hidden").note);
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void generatedSingleAndBatchUpdatesKeepTenantAndParticipantConditions(@TempDir Path output) throws Exception {
        generateRequests(output);
        var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.URL, "jdbc:h2:mem:generated_scope_jpa;DB_CLOSE_DELAY=-1")
                .applySetting(AvailableSettings.USER, "sa")
                .applySetting(AvailableSettings.PASS, "")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .build();
        try (SessionFactory factory = new MetadataSources(registry).addAnnotatedClass(UserOrder.class)
                .buildMetadata().buildSessionFactory();
             URLClassLoader loader = new URLClassLoader(new URL[]{output.resolve("compiled").toUri().toURL()},
                     getClass().getClassLoader())) {
            try (Session session = factory.openSession()) {
                session.beginTransaction();
                for (String id : List.of("a-own", "b-own", "a-foreign")) {
                    UserOrder row = new UserOrder();
                    row.id = id;
                    row.tenantId = id.startsWith("a-") ? "A" : "B";
                    row.salesOrgId = id.equals("a-foreign") ? "OTHER" : "D1";
                    row.salesUserId = id.equals("a-foreign") ? "OTHER" : "U1";
                    session.persist(row);
                }
                session.getTransaction().commit();
            }

            Class<?> updateType = loader.loadClass("probe_jpa.services.userorder.req.UpdateUserOrderReq");
            Class<?> simpleType = loader.loadClass("probe_jpa.services.userorder.req.SimpleUpdateUserOrderReq");
            Class<?> queryType = loader.loadClass("probe_jpa.services.userorder.req.QueryUserOrderReq");
            Class<?> idType = loader.loadClass("probe_jpa.services.userorder.req.UserOrderIdReq");
            Object internalIdRequest = idType.getConstructor(String.class).newInstance("a-own");
            try {
                com.levin.commons.service.support.VariableInjector.setVariableResolversForCurrentThread(
                        List.of(com.levin.commons.service.support.VariableInjector.newResolverByMap(null,
                                Map.of("accessibleOrgIds", List.of("D1"), "accessibleOwnerIds", List.of("U1")))));
                assertEquals(0, select(factory, internalIdRequest),
                        "仅设置线程解析器不会给服务内部新建的请求注入范围");
            } finally {
                com.levin.commons.service.support.VariableInjector.setVariableResolversForCurrentThread(null);
            }
            injectScope(internalIdRequest, idType.getSuperclass(), null);
            assertEquals(1, select(factory, internalIdRequest));
            org.junit.jupiter.api.Assertions.assertThrows(NoSuchFieldException.class,
                    () -> simpleType.getDeclaredField("preSalesOrgId"));
            org.junit.jupiter.api.Assertions.assertThrows(NoSuchFieldException.class,
                    () -> simpleType.getDeclaredField("preSalesUserId"));

            Object single = updateType.getConstructor(String.class).newInstance("a-own");
            field(simpleType, "salesUserId").set(single, "SINGLE");
            injectScope(single, simpleType.getSuperclass(), simpleType);
            assertEquals(1, execute(factory, single, new TenantWhere("A")));

            Object wrongTenant = updateType.getConstructor(String.class).newInstance("b-own");
            field(simpleType, "salesUserId").set(wrongTenant, "WRONG");
            injectScope(wrongTenant, simpleType.getSuperclass(), simpleType);
            assertEquals(0, execute(factory, wrongTenant, new TenantWhere("A")));

            Object remarkOnly = updateType.getConstructor(String.class).newInstance("a-own");
            field(simpleType, "remark").set(remarkOnly, "remark-updated");
            injectScope(remarkOnly, simpleType.getSuperclass(), simpleType);
            assertEquals(1, execute(factory, remarkOnly, new TenantWhere("A")));

            Object set = simpleType.getConstructor().newInstance();
            field(simpleType, "salesUserId").set(set, "BATCH");
            injectScope(set, simpleType.getSuperclass(), simpleType);
            Object where = queryType.getConstructor().newInstance();
            injectScope(where, queryType.getSuperclass().getSuperclass(), null);
            assertEquals(1, execute(factory, set, where, new TenantWhere("B")));

            try (Session session = factory.openSession()) {
                assertEquals("SINGLE", session.find(UserOrder.class, "a-own").salesUserId);
                assertEquals("remark-updated", session.find(UserOrder.class, "a-own").remark);
                assertEquals("BATCH", session.find(UserOrder.class, "b-own").salesUserId);
                assertEquals("OTHER", session.find(UserOrder.class, "a-foreign").salesUserId);
            }

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static void generateRequests(Path output) throws Exception {
        String oldModule = ServiceModelCodeGenerator.moduleName("probe_jpa");
        String oldPackage = ServiceModelCodeGenerator.modulePackageName("probe_jpa");
        Class<?> oldEntity = ServiceModelCodeGenerator.entityClass(UserOrder.class);
        Field sourceMap = ServiceModelCodeGenerator.class.getDeclaredField("srcFileCompilationMap");
        sourceMap.setAccessible(true);
        Object oldSourceMap = sourceMap.get(null);
        try {
            Path sourceRoot = output.resolve("entity-source");
            Path entitySource = sourceRoot.resolve("com/levin/commons/dao/codegen/UserOrder.java");
            Files.createDirectories(entitySource.getParent());
            Path original = Path.of("src/test/java/com/levin/commons/dao/codegen/UserOrder.java");
            if (!Files.exists(original)) original = Path.of("simple-dao-code-gen").resolve(original);
            Files.copy(original, entitySource);
            sourceMap.set(null, ServiceModelCodeGenerator.parseSrcFile(sourceRoot.toFile()));
            Method buildFields = ServiceModelCodeGenerator.class.getDeclaredMethod("buildFieldModel",
                    Class.class, Map.class, boolean.class, String.class);
            buildFields.setAccessible(true);
            @SuppressWarnings("unchecked") List<com.levin.commons.dao.codegen.model.FieldModel> fields =
                    (List<com.levin.commons.dao.codegen.model.FieldModel>) buildFields.invoke(null,
                            UserOrder.class, new HashMap<>(), false, "info");
            Method buildRequests = ServiceModelCodeGenerator.class.getDeclaredMethod("buildRequests",
                    Class.class, Map.class, String.class, Map.class, List.class);
            buildRequests.setAccessible(true);
            buildRequests.invoke(null, UserOrder.class, new HashMap<>(), output.resolve("services").toString(),
                    new HashMap<>(Map.of("reqExtendClass", "BaseReq")), fields);
        } finally {
            sourceMap.set(null, oldSourceMap);
            ServiceModelCodeGenerator.entityClass(oldEntity);
            ServiceModelCodeGenerator.modulePackageName(oldPackage);
            ServiceModelCodeGenerator.moduleName(oldModule);
        }
        Path stubs = output.resolve("stubs");
        List<Path> sources = new ArrayList<>();
        sources.add(write(stubs, "probe_jpa/entities/EntityConst.java",
                "package probe_jpa.entities; public class EntityConst {"
                        + " public static final String CREATE_ACTION=\"创建\", QUERY_ACTION=\"查询\","
                        + " UPDATE_ACTION=\"更新\", DELETE_ACTION=\"删除\"; }"));
        sources.add(write(stubs, "probe_jpa/services/commons/req/BaseReq.java",
                "package probe_jpa.services.commons.req; public class BaseReq implements java.io.Serializable {"
                        + " protected <T> T checkSQLInject(Object... v) { return (T)this; }"
                        + " protected boolean isNotBlank(Object v) { return v != null; }"
                        + " protected void checkUpdateOrgTarget(String title, String value, java.util.Collection<?> values) { if (!isUnsafeContext() || isCanAccessAllOrg() || value == null || value.isBlank()) return; if (values == null || !values.contains(value)) throw new IllegalArgumentException(\"更新 \"+title+\"：组织ID不在授权范围内\"); }"
                        + " public boolean isUnsafeContext() { return true; }"
                        + " public boolean isAdmin() { return false; }"
                        + " public boolean isCanAccessAllOrg() { return false; }"
                        + " public String get_currentUserId() { return \"U1\"; }"
                        + " protected void checkUpdateOwnerTarget(String title, String ownerId) { if (!isUnsafeContext() || isAdmin() || ownerId == null || ownerId.isBlank()) return; if (get_currentUserId() == null || get_currentUserId().isBlank()) throw new IllegalArgumentException(\"更新 \" + title + \"：缺少当前用户ID\"); }"
                        + " @com.levin.commons.dao.CtxVar(varName=\"_req\")"
                        + " public BaseReq getRequestContext() { return this; }"
                        + " public boolean isForceUpdateField(String name) { return false; } }"));
        sources.add(write(stubs, "probe_jpa/services/userorder/info/UserOrderInfo.java",
                "package probe_jpa.services.userorder.info; public class UserOrderInfo {}"));
        sources.add(write(stubs, "probe_jpa/services/userorder/req/CustomUserOrderReq.java",
                "package probe_jpa.services.userorder.req;"
                        + " public class CustomUserOrderReq extends UserOrderBaseRequest<CustomUserOrderReq> {"
                        + " @com.levin.commons.dao.annotation.Eq public String id;"
                        + " @com.levin.commons.dao.annotation.update.Update public String note; }"));
        try (var generated = Files.walk(output.resolve("services"))) {
            sources.addAll(generated.filter(p -> p.toString().endsWith(".java")).toList());
        }
        Path compiled = output.resolve("compiled");
        Files.createDirectories(compiled);
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var manager = compiler.getStandardFileManager(null, null, null)) {
            org.junit.jupiter.api.Assertions.assertTrue(compiler.getTask(null, manager, null,
                    List.of("-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                            "-classpath", System.getProperty("java.class.path"), "-d", compiled.toString()),
                    null, manager.getJavaFileObjectsFromPaths(sources)).call());
        }
    }

    private static Path write(Path root, String relative, String source) throws Exception {
        Path file = root.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source);
        return file;
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void injectScope(Object request, Class<?> visibilityType, Class<?> updateType) throws Exception {
        for (Class<?> type = request.getClass(); type != Object.class; type = type.getSuperclass()) {
            for (Field f : type.getDeclaredFields()) {
                if ((f.getName().startsWith("in") && f.getName().endsWith("List"))
                        || f.getName().startsWith("updateScope")) {
                    f.setAccessible(true);
                    f.set(request, f.getName().contains("Org") ? List.of("D1") : List.of("U1"));
                }
            }
        }
        if (updateType != null) {
            field(updateType, "oldOwnerGuardSalesUserId").set(request, "U1");
        }
    }

    private static int execute(SessionFactory factory, Object... requests) {
        UpdateDaoImpl<UserOrder> builder = new UpdateDaoImpl<>(miniDao(), false, UserOrder.class, "r");
        for (Object request : requests) builder.appendByQueryObj(request);
        builder.limit(0, 10);
        String hql = builder.genFinalStatement();
        List<String> values = builder.genFinalParamList().stream().filter(String.class::isInstance)
                .map(String.class::cast).toList();
        for (int i = 0; i < values.size(); i++) hql = hql.replaceFirst(":\\?", "?" + (i + 1));
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            var query = session.createMutationQuery(hql);
            for (int i = 0; i < values.size(); i++) query.setParameter(i + 1, values.get(i));
            int changed = query.executeUpdate();
            session.getTransaction().commit();
            return changed;
        }
    }

    private static int select(SessionFactory factory, Object request) {
        SelectDaoImpl<UserOrder> builder = new SelectDaoImpl<>(miniDao(), false, UserOrder.class, "r");
        builder.appendByQueryObj(request);
        String hql = builder.genFinalStatement();
        List<String> values = builder.genFinalParamList().stream().filter(String.class::isInstance)
                .map(String.class::cast).toList();
        for (int i = 0; i < values.size(); i++) hql = hql.replaceFirst(":\\?", "?" + (i + 1));
        try (Session session = factory.openSession()) {
            var query = session.createQuery(hql, UserOrder.class);
            for (int i = 0; i < values.size(); i++) query.setParameter(i + 1, values.get(i));
            return query.getResultList().size();
        }
    }

    private static int delete(SessionFactory factory, Object request) {
        DeleteDaoImpl<UserOrder> builder = new DeleteDaoImpl<>(miniDao(), false, UserOrder.class, "r");
        builder.appendByQueryObj(request);
        builder.limit(0, 10);
        String hql;
        try {
            Method physical = DeleteDaoImpl.class.getDeclaredMethod("genFinalStatement", boolean.class);
            physical.setAccessible(true);
            hql = (String) physical.invoke(builder, false);
        } catch (ReflectiveOperationException error) {
            throw new RuntimeException(error);
        }
        List<String> values = builder.genFinalParamList(false).stream().filter(String.class::isInstance)
                .map(String.class::cast).toList();
        for (int i = 0; i < values.size(); i++) hql = hql.replaceFirst(":\\?", "?" + (i + 1));
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            var query = session.createMutationQuery(hql);
            for (int i = 0; i < values.size(); i++) query.setParameter(i + 1, values.get(i));
            int count = query.executeUpdate();
            session.getTransaction().commit();
            return count;
        }
    }

    private static MiniDao miniDao() {
        return (MiniDao) Proxy.newProxyInstance(GeneratedScopeJpaExecutionTest.class.getClassLoader(),
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

    static class TenantWhere {
        @Eq("tenantId") String tenantId;
        TenantWhere(String tenantId) { this.tenantId = tenantId; }
    }
}
