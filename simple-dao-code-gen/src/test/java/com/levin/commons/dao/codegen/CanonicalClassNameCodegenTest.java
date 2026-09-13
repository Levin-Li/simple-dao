package com.levin.commons.dao.codegen;

import com.levin.commons.dao.codegen.model.FieldModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import org.springframework.core.ResolvableType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanonicalClassNameCodegenTest {

    @TempDir
    Path tempDir;

    @Test
    void generatedSourceMetadataShouldUseCanonicalNamesForNestedTypes() throws Exception {
        Map<String, Object> parameters = baseInfoFor(Outer.NestedEntity.class);

        assertEquals(Outer.NestedEntity.class.getCanonicalName(), parameters.get("entityClassName"));
        assertTrue(((Collection<String>) parameters.get("importList"))
                .contains(CanonicalClassNameCodegenTest.class.getCanonicalName()));
        assertTrue(((String) parameters.get("implementsListStr"))
                .contains(Outer.NestedMarker.class.getCanonicalName()
                        .substring(Outer.NestedMarker.class.getPackageName().length() + 1)));

        String source = renderSimpleQuery(parameters);
        assertTrue(source.contains("import " + Outer.NestedEntity.class.getCanonicalName() + ";"), source);
        assertTrue(source.contains("import " + CanonicalClassNameCodegenTest.class.getCanonicalName() + ";"), source);
        assertTrue(!source.contains("$"), source);

        String infoSource = renderInfo(parameters);
        assertTrue(infoSource.contains("implements Serializable, " + Outer.NestedMarker.class.getCanonicalName()
                .substring(Outer.NestedMarker.class.getPackageName().length() + 1)), infoSource);
        assertTrue(!infoSource.contains("$"), infoSource);
    }

    @Test
    void fieldImportsShouldUseCanonicalNamesForNestedTypes() {
        FieldModel fieldModel = new FieldModel(Outer.NestedEntity.class);
        fieldModel.addImport(Outer.NestedValue.class);

        assertTrue(fieldModel.getImports().contains(Outer.NestedValue.class.getCanonicalName()));
        assertTrue(fieldModel.getImports().contains(Outer.class.getCanonicalName()));
    }

    @Test
    void genericNestedTypesShouldAlwaysBeCollectedAsFieldImports() throws Exception {
        Field field = GenericHolder.class.getDeclaredField("opButtonList");
        FieldModel fieldModel = new FieldModel(GenericHolder.class);

        Method method = ServiceModelCodeGenerator.class.getDeclaredMethod("addResolvableTypeImports",
                FieldModel.class, ResolvableType.class);
        method.setAccessible(true);
        method.invoke(null, fieldModel, ResolvableType.forField(field));

        assertTrue(fieldModel.getImports().contains(MenuItem.OpButton.class.getCanonicalName()));
        assertTrue(fieldModel.getImports().contains(MenuItem.class.getCanonicalName()));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> baseInfoFor(Class<?> entityClass) throws Exception {
        ServiceModelCodeGenerator.moduleName("canonical-test");
        ServiceModelCodeGenerator.modulePackageName("com.example.generated");

        Method method = ServiceModelCodeGenerator.class.getDeclaredMethod("getBaseInfo",
                Class.class, List.class, String.class, String.class);
        method.setAccessible(true);
        return (Map<String, Object>) method.invoke(null, entityClass, List.of(), "com.example.generated", "NestedEntityInfo");
    }

    private String renderSimpleQuery(Map<String, Object> baseParameters) throws Exception {
        Map<String, Object> parameters = new LinkedHashMap<>(baseParameters);
        parameters.put("servicePackageName", "com.example.generated.services.nested");
        parameters.put("reqExtendClass", "BaseReq");
        parameters.put("className", "SimpleNestedEntityReq");

        Path output = tempDir.resolve("SimpleNestedEntityReq.java");
        ServiceModelCodeGenerator.genFileByTemplate(ServiceModelCodeGenerator.SIMPLE_QUERY_EVT_FTL,
                parameters, output.toString());
        return Files.readString(output);
    }

    private String renderInfo(Map<String, Object> baseParameters) throws Exception {
        Map<String, Object> parameters = new LinkedHashMap<>(baseParameters);
        parameters.put("infoExtendClass", "BaseInfo");

        Path output = tempDir.resolve("NestedEntityInfo.java");
        ServiceModelCodeGenerator.genFileByTemplate(ServiceModelCodeGenerator.INFO_FTL,
                parameters, output.toString());
        return Files.readString(output);
    }

    static class Outer {
        interface NestedMarker {
        }

        static class NestedEntity implements NestedMarker {
        }

        static class NestedValue {
        }
    }

    static class MenuItem {
        enum OpButton { Save }
    }

    static class GenericHolder {
        java.util.Set<MenuItem.OpButton> opButtonList;
    }
}
