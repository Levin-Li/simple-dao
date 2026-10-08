package com.levin.commons.dao.codegen;

import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.codegen.model.FieldModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateQueryTemplateTest {
    @TempDir Path dir;

    @Test
    void localDateEqualityMustCompileAlongsideExistingRanges() throws Exception {
        List<FieldModel> fields = new ArrayList<>();
        for (var field : Sample.class.getDeclaredFields()) {
            FieldModel model = new FieldModel(Sample.class).setField(field).setName(field.getName())
                    .setType(field.getType()).setTypeName(field.getType().getCanonicalName())
                    .setTitle(field.getName()).setSchemaDescUseConstRef(false);
            model.addAnnotation(field.getAnnotations());
            fields.add(model);
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("packageName", "probe.req");
        params.put("modulePackageName", "probe");
        params.put("entityClassPackage", "probe.entities");
        params.put("entityClassName", "probe.entities.Sample");
        params.put("entityName", "Sample");
        params.put("entityTitle", "日期测试");
        params.put("servicePackageName", "probe.services");
        params.put("className", "QuerySampleReq");
        params.put("reqExtendClass", "BaseReq");
        params.put("serialVersionUID", "1");
        params.put("fields", fields);
        params.put("importList", Set.of());
        params.put("requestImplementsListStr", "");
        params.put("classModel", new ClassModel(Sample.class).setFieldModels(fields));
        Path request = dir.resolve("probe/req/QuerySampleReq.java");
        ServiceModelCodeGenerator.genFileByTemplate(ServiceModelCodeGenerator.QUERY_EVT_FTL, params, request.toString());
        List<Path> sources = new ArrayList<>(List.of(request));
        sources.add(source("probe/entities/Sample.java", "package probe.entities; public class Sample {}"));
        sources.add(source("probe/entities/E_Sample.java", "package probe.entities; public class E_Sample { public static final String ALIAS = \"s\"; }"));
        sources.add(source("probe/entities/EntityConst.java", "package probe.entities; public class EntityConst { public static final String QUERY_ACTION = \"查询\", BIZ_NAME = \"测试\"; }"));
        sources.add(source("probe/services/info/SampleInfo.java", "package probe.services.info; public class SampleInfo {}"));
        sources.add(source("probe/services/commons/req/BaseReq.java", "package probe.services.commons.req; public class BaseReq { protected <T extends BaseReq> T checkSQLInject(String... values) { return (T)this; } protected <T extends BaseReq> T checkSQLInject(Iterable<String> values) { return (T)this; } }"));
        sources.add(source("com/levin/commons/dao/support/ProbeSupport.java", "package com.levin.commons.dao.support; public class ProbeSupport {}"));
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        try (var fm = compiler.getStandardFileManager(null, null, null)) {
            assertTrue(compiler.getTask(null, fm, null,
                    List.of("-proc:none", "-classpath", System.getProperty("java.class.path"), "-d", dir.toString()),
                    null, fm.getJavaFileObjectsFromPaths(sources)).call(), Files.readString(request));
        }
        try (var loader = new URLClassLoader(new java.net.URL[]{dir.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> generated = loader.loadClass("probe.req.QuerySampleReq");
            assertEquals(LocalDate.class, generated.getDeclaredField("day").getType());
            assertNotNull(generated.getDeclaredField("day").getAnnotation(Eq.class));
            assertEquals("otherDay", generated.getDeclaredField("annotatedDay").getAnnotation(Eq.class).value());
            for (String name : List.of("day", "annotatedDay", "timestamp", "legacyDate")) {
                String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                assertNotNull(generated.getDeclaredField("between" + cap));
                assertNotNull(generated.getDeclaredField("gte" + cap));
                assertNotNull(generated.getDeclaredField("lte" + cap));
            }
            assertThrows(NoSuchFieldException.class, () -> generated.getDeclaredField("timestamp"));
            assertThrows(NoSuchFieldException.class, () -> generated.getDeclaredField("legacyDate"));
        }
    }

    private Path source(String name, String content) throws Exception {
        Path path = dir.resolve(name);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path;
    }

    static class Sample {
        LocalDate day;
        @Eq("otherDay") LocalDate annotatedDay;
        LocalDateTime timestamp;
        Date legacyDate;
    }
}
