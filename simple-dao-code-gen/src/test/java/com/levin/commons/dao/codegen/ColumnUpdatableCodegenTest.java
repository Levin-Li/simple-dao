package com.levin.commons.dao.codegen;

import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.dao.codegen.model.DaoLogicGroupField;
import com.levin.commons.dao.DaoLogicGroup;
import com.levin.commons.dao.GenDaoLogicGroup;
import com.levin.commons.dao.annotation.In;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnUpdatableCodegenTest {

    @TempDir
    Path tempDir;

    @Test
    void columnMarkedNotUpdatableShouldBeExcludedFromSimpleUpdateRequest() throws Exception {
        List<FieldModel> fields = fieldsOf(UpdateEntity.class);
        Map<String, FieldModel> fieldsByName = fields.stream()
                .collect(Collectors.toMap(FieldModel::getName, field -> field));

        assertFalse(ServiceModelCodeGenerator.isColumnUpdatable(UpdateEntity.class.getDeclaredField("createdBy")));
        assertTrue(ServiceModelCodeGenerator.isColumnUpdatable(UpdateEntity.class.getDeclaredField("displayName")));
        assertTrue(fieldsByName.get("createdBy").isNotUpdate());
        assertFalse(fieldsByName.get("displayName").isNotUpdate());

        String source = renderSimpleUpdate(fields);

        assertFalse(source.contains("String createdBy;"), source);
        assertFalse(source.contains("setCreatedBy("), source);
        assertTrue(source.contains("String displayName;"), source);
        assertTrue(source.contains("setDisplayName("), source);
    }

    @Test
    void nonUpdatableParticipantDoesNotProduceAssignmentCheck() throws Exception {
        Class<?> entity = RoleUpdateEntity.class;
        List<FieldModel> fields = List.of(fieldModel(entity.getDeclaredField("subjectOrgId"), entity),
                fieldModel(entity.getDeclaredField("remark"), entity));
        String source = renderSimpleUpdate(entity, fields);
        assertFalse(source.contains("String subjectOrgId;"), source);
        assertFalse(source.contains("setSubjectOrgId("), source);
        assertFalse(source.contains("checkSubjectOrgId(subjectOrgId)"), source);
        assertFalse(source.contains("protected void checkSubjectOrgId("), source);
        assertTrue(source.contains("String remark;"), source);
    }

    private static List<FieldModel> fieldsOf(Class<?> entityClass) throws Exception {
        Field createdBy = entityClass.getDeclaredField("createdBy");
        Field displayName = entityClass.getDeclaredField("displayName");
        return List.of(fieldModel(createdBy), fieldModel(displayName));
    }

    private static FieldModel fieldModel(Field field) {
        return fieldModel(field, UpdateEntity.class);
    }

    private static FieldModel fieldModel(Field field, Class<?> entity) {
        FieldModel fieldModel = new FieldModel(entity)
                .setField(field)
                .setName(field.getName())
                .setType(field.getType())
                .setTypeName(field.getType().getSimpleName())
                .setTitle(field.getName())
                .setSchemaDescUseConstRef(false);
        return fieldModel.setNotUpdate(ServiceModelCodeGenerator.isNotUpdateField(fieldModel, field, false));
    }

    private String renderSimpleUpdate(List<FieldModel> fields) throws Exception {
        return renderSimpleUpdate(UpdateEntity.class, fields);
    }

    private String renderSimpleUpdate(Class<?> entity, List<FieldModel> fields) throws Exception {
        Path output = tempDir.resolve("SimpleUpdate" + entity.getSimpleName() + "Req.java");
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("packageName", "com.example.generated.services.req");
        parameters.put("modulePackageName", "com.example.generated");
        parameters.put("entityClassPackage", entity.getPackageName());
        parameters.put("entityClassName", entity.getName());
        parameters.put("entityName", entity.getSimpleName());
        parameters.put("entityTitle", "更新实体");
        parameters.put("className", "SimpleUpdate" + entity.getSimpleName() + "Req");
        parameters.put("reqExtendClass", "BaseReq");
        parameters.put("serialVersionUID", "1");
        parameters.put("fields", fields);
        parameters.put("UPDATE_fields", Collections.emptyList());
        parameters.put("importList", Set.of());
        parameters.put("classModel", new ClassModel(entity).setFieldModels(fields));
        parameters.put("daoLogicGroupFields", DaoLogicGroupField.forType(entity));
        parameters.put("hasRoleScopeFields", DaoLogicGroupField.forType(entity).stream()
                .anyMatch(field -> field.getScopeRole() != GenDaoLogicGroup.ScopeRole.NONE));

        ServiceModelCodeGenerator.genFileByTemplate(ServiceModelCodeGenerator.SIMPLE_UPDATE_EVT_FTL,
                parameters, output.toString());
        return Files.readString(output);
    }

    static class UpdateEntity {
        @Column(updatable = false)
        String createdBy;

        @Column
        String displayName;
    }

    @DaoLogicGroup(name = "visible", logicOp = DaoLogicGroup.LogicOp.OR,
            minConditions = 1, unless = "1=2")
    static class RoleUpdateEntity {
        @Column(updatable = false)
        @GenDaoLogicGroup(name = "visible", scopeRole = GenDaoLogicGroup.ScopeRole.ORG,
                filterOp = In.class, injectVarName = "accessibleOrgIds")
        String subjectOrgId;

        @Column
        String remark;
    }
}
