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
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Test;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对生成模板运行真实的注解解析、SpEL 和 SELECT/UPDATE/DELETE 原生 SQL 构建链。
 * 数据库连接仅以元数据桩替代，权限条件本身不复制到测试请求中。
 */
class RoleScopeSqlTest {

    private static final String GENERATED_PACKAGE = "com.example.scopesql.services.commons.req";
    private static final String TENANT = "tenant-a";
    private static final String ROLE_ID = "role-target";
    private static final int VERSION = 7;

    @TempDir
    static Path tempDir;

    private static URLClassLoader generatedLoader;

    enum Role { TOP_SUPER, SUPER, PLATFORM_ADMIN, TENANT_ADMIN, TENANT_USER, PLATFORM_USER }
    enum Action { QUERY, UPDATE, DELETE }
    enum Capability { NONE, PUBLIC, SHARED, BOTH }
    enum Scope { ALL, PLATFORM, TENANT, TENANT_AND_PLATFORM, REJECT }

    record Scenario(Role role, String tenant, Scope query, Scope mutation) {}
    record BuiltSql(String sql, List<?> params) {}

    @BeforeAll
    static void compileActualTemplates() throws Exception {
        Path sourceRoot = tempDir.resolve("src");
        Path packageDir = sourceRoot.resolve(GENERATED_PACKAGE.replace('.', '/'));
        Files.createDirectories(packageDir);
        List<File> sources = new ArrayList<>();
        for (String template : List.of("BaseReq.java", "MultiTenantReq.java")) {
            File source = packageDir.resolve(template).toFile();
            Utils.copyAndReplace(sourceRoot.toString(), true,
                    "simple.dao/codegen/template/services/commons/req/" + template,
                    source, Map.of("modulePackageName", "com.example.scopesql"));
            sources.add(source);
        }

        Path requests = packageDir.resolve("FixtureRequests.java");
        Files.writeString(requests, "package " + GENERATED_PACKAGE + ";\n" + """
                import com.levin.commons.dao.annotation.Eq;
                import com.levin.commons.dao.annotation.Ignore;
                import com.levin.commons.dao.domain.MultiTenantPublicObject;
                import com.levin.commons.dao.domain.MultiTenantSharedObject;
                public class FixtureRequests {
                    public static class ScopedReq extends MultiTenantReq<ScopedReq> {
                        @Eq public String id;
                        @Eq public Integer optimisticLock;
                        @Ignore public boolean includePublic;
                        @Ignore public boolean includeShared;
                        @Override public boolean isContainsPublicData() { return includePublic; }
                        @Override public boolean isTenantShared() { return includeShared; }
                    }
                    public static class NONE extends ScopedReq {}
                    public static class PUBLIC extends ScopedReq implements MultiTenantPublicObject {}
                    public static class SHARED extends ScopedReq implements MultiTenantSharedObject {}
                    public static class BOTH extends ScopedReq implements MultiTenantPublicObject, MultiTenantSharedObject {}
                    public static class RestrictedReq extends ScopedReq {
                        @Override public boolean isPlatformAdmin() { return false; }
                    }
                }
                """);
        sources.add(requests.toFile());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "测试需要 JDK 编译器来验证实际生成的请求");
        Path classes = tempDir.resolve("classes");
        Files.createDirectories(classes);
        String lombokPath = new File(Data.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, files, null, List.of(
                            "-classpath", System.getProperty("java.class.path"),
                            "-processorpath", lombokPath,
                            "-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                            "-d", classes.toString()), null, files.getJavaFileObjectsFromFiles(sources)).call(),
                    "实际生成模板必须通过编译");
        }
        generatedLoader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, RoleScopeSqlTest.class.getClassLoader());
    }

    @AfterAll
    static void closeGeneratedClasses() throws Exception {
        if (generatedLoader != null) {
            generatedLoader.close();
        }
    }

    /** 范围真值表独立列出；能力与开关扩展指定租户查询，超管类的共享范围在更新、删除时同样生效。 */
    static Stream<Scenario> scenarios() {
        return Stream.of(
                new Scenario(Role.TOP_SUPER, null, Scope.ALL, Scope.ALL),
                new Scenario(Role.TOP_SUPER, TENANT, Scope.TENANT_AND_PLATFORM, Scope.TENANT_AND_PLATFORM),
                new Scenario(Role.SUPER, null, Scope.ALL, Scope.ALL),
                new Scenario(Role.SUPER, TENANT, Scope.TENANT_AND_PLATFORM, Scope.TENANT_AND_PLATFORM),
                new Scenario(Role.PLATFORM_ADMIN, null, Scope.ALL, Scope.ALL),
                new Scenario(Role.PLATFORM_ADMIN, TENANT, Scope.TENANT_AND_PLATFORM, Scope.TENANT_AND_PLATFORM),
                new Scenario(Role.TENANT_ADMIN, TENANT, Scope.TENANT, Scope.TENANT),
                new Scenario(Role.TENANT_USER, TENANT, Scope.TENANT, Scope.TENANT),
                new Scenario(Role.PLATFORM_USER, null, Scope.PLATFORM, Scope.PLATFORM),
                new Scenario(Role.PLATFORM_USER, TENANT, Scope.TENANT, Scope.REJECT));
    }

    @TestFactory
    Stream<DynamicTest> finalSqlAndParametersMustMatchRoleViewAndOperation() {
        return scenarios().flatMap(scenario -> Arrays.stream(Capability.values()).flatMap(capability ->
                Stream.of(0, 1, 2, 3).flatMap(flags -> Arrays.stream(Action.values()).map(action ->
                        DynamicTest.dynamicTest(scenario + " / " + capability + " flags=" + flags + " / " + action,
                                () -> {
                                    Object request = request(scenario.role(), scenario.tenant(), capability, flags);
                                    Scope scope = action == Action.QUERY ? scenario.query() : scenario.mutation();
                                    if (scope == Scope.REJECT) {
                                        assertRejected(action, request, "普通平台用户不能使用非空租户 ID 更新或删除租户数据");
                                        return;
                                    }
                                    boolean queryWithTenant = action == Action.QUERY && scenario.tenant() != null;
                                    boolean publicData = queryWithTenant && (flags & 1) != 0
                                            && (capability == Capability.PUBLIC || capability == Capability.BOTH);
                                    boolean sharedData = scenario.tenant() != null
                                            && (action == Action.QUERY || scope == Scope.TENANT_AND_PLATFORM)
                                            && (flags & 2) != 0
                                            && (capability == Capability.SHARED || capability == Capability.BOTH);
                                    assertSql(action, request, scope, publicData, sharedData);
                                })))));
    }

    @TestFactory
    Stream<DynamicTest> tenantUsersMustRejectMissingOrForeignTenantBeforeBuildingEveryStatement() {
        return Stream.of(Role.TENANT_USER, Role.TENANT_ADMIN).flatMap(role ->
                Stream.<String>of(null, "", "   ", "tenant-b").flatMap(target ->
                        Arrays.stream(Action.values()).map(action -> DynamicTest.dynamicTest(
                                role + " target=[" + target + "] / " + action,
                                () -> assertRejected(action, request(role, target, Capability.BOTH, 3),
                                        "租户用户的空值或跨租户目标必须被拒绝，公共/共享开关不能绕过该边界")))));
    }

    @TestFactory
    Stream<DynamicTest> inconsistentSuperAdminFlagMustNotBypassTenantIdentityBoundary() {
        return Arrays.stream(Action.values()).map(action -> DynamicTest.dynamicTest(action.toString(), () -> {
            Object request = request(Role.TENANT_USER, "tenant-b", Capability.BOTH, 3);
            setField(request, "isSuperAdmin", true);
            assertRejected(action, request, "仍带租户用户身份时，即使超管标志异常为真，也必须先校验租户边界");
        }));
    }

    @Test
    void platformAdminOverrideMustRestrictScope() throws Exception {
        Object req = generatedLoader.loadClass(GENERATED_PACKAGE + ".FixtureRequests$RestrictedReq")
                .getConstructor().newInstance();
        setField(req, "isTenantUser", false);
        setField(req, "isPlatformUser", true);
        setField(req, "isPlatformAdmin", true);
        setField(req, "id", ROLE_ID);
        setField(req, "optimisticLock", VERSION);
        assertEquals(false, req.getClass().getMethod("isPlatformAdmin").invoke(req));
        for (Action action : Action.values()) assertSql(action, req, Scope.PLATFORM, false, false);
    }

    private static void assertSql(Action action, Object request, Scope scope,
                                  boolean publicData, boolean sharedData) {
        BuiltSql built = statement(action, request);
        String sql = normalize(built.sql());
        List<String> branches = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (action == Action.UPDATE) {
            params.add("changed");
        }
        if (scope == Scope.TENANT || scope == Scope.TENANT_AND_PLATFORM) {
            branches.add("r.tenant_id = :?");
            params.add(TENANT);
        }
        if (sharedData) {
            branches.add("r.tenant_shared = (true)");
        }
        if (scope == Scope.PLATFORM || scope == Scope.TENANT_AND_PLATFORM || publicData) {
            branches.add("r.tenant_id is null");
        }
        params.add(ROLE_ID);
        params.add(VERSION);

        // 整个 WHERE 必须精确相等：公共/共享的 OR 不得吞掉主键或乐观锁的 AND 条件。
        String tenant = branches.isEmpty() ? "" : branches.size() == 1 ? branches.get(0) + " and "
                : "(" + String.join(" or ", branches) + ") and ";
        String expectedWhere = tenant + "r.id = :? and r.optimistic_lock = :?";
        String prefix = switch (action) {
            case QUERY -> "select r.id from tenant_scope_role r where ";
            case UPDATE -> "update tenant_scope_role r set r.name = :? where ";
            case DELETE -> "delete from tenant_scope_role r where ";
        };
        assertEquals(prefix + expectedWhere + (action == Action.QUERY ? "" : " limit 1"), sql);
        // DAO 参数中包含 SpEL/命名参数上下文 Map；本 SQL 仅有 :? 位置参数。
        assertEquals(params, built.params().stream().filter(value -> !(value instanceof Map)).toList(), sql);
    }

    private static void assertRejected(Action action, Object request, String message) {
        RuntimeException failure = assertThrows(RuntimeException.class, () -> statement(action, request), message);
        StringBuilder causes = new StringBuilder();
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            causes.append(cause.getMessage()).append('\n');
        }
        assertTrue(causes.toString().contains("越界") || causes.toString().contains("平台"),
                "失败必须来自租户权限校验，而非无关构建异常：" + causes);
    }

    private static BuiltSql statement(Action action, Object request) {
        AtomicReference<BuiltSql> capturedDelete = new AtomicReference<>();
        MiniDao metadata = metadataDao(capturedDelete);
        ConditionBuilder<?, ?> builder;
        switch (action) {
            case QUERY:
                builder = new SelectDaoImpl<>(metadata, true, RoleRow.class, "r")
                        .select("id").appendByQueryObj(request);
                break;
            case UPDATE:
                builder = new UpdateDaoImpl<>(metadata, true, RoleRow.class, "r")
                        .set("name", "changed").limit(0, 1).appendByQueryObj(request);
                break;
            case DELETE:
                DeleteDao<RoleRow> delete = new DeleteDaoImpl<>(metadata, true, RoleRow.class, "r")
                        .limit(0, 1).appendByQueryObj(request);
                // 捕获真实 delete() 分支发给 DAO 的语句；代理不连接数据库、不执行删除。
                delete.delete();
                assertNotNull(capturedDelete.get(), "删除必须生成实际交付 DAO 的 SQL");
                return capturedDelete.get();
            default:
                throw new AssertionError(action);
        }
        return new BuiltSql(builder.genFinalStatement(), builder.genFinalParamList());
    }

    private static Object request(Role role, String tenant, Capability capability, int flags) throws Exception {
        Object request = Class.forName(GENERATED_PACKAGE + ".FixtureRequests$" + capability, true, generatedLoader)
                .getConstructor().newInstance();
        boolean tenantUser = role == Role.TENANT_USER || role == Role.TENANT_ADMIN;
        setField(request, "isTenantUser", tenantUser);
        setField(request, "isPlatformUser", !tenantUser);
        setField(request, "isTopSuperAdmin", role == Role.TOP_SUPER);
        setField(request, "isSuperAdmin", role == Role.SUPER);
        setField(request, "isPlatformAdmin", role == Role.PLATFORM_ADMIN);
        setField(request, "isTenantAdmin", role == Role.TENANT_ADMIN);
        setField(request, "tenantId", tenant);
        setField(request, "_currentUserTenantId", tenantUser ? TENANT : null);
        setField(request, "enableDefaultOrderBy", false);
        setField(request, "includePublic", (flags & 1) != 0);
        setField(request, "includeShared", (flags & 2) != 0);
        setField(request, "id", ROLE_ID);
        setField(request, "optimisticLock", VERSION);
        return request;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                // 请求身份字段位于实际生成的父类。
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static MiniDao metadataDao(AtomicReference<BuiltSql> capturedDelete) {
        return (MiniDao) Proxy.newProxyInstance(RoleScopeSqlTest.class.getClassLoader(), new Class[]{MiniDao.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getParamPlaceholder": return ":?";
                        case "getSafeModeMaxLimit": return 100;
                        case "getNamingStrategy": return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                        case "getTableName":
                        case "getColumnName": return InvocationHandler.invokeDefault(proxy, method, args);
                        case "update":
                            assertEquals(5, args.length);
                            assertEquals(true, args[0], "删除须生成原生 SQL");
                            capturedDelete.set(new BuiltSql((String) args[3], QueryAnnotationUtil.flattenParams(null, (Object[]) args[4])));
                            return 0;
                        case "find": throw new AssertionError("SQL 构建测试不得执行数据库操作");
                        default:
                            if (method.getReturnType() == boolean.class) return false;
                            if (method.getReturnType() == int.class) return 0;
                            return null;
                    }
                });
    }

    private static String normalize(String sql) {
        return sql.trim().replaceAll("\\s+", " ").replaceAll("\\( +", "(").replaceAll(" +\\)", ")")
                .toLowerCase(java.util.Locale.ROOT);
    }

    @Entity
    @Table(name = "tenant_scope_role")
    static class RoleRow {
        @Id String id;
        @Column(name = "tenant_id") String tenantId;
        @Column(name = "tenant_shared") boolean tenantShared;
        @Column(name = "optimistic_lock") Integer optimisticLock;
        String name;
    }
}
