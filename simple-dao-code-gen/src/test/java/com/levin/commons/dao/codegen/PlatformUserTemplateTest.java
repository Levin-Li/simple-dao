package com.levin.commons.dao.codegen;

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
    void generatedBaseReqShouldExposePlatformAndTenantUsersWithDeprecatedSaasCompatibility() throws Exception {
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
        assertTrue(source.contains("@Deprecated\n    @Ignore\n    @Schema(title = \"是否SAAS用户\", hidden = true)\n    public boolean isSaasUser() {\n        return isPlatformUser();"), source);
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
    }
}
