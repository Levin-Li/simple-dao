package com.levin.commons.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import com.levin.commons.service.support.SimpleVariableInjector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** 实际模板编译后的请求对象经 JPA 执行，事务回滚隔离测试数据。 */
@ActiveProfiles("dev")
@SpringBootTest(classes = TestConfiguration.class, properties = {
        "spring.jpa.properties.hibernate.query.hql.json_functions_enabled=true",
        "com.levin.commons.service.support.DefaultSpringMvcEnumFormatterConfiguration.enabled=false",
        "com.levin.commons.service.support.DefaultSpringMvcJsonDeserializerConfiguration.enabled=false"
})
@Transactional
class GeneratedTenantScopeJpaTest {
    @TempDir static Path temporary;
    static URLClassLoader loader;
    static Class<?> requestClass;
    @Autowired SimpleDao dao;
    @Autowired EntityManager em;

    enum Role { TOP_SUPER, SUPER, PLATFORM_ADMIN, PLATFORM, TENANT_ADMIN, TENANT }

    @BeforeAll
    static void compileTemplates() throws Exception {
        Path repository = Path.of("").toAbsolutePath();
        if (!Files.isDirectory(repository.resolve("simple-dao-code-gen"))) repository = repository.getParent();
        Path templates = repository.resolve("simple-dao-code-gen/src/main/resources/simple.dao/codegen/template/services/commons/req");
        List<java.io.File> sources = new ArrayList<>();
        for (String name : List.of("BaseReq", "MultiTenantReq", "MultiTenantOrgReq", "MultiTenantPersonalReq", "MultiTenantOrgPersonalReq")) {
            // 这些模板仅包含包名与生成时间占位符；保留所有注解与方法原样编译。
            String source = Files.readString(templates.resolve(name + ".java"))
                    .replace("${modulePackageName}", "tenant.fixture").replace("${.now}", "test");
            assertFalse(source.contains("${"), "模板新增变量时需要更新测试渲染器");
            Path file = temporary.resolve(name + ".java");
            Files.writeString(file, source);
            sources.add(file.toFile());
        }
        Path fixture = temporary.resolve("ScopeReq.java");
        Files.writeString(fixture, """
                package tenant.fixture.services.commons.req;
                import com.levin.commons.dao.annotation.Ignore;
                import com.levin.commons.dao.domain.MultiTenantPublicObject;
                import com.levin.commons.dao.domain.MultiTenantSharedObject;
                public class ScopeReq extends MultiTenantReq<ScopeReq>
                        implements MultiTenantPublicObject, MultiTenantSharedObject {
                    @Ignore public boolean includePublic;
                    @Ignore public boolean includeShared;
                    @Override public boolean isContainsPublicData() { return includePublic; }
                    @Override public boolean isTenantShared() { return includeShared; }
                }
                """);
        sources.add(fixture.toFile());
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        String lombok = Path.of(Data.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        try (var manager = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, manager, null, List.of("-classpath", System.getProperty("java.class.path"),
                            "-processorpath", lombok, "-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                            "-d", temporary.toString()), null, manager.getJavaFileObjectsFromFiles(sources)).call());
        }
        loader = new URLClassLoader(new URL[]{temporary.toUri().toURL()}, GeneratedTenantScopeJpaTest.class.getClassLoader());
        requestClass = loader.loadClass("tenant.fixture.services.commons.req.ScopeReq");
    }

    @AfterAll static void closeLoader() throws Exception { if (loader != null) loader.close(); }

    @BeforeEach
    void seed() {
        em.persist(new TenantScopeRow(91001L, "A", false));
        em.persist(new TenantScopeRow(91002L, "A", true));
        em.persist(new TenantScopeRow(91003L, "B", false));
        em.persist(new TenantScopeRow(91004L, "B", true));
        em.persist(new TenantScopeRow(91005L, null, false));
        em.persist(new TenantScopeRow(91006L, null, true));
        em.flush();
        em.clear();
    }

    static Stream<Object[]> cases() {
        List<Object[]> cases = new ArrayList<>();
        for (Role role : Role.values()) {
            for (String target : role == Role.TENANT || role == Role.TENANT_ADMIN ? new String[]{"A"} : new String[]{null, "A"}) {
                for (boolean pub : new boolean[]{false, true}) {
                    for (boolean shared : new boolean[]{false, true}) cases.add(new Object[]{role, target, pub, shared});
                }
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0}, tenant={1}, public={2}, shared={3}")
    @MethodSource("cases")
    void queryAndMutationMustMatchExpectedRecords(Role role, String target, boolean pub, boolean shared) throws Exception {
        Object req = request(role, target, pub, shared);
        Set<Long> expectedRead = new java.util.HashSet<>();
        boolean admin = role == Role.TOP_SUPER || role == Role.SUPER || role == Role.PLATFORM_ADMIN;
        if (target == null) {
            expectedRead.addAll(admin ? Set.of(91001L, 91002L, 91003L, 91004L, 91005L, 91006L) : Set.of(91005L, 91006L));
        } else {
            expectedRead.addAll(Set.of(91001L, 91002L));
            if (admin || pub) expectedRead.addAll(Set.of(91005L, 91006L));
            if (shared) expectedRead.addAll(Set.of(91002L, 91004L, 91006L));
        }
        List<TenantScopeRow> rows = dao.selectFrom(TenantScopeRow.class).appendByQueryObj(req).find(TenantScopeRow.class);
        assertEquals(expectedRead, rows.stream().map(row -> row.id).collect(Collectors.toSet()));

        if (role == Role.PLATFORM && target != null) {
            assertScopeRejected(() -> dao.updateTo(TenantScopeRow.class).appendByQueryObj(req)
                    .eq("id", 91001L).set("label", "changed").update());
            assertScopeRejected(() -> dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).delete());
            assertEquals(6, dao.selectFrom(TenantScopeRow.class).count());
            assertEquals("original", em.find(TenantScopeRow.class, 91001L).label);
            return;
        }
        Set<Long> expectedWrite = new java.util.HashSet<>(admin
                ? (target == null ? Set.of(91001L, 91002L, 91003L, 91004L, 91005L, 91006L) : Set.of(91001L, 91002L, 91005L, 91006L))
                : (role == Role.PLATFORM ? Set.of(91005L, 91006L) : Set.of(91001L, 91002L)));
        if (admin && target != null && shared) expectedWrite.add(91004L);
        for (long id = 91001L; id <= 91006L; id++) {
            // 不匹配的主键/乐观锁必须不能被租户 OR 条件绕过。
            assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", id)
                    .eq("optimisticLock", 99).set("label", "bad").update());
            int expected = expectedWrite.contains(id) ? 1 : 0;
            assertEquals(expected, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", id)
                    .eq("optimisticLock", 0).set("label", "changed").update());
            em.clear();
            assertEquals(expected == 1 ? "changed" : "original", em.find(TenantScopeRow.class, id).label);
            assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", id).eq("optimisticLock", 99).delete());
            assertEquals(expected, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", id).eq("optimisticLock", 0).delete());
        }
        em.clear();
        assertEquals(6 - expectedWrite.size(), dao.selectFrom(TenantScopeRow.class).count());
    }

    @Test
    void missingOrForeignTenantMustFailBeforeAllDatabaseOperations() throws Exception {
        for (Role role : List.of(Role.TENANT, Role.TENANT_ADMIN)) {
            for (String target : new String[]{null, "", " ", "B"}) {
                Object req = request(role, target, true, true);
                assertScopeRejected(() -> dao.selectFrom(TenantScopeRow.class).appendByQueryObj(req).count());
                assertScopeRejected(() -> dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91003L).set("label", "bad").update());
                assertScopeRejected(() -> dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91003L).delete());
            }
        }
        assertEquals(6, dao.selectFrom(TenantScopeRow.class).count());
        assertEquals("original", em.find(TenantScopeRow.class, 91003L).label);
    }

    static void assertScopeRejected(org.junit.jupiter.api.function.Executable operation) {
        RuntimeException failure = assertThrows(RuntimeException.class, operation);
        StringBuilder reasons = new StringBuilder();
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) reasons.append(cause.getMessage());
        assertTrue(reasons.toString().contains("非法的越界访问") || reasons.toString().contains("普通平台用户不能写入租户数据"),
                "必须因租户范围校验而拒绝：" + reasons);
    }

    static Object request(Role role, String tenant, boolean pub, boolean shared) throws Exception {
        Object req = requestClass.getConstructor().newInstance();
        boolean tenantUser = role == Role.TENANT || role == Role.TENANT_ADMIN;
        field(req, "isTenantUser", tenantUser);
        field(req, "isPlatformUser", !tenantUser);
        field(req, "isTopSuperAdmin", role == Role.TOP_SUPER);
        field(req, "isSuperAdmin", role == Role.SUPER);
        field(req, "isPlatformAdmin", role == Role.PLATFORM_ADMIN);
        field(req, "isTenantAdmin", role == Role.TENANT_ADMIN);
        field(req, "isUnsafeContext", true);
        field(req, "_currentUserTenantId", tenantUser ? "A" : null);
        field(req, "tenantId", tenant);
        field(req, "includePublic", pub);
        field(req, "includeShared", shared);
        field(req, "enableDefaultOrderBy", false);
        return req;
    }

    @Test
    void injectedOwnerScopeMustAllowOwnRecordsAndExcludeOtherOwnersAndTenants() throws Exception {
        Object req = loader.loadClass("tenant.fixture.services.commons.req.MultiTenantPersonalReq").getConstructor().newInstance();
        field(req, "isTenantUser", true);
        field(req, "isUnsafeContext", true);
        field(req, "tenantId", "A");
        field(req, "_currentUserTenantId", "A");
        field(req, "_currentUserId", "user-a");
        field(req, "ownerId", "forged-owner");
        field(req, "ownerIdList", List.of("forged-owner", "user-a"));
        SimpleVariableInjector injector = new SimpleVariableInjector() {};
        var context = java.util.Map.<String, Object>of("isSuperAdmin", false, "isPlatformAdmin", false, "isTenantAdmin", false,
                "ownerId", "user-a", "ownerIdList", List.of("user-a"));
        for (String name : List.of("ownerIdList", "ownerId")) {
            var ownerField = req.getClass().getDeclaredField(name);
            ownerField.setAccessible(true);
            injector.injectValue(req, ownerField, context);
        }
        List<TenantScopeRow> rows = dao.selectFrom(TenantScopeRow.class).appendByQueryObj(req).find(TenantScopeRow.class);
        assertEquals(Set.of(91001L), rows.stream().map(row -> row.id).collect(Collectors.toSet()));
        for (long id : new long[]{91001L, 91002L, 91003L, 91005L}) {
            int expected = id == 91001L ? 1 : 0;
            assertEquals(expected, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", id)
                    .set("label", "owner-updated").update());
            assertEquals(expected, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", id).delete());
        }
        assertEquals(5, dao.selectFrom(TenantScopeRow.class).count());
    }

    static void field(Object req, String name, Object value) throws Exception {
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

    @Test
    void singleOrganizationDeleteMustNotDeleteAnotherOrganization() throws Exception {
        Object req = organizationRequest(false, false, null, "org-a");
        assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).delete());
        assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91003L).delete());
        assertEquals(1, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).delete());
        em.clear();
        assertNotNull(em.find(TenantScopeRow.class, 91002L));
        assertEquals(5, dao.selectFrom(TenantScopeRow.class).count());
    }

    @Test
    void contextPersonalAccessMustNotBypassTenantIsolation() throws Exception {
        TenantScopeRow foreign = em.find(TenantScopeRow.class, 91003L);
        foreign.ownerId = "other-owner";
        em.flush();
        em.clear();
        Object req = loader.loadClass("tenant.fixture.services.commons.req.MultiTenantPersonalReq").getConstructor().newInstance();
        field(req, "isTenantUser", true);
        field(req, "isTenantAdmin", true);
        field(req, "isUnsafeContext", true);
        field(req, "tenantId", "A");
        field(req, "_currentUserTenantId", "A");
        field(req, "_currentUserId", "user-a");
        field(req, "ownerId", "other-owner");
        field(req, "ownerIdList", List.of("other-owner"));
        // 仅具备管理员身份但没有上下文授权，仍不能访问其他拥有者。
        assertScopeRejected(() -> dao.selectFrom(TenantScopeRow.class).appendByQueryObj(req).count());
        var permissionField = req.getClass().getSuperclass().getSuperclass().getDeclaredField("isCanAccessAllPersonal");
        permissionField.setAccessible(true);
        new SimpleVariableInjector() {}.injectValue(req, permissionField,
                java.util.Map.of(com.levin.commons.service.support.InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, true));
        List<TenantScopeRow> visible = dao.selectFrom(TenantScopeRow.class).appendByQueryObj(req).find(TenantScopeRow.class);
        assertEquals(Set.of(91002L), visible.stream().map(row -> row.id).collect(Collectors.toSet()));
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91003L).set("label", "bad").update());
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).set("label", "allowed").update());
        assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91003L).delete());
        assertEquals(1, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).delete());
        em.clear();
        assertEquals("original", em.find(TenantScopeRow.class, 91003L).label);
    }

    @Test
    void organizationListMustTakePriorityForDeletion() throws Exception {
        Object req = organizationRequest(false, false, List.of("org-b"), "org-a");
        assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).delete());
        assertEquals(1, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).delete());
    }

    @Test
    void restrictedUpdateCannotUseNewOrganizationAsAuthorizationScope() throws Exception {
        for (List<String> scope : java.util.Arrays.<List<String>>asList(null, List.of(), java.util.Arrays.asList((String) null), List.of(" "))) {
            Object req = organizationRequest(true, false, scope, "org-b");
            RuntimeException error = assertThrows(RuntimeException.class, () -> dao.updateTo(TenantScopeRow.class)
                    .appendByQueryObj(req).eq("id", 91001L).set("label", "bad").update());
            StringBuilder messages = new StringBuilder();
            for (Throwable cause = error; cause != null; cause = cause.getCause()) messages.append(cause.getMessage());
            assertTrue(messages.toString().contains("组织"), messages.toString());
        }
        em.clear();
        TenantScopeRow row = em.find(TenantScopeRow.class, 91001L);
        assertEquals("org-a", row.orgId);
        assertEquals("original", row.label);
    }

    @Test
    void administratorMustSelectOldOrganizationAndSetNewOrganizationSeparately() throws Exception {
        Object req = organizationRequest(true, false, List.of("org-a"), "org-b");
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).update());
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91003L).update());
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).update());
        em.clear();
        assertEquals("org-b", em.find(TenantScopeRow.class, 91001L).orgId);
        assertEquals("org-a", em.find(TenantScopeRow.class, 91003L).orgId);
    }

    @Test
    void allOrganizationAccessDoesNotGrantOrganizationReassignment() throws Exception {
        Object req = organizationRequest(false, true, List.of("org-a"), "org-b");
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).set("label", "changed").update());
        em.clear();
        assertEquals("org-a", em.find(TenantScopeRow.class, 91001L).orgId);
        assertEquals("changed", em.find(TenantScopeRow.class, 91001L).label);
    }

    @Test
    void nonAdministratorUpdateMustUseSingleOrganizationAsWhereInsteadOfSet() throws Exception {
        for (boolean allOrganizations : new boolean[]{false, true}) {
            Object req = organizationRequest(false, allOrganizations, null, "org-a");
            assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).set("label", "wrong").update());
            assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).set("label", "changed").update());
            em.clear();
            assertEquals("org-a", em.find(TenantScopeRow.class, 91001L).orgId);
            assertEquals("org-b", em.find(TenantScopeRow.class, 91002L).orgId);
            assertEquals("original", em.find(TenantScopeRow.class, 91002L).label);
        }
    }

    private static Object organizationRequest(boolean admin, boolean allOrganizations, List<String> organizations, String organization) throws Exception {
        Object req = loader.loadClass("tenant.fixture.services.commons.req.MultiTenantOrgReq").getConstructor().newInstance();
        field(req, "isTenantUser", true);
        field(req, "isTenantAdmin", admin);
        field(req, "isCanAccessAllOrg", allOrganizations);
        field(req, "isUnsafeContext", true);
        field(req, "tenantId", "A");
        field(req, "_currentUserTenantId", "A");
        field(req, "orgIdList", organizations);
        field(req, "orgId", organization);
        field(req, "enableDefaultOrderBy", false);
        return req;
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void authorizedNonAdminMustUseSingleOwnerForUpdateAndDelete(String template) throws Exception {
        Object req = personalRequest(template, false, true, null, "other-owner");
        List<TenantScopeRow> selected = dao.selectFrom(TenantScopeRow.class).appendByQueryObj(req).find(TenantScopeRow.class);
        assertEquals(Set.of(91002L), selected.stream().map(row -> row.id).collect(Collectors.toSet()));
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).set("label", "wrong").update());
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).set("label", "allowed").update());
        em.clear();
        assertEquals("other-owner", em.find(TenantScopeRow.class, 91002L).ownerId);
        assertEquals("original", em.find(TenantScopeRow.class, 91001L).label);
        assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).delete());
        assertEquals(1, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).delete());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void ownerListMustTakePriorityAndNonAdminMustNotReassign(String template) throws Exception {
        Object req = personalRequest(template, false, true, List.of("user-a"), "other-owner");
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).set("label", "wrong").update());
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).set("label", "allowed").update());
        em.clear();
        assertEquals("user-a", em.find(TenantScopeRow.class, 91001L).ownerId);
        assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).delete());
        assertEquals(1, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).delete());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void administratorMustSeparateOldOwnerScopeFromNewOwnership(String template) throws Exception {
        Object req = personalRequest(template, true, true, List.of("user-a"), "other-owner");
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).update());
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91003L).update());
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).update());
        em.clear();
        assertEquals("other-owner", em.find(TenantScopeRow.class, 91001L).ownerId);
        assertEquals("user-a", em.find(TenantScopeRow.class, 91003L).ownerId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void restrictedAdministratorCannotUseNewOwnerAsOldRecordScope(String template) throws Exception {
        Object noOldScope = personalRequest(template, true, false, null, "user-a");
        assertPersonalScopeRejected(() -> dao.updateTo(TenantScopeRow.class).appendByQueryObj(noOldScope).eq("id", 91002L).update());
        Object ownScope = personalRequest(template, true, false, List.of("user-a"), "other-owner");
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(ownScope).eq("id", 91002L).update());
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(ownScope).eq("id", 91001L).update());
    }

    @Test
    void personalAuthorizationMustKeepParentOrganizationRestriction() throws Exception {
        em.find(TenantScopeRow.class, 91001L).ownerId = "other-owner";
        em.flush();
        em.clear();
        Object req = personalRequest("MultiTenantOrgPersonalReq", false, true, null, "other-owner");
        field(req, "orgIdList", List.of("org-a"));
        // 两条记录同租户、同拥有者，但分别属于 org-a 与 org-b；组织过滤不能靠租户过滤代为保护。
        List<TenantScopeRow> selected = dao.selectFrom(TenantScopeRow.class).appendByQueryObj(req).find(TenantScopeRow.class);
        assertEquals(Set.of(91001L), selected.stream().map(row -> row.id).collect(Collectors.toSet()));
        assertEquals(0, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).set("label", "wrong").update());
        assertEquals(1, dao.updateTo(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).set("label", "allowed").update());
        assertEquals(0, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91002L).delete());
        assertEquals(1, dao.deleteFrom(TenantScopeRow.class).appendByQueryObj(req).eq("id", 91001L).delete());
        em.clear();
        assertEquals("original", em.find(TenantScopeRow.class, 91002L).label);
    }

    private static Object personalRequest(String template, boolean admin, boolean access, List<String> owners, String owner) throws Exception {
        Object req = loader.loadClass("tenant.fixture.services.commons.req." + template).getConstructor().newInstance();
        field(req, "isTenantUser", true);
        field(req, "isTenantAdmin", admin);
        field(req, "isUnsafeContext", true);
        field(req, "tenantId", "A");
        field(req, "_currentUserTenantId", "A");
        field(req, "_currentUserId", "user-a");
        field(req, "isCanAccessAllPersonal", access);
        field(req, "ownerIdList", owners);
        field(req, "ownerId", owner);
        if (template.equals("MultiTenantOrgPersonalReq")) field(req, "orgIdList", List.of("org-a", "org-b"));
        return req;
    }

    private static void assertPersonalScopeRejected(org.junit.jupiter.api.function.Executable operation) {
        RuntimeException error = assertThrows(RuntimeException.class, operation);
        StringBuilder causes = new StringBuilder();
        for (Throwable cause = error; cause != null; cause = cause.getCause()) causes.append(cause.getMessage());
        assertTrue(causes.toString().contains("拥有者") || causes.toString().contains("个人数据"), causes.toString());
    }

    @Entity(name = "GeneratedTenantScopeRow")
    @Table(name = "dao_generated_tenant_scope_test")
    public static class TenantScopeRow {
        @Id public Long id;
        @Column(name = "tenant_id") public String tenantId;
        @Column(name = "tenant_shared") public boolean tenantShared;
        @Column(name = "optimistic_lock") public int optimisticLock;
        @Column(name = "owner_id") public String ownerId;
        @Column(name = "org_id") public String orgId;
        public String label;
        public TenantScopeRow() { }
        TenantScopeRow(Long id, String tenant, boolean shared) {
            this.id = id; tenantId = tenant; tenantShared = shared; label = "original";
            ownerId = id == 91002L ? "other-owner" : "user-a";
            orgId = id == 91002L ? "org-b" : "org-a";
        }
    }
}
