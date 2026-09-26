package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonalRequestTemplateTest {

    @Test
    void personalRequestTemplatesShouldSupportMultipleOwnerIds() throws Exception {
        String personal = render("services/commons/req/MultiTenantPersonalReq.java");
        String orgPersonal = render("services/commons/req/MultiTenantOrgPersonalReq.java");

        for (String source : new String[]{personal, orgPersonal}) {
            assertTrue(source.contains("@In(value = \"ownerId\", condition = \"ownerIdListCondition(#_isDelete)\")"), source);
            assertTrue(source.contains("protected Collection<String> ownerIdList;"), source);
            assertFalse(source.contains("paramExpr = \"1 = 2\""), source);
            assertTrue(source.contains("isCanVisitPersonalData()"), source);
            assertFalse(source.contains("canVisitPersonalData()"), source);
            assertTrue(source.contains("protected void checkOwnerScope(boolean isDeleteAction)"), source);
            assertTrue(source.contains("get_currentUserId()"), source);
            assertTrue(source.contains("ownerIdCondition(#_isQuery, #_isDelete)"), source);
            assertTrue(source.contains("if (!isPersonalObject()) return false;"), source);
            assertTrue(source.contains("setOwnerIdList(Collection<String> ownerIdList)"), source);
            assertDoesNotThrow(() -> StaticJavaParser.parse(source), source);
        }
    }

    private String render(String templatePath) throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");
        Template template = configuration.getTemplate("simple.dao/codegen/template/" + templatePath);
        StringWriter output = new StringWriter();
        template.process(Collections.singletonMap("modulePackageName", "com.example.generated"), output);
        return output.toString();
    }
}
