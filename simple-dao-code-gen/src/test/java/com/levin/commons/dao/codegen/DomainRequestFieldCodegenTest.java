package com.levin.commons.dao.codegen;

import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.dao.domain.DomainObject;
import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.dao.domain.OrganizedObject;
import com.levin.commons.dao.domain.PersonalObject;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainRequestFieldCodegenTest {

    @Test
    void domainIdShouldBeInheritedFromTheRequestBaseInsteadOfRegenerated() throws Exception {
        List<String> names = requestFields(DomainEntity.class)
                .stream().map(FieldModel::getName).toList();

        assertEquals(List.of("name"), names);
    }

    @Test
    void nonDomainEntityShouldKeepItsDomainIdField() throws Exception {
        List<String> names = requestFields(NonDomainEntity.class)
                .stream().map(FieldModel::getName).toList();

        assertEquals(List.of("domainId"), names);
    }

    @Test
    void fixedRequestInterfacesShouldOnlyContainTheSupportedEntityInterfaces() {
        assertEquals(List.of("MultiTenantObject", "OrganizedObject", "PersonalObject", "DomainObject"),
                ServiceModelCodeGenerator.getFixedRequestImplements(AllSupportedEntity.class));
        assertEquals(List.of("DomainObject"),
                ServiceModelCodeGenerator.getFixedRequestImplements(DomainEntity.class));
    }

    @SuppressWarnings("unchecked")
    private static List<FieldModel> requestFields(Class<?> entityClass) throws Exception {
        Field sourceFileCompilationMap = ServiceModelCodeGenerator.class.getDeclaredField("srcFileCompilationMap");
        sourceFileCompilationMap.setAccessible(true);
        if (sourceFileCompilationMap.get(null) == null) {
            sourceFileCompilationMap.set(null, new HashMap<>());
        }

        Method method = ServiceModelCodeGenerator.class.getDeclaredMethod("buildFieldModel",
                Class.class, java.util.Map.class, boolean.class, String.class);
        method.setAccessible(true);
        return (List<FieldModel>) method.invoke(null, entityClass, Collections.emptyMap(), true, "query");
    }

    static class DomainEntity implements DomainObject {
        String domainId;
        String name;

        @Override
        public String getDomainId() {
            return domainId;
        }
    }

    static class NonDomainEntity {
        String domainId;
    }

    static class AllSupportedEntity implements MultiTenantObject, OrganizedObject, PersonalObject, DomainObject {
        @Override
        public <TID extends Serializable> TID getTenantId() {
            return null;
        }

        @Override
        public <ORG_ID extends Serializable> ORG_ID getOrgId() {
            return null;
        }

        @Override
        public <UID extends Serializable> UID getOwnerId() {
            return null;
        }

        @Override
        public String getDomainId() {
            return null;
        }
    }
}
