package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.plugins.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiTenantOrgTemplateTest {

    @TempDir
    Path tempDir;

    @Test
    void generatedRequestShouldDelegateOrganizationAuthorizationToTheInjector() throws Exception {
        Path req = tempDir.resolve("MultiTenantOrgReq.java");
        Utils.copyAndReplace(tempDir.toString(), true,
                "simple.dao/codegen/template/services/commons/req/MultiTenantOrgReq.java", req.toFile(),
                Map.of("modulePackageName", "com.example.generated"));

        String source = Files.readString(req);

        assertTrue(source.contains("组织数据访问权限由变量注入提供方预先完成校验"), source);
        assertTrue(source.contains("@In(value = InjectConst.ORG_ID, condition = \"orgIdListCondition()\")"), source);
        assertFalse(source.contains("paramExpr = \" 1 = 2 \""), source);
        assertTrue(source.contains("@Eq(condition = \"orgIdCondition(#_isQuery)\""), source);
        assertTrue(source.contains("@Update(condition = \"isOrganizedObject() && #_isUpdate && isAdmin()"), source);
        assertTrue(source.contains("public boolean orgIdCondition(boolean isQueryAction)"), source);
        assertTrue(source.contains("public boolean orgIdListCondition()"), source);
        assertTrue(source.contains("public boolean orgDataCondition(boolean isQueryAction, boolean isPublicData)"), source);
        assertTrue(source.contains("protected void checkOrgScopeParam()"), source);
        assertTrue(source.contains("this instanceof OrganizedPublicObject"), source);
        assertTrue(source.contains("this instanceof OrganizedSharedObject"), source);
        assertFalse(source.contains("canUpdateOrgId("), source);
        assertFalse(source.contains("orgPublicCondition("), source);
        assertFalse(source.contains("orgSharedCondition("), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source), source);
    }
}
