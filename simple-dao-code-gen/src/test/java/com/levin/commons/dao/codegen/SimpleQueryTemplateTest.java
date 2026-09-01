package com.levin.commons.dao.codegen;

import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.codegen.model.FieldModel;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleQueryTemplateTest {

    @TempDir
    Path tempDir;

    @Test
    void queryRequestShouldExtendItsGeneratedSimpleQueryRequest() throws Exception {
        FieldModel id = field("id", true, "编号");
        FieldModel name = field("name", false, "名称");
        List<FieldModel> fields = List.of(id, name);

        String simpleQuerySource = render(ServiceModelCodeGenerator.SIMPLE_QUERY_EVT_FTL,
                "SimpleQuerySampleEntityReq", "BaseReq", fields, id);
        String querySource = render(ServiceModelCodeGenerator.QUERY_EVT_FTL,
                "QuerySampleEntityReq", "SimpleQuerySampleEntityReq<QuerySampleEntityReq>", fields, id);
        String multiTenantSimpleQuerySource = render(ServiceModelCodeGenerator.SIMPLE_QUERY_EVT_FTL,
                "SimpleQuerySampleEntityReq", "MultiTenantReq<T>", fields, id);

        assertEquals("services/req/simple_query_evt.ftl", ServiceModelCodeGenerator.SIMPLE_QUERY_EVT_FTL);
        assertTrue(simpleQuerySource.contains("class SimpleQuerySampleEntityReq<T extends SimpleQuerySampleEntityReq<T>> extends BaseReq"), simpleQuerySource);
        assertTrue(multiTenantSimpleQuerySource.contains("class SimpleQuerySampleEntityReq<T extends SimpleQuerySampleEntityReq<T>> extends MultiTenantReq<T>"), multiTenantSimpleQuerySource);
        assertTrue(simpleQuerySource.contains("String orderBy;"), simpleQuerySource);
        assertTrue(simpleQuerySource.contains("Set<String> selectColumns;"), simpleQuerySource);
        assertTrue(simpleQuerySource.contains("public T setOrderBy(String orderBy)"), simpleQuerySource);
        assertTrue(simpleQuerySource.contains("public T setOrderDir(OrderBy.Type orderDir)"), simpleQuerySource);

        assertTrue(querySource.contains("class QuerySampleEntityReq extends SimpleQuerySampleEntityReq<QuerySampleEntityReq>"), querySource);
        assertTrue(querySource.contains("List<String> idList;"), querySource);
        assertTrue(querySource.contains("String name;"), querySource);
        assertFalse(querySource.contains("String orderBy;"), querySource);
        assertFalse(querySource.contains("Set<String> selectColumns;"), querySource);
    }

    private String render(String template, String className, String reqExtendClass,
                          List<FieldModel> fields, FieldModel pkField) throws Exception {
        Path output = tempDir.resolve(className + ".java");
        ServiceModelCodeGenerator.genFileByTemplate(template,
                templateParameters(className, reqExtendClass, fields, pkField), output.toString());
        return Files.readString(output);
    }

    private static Map<String, Object> templateParameters(String className, String reqExtendClass,
                                                           List<FieldModel> fields, FieldModel pkField) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("packageName", "com.example.generated.services.sample.req");
        params.put("modulePackageName", "com.example.generated");
        params.put("entityClassPackage", SimpleQueryTemplateTest.class.getPackageName());
        params.put("entityClassName", SampleEntity.class.getName());
        params.put("entityName", "SampleEntity");
        params.put("entityTitle", "示例实体");
        params.put("servicePackageName", "com.example.generated.services.sample");
        params.put("className", className);
        params.put("reqExtendClass", reqExtendClass);
        params.put("serialVersionUID", "1");
        params.put("fields", fields);
        params.put("pkField", pkField);
        params.put("importList", Set.of());
        params.put("classModel", new ClassModel(SampleEntity.class).setFieldModels(fields));
        return params;
    }

    private static FieldModel field(String name, boolean primaryKey, String title) throws Exception {
        Field field = SampleEntity.class.getDeclaredField(name);
        FieldModel fieldModel = new FieldModel(SampleEntity.class)
                .setField(field)
                .setName(name)
                .setType(field.getType())
                .setTypeName(field.getType().getSimpleName())
                .setTitle(title)
                .setSchemaDescUseConstRef(false)
                .setPk(primaryKey);
        if (primaryKey) {
            fieldModel.addAnnotation(Id.class);
        }
        return fieldModel;
    }

    static class SampleEntity {
        String id;
        String name;
    }
}
