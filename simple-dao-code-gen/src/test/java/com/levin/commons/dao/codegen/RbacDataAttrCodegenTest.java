package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.rbac.RbacDataAttr;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** 枚举记录范围不产生请求授权值，受控查询不能由共享缓存提前返回。 */
class RbacDataAttrCodegenTest {
    enum Kind { CUSTOMER, SUPPLIER }
    @jakarta.persistence.MappedSuperclass
    static class ScopeParent {
        @RbacDataAttr @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING) Kind kind;
    }
    @jakarta.persistence.Entity
    @jakarta.persistence.Access(jakarta.persistence.AccessType.FIELD)
    static class Controlled extends ScopeParent { @jakarta.persistence.Id String id; String code; }
    static class Plain { String id; String code; }
    static class Invalid {
        @RbacDataAttr
        @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"CUSTOMER", "SUPPLIER"})
        String kind;
    }

    @Test
    void completeEntityMetadataMustValidateInheritedStrictJavaEnums() {
        assertTrue(new ClassModel(Controlled.class).isRbacDataAttrControlled());
        assertFalse(new ClassModel(Plain.class).isRbacDataAttrControlled());
        assertThrows(IllegalArgumentException.class, () -> new ClassModel(Invalid.class));
        assertThrows(IllegalArgumentException.class, () ->
                ServiceModelCodeGenerator.genCodeByEntityClass(Invalid.class, "unused", "unused", Map.of()));
    }

    @Test
    void sourceOnlyMarkerMustNeverBeCopiedEvenWithWildcardWhitelist() {
        List<String> previous = new ArrayList<>(ServiceModelCodeGenerator.keepAnnotationList());
        try {
            ServiceModelCodeGenerator.keepAnnotationList("*");
            for (String marker : List.of("@RbacDataAttr", "@RbacDataAttr(name=\"类型\")",
                    "@com.levin.commons.rbac.RbacDataAttr")) {
                assertFalse(ServiceModelCodeGenerator.isMatch(marker, "query"), marker);
            }
        } finally {
            ServiceModelCodeGenerator.keepAnnotationList(previous.toArray(new String[0]));
        }
    }

    @Test
    void controlledIdUniqueAndLoaderReadsMustAlwaysReachDaoWithoutChangingPlainCache() throws Exception {
        for (Class<?> entity : List.of(Controlled.class, Plain.class)) {
            String generated = renderService(entity);
            var parsed = StaticJavaParser.parse(generated);
            var type = parsed.getClassByName(entity.getSimpleName() + "ServiceImpl").orElseThrow();
            boolean controlled = entity == Controlled.class;
            for (String name : List.of("findById", "findByCode", "getCache")) {
                var method = type.getMethodsByName(name).get(0);
                assertEquals(!controlled, method.getAnnotationByName("Cacheable").isPresent(), name);
            }
            assertTrue(type.getMethodsByName("findById").get(0).toString().contains("newInfoQuery()"));
            assertTrue(type.getMethodsByName("findByCode").get(0).toString().contains("newInfoQuery()"));
            assertTrue(type.getMethodsByName("isUniqueFindCacheEnabled").get(0).toString()
                    .contains("return " + !controlled + " &&"));
            // 保留清理/事件能力，不能因关闭读取缓存改变旧维护入口。
            assertTrue(type.getMethodsByName("clearAllCache").get(0).getAnnotationByName("CacheEvict").isPresent());
        }
    }

    private String renderService(Class<?> entity) throws Exception {
        List<FieldModel> fields = new ArrayList<>();
        for (Field field : entity.getDeclaredFields()) {
            fields.add(new FieldModel(entity).setField(field).setName(field.getName())
                    .setType(field.getType()).setTypeName(field.getType().getSimpleName())
                    .setUk(field.getName().equals("code")).setPk(field.getName().equals("id")));
        }
        Method base = ServiceModelCodeGenerator.class.getDeclaredMethod("getBaseInfo", Class.class, List.class, String.class, String.class);
        base.setAccessible(true);
        ServiceModelCodeGenerator.moduleName("probe");
        ServiceModelCodeGenerator.modulePackageName("probe");
        @SuppressWarnings("unchecked") Map<String, Object> params = (Map<String, Object>) base.invoke(null, entity, fields, "probe.services", entity.getSimpleName() + "ServiceImpl");
        params.put("serviceName", entity.getSimpleName() + "Service");
        params.put("servicePackageName", "probe.services");
        params.put("cacheSpelUtilsBeanName", "cacheSpelUtils");
        params.put("isCacheableEntity", true);
        params.put("enableDubbo", false);
        params.put("isMultiTenantObject", false);
        params.put("fields", fields);
        params.put("importList", Collections.emptySet());
        params.put("selfOverridableMatchFields", Collections.emptyList());
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter result = new StringWriter();
        cfg.getTemplate("simple.dao/codegen/template/services/service_impl.ftl").process(params, result);
        return result.toString();
    }
}
