package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.plugins.Utils;
import com.levin.commons.service.support.InjectConst;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("unchecked")
class PlatformUserTemplateTest {

    @TempDir
    Path tempDir;

    @Test
    void injectConstantsShouldExposePlatformAndTenantUsers() throws Exception {
        assertEquals("isPlatformUser", InjectConst.IS_PLATFORM_USER);
        assertEquals("isTenantUser", InjectConst.IS_TENANT_USER);
        assertTrue(InjectConst.class.getField("IS_SAAS_USER").isAnnotationPresent(Deprecated.class));
    }

    @Test
    void generatedBaseReqShouldExposePlatformAndTenantUsers() throws Exception {
        Path baseReq = tempDir.resolve("BaseReq.java");
        Utils.copyAndReplace(tempDir.toString(), true,
                "simple.dao/codegen/template/services/commons/req/BaseReq.java", baseReq.toFile(),
                Map.of("modulePackageName", "com.example.generated"));

        String source = Files.readString(baseReq);

        assertTrue(source.contains("InjectConst.IS_PLATFORM_USER"), source);
        assertTrue(source.contains("InjectConst.IS_TENANT_USER"), source);
        assertTrue(source.contains("protected boolean isPlatformUser = false;"), source);
        assertTrue(source.contains("protected boolean isTenantUser = false;"), source);
        assertTrue(source.contains("public boolean isPlatformUser()"), source);
        assertTrue(source.contains("public boolean isTenantUser()"), source);
        assertTrue(source.contains("protected String _currentUserId;"), source);
        assertTrue(source.contains("protected String _currentUserOrgId;"), source);
        assertTrue(source.contains("protected String _currentUserTenantId;"), source);
        assertTrue(source.contains("protected String _currentUserName;"), source);
        assertTrue(source.contains("替代原 {@code _operatorId} 属性"), source);
        assertTrue(source.contains("替代原 {@code _operatorName} 属性"), source);
        assertFalse(source.contains("protected String _operatorId;"), source);
        assertFalse(source.contains("protected String _operatorName;"), source);
        assertTrue(source.contains("return isTopSuperAdmin()"), source);
        assertTrue(source.contains("ConfidentialLevel.PERSON_PRIVATE.code()"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source), source);
    }

    @Test
    void generatedMultiTenantReqShouldUsePlatformUserCondition() throws Exception {
        Path multiTenantReq = tempDir.resolve("MultiTenantReq.java");
        Utils.copyAndReplace(tempDir.toString(), true,
                "simple.dao/codegen/template/services/commons/req/MultiTenantReq.java", multiTenantReq.toFile(),
                Map.of("modulePackageName", "com.example.generated"));

        String source = Files.readString(multiTenantReq);

        assertTrue(source.contains("!isPlatformUser()"), source);
        assertFalse(source.contains("!isSaasUser()"), source);
        assertTrue(source.contains("protected boolean isUnscopedPlatformAdminQuery(boolean isQueryAction)"), source);
        assertTrue(source.contains("&& (isSuperAdmin() || isSaasAdmin())"), source);
        assertTrue(source.contains("if (isUnscopedPlatformAdminQuery(isQueryAction)) {\n            return false;\n        }"), source);
    }
}
