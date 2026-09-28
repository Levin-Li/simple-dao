package com.levin.commons.dao.codegen;

import com.github.javaparser.StaticJavaParser;
import com.levin.commons.plugins.Utils;
import com.levin.commons.rbac.RbacRoleInfo;
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
        assertEquals("isPlatformAdmin", InjectConst.IS_PLATFORM_ADMIN);
        assertEquals("isCanAccessAllOrg", InjectConst.IS_CAN_ACCESS_ALL_ORG);
        assertEquals("isCanAccessAllPersonal", InjectConst.IS_CAN_ACCESS_ALL_PERSONAL);
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
        assertTrue(source.contains("InjectConst.IS_CAN_ACCESS_ALL_PERSONAL"), source);
        assertTrue(source.contains("protected boolean isPlatformUser = false;"), source);
        assertTrue(source.contains("protected boolean isTenantUser = true;"), source);
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
        assertTrue(source.contains("return this.isCanAccessAllPersonal;"), source);
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
        assertTrue(source.contains("RbacRoleInfo.PLATFORM_ROLE_PREFIX + \"*\""), source);
        assertFalse(source.contains("!isSaasUser()"), source);
        assertDoesNotThrow(() -> StaticJavaParser.parse(source), source);
    }

    @Test
    void platformRolePrefixShouldMatchServiceSupport() {
        assertEquals("R_PLATFORM_", RbacRoleInfo.PLATFORM_ROLE_PREFIX);
        assertEquals("R_PLATFORM_SA", RbacRoleInfo.PLATFORM_SA);
        assertEquals("R_ADMIN", RbacRoleInfo.TENANT_ADMIN);
        assertEquals("R_ORG_ADMIN", RbacRoleInfo.TENANT_ORG_ADMIN);
    }
}
