package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.dao.codegen.model.FieldModel;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LazyMapperTemplateTest {

    @Test
    void generatedMapperShouldGuardOnlyLazyPropertiesAndKeepSingleArgumentEntry() throws Exception {
        List<FieldModel> fields = Arrays.stream(FixtureEntity.class.getDeclaredFields())
                .map(this::fieldModel).collect(Collectors.toList());
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        StringWriter output = new StringWriter();
        configuration.getTemplate("simple.dao/codegen/template/services/mapper.ftl").process(Map.of(
                "packageName", "com.example.services",
                "modulePackageName", "com.example",
                "entityClassPackage", "com.example.entities",
                "entityClassName", "com.example.entities.FixtureEntity",
                "entityName", "FixtureEntity",
                "entityTitle", "测试实体",
                "importList", Collections.emptyList(),
                "fields", fields), output);

        String source = output.toString();
        assertTrue(source.contains("@Mapping(target = \"children\", conditionExpression"), source);
        assertTrue(source.contains("@Mapping(target = \"parent\", conditionExpression"), source);
        assertTrue(source.contains("@Mapping(target = \"lazyName\", conditionExpression"), source);
        assertFalse(source.contains("@Mapping(target = \"eagerParent\""), source);
        assertFalse(source.contains("@Mapping(target = \"name\""), source);
        assertTrue(source.contains("default FixtureEntityInfo toInfo(FixtureEntity entity)"), source);
        assertTrue(source.contains("toInfo(FixtureEntity entity, @Context boolean allowLazyLoading)"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source));
    }

    private FieldModel fieldModel(Field field) {
        return new FieldModel(FixtureEntity.class).setField(field)
                .setName(field.getName()).setType(field.getType());
    }

    static class FixtureEntity {
        String name;
        @Basic(fetch = FetchType.LAZY)
        String lazyName;
        @OneToMany
        List<FixtureEntity> children;
        @ManyToOne(fetch = FetchType.LAZY)
        FixtureEntity parent;
        @ManyToOne
        FixtureEntity eagerParent;
    }
}
