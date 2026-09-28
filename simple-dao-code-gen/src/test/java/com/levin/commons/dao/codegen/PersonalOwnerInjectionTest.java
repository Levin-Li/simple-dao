package com.levin.commons.dao.codegen;

import com.levin.commons.plugins.Utils;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import com.levin.commons.service.support.SimpleVariableInjector;
import com.levin.commons.service.support.VariableInjector;
import java.lang.reflect.Field;
import lombok.Data;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 编译实际模板，校验入口层 default 域对拥有者字段的注入契约。 */
class PersonalOwnerInjectionTest {
    @TempDir static Path tempDir;
    private static URLClassLoader loader;
    private static final String PACKAGE = "com.example.owner.services.commons.req.";

    @BeforeAll
    static void compileTemplates() throws Exception {
        List<File> sources = new ArrayList<>();
        for (String name : List.of("BaseReq", "MultiTenantReq", "MultiTenantOrgReq",
                "MultiTenantPersonalReq", "MultiTenantOrgPersonalReq")) {
            File source = tempDir.resolve(name + ".java").toFile();
            Utils.copyAndReplace(tempDir.toString(), true,
                    "simple.dao/codegen/template/services/commons/req/" + name + ".java", source,
                    Map.of("modulePackageName", "com.example.owner"));
            sources.add(source);
        }
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        Path output = Files.createDirectories(tempDir.resolve("classes"));
        String lombokPath = Path.of(Data.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        try (var manager = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, manager, null, List.of(
                    "-classpath", System.getProperty("java.class.path"),
                    "-processorpath", lombokPath, "-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                    "-d", output.toString()), null, manager.getJavaFileObjectsFromFiles(sources)).call());
        }
        loader = new URLClassLoader(new URL[]{output.toUri().toURL()}, PersonalOwnerInjectionTest.class.getClassLoader());
    }

    @AfterAll
    static void closeLoader() throws Exception {
        if (loader != null) loader.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void bothOwnerFieldsMustDeclareContextInjection(String template) throws Exception {
        Class<?> type = loader.loadClass(PACKAGE + template);
        for (String name : List.of("ownerId", "ownerIdList")) {
            InjectVar injection = type.getDeclaredField(name).getAnnotation(InjectVar.class);
            assertNotNull(injection, template + "." + name + " 必须声明上下文注入");
            assertArrayEquals(new String[]{"default"}, injection.domain());
            assertTrue(injection.value().isEmpty() || injection.value().equals(name), "使用字段同名上下文键");
            String restricted = InjectVar.SPEL_PREFIX + type.getField("EXPR_NOT_IS_CAN_ACCESS_ALL_PERSONAL").get(null);
            assertEquals(restricted, injection.isOverride(), "覆盖条件应由可信个人访问权限决定");
            assertEquals(restricted, injection.isRequired(), "必填条件应由可信个人访问权限决定");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void trustedPersonalPermissionMustDetermineInjectionRegardlessOfRoleOrFieldOrder(String template) throws Exception {
        for (String role : List.of("ordinary", "isSuperAdmin", "isPlatformAdmin", "isTenantAdmin")) {
            for (boolean personalAccess : new boolean[]{false, true}) {
                for (boolean ownersFirst : new boolean[]{true, false}) {
                    Object request = loader.loadClass(PACKAGE + template).getConstructor().newInstance();
                    boolean admin = !role.equals("ordinary");
                    boolean tenantUser = role.equals("ordinary") || role.equals("isTenantAdmin");
                    // 请求伪造相反的身份及个人访问权限；覆盖判断只能读取可信上下文。
                    for (String identity : List.of("isSuperAdmin", "isPlatformAdmin", "isTenantAdmin")) field(request, identity).set(request, !admin);
                    field(request, "isCanAccessAllPersonal").set(request, !personalAccess);
                    field(request, "ownerId").set(request, "selected-owner");
                    field(request, "ownerIdList").set(request, List.of("selected-owner"));
                    Map<String, Object> context = new java.util.HashMap<>(Map.of(
                            "isSuperAdmin", role.equals("isSuperAdmin"), "isPlatformAdmin", role.equals("isPlatformAdmin"),
                            "isTenantAdmin", role.equals("isTenantAdmin"),
                            "isTenantUser", tenantUser, "isPlatformUser", !tenantUser,
                            InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, personalAccess,
                            "ownerId", "current-user", "ownerIdList", List.of("current-user")));
                    List<String> sequence = ownersFirst
                            ? List.of("ownerId", "ownerIdList", "isCanAccessAllPersonal", "isTenantUser", "isPlatformUser", "isSuperAdmin", "isPlatformAdmin", "isTenantAdmin")
                            : List.of("isTenantUser", "isPlatformUser", "isSuperAdmin", "isPlatformAdmin", "isTenantAdmin", "isCanAccessAllPersonal", "ownerIdList", "ownerId");
                    inject(request, context, sequence);
                    String expected = personalAccess ? "selected-owner" : "current-user";
                    assertEquals(expected, field(request, "ownerId").get(request), role + " personalAccess=" + personalAccess + " ownersFirst=" + ownersFirst);
                    assertEquals(List.of(expected), field(request, "ownerIdList").get(request));
                    assertEquals(personalAccess, request.getClass().getMethod("isCanAccessAllPersonal").invoke(request));
                    assertEquals(admin, request.getClass().getMethod("isAdmin").invoke(request), "测试角色必须实际生效：" + role);
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void restrictedPersonalAccessMustRequireBothTrustedOwnerValuesEvenForAdmins(String template) throws Exception {
        for (boolean admin : new boolean[]{false, true}) {
            for (boolean missingPermission : new boolean[]{false, true}) {
                for (String missing : List.of("ownerId", "ownerIdList")) {
                    Object request = loader.loadClass(PACKAGE + template).getConstructor().newInstance();
                    field(request, "isCanAccessAllPersonal").set(request, true);
                    field(request, "isTenantUser").set(request, true);
                    field(request, "isTenantAdmin").set(request, admin);
                    assertEquals(admin, request.getClass().getMethod("isAdmin").invoke(request));
                    field(request, "ownerId").set(request, "forged");
                    field(request, "ownerIdList").set(request, List.of("forged"));
                    Map<String, Object> context = new java.util.HashMap<>(Map.of(
                            "isSuperAdmin", false, "isPlatformAdmin", false, "isTenantAdmin", admin,
                            "ownerId", "current-user", "ownerIdList", List.of("current-user")));
                    if (!missingPermission) context.put(InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, false);
                    context.remove(missing);
                    RuntimeException failure = assertThrows(RuntimeException.class, () -> inject(request, context, List.of(missing)));
                    assertTrue(failure.getMessage().contains(missing), failure.toString());
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void authorizedPersonalAccessMayUseDefaultsOrRemainUnscopedWithoutAdminRole(String template) throws Exception {
        Object request = loader.loadClass(PACKAGE + template).getConstructor().newInstance();
        Map<String, Object> defaults = Map.of(InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, true, "ownerId", "provided-owner",
                "ownerIdList", List.of("provided-owner"));
        inject(request, defaults, List.of("ownerId", "ownerIdList"));
        assertEquals("provided-owner", field(request, "ownerId").get(request));
        assertEquals(List.of("provided-owner"), field(request, "ownerIdList").get(request));
        Object unscoped = loader.loadClass(PACKAGE + template).getConstructor().newInstance();
        inject(unscoped, Map.of(InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, true), List.of("ownerId", "ownerIdList"));
        assertNull(field(unscoped, "ownerId").get(unscoped));
        assertNull(field(unscoped, "ownerIdList").get(unscoped));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void personalAccessMustComeOnlyFromTrustedContext(String template) throws Exception {
        Object request = loader.loadClass(PACKAGE + template).getConstructor().newInstance();
        field(request, "isTenantUser").set(request, false);
        field(request, "isPlatformUser").set(request, true);
        field(request, "isTopSuperAdmin").set(request, true);
        field(request, "_confidentialDataAccessLevel").set(request, Integer.MAX_VALUE);
        var permission = request.getClass().getMethod("isCanAccessAllPersonal");
        assertEquals(false, permission.invoke(request), "角色和保密级别不能自动授予个人数据访问权限");

        field(request, "isCanAccessAllPersonal").set(request, true);
        inject(request, Map.of(InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, false), List.of("isCanAccessAllPersonal"));
        assertEquals(false, permission.invoke(request), "服务端 false 必须覆盖请求中的 true");
        field(request, "isTopSuperAdmin").set(request, false);
        field(request, "_confidentialDataAccessLevel").set(request, null);
        inject(request, Map.of(InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, true), List.of("isCanAccessAllPersonal"));
        assertEquals(true, permission.invoke(request), "权限由可信上下文独立决定");
        inject(request, Map.of(), List.of("isCanAccessAllPersonal"));
        assertEquals(false, permission.invoke(request), "缺失授权必须恢复为 false，不能沿用旧值");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void contextPermissionMustGrantNonAdminDeletionWithinAuthorizedOwnerScope(String template) throws Exception {
        Object request = loader.loadClass(PACKAGE + template).getConstructor().newInstance();
        field(request, "isUnsafeContext").set(request, true);
        field(request, "_currentUserId").set(request, "current-user");
        field(request, "ownerId").set(request, "current-user");
        field(request, "ownerIdList").set(request, List.of("current-user"));
        inject(request, Map.of(), List.of("isCanAccessAllPersonal"));
        var condition = request.getClass().getMethod("ownerIdListCondition", boolean.class, boolean.class);
        assertEquals(true, condition.invoke(request, true, false), "权限 false 仍允许在合法范围内访问自己");
        field(request, "ownerId").set(request, "other-user");
        field(request, "ownerIdList").set(request, List.of("other-user"));
        var denied = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> condition.invoke(request, true, false));
        assertInstanceOf(IllegalStateException.class, denied.getCause());
        inject(request, Map.of(InjectConst.IS_CAN_ACCESS_ALL_PERSONAL, true), List.of("isCanAccessAllPersonal"));
        assertEquals(true, condition.invoke(request, true, false));
        assertEquals(true, condition.invoke(request, false, true), "明确个人访问授权也允许非管理员删除授权范围内记录");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void restrictedScopeMustUseOwnerListBeforeSingleOwnerForEveryAction(String template) throws Exception {
        for (boolean admin : new boolean[]{false, true}) {
            for (Action action : Action.values()) {
                for (String singleOwner : new String[]{null, "", " ", "another-owner", "current-user"}) {
                    Object request = scopeRequest(template, admin, false, singleOwner, List.of("current-user"));
                    assertOwnerConditions(request, action, true, false);
                }
                for (List<String> illegalOwners : List.of(List.of("other-user"), List.of("current-user", "other-user"),
                        List.of(""), java.util.Collections.<String>singletonList(null))) {
                    assertScopeRejected(scopeRequest(template, admin, false, "current-user", illegalOwners), action);
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void restrictedAdminUpdateMustHaveOldOwnerListSeparateFromNewOwner(String template) throws Exception {
        for (List<String> noList : java.util.Arrays.<List<String>>asList(null, List.of())) {
            for (String singleOwner : new String[]{null, "", " ", "another-owner", "current-user"}) {
                Object admin = scopeRequest(template, true, false, singleOwner, noList);
                assertScopeRejected(admin, Action.UPDATE);
                for (Action action : List.of(Action.QUERY, Action.DELETE)) {
                    if ("current-user".equals(singleOwner)) assertOwnerConditions(admin, action, false, true);
                    else assertScopeRejected(admin, action);
                }
                Object ordinary = scopeRequest(template, false, false, singleOwner, noList);
                for (Action action : Action.values()) {
                    if ("current-user".equals(singleOwner)) assertOwnerConditions(ordinary, action, false, true);
                    else assertScopeRejected(ordinary, action);
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"MultiTenantPersonalReq", "MultiTenantOrgPersonalReq"})
    void authorizedScopeMustApplyToReadUpdateAndDeleteButAdminUpdateOwnerIsOnlyNewValue(String template) throws Exception {
        for (boolean admin : new boolean[]{false, true}) {
            for (Action action : Action.values()) {
                assertOwnerConditions(scopeRequest(template, admin, true, "new-owner", List.of("old-owner")), action, true, false);
                assertOwnerConditions(scopeRequest(template, admin, true, "selected-owner", null), action, false,
                        !(admin && action == Action.UPDATE));
                assertOwnerConditions(scopeRequest(template, admin, true, null, null), action, false, false);
            }
        }
    }

    private enum Action {
        QUERY(true, false), UPDATE(false, false), DELETE(false, true);
        final boolean query;
        final boolean delete;
        Action(boolean query, boolean delete) { this.query = query; this.delete = delete; }
    }

    private static Object scopeRequest(String template, boolean admin, boolean personalAccess,
                                       String owner, List<String> owners) throws Exception {
        Object request = loader.loadClass(PACKAGE + template).getConstructor().newInstance();
        field(request, "isUnsafeContext").set(request, true);
        field(request, "isTenantUser").set(request, true);
        field(request, "isPlatformUser").set(request, false);
        field(request, "isTenantAdmin").set(request, admin);
        assertEquals(admin, request.getClass().getMethod("isAdmin").invoke(request), "个人范围测试必须使用有效管理员身份");
        field(request, "isCanAccessAllPersonal").set(request, personalAccess);
        field(request, "_currentUserId").set(request, "current-user");
        field(request, "ownerId").set(request, owner);
        field(request, "ownerIdList").set(request, owners);
        return request;
    }

    private static void assertOwnerConditions(Object request, Action action, boolean list, boolean single) throws Exception {
        String description = action + " admin=" + request.getClass().getMethod("isAdmin").invoke(request);
        assertEquals(list, request.getClass().getMethod("ownerIdListCondition", boolean.class, boolean.class)
                .invoke(request, action.query, action.delete), description);
        assertEquals(single, request.getClass().getMethod("ownerIdCondition", boolean.class, boolean.class)
                .invoke(request, action.query, action.delete), description);
    }

    private static void assertScopeRejected(Object request, Action action) throws Exception {
        for (String method : List.of("ownerIdListCondition", "ownerIdCondition")) {
            var condition = request.getClass().getMethod(method, boolean.class, boolean.class);
            var failure = assertThrows(java.lang.reflect.InvocationTargetException.class,
                    () -> condition.invoke(request, action.query, action.delete), method + " " + action);
            assertInstanceOf(IllegalStateException.class, failure.getCause());
            assertTrue(failure.getCause().getMessage().contains("越界"), failure.getCause().toString());
        }
    }

    private static void inject(Object request, Map<String, Object> context, List<String> sequence) throws Exception {
        SimpleVariableInjector injector = new SimpleVariableInjector() {};
        // 与模板 Controller 切面一致：请求对象解析器在前，服务端身份解析器在后。
        var resolvers = List.of(VariableInjector.newResolverByMap((Object) null, Map.of("_this", request), Map.of()),
                VariableInjector.newResolverByMap((Object) null, context));
        VariableInjector.setVariableResolversForCurrentThread(new ArrayList<>(resolvers));
        try {
            for (String name : sequence) injector.injectValue(request, field(request, name), new ArrayList<>(resolvers));
        } finally {
            VariableInjector.setVariableResolversForCurrentThread(null);
        }
    }

    private static Field field(Object request, String name) throws Exception {
        for (Class<?> type = request.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }
}
