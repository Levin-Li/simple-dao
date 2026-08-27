package com.levin.commons.dao.codegen;

import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.domain.ExpiredObject;
import com.levin.commons.dao.domain.MultiTenantPublicObject;
import com.levin.commons.dao.domain.OrganizedPublicObject;
import com.levin.commons.dao.domain.SelfOverridableObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelfOverridableMatchCodegenTest {

    @TempDir
    Path tempDir;

    @Test
    void publicTenantAndOrganizationFieldsShouldPrecedeAnnotationFieldsAndBeDeduplicated() throws Exception {
        List<FieldModel> fields = fieldsOf(PublicOverrideEntity.class);

        List<String> names = ServiceModelCodeGenerator.getSelfOverridableMatchFields(PublicOverrideEntity.class, fields)
                .stream().map(FieldModel::getName).collect(Collectors.toList());

        assertEquals(Arrays.asList("tenantId", "orgId", "domain", "userType", "orgType"), names);
    }

    @Test
    void annotationFieldsShouldBeUsedAsDeclaredWhenEntityIsNotPublic() throws Exception {
        List<FieldModel> fields = fieldsOf(LocalOverrideEntity.class);

        List<String> names = ServiceModelCodeGenerator.getSelfOverridableMatchFields(LocalOverrideEntity.class, fields)
                .stream().map(FieldModel::getName).collect(Collectors.toList());

        assertEquals(Arrays.asList("domain", "userType"), names);
        assertTrue(ServiceModelCodeGenerator.getSelfOverridableMatchFields(NoOverrideEntity.class, fields).isEmpty());
    }

    @Test
    void businessServiceTemplatesShouldGenerateElementFilteringAndPriorityOrderingWithEntityConstants() throws Exception {
        List<FieldModel> fields = fieldsOf(PublicOverrideEntity.class);
        List<FieldModel> matchFields = ServiceModelCodeGenerator.getSelfOverridableMatchFields(PublicOverrideEntity.class, fields);

        String serviceSource = render("biz/biz_service.ftl", "BizPublicOverrideEntityService.java", matchFields);
        String implSource = render("biz/biz_service_impl.ftl", "BizPublicOverrideEntityServiceImpl.java", matchFields);

        assertTrue(serviceSource.contains("PublicOverrideEntityInfo findBestMatch("), serviceSource);
        assertTrue(serviceSource.contains("String tenantId,"), serviceSource);
        assertTrue(serviceSource.indexOf("String tenantId") < serviceSource.indexOf("String orgId"), serviceSource);
        assertTrue(serviceSource.indexOf("String orgId") < serviceSource.indexOf("String domain"), serviceSource);

        assertTrue(implSource.contains(".isNullOrEq(E_PublicOverrideEntity.tenantId, tenantId)"), implSource);
        assertTrue(implSource.contains(".isNullOrEq(E_PublicOverrideEntity.orgId, orgId)"), implSource);
        assertTrue(implSource.contains(".isNullOrEq(E_PublicOverrideEntity.domain, domain)"), implSource);
        assertTrue(implSource.contains(".isNull(E_PublicOverrideEntity.expiredTime)"), implSource);
        assertTrue(implSource.contains(".gte(E_PublicOverrideEntity.expiredTime, LocalDateTime.now())"), implSource);
        assertTrue(implSource.contains(".orderByDescForEqOrNull(true, E_PublicOverrideEntity.userType, userType)"), implSource);
        assertTrue(implSource.contains(".orderByDescForEqOrNull(true, E_PublicOverrideEntity.orgType, orgType)"), implSource);
        assertFalse(implSource.contains(".eq(\"domain\""), implSource);
        assertTrue(implSource.contains(".limit(0, 1)"), implSource);
    }

    @Test
    void nonExpiredObjectShouldNotGenerateExpirationFiltering() throws Exception {
        List<FieldModel> matchFields = ServiceModelCodeGenerator.getSelfOverridableMatchFields(
                LocalOverrideEntity.class, fieldsOf(LocalOverrideEntity.class));

        String implSource = render("biz/biz_service_impl.ftl", "BizLocalOverrideEntityServiceImpl.java",
                matchFields, LocalOverrideEntity.class);

        assertFalse(implSource.contains(".isNull(E_PublicOverrideEntity.expiredTime)"), implSource);
        assertFalse(implSource.contains("LocalDateTime.now()"), implSource);
    }

    private String render(String template, String fileName, List<FieldModel> matchFields) throws Exception {
        return render(template, fileName, matchFields, PublicOverrideEntity.class);
    }

    private String render(String template, String fileName, List<FieldModel> matchFields, Class<?> entityClass) throws Exception {
        Path output = tempDir.resolve(fileName);
        ServiceModelCodeGenerator.genFileByTemplate(template, templateParameters(matchFields, entityClass), output.toString());
        return Files.readString(output);
    }

    private static Map<String, Object> templateParameters(List<FieldModel> matchFields, Class<?> entityClass) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("packageName", "com.example.generated.biz");
        params.put("entityClassPackage", "com.example.generated.entity");
        params.put("entityClassName", "com.example.generated.entity.PublicOverrideEntity");
        params.put("entityName", "PublicOverrideEntity");
        params.put("entityTitle", "公共覆盖对象");
        params.put("modulePackageName", "com.example.generated");
        params.put("bizBoPackageName", "com.example.generated.biz.bo.publicoverrideentity");
        params.put("servicePackageName", "com.example.generated.service");
        params.put("serviceName", "PublicOverrideEntityService");
        params.put("cacheSpelUtilsBeanName", "cacheSpelUtils");
        params.put("className", "BizPublicOverrideEntityService");
        params.put("fields", Collections.emptyList());
        params.put("importList", Collections.emptySet());
        params.put("pkField", matchFields.get(0));
        params.put("selfOverridableMatchFields", matchFields);
        params.put("classModel", new ClassModel(entityClass));
        params.put("enableDubbo", false);
        params.put("isCacheableEntity", true);
        return params;
    }

    private static List<FieldModel> fieldsOf(Class<?> type) throws Exception {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .map(SelfOverridableMatchCodegenTest::toFieldModel)
                .collect(Collectors.toList());
    }

    private static FieldModel toFieldModel(Field field) {
        return new FieldModel(field.getDeclaringClass())
                .setField(field)
                .setName(field.getName())
                .setType(field.getType())
                .setTypeName(field.getType().getSimpleName());
    }

    static final class E_PublicOverrideEntity {
        static final String tenantId = "tenantId";
        static final String orgId = "orgId";
        static final String domain = "domain";
        static final String userType = "userType";
        static final String orgType = "orgType";
    }

    @SelfOverridableObject(overrideColumnNames = {
            E_PublicOverrideEntity.domain,
            E_PublicOverrideEntity.userType,
            E_PublicOverrideEntity.orgType,
            E_PublicOverrideEntity.tenantId,
            E_PublicOverrideEntity.orgId
    })
    static class PublicOverrideEntity implements MultiTenantPublicObject, OrganizedPublicObject, ExpiredObject {
        String tenantId;
        String orgId;
        String domain;
        String userType;
        String orgType;
        LocalDateTime expiredTime;

        @Override
        public Serializable getTenantId() {
            return tenantId;
        }

        @Override
        public Serializable getOrgId() {
            return orgId;
        }

        @Override
        public LocalDateTime getExpiredTime() {
            return expiredTime;
        }
    }

    @SelfOverridableObject(overrideColumnNames = {E_PublicOverrideEntity.domain, E_PublicOverrideEntity.userType})
    static class LocalOverrideEntity {
        String domain;
        String userType;
    }

    static class NoOverrideEntity {
    }
}
