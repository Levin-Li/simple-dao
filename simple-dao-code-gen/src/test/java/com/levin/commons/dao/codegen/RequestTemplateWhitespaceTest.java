package com.levin.commons.dao.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTemplateWhitespaceTest {
    private String resource(String name) throws Exception {
        try (var input = getClass().getResourceAsStream("/simple.dao/codegen/template/services/req/" + name)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String render(String template, Map<String, Object> model) throws Exception {
        StringWriter output = new StringWriter();
        new Template("test", template, new Configuration(Configuration.VERSION_2_3_28)).process(model, output);
        return output.toString();
    }

    @Test
    void createCommentDoesNotEmitTrailingSpaces() throws Exception {
        // 直接渲染正式模板的字段行，覆盖无默认值与空默认值。
        String template = resource("create_evt.ftl");
        String line = template.lines().filter(s -> s.contains("//参考默认值")).findFirst().orElseThrow();
        for (Map<String, Object> field : new Map[]{
                Map.of("modifiersPrefix", "", "typeName", "String", "name", "name", "hasDefaultValue", false, "defaultValue", ""),
                Map.of("modifiersPrefix", "", "typeName", "String", "name", "name", "hasDefaultValue", true, "defaultValue", ""),
                Map.of("modifiersPrefix", "", "typeName", "String", "name", "name", "hasDefaultValue", true, "defaultValue", "abc")}) {
            String result = render(line, Map.of("field", field));
            assertEquals(result.stripTrailing(), result);
        }
    }

    @Test
    void queryEmptyAnnotationMacroDoesNotEmitIndentation() throws Exception {
        // 模板宏无注解时不应输出只有缩进的行，有注解时保留注解内容。
        String template = resource("query_evt.ftl");
        String macro = template.substring(template.indexOf("<#macro FieldAnnotationList"), template.indexOf("</#macro>") + 9);
        String call = template.lines().filter(s -> s.contains("<@FieldAnnotationList field = field  keyword='@Options'/>")).findFirst().orElseThrow();
        String empty = render(macro + "\n" + call + "\n", Map.of("field", Map.of("annotations", java.util.List.of())));
        assertFalse(empty.lines().anyMatch(s -> !s.isEmpty() && s.isBlank()));
        String annotated = render(macro + "\n" + call + "\n", Map.of("field", Map.of("annotations", java.util.List.of("@Options(dictCode = \"x\")"))));
        assertTrue(annotated.contains("@Options(dictCode = \"x\")"));
    }
}
