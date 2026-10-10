package com.levin.commons.dao.codegen;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.ap.MappingProcessor;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NestedMapperBehaviorTest {

    @TempDir
    Path tempDir;

    @Test
    void generatedMappersShouldDeepCopyThreeLevelNestedInfoAndPreserveCycles() throws Exception {
        Path sources = tempDir.resolve("src");
        Path classes = Files.createDirectories(tempDir.resolve("classes"));
        Path generated = Files.createDirectories(tempDir.resolve("generated"));
        writeFixtures(sources);
        renderMapper(sources, "Parent", List.of("Child"), List.of(
                collection("Child", "List"), collection("Child", "Set"),
                collection("Child", "Collection"), collection("Child", "Array"),
                collection("Parent", "List")));
        renderMapper(sources, "Child", List.of("Parent", "Grandchild"),
                List.of(collection("Grandchild", "List")));
        renderMapper(sources, "Grandchild", List.of("Child"), List.of());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue(compiler != null, "测试需要 JDK 编译器");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        String processorJar = new File(MappingProcessor.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
             Stream<Path> paths = Files.walk(sources)) {
            List<File> files = paths.filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toFile).collect(Collectors.toList());
            List<String> options = List.of("-classpath", System.getProperty("java.class.path"),
                    "-processorpath", processorJar,
                    "-processor", MappingProcessor.class.getName(),
                    "--release", "11", "-d", classes.toString(), "-s", generated.toString());
            boolean compiled = Boolean.TRUE.equals(compiler.getTask(null, fileManager, diagnostics, options,
                    null, fileManager.getJavaFileObjectsFromFiles(files)).call());
            assertTrue(compiled, diagnostics.getDiagnostics().toString());
        }

        String implementation = Files.readString(generated.resolve(
                "com/example/services/parent/ParentMapperImpl.java"));
        String mapperSource = Files.readString(sources.resolve(
                "com/example/services/parent/ParentMapper.java"));
        assertTrue(mapperSource.contains("ChildMapper.INSTANCE"), mapperSource);
        assertTrue(mapperSource.contains(".toInfo(info, cycleContext)"), mapperSource);
        assertTrue(implementation.contains("mapNested( info.child, cycleContext )"), implementation);
        assertTrue(implementation.contains("copyChildInfoList( info.children, cycleContext )"), implementation);
        String childImplementation = Files.readString(generated.resolve(
                "com/example/services/child/ChildMapperImpl.java"));
        assertTrue(childImplementation.contains("mapNested( info.grandchild, cycleContext )"), childImplementation);
        assertTrue(childImplementation.contains("copyGrandchildInfoList( info.grandchildren, cycleContext )"),
                childImplementation);
        String grandchildImplementation = Files.readString(generated.resolve(
                "com/example/services/grandchild/GrandchildMapperImpl.java"));
        assertTrue(grandchildImplementation.contains("mapNested( info.owner, cycleContext )"),
                grandchildImplementation);

        try (URLClassLoader loader = new URLClassLoader(new URL[]{classes.toUri().toURL()},
                getClass().getClassLoader())) {
            Class.forName("NestedCopyProbe", true, loader).getMethod("run").invoke(null);
        }
    }

    private void renderMapper(Path sources, String name, List<String> related,
                              List<Map<String, Object>> collections) throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        Map<String, Object> params = new HashMap<>();
        params.put("packageName", "com.example.services." + name.toLowerCase());
        params.put("modulePackageName", "com.example");
        params.put("entityClassPackage", "com.example.entities");
        params.put("entityName", name);
        params.put("entityTitle", name);
        params.put("fields", List.of());
        List<Map<String, Object>> relatedTypes = related.stream()
                .map(NestedMapperBehaviorTest::entity).collect(Collectors.toList());
        params.put("nestedEntityTypes", relatedTypes);
        params.put("nestedInfoTypes", relatedTypes);
        params.put("nestedInfoCollectionMappings", collections);
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl")
                .process(params, output);
        writeSource(sources, "com/example/services/" + name.toLowerCase() + "/"
                + name + "Mapper.java", output.toString());
    }

    private static Map<String, Object> entity(String name) {
        return Map.of("package", Map.of("name", "com.example.entities"),
                "simpleName", name, "canonicalName", "com.example.entities." + name);
    }

    private static Map<String, Object> collection(String entity, String kind) {
        return Map.of("entityType", entity(entity), "kind", kind);
    }

    private static void writeFixtures(Path sources) throws Exception {
        writeSource(sources, "com/example/ModuleOption.java", "package com.example; public class ModuleOption {}");
        writeSource(sources, "com/example/entities/EntityConst.java",
                "package com.example.entities; public class EntityConst {}");
        writeSource(sources, "com/example/entities/Parent.java",
                "package com.example.entities; public class Parent { public Child child;"
                        + " public java.util.List<Child> children; public java.util.Set<Child> peers;"
                        + " public java.util.Collection<Child> all; public Child[] childArray;"
                        + " public java.util.List<Parent> siblings; }");
        writeSource(sources, "com/example/entities/Child.java",
                "package com.example.entities; public class Child { public Parent parent;"
                        + " public Grandchild grandchild; public java.util.List<Grandchild> grandchildren;"
                        + " public String name; }");
        writeSource(sources, "com/example/entities/Grandchild.java",
                "package com.example.entities; public class Grandchild {"
                        + " public Child owner; public String name; }");
        writeSource(sources, "com/example/services/parent/info/ParentInfo.java",
                "package com.example.services.parent.info; public class ParentInfo {"
                        + " public com.example.services.child.info.ChildInfo child;"
                        + " public java.util.List<com.example.services.child.info.ChildInfo> children;"
                        + " public java.util.Set<com.example.services.child.info.ChildInfo> peers;"
                        + " public java.util.Collection<com.example.services.child.info.ChildInfo> all;"
                        + " public com.example.services.child.info.ChildInfo[] childArray;"
                        + " public java.util.List<ParentInfo> siblings; }");
        writeSource(sources, "com/example/services/child/info/ChildInfo.java",
                "package com.example.services.child.info; public class ChildInfo {"
                        + " public com.example.services.parent.info.ParentInfo parent;"
                        + " public com.example.services.grandchild.info.GrandchildInfo grandchild;"
                        + " public java.util.List<com.example.services.grandchild.info.GrandchildInfo> grandchildren;"
                        + " public String name; }");
        writeSource(sources, "com/example/services/grandchild/info/GrandchildInfo.java",
                "package com.example.services.grandchild.info; public class GrandchildInfo {"
                        + " public com.example.services.child.info.ChildInfo owner; public String name; }");
        for (String name : List.of("Parent", "Child", "Grandchild")) {
            String packageName = "com.example.services." + name.toLowerCase() + ".req";
            for (String request : List.of("Create" + name + "Req", "Update" + name + "Req",
                    "Query" + name + "Req", name + "IdReq")) {
                writeSource(sources, packageName.replace('.', '/') + "/" + request + ".java",
                        "package " + packageName + "; public class " + request + " {}");
            }
        }
        writeSource(sources, "NestedCopyProbe.java", String.join("\n",
                "import com.example.entities.*;",
                "import com.example.services.parent.ParentMapper;",
                "import com.example.services.parent.info.ParentInfo;",
                "import java.util.*;",
                "public class NestedCopyProbe {",
                "  public static void run() {",
                "    Parent parent = new Parent(); Child child = new Child();",
                "    Grandchild grandchild = new Grandchild();",
                "    parent.child = child; parent.children = List.of(child); parent.peers = Set.of(child);",
                "    parent.all = List.of(child); parent.childArray = new Child[]{child};",
                "    parent.siblings = List.of(parent); child.parent = parent; child.name = \"child\";",
                "    child.grandchild = grandchild; child.grandchildren = List.of(grandchild);",
                "    grandchild.owner = child; grandchild.name = \"grandchild\";",
                "    ParentInfo original = ParentMapper.INSTANCE.toInfo(parent, false);",
                "    ParentInfo copy = ParentMapper.INSTANCE.toInfo(original);",
                "    if (copy == original || copy.child == original.child || copy.child.parent != copy",
                "        || copy.children.get(0) != copy.child || copy.peers.iterator().next() != copy.child",
                "        || copy.all.iterator().next() != copy.child || copy.childArray[0] != copy.child",
                "        || copy.siblings.get(0) != copy || !\"child\".equals(copy.child.name))",
                "      throw new AssertionError(\"nested Info copy lost identity or reused source objects\");",
                "    if (copy.child.grandchild == original.child.grandchild",
                "        || copy.child.grandchild.owner != copy.child",
                "        || copy.child.grandchildren.get(0) != copy.child.grandchild",
                "        || !\"grandchild\".equals(copy.child.grandchild.name))",
                "      throw new AssertionError(\"third-level Info was not deep-copied by its Mapper\");",
                "  }",
                "}"));
    }

    private static void writeSource(Path root, String relativePath, String content) throws Exception {
        Path output = root.resolve(relativePath);
        Files.createDirectories(output.getParent());
        Files.writeString(output, content);
    }
}
