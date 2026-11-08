package com.levin.commons.dao.codegen;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.IsNull;
import com.levin.commons.plugins.Utils;
import com.levin.commons.rbac.DataMasking;
import com.levin.commons.service.domain.InjectVar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MultiTenantMaskingTemplateTest {

    @Test
    void renderedTenantFieldsKeepScopeButHaveNoMasking(@TempDir Path output) throws Exception {
        String module = "com.example.tenantmask";
        Path root = output.resolve("src");
        List<Path> sources = new ArrayList<>();
        for (String name : List.of("BaseReq.java", "MultiTenantReq.java"))
            sources.add(render(root, module, "req", name));
        for (String name : List.of("BaseInfo.java", "MultiTenantInfo.java"))
            sources.add(render(root, module, "info", name));

        String requestSource = Files.readString(sources.get(1));
        String infoSource = Files.readString(sources.get(3));
        assertFalse(requestSource.contains("DataMasking"), requestSource);
        assertFalse(infoSource.contains("DataMasking"), infoSource);
        assertTrue(requestSource.contains("@InjectVar(value = InjectConst.TENANT_ID"), requestSource);
        assertTrue(requestSource.contains("tenantIsNullCondition(#_isQuery)"), requestSource);
        assertTrue(infoSource.contains("@RefInject(refObjectType = \"Tenant\""), infoSource);

        Path classes = output.resolve("classes");
        Files.createDirectories(classes);
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        try (var manager = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, manager, null,
                    List.of("-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                            "-classpath", System.getProperty("java.class.path"), "-d", classes.toString()),
                    null, manager.getJavaFileObjectsFromPaths(sources)).call());
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, getClass().getClassLoader())) {
            var requestTenant = loader.loadClass(module + ".services.commons.req.MultiTenantReq")
                    .getDeclaredField("tenantId");
            assertNull(requestTenant.getAnnotation(DataMasking.class));
            assertNotNull(requestTenant.getAnnotation(InjectVar.class));
            assertTrue(requestTenant.getAnnotationsByType(Eq.class).length > 0);
            assertNotNull(requestTenant.getAnnotation(IsNull.class));

            Class<?> info = loader.loadClass(module + ".services.commons.info.MultiTenantInfo");
            var infoTenant = info.getDeclaredField("tenantId");
            var infoName = info.getDeclaredField("tenantName");
            assertNull(infoTenant.getAnnotation(DataMasking.class));
            assertNull(infoName.getAnnotation(DataMasking.class));
            assertTrue(List.of(infoName.getDeclaredAnnotations()).stream()
                    .anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("RefInject")));
        }
    }

    private static Path render(Path root, String module, String kind, String name) throws Exception {
        Path target = root.resolve(module.replace('.', '/') + "/services/commons/" + kind + "/" + name);
        Files.createDirectories(target.getParent());
        Utils.copyAndReplace(root.toString(), true,
                "simple.dao/codegen/template/services/commons/" + kind + "/" + name,
                target.toFile(), Map.of("modulePackageName", module));
        return target;
    }
}
