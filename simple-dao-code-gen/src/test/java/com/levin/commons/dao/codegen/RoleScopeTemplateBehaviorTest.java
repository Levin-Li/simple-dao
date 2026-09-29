package com.levin.commons.dao.codegen;

import com.levin.commons.plugins.Utils;
import com.levin.commons.service.support.InjectConst;
import com.levin.commons.service.support.SimpleVariableInjector;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleScopeTemplateBehaviorTest {

    @TempDir
    static Path tempDir;

    private static Class<?> requestType;
    private static URLClassLoader loader;

    @BeforeAll
    static void compileGeneratedRequestTypes() throws Exception {
        Path sourceRoot = tempDir.resolve("src");
        Path packageDir = sourceRoot.resolve("com/example/generated/services/commons/req");
        Files.createDirectories(packageDir);

        for (String template : List.of("BaseReq.java", "MultiTenantReq.java")) {
            Utils.copyAndReplace(sourceRoot.toString(), true,
                    "simple.dao/codegen/template/services/commons/req/" + template,
                    packageDir.resolve(template).toFile(), Map.of("modulePackageName", "com.example.generated"));
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue(compiler != null, "测试必须使用 JDK 编译器");
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            String lombokPath = new File(Data.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            boolean compiled = compiler.getTask(null, fileManager, null,
                    List.of(
                            "-classpath", System.getProperty("java.class.path"),
                            "-processorpath", lombokPath,
                            "-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                            "-d", classesDir.toString()),
                    null, fileManager.getJavaFileObjectsFromFiles(List.of(
                            packageDir.resolve("BaseReq.java").toFile(),
                            packageDir.resolve("MultiTenantReq.java").toFile())))
                    .call();
            assertTrue(compiled, "生成的请求对象必须可编译");
        }

        loader = new URLClassLoader(new URL[]{classesDir.toUri().toURL()}, RoleScopeTemplateBehaviorTest.class.getClassLoader());
        requestType = Class.forName("com.example.generated.services.commons.req.MultiTenantReq", true, loader);
    }

    @AfterAll
    static void closeGeneratedClasses() throws Exception {
        if (loader != null) loader.close();
    }

    @Test
    void topSuperAdminShouldUseSameTenantScopeForReadAndWrite() throws Exception {
        Object request = newRequest();
        setField(request, "isPlatformUser", true);
        setField(request, "isTopSuperAdmin", true);

        assertTrue((Boolean) requestType.getMethod("isSuperAdmin").invoke(request));
        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));
        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, false));

        setField(request, "tenantId", "tenant-a");
        assertTrue((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));
        assertTrue((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, false));
    }

    @Test
    void platformAdminShouldUseSameTenantScopeForReadAndWrite() throws Exception {
        Object request = newRequest();
        setField(request, "isPlatformUser", true);
        setField(request, "isPlatformAdmin", true);

        assertTrue((Boolean) requestType.getMethod("isPlatformAdmin").invoke(request));
        assertTrue((Boolean) requestType.getMethod("isAdmin").invoke(request));
        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));

        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, false));
        setField(request, "tenantId", "tenant-a");
        assertTrue((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));
        assertTrue((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, false));

        setField(request, "isPlatformUser", false);
        assertFalse((Boolean) requestType.getMethod("isPlatformAdmin").invoke(request));
    }

    @Test
    void tenantAdminShouldRemainBoundToCurrentTenant() throws Exception {
        Object request = newRequest();
        setField(request, "isTenantUser", true);
        setField(request, "isTenantAdmin", true);
        setField(request, "tenantId", "tenant-a");
        setField(request, "_currentUserTenantId", "tenant-a");

        assertTrue((Boolean) requestType.getMethod("isTenantAdmin").invoke(request));
        assertTrue((Boolean) requestType.getMethod("isAdmin").invoke(request));
        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));
        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, false));

        setField(request, "tenantId", "tenant-b");
        assertTenantRejected(request, true);
        assertTenantRejected(request, false);

        setField(request, "tenantId", null);
        assertTenantRejected(request, false);
    }

    @Test
    void ordinaryTenantUserShouldNotGainAdminOrPlatformScope() throws Exception {
        Object request = newRequest();
        setField(request, "isTenantUser", true);
        setField(request, "tenantId", "tenant-a");
        setField(request, "_currentUserTenantId", "tenant-a");

        assertFalse((Boolean) requestType.getMethod("isAdmin").invoke(request));
        assertFalse((Boolean) requestType.getMethod("isPlatformUser").invoke(request));
        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));
        assertFalse((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, false));

        setField(request, "tenantId", "tenant-b");
        assertTenantRejected(request, false);
    }

    @Test
    void ordinaryPlatformUserShouldOnlySeePlatformDataWithoutTenantScope() throws Exception {
        Object request = newRequest();
        setField(request, "isPlatformUser", true);

        assertFalse((Boolean) requestType.getMethod("isAdmin").invoke(request));
        assertTrue((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));
        assertTrue((Boolean) requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, false));
    }

    @Test
    void missingPlatformAndTenantIdentityMustBeRejected() throws Exception {
        Object request = newRequest();

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, true));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("必须是平台用户或租户用户"));

        exception = assertThrows(InvocationTargetException.class,
                () -> requestType.getMethod("tenantSharedCondition", boolean.class).invoke(request, true));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("必须是平台用户或租户用户"));
    }

    private static Object newRequest() throws Exception {
        Object request = requestType.getConstructor().newInstance();
        // 各身份场景模拟外部、不可信入口；平台用户不能依赖请求对象的租户默认值。
        setField(request, "isUnsafeContext", true);
        setField(request, "isTenantUser", false);
        return request;
    }

    @Test
    void platformAdminMustInjectFromNewContextKeyAndRemoveOldConstant() throws Exception {
        Object request = newRequest();
        setField(request, "isPlatformUser", true);
        var adminField = requestType.getSuperclass().getDeclaredField("isPlatformAdmin");
        adminField.setAccessible(true);
        SimpleVariableInjector injector = new SimpleVariableInjector() {};
        injector.injectValue(request, adminField, Map.of(InjectConst.IS_PLATFORM_ADMIN, true));
        assertTrue((Boolean) requestType.getMethod("isPlatformAdmin").invoke(request));
        assertThrows(NoSuchMethodException.class, () -> requestType.getMethod("isSaasAdmin"));
        assertThrows(NoSuchFieldException.class, () -> requestType.getSuperclass().getDeclaredField("isSaasAdmin"));
        assertEquals("(#isPlatformAdmin?:false)", ((String) requestType.getField("EXPR_IS_PLATFORM_ADMIN").get(null)).trim());
        assertThrows(NoSuchFieldException.class, () -> requestType.getField("IS_PLATFORM_ADMIN"));
        assertThrows(NoSuchFieldException.class, () -> requestType.getField("IS_SAAS_ADMIN"));
        injector.injectValue(request, adminField, Map.of("isSaasAdmin", true));
        assertFalse((Boolean) requestType.getMethod("isPlatformAdmin").invoke(request), "旧上下文键不能被当作平台管理员权限");
    }

    @Test
    void defaultTenantIdentityMustPreventImplicitPlatformPrivileges() throws Exception {
        Object request = requestType.getConstructor().newInstance();
        assertTrue((Boolean) requestType.getMethod("isTenantUser").invoke(request));
        setField(request, "isPlatformUser", true);
        setField(request, "isTopSuperAdmin", true);
        setField(request, "isSuperAdmin", true);
        setField(request, "isPlatformAdmin", true);
        for (String method : List.of("isPlatformUser", "isTopSuperAdmin", "isSuperAdmin", "isPlatformAdmin")) {
            assertFalse((Boolean) requestType.getMethod(method).invoke(request), method);
        }
        setField(request, "isTenantUser", false);
        assertTrue((Boolean) requestType.getMethod("isPlatformUser").invoke(request));
        assertTrue((Boolean) requestType.getMethod("isSuperAdmin").invoke(request));
        setField(request, "isPlatformUser", false);
        assertFalse((Boolean) requestType.getMethod("isSuperAdmin").invoke(request));

        var tenantField = requestType.getSuperclass().getDeclaredField("isTenantUser");
        tenantField.setAccessible(true);
        SimpleVariableInjector injector = new SimpleVariableInjector() {};
        injector.injectValue(request, tenantField, Map.of());
        assertTrue((Boolean) tenantField.get(request), "缺失上下文身份时按租户用户处理");
        injector.injectValue(request, tenantField, Map.of(InjectConst.IS_TENANT_USER, false));
        assertFalse((Boolean) tenantField.get(request));
    }

    @Test
    void tenantInjectionMustOverrideForgedTargetAndRequireTrustedTenant() throws Exception {
        // 身份/租户由入口层的 default 域注入器注入；DAO 域仅负责 DAO 字段转换。
        SimpleVariableInjector injector = new SimpleVariableInjector() {};
        var tenantField = requestType.getDeclaredField("tenantId");
        tenantField.setAccessible(true);
        for (boolean tenantAdmin : new boolean[]{false, true}) {
            Object request = newRequest();
            setField(request, "isTenantUser", true);
            setField(request, "isTenantAdmin", tenantAdmin);
            setField(request, "tenantId", "tenant-b");
            Map<String, Object> context = new java.util.HashMap<>(Map.of(
                    InjectConst.IS_PLATFORM_USER, false,
                    InjectConst.IS_TENANT_USER, true,
                    InjectConst.IS_SUPER_ADMIN, false,
                    InjectConst.IS_PLATFORM_ADMIN, false,
                    InjectConst.IS_TENANT_ADMIN, tenantAdmin,
                    InjectConst.TENANT_ID, "tenant-a"));
            injector.injectValue(request, tenantField, context);
            assertEquals("tenant-a", tenantField.get(request));

            context.remove(InjectConst.TENANT_ID);
            setField(request, "tenantId", "tenant-b");
            assertThrows(RuntimeException.class, () -> injector.injectValue(request, tenantField, context));
        }
    }

    private static void assertTenantRejected(Object request, boolean query) {
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> requestType.getMethod("tenantIsNullCondition", boolean.class).invoke(request, query));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("非法的越界访问"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
