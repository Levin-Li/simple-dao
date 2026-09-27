package com.levin.commons.dao.codegen;

import com.levin.commons.dao.ConditionBuilder;
import com.levin.commons.dao.DeleteDao;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.support.DeleteDaoImpl;
import com.levin.commons.dao.support.SelectDaoImpl;
import com.levin.commons.dao.support.UpdateDaoImpl;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import com.levin.commons.plugins.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** 真实渲染、编译组织模板并解析注解，校验交给 DAO 的 SQL 和参数；不复制权限判断实现。 */
class OrgMutationScopeSqlTest {

    private static final String PACKAGE = "com.example.orgsql.services.commons.req";
    private static final String TENANT = "tenant-a";
    private static final String TARGET_ID = "record-in-another-org";
    @TempDir static Path tempDir;
    private static URLClassLoader loader;

    enum Action { QUERY, UPDATE, DELETE }
    enum Role { TOP_SUPER, SUPER, PLATFORM_ADMIN, TENANT_ADMIN, TENANT_USER, PLATFORM_USER }
    record BuiltSql(String sql, List<?> params) {}

    @BeforeAll
    static void compileActualTemplates() throws Exception {
        Path sourceRoot = tempDir.resolve("src");
        Path packageDir = sourceRoot.resolve(PACKAGE.replace('.', '/'));
        Files.createDirectories(packageDir);
        List<File> sources = new ArrayList<>();
        for (String template : List.of("BaseReq.java", "MultiTenantReq.java", "MultiTenantOrgReq.java")) {
            File source = packageDir.resolve(template).toFile();
            Utils.copyAndReplace(sourceRoot.toString(), true,
                    "simple.dao/codegen/template/services/commons/req/" + template,
                    source, Map.of("modulePackageName", "com.example.orgsql"));
            sources.add(source);
        }
        Path fixture = packageDir.resolve("FixtureReq.java");
        Files.writeString(fixture, "package " + PACKAGE + ";\n" + """
                import com.levin.commons.dao.annotation.Eq;
                import com.levin.commons.dao.annotation.Ignore;
                import com.levin.commons.dao.domain.OrganizedPublicObject;
                import com.levin.commons.dao.domain.OrganizedSharedObject;
                public class FixtureReq extends MultiTenantOrgReq<FixtureReq>
                        implements OrganizedPublicObject, OrganizedSharedObject {
                    @Eq public String id;
                    @Eq public Integer optimisticLock;
                    @Ignore public boolean includePublic;
                    @Ignore public boolean includeShared;
                    @Override public boolean isContainsOrgPublicData() { return includePublic; }
                    @Override public boolean isOrgShared() { return includeShared; }
                }
                """);
        sources.add(fixture.toFile());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "需要 JDK 编译实际生成模板");
        Path classes = tempDir.resolve("classes");
        Files.createDirectories(classes);
        String lombok = new File(Data.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, files, null, List.of(
                    "-classpath", System.getProperty("java.class.path"),
                    "-processorpath", lombok,
                    "-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                    "-d", classes.toString()), null, files.getJavaFileObjectsFromFiles(sources)).call());
        }
        loader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, OrgMutationScopeSqlTest.class.getClassLoader());
    }

    @AfterAll
    static void closeLoader() throws Exception {
        if (loader != null) loader.close();
    }

    @TestFactory
    Stream<DynamicTest> singleOrgMustConstrainQueriesAndDeletesIncludingUnrelatedRecordId() {
        return Stream.of(Action.QUERY, Action.DELETE).flatMap(action -> Stream.of(false, true).map(allOrg ->
                DynamicTest.dynamicTest(action + " allOrg=" + allOrg, () -> {
                    Object req = request(Role.TENANT_USER, allOrg, null, "org-old");
                    // 主键本身不能取代组织约束；即使主键指向其它组织，WHERE 仍必须保留 org_id。
                    assertSql(action, req, "r.org_id = :?", List.of("org-old"), null);
                })));
    }

    @TestFactory
    Stream<DynamicTest> orgListHasPriorityOverSingleOrgForQueriesAndDeletes() {
        return Stream.of(Action.QUERY, Action.DELETE).map(action -> DynamicTest.dynamicTest(action.toString(), () -> {
            Object req = request(Role.TENANT_USER, false, List.of("org-old", "org-second"), "org-ignored");
            assertSql(action, req, "r.org_id in (:?,:?)", List.of("org-old", "org-second"), null);
        }));
    }

    @TestFactory
    Stream<DynamicTest> eachAdministratorMaySetNewOrgWhileWhereUsesOldOrgList() {
        return Stream.of(Role.TOP_SUPER, Role.SUPER, Role.PLATFORM_ADMIN, Role.TENANT_ADMIN)
                .map(role -> DynamicTest.dynamicTest(role.toString(), () -> {
                    Object req = request(role, false, List.of("org-old", "org-second"), "org-new");
                    assertSql(Action.UPDATE, req, "r.org_id in (:?,:?)", List.of("org-old", "org-second"), "org-new");
                }));
    }

    @TestFactory
    Stream<DynamicTest> ordinaryUsersCannotSetOrgEvenWithAllOrgScope() {
        return Stream.of(Role.TENANT_USER, Role.PLATFORM_USER).flatMap(role -> Stream.of(false, true).map(allOrg ->
                DynamicTest.dynamicTest(role + " allOrg=" + allOrg, () -> {
                    Object req = request(role, allOrg, List.of("org-old"), "org-new");
                    assertSql(Action.UPDATE, req, "r.org_id in (:?)", List.of("org-old"), null);
                })));
    }

    @TestFactory
    Stream<DynamicTest> newOrgCannotReplaceMissingOldOrgScopeOnRestrictedUpdates() {
        return Stream.<Collection<String>>of(null, List.of()).map(orgs ->
                DynamicTest.dynamicTest("TENANT_ADMIN oldOrgList=" + orgs, () -> {
                    Object req = request(Role.TENANT_ADMIN, false, orgs, "org-new");
                    assertOrgRejected(Action.UPDATE, req);
                }));
    }

    @TestFactory
    Stream<DynamicTest> ordinaryUserSingleOrgFiltersUpdateWithoutChangingOwnership() {
        return Stream.of(Role.TENANT_USER, Role.PLATFORM_USER).flatMap(role -> Stream.of(false, true).map(allOrg ->
                DynamicTest.dynamicTest(role + " allOrg=" + allOrg, () -> {
                    Object req = request(role, allOrg, null, "org-old");
                    assertSql(Action.UPDATE, req, "r.org_id = :?", List.of("org-old"), null);
                })));
    }

    @TestFactory
    Stream<DynamicTest> allOrgPermissionAllowsAdminUnscopedUpdateButDoesNotGrantReassignment() {
        return Stream.of(Role.TENANT_ADMIN, Role.TENANT_USER).map(role -> DynamicTest.dynamicTest(role.toString(), () -> {
            Object req = request(role, true, null, "org-new");
            assertSql(Action.UPDATE, req, role == Role.TENANT_ADMIN ? "" : "r.org_id = :?",
                    role == Role.TENANT_ADMIN ? List.of() : List.of("org-new"),
                    role == Role.TENANT_ADMIN ? "org-new" : null);
        }));
    }

    @TestFactory
    Stream<DynamicTest> invalidOrgListMembersMustNotDisappearAndBroadenScope() {
        return Stream.of(Arrays.asList((String) null), List.of(""), List.of("  "),
                        Arrays.asList("org-old", null), List.of("org-old", " "))
                .flatMap(orgs -> Arrays.stream(Action.values()).map(action ->
                        DynamicTest.dynamicTest(action + " invalid=" + orgs, () ->
                                assertOrgRejected(action, request(Role.TENANT_USER, false, orgs, "org-new")))));
    }

    @TestFactory
    Stream<DynamicTest> restrictedUsersWithoutEitherScopeMustBeRejected() {
        return Arrays.stream(Action.values()).map(action -> DynamicTest.dynamicTest(action.toString(), () ->
                assertOrgRejected(action, request(Role.TENANT_USER, false, null, null))));
    }

    @Test
    void queryPublicAndSharedBranchesStayInsideOrgGroup() throws Exception {
        Object req = request(Role.TENANT_USER, false, List.of("org-old"), null);
        field(req, "includePublic", true);
        field(req, "includeShared", true);
        assertSql(Action.QUERY, req, "(r.org_id in (:?) or r.org_id is null or r.org_shared = (true))",
                List.of("org-old"), null);
    }

    @TestFactory
    Stream<DynamicTest> orgPublicAndSharedSwitchesCannotBroadenMutations() {
        return Stream.of(Action.UPDATE, Action.DELETE).map(action -> DynamicTest.dynamicTest(action.toString(), () -> {
            Object req = request(Role.TENANT_ADMIN, false, List.of("org-old"), null);
            field(req, "includePublic", true);
            field(req, "includeShared", true);
            assertSql(action, req, "r.org_id in (:?)", List.of("org-old"), null);
        }));
    }

    private static void assertOrgRejected(Action action, Object req) {
        RuntimeException failure = assertThrows(RuntimeException.class, () -> statement(action, req));
        StringBuilder causes = new StringBuilder();
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) causes.append(cause.getMessage());
        assertTrue(causes.toString().contains("组织"), "必须由组织范围校验拒绝，而非无关构建错误：" + causes);
    }

    private static void assertSql(Action action, Object req, String orgWhere, List<String> orgParams, String newOrg)
            throws Exception {
        BuiltSql built = statement(action, req);
        boolean platform = (boolean) req.getClass().getMethod("isPlatformUser").invoke(req);
        String tenantWhere = platform ? "r.tenant_id is null" : "r.tenant_id = :?";
        // 超管类未指定租户不生成租户约束；普通平台用户严格限定平台记录。
        if (platform && (boolean) req.getClass().getMethod("isAdmin").invoke(req)) tenantWhere = "";
        List<Object> params = new ArrayList<>();
        String prefix = switch (action) {
            case QUERY -> "select r.id from org_scope_record r where ";
            case DELETE -> "delete from org_scope_record r where ";
            case UPDATE -> "update org_scope_record r set r.name = :?" + (newOrg != null ? ", r.org_id = :?" : "") + " where ";
        };
        if (action == Action.UPDATE) {
            params.add("changed");
            if (newOrg != null) params.add(newOrg);
        }
        List<String> conditions = new ArrayList<>();
        if (!tenantWhere.isEmpty()) {
            conditions.add(tenantWhere);
            if (!platform) params.add(TENANT);
        }
        if (!orgWhere.isEmpty()) conditions.add(orgWhere);
        params.addAll(orgParams);
        conditions.add("r.id = :?");
        conditions.add("r.optimistic_lock = :?");
        params.add(TARGET_ID);
        params.add(7);
        assertEquals(normalize(prefix + String.join(" and ", conditions) + (action == Action.QUERY ? "" : " limit 1")),
                normalize(built.sql()));
        assertEquals(params, built.params().stream().filter(value -> !(value instanceof Map)).toList(), built.sql());
    }

    private static Object request(Role role, boolean allOrg, Collection<String> orgs, String org) throws Exception {
        Object req = loader.loadClass(PACKAGE + ".FixtureReq").getConstructor().newInstance();
        boolean tenant = role == Role.TENANT_ADMIN || role == Role.TENANT_USER;
        field(req, "isTenantUser", tenant);
        field(req, "isPlatformUser", !tenant);
        field(req, "isTopSuperAdmin", role == Role.TOP_SUPER);
        field(req, "isSuperAdmin", role == Role.SUPER);
        field(req, "isPlatformAdmin", role == Role.PLATFORM_ADMIN);
        field(req, "isTenantAdmin", role == Role.TENANT_ADMIN);
        field(req, "tenantId", tenant ? TENANT : null);
        field(req, "_currentUserTenantId", tenant ? TENANT : null);
        field(req, "isUnsafeContext", true);
        field(req, "isAllOrgScope", allOrg);
        field(req, "orgIdList", orgs);
        field(req, "orgId", org);
        field(req, "enableDefaultOrderBy", false);
        field(req, "id", TARGET_ID);
        field(req, "optimisticLock", 7);
        return req;
    }

    private static void field(Object req, String name, Object value) throws Exception {
        for (Class<?> type = req.getClass(); type != null; type = type.getSuperclass()) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(req, value);
                return;
            } catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static BuiltSql statement(Action action, Object request) {
        AtomicReference<BuiltSql> captured = new AtomicReference<>();
        MiniDao dao = (MiniDao) Proxy.newProxyInstance(OrgMutationScopeSqlTest.class.getClassLoader(), new Class[]{MiniDao.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParamPlaceholder": return ":?";
                        case "getSafeModeMaxLimit": return 100;
                        case "getNamingStrategy": return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                        case "getTableName":
                        case "getColumnName": return InvocationHandler.invokeDefault(proxy, method, args);
                        case "update":
                            assertEquals(true, args[0]);
                            captured.set(new BuiltSql((String) args[3], QueryAnnotationUtil.flattenParams(null, (Object[]) args[4])));
                            return 0;
                        case "find": throw new AssertionError("不得执行数据库查询");
                        default:
                            if (method.getReturnType() == boolean.class) return false;
                            if (method.getReturnType() == int.class) return 0;
                            return null;
                    }
                });
        ConditionBuilder<?, ?> builder;
        switch (action) {
            case QUERY:
                builder = new SelectDaoImpl<>(dao, true, OrgRow.class, "r").select("id").appendByQueryObj(request);
                break;
            case UPDATE:
                builder = new UpdateDaoImpl<>(dao, true, OrgRow.class, "r").set("name", "changed")
                        .limit(0, 1).appendByQueryObj(request);
                break;
            case DELETE:
                DeleteDao<OrgRow> delete = new DeleteDaoImpl<>(dao, true, OrgRow.class, "r")
                        .limit(0, 1).appendByQueryObj(request);
                delete.delete();
                assertNotNull(captured.get());
                return captured.get();
            default: throw new AssertionError(action);
        }
        return new BuiltSql(builder.genFinalStatement(), builder.genFinalParamList());
    }

    private static String normalize(String sql) {
        return sql.trim().replaceAll("\\s+", " ").replaceAll("\\( +", "(")
                .replaceAll(" +\\)", ")").replaceAll("\\s*,\\s*", ",").toLowerCase(java.util.Locale.ROOT);
    }

    @Entity
    @Table(name = "org_scope_record")
    static class OrgRow {
        @Id String id;
        @Column(name = "tenant_id") String tenantId;
        @Column(name = "org_id") String orgId;
        @Column(name = "org_shared") boolean orgShared;
        @Column(name = "optimistic_lock") Integer optimisticLock;
        String name;
    }
}
