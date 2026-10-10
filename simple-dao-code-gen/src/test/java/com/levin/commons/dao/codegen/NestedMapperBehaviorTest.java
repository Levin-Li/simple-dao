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
        assertTrue(implementation.matches("(?s).*copyInfoList\\d+\\( info.children, cycleContext \\).*"), implementation);
        String childImplementation = Files.readString(generated.resolve(
                "com/example/services/child/ChildMapperImpl.java"));
        assertTrue(childImplementation.contains("mapNested( info.grandchild, cycleContext )"), childImplementation);
        assertTrue(childImplementation.matches("(?s).*copyInfoList\\d+\\( info.grandchildren, cycleContext \\).*"),
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
        if ("Parent".equals(name)) {
            params.put("jsonTargetTypes", List.of("java.util.List<java.lang.String>"));
            params.put("valueCopyFields", List.of(
                    Map.of("name", "value", "copyMethod", "copyValue0"),
                    Map.of("name", "values", "copyMethod", "copyValueMap")));
            params.put("valueCopyModels", List.of(Map.of(
                    "typeName", "com.example.value.Value", "index", 0,
                    "overloadedProperties", List.of(),
                    "inaccessibleProperties", List.of(Map.of("name", "occurred", "getter", "getOccurred")),
                    "dynamicProperties", List.of(
                            Map.of("name", "ext", "getter", "getExt",
                                    "typeName", "java.util.Map", "path", "Value.ext"),
                            Map.of("name", "children", "getter", "getChildren",
                                    "typeName", "java.util.Map", "path", "Value.children")),
                    "qualifiedProperties", List.of())));
        }
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
        writeSource(sources, "com/example/value/Value.java", "package com.example.value;"
                + " public class Value { private java.util.Date occurred;"
                + " private java.util.Map<String,Object> ext;"
                + " private java.util.Map<String,Value> children;"
                + " public java.util.Date getOccurred(){ return occurred; }"
                + " public java.util.Map<String,Object> getExt(){ return ext; }"
                + " public void setExt(java.util.Map<String,Object> ext){ this.ext=ext; }"
                + " public java.util.Map<String,Value> getChildren(){ return children; }"
                + " public void setChildren(java.util.Map<String,Value> children){ this.children=children; }"
                + " public Value withOccurred(java.util.Date date){ this.occurred=date; return this; } }");
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
                        + " public com.example.value.Value value;"
                        + " public java.util.Map<String,com.example.value.Value> values;"
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
                "    com.levin.commons.dao.support.MapperJsonUtils.JsonCodec originalCodec =",
                "        com.levin.commons.dao.support.MapperJsonUtils.codec.get();",
                "    com.levin.commons.dao.support.MapperJsonUtils.codec.set(",
                "        new com.levin.commons.dao.support.MapperJsonUtils.JsonCodec() {",
                "      @Override @SuppressWarnings(\"unchecked\")",
                "      public <T> T parse(String json, java.lang.reflect.Type type) {",
                "        if (!\"custom\".equals(json) || !\"java.util.List<java.lang.String>\".equals(type.getTypeName()))",
                "          throw new AssertionError(\"JSON codec did not receive declared generic type\");",
                "        return (T) java.util.List.of(\"decoded\");",
                "      }",
                "      @Override public String stringify(Object value) {",
                "        if (!java.util.List.of(\"decoded\").equals(value)) throw new AssertionError(\"JSON codec value\");",
                "        return \"encoded\";",
                "      }",
                "    });",
                "    try {",
                "      if (ParentMapper.INSTANCE.fromJsonType0(null) != null",
                "          || ParentMapper.INSTANCE.fromJsonType0(\"\") != null",
                "          || ParentMapper.INSTANCE.toJsonType0(null) != null)",
                "        throw new AssertionError(\"generated Mapper JSON null handling\");",
                "      if (!java.util.List.of(\"decoded\").equals(ParentMapper.INSTANCE.fromJsonType0(\"custom\"))",
                "          || !\"encoded\".equals(ParentMapper.INSTANCE.toJsonType0(java.util.List.of(\"decoded\"))))",
                "        throw new AssertionError(\"generated Mapper ignored configured codec\");",
                "    } finally {",
                "      com.levin.commons.dao.support.MapperJsonUtils.codec.set(originalCodec);",
                "    }",
                "    Parent parent = new Parent(); Child child = new Child();",
                "    Grandchild grandchild = new Grandchild();",
                "    parent.child = child; parent.children = List.of(child); parent.peers = Set.of(child);",
                "    parent.all = List.of(child); parent.childArray = new Child[]{child};",
                "    parent.siblings = List.of(parent); child.parent = parent; child.name = \"child\";",
                "    child.grandchild = grandchild; child.grandchildren = List.of(grandchild);",
                "    grandchild.owner = child; grandchild.name = \"grandchild\";",
                "    ParentInfo original = ParentMapper.INSTANCE.toInfo(parent, false);",
                "    com.example.value.Value value = new com.example.value.Value().withOccurred(new java.util.Date());",
                "    java.util.Map<String,Object> ext = new java.util.HashMap<>(); ext.put(\"self\", ext);",
                "    java.util.List<Object> loop = new java.util.ArrayList<>(); loop.add(loop); ext.put(\"list\", loop);",
                "    Object[] array = new Object[1]; array[0] = array; ext.put(\"array\", array);",
                "    java.util.Set<String> shared = new java.util.HashSet<>(java.util.Set.of(\"x\"));",
                "    ext.put(\"set1\", shared); ext.put(\"set2\", shared);",
                "    java.util.LinkedList<String> linked = new java.util.LinkedList<>(java.util.List.of(\"x\"));",
                "    ext.put(\"linked\", linked);",
                "    value.setExt(ext); original.value = value;",
                "    original.values = java.util.Map.of(\"same\", value);",
                "    value.setChildren(java.util.Map.of(\"self\", value));",
                "    ParentInfo copy = ParentMapper.INSTANCE.toInfo(original);",
                "    if (copy.value == value) throw new AssertionError(\"value alias\");",
                "    if (copy.values == original.values || copy.values.get(\"same\") != copy.value)",
                "      throw new AssertionError(\"typed Map value alias\");",
                "    if (copy.value.getOccurred() == value.getOccurred()) throw new AssertionError(\"readonly Date alias\");",
                "    if (copy.value.getExt() == ext) throw new AssertionError(\"Map alias\");",
                "    if (!(copy.value.getExt() instanceof java.util.HashMap)) throw new AssertionError(\"Map type\");",
                "    if (copy.value.getExt().get(\"self\") != copy.value.getExt()) throw new AssertionError(\"Map cycle\");",
                "    java.util.List<?> copiedLoop = (java.util.List<?>) copy.value.getExt().get(\"list\");",
                "    if (copiedLoop == loop || copiedLoop.get(0) != copiedLoop) throw new AssertionError(\"List cycle\");",
                "    Object[] copiedArray = (Object[]) copy.value.getExt().get(\"array\");",
                "    if (copiedArray == array || copiedArray[0] != copiedArray) throw new AssertionError(\"array cycle\");",
                "    if (copy.value.getExt().get(\"set1\") != copy.value.getExt().get(\"set2\")",
                "        || copy.value.getExt().get(\"set1\") == shared",
                "        || !(copy.value.getExt().get(\"set1\") instanceof java.util.HashSet))",
                "      throw new AssertionError(\"shared Set\");",
                "    if (!(copy.value.getExt().get(\"linked\") instanceof java.util.LinkedList))",
                "      throw new AssertionError(\"concrete List type\");",
                "    if (copy.value.getChildren().get(\"self\") != copy.value) throw new AssertionError(\"Map value alias\");",
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
