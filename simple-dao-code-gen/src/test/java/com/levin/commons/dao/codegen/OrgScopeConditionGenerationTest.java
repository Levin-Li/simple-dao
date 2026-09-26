package com.levin.commons.dao.codegen;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.In;
import com.levin.commons.dao.annotation.IsNull;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.OrganizedPublicObject;
import com.levin.commons.dao.domain.OrganizedSharedObject;
import com.levin.commons.dao.support.SelectDaoImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrgScopeConditionGenerationTest {

    @Test
    void queryWithAuthorizedListShouldGenerateListPublicAndSharedRestrictions() {
        String statement = new SelectDaoImpl<OrgEntity>(stubDao(), false, OrgEntity.class, "o")
                .appendByQueryObj(new OrgScopeReq().setOrgIdList(List.of("org-1", "org-2")).setOrgId("ignored"))
                .genFinalStatement();

        verifyContains(statement, "o.orgId IN", "o.orgId IS NULL");
        assertFalse(statement.contains("o.orgShared ="), statement);
        assertFalse(statement.contains("ignored"), statement);
    }

    @Test
    void queryWithSingleOrgShouldUseSingleOrgAndStillIncludePublicAndSharedRestrictions() {
        String statement = new SelectDaoImpl<OrgEntity>(stubDao(), false, OrgEntity.class, "o")
                .appendByQueryObj(new OrgScopeReq().setOrgId("org-1"))
                .genFinalStatement();

        verifyContains(statement, "o.orgId =", "o.orgId IS NULL");
        assertFalse(statement.contains("o.orgShared ="), statement);
    }

    @Test
    void unscopedAllOrgViewShouldNotGenerateOrganizationRestrictions() {
        String statement = new SelectDaoImpl<OrgEntity>(stubDao(), false, OrgEntity.class, "o")
                .appendByQueryObj(new OrgScopeReq().setUnsafeContext(true).setAllOrgScope(true))
                .genFinalStatement();

        assertFalse(statement.contains("o.orgId"), statement);
        assertFalse(statement.contains("o.orgShared"), statement);
    }

    @Test
    void explicitPublicAndSharedOptionsShouldGenerateBothRestrictions() {
        String statement = new SelectDaoImpl<OrgEntity>(stubDao(), false, OrgEntity.class, "o")
                .appendByQueryObj(new OrgScopeReq().setOrgId("org-1").setIncludeShared(true))
                .genFinalStatement();

        verifyContains(statement, "o.orgId =", "o.orgId IS NULL", "o.orgShared =");
    }

    @Test
    void unsafeRestrictedRequestWithoutOrganizationScopeShouldThrow() {
        org.junit.jupiter.api.Assertions.assertThrows(com.levin.commons.dao.exception.StatementBuildException.class,
                () -> new SelectDaoImpl<OrgEntity>(stubDao(), false, OrgEntity.class, "o")
                        .appendByQueryObj(new OrgScopeReq().setUnsafeContext(true)));
    }

    private static void verifyContains(String statement, String... fragments) {
        for (String fragment : fragments) {
            assertTrue(statement.contains(fragment), () -> "缺少片段 " + fragment + "，实际语句：" + statement);
        }
    }

    private static MiniDao stubDao() {
        return (MiniDao) Proxy.newProxyInstance(OrgScopeConditionGenerationTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if ("getParamPlaceholder".equals(method.getName())) {
                        return ":?";
                    }
                    if ("getNamingStrategy".equals(method.getName())) {
                        return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    static class OrgEntity {
        String orgId;
        Boolean orgShared;
    }

    static class OrgScopeReq implements OrganizedPublicObject, OrganizedSharedObject {

        @OR(autoClose = true)
        @In("orgId")
        @IsNull(condition = "orgDataCondition(#_isQuery, true)", value = "orgId")
        @Eq(condition = "orgDataCondition(#_isQuery, false)", value = "orgShared", paramExpr = "true")
        List<String> orgIdList;

        @Eq(condition = "orgIdCondition(#_isQuery)")
        String orgId;
        boolean unsafeContext;
        boolean allOrgScope;
        boolean includeShared;
        boolean includePublic = true;

        public boolean orgIdListCondition() {
            checkOrgScopeParam();
            return orgIdList != null && !orgIdList.isEmpty();
        }

        public boolean orgIdCondition(boolean isQueryAction) {
            checkOrgScopeParam();
            return isQueryAction && (orgIdList == null || orgIdList.isEmpty()) && orgId != null && !orgId.isBlank();
        }

        public boolean orgDataCondition(boolean isQueryAction, boolean isPublicData) {
            checkOrgScopeParam();
            return isQueryAction && hasOrgView()
                    && (isPublicData ? this instanceof OrganizedPublicObject && includePublic : this instanceof OrganizedSharedObject && includeShared);
        }

        @Override
        public boolean isOrgShared() {
            return true;
        }

        private boolean hasOrgView() {
            return (orgIdList != null && !orgIdList.isEmpty()) || (orgId != null && !orgId.isBlank());
        }

        private void checkOrgScopeParam() {
            if (unsafeContext && !allOrgScope && !hasOrgView()) {
                throw new IllegalStateException("未注入组织访问范围");
            }
        }

        OrgScopeReq setOrgIdList(List<String> orgIdList) {
            this.orgIdList = orgIdList;
            return this;
        }

        OrgScopeReq setOrgId(String orgId) {
            this.orgId = orgId;
            return this;
        }

        OrgScopeReq setUnsafeContext(boolean unsafeContext) {
            this.unsafeContext = unsafeContext;
            return this;
        }

        OrgScopeReq setAllOrgScope(boolean allOrgScope) {
            this.allOrgScope = allOrgScope;
            return this;
        }

        OrgScopeReq setIncludeShared(boolean includeShared) {
            this.includeShared = includeShared;
            return this;
        }


        @Override
        @SuppressWarnings("unchecked")
        public <ORG_ID extends java.io.Serializable> ORG_ID getOrgId() {
            return (ORG_ID) orgId;
        }
    }
}
