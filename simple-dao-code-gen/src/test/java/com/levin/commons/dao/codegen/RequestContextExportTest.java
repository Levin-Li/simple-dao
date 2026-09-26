package com.levin.commons.dao.codegen;

import com.levin.commons.dao.CtxVar;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.support.SelectDaoImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestContextExportTest {

    @Test
    void laterQueryObjectCanReadWholeRequestFromReqContext() {
        SelectDaoImpl<TestEntity> dao = dao();
        dao.appendByQueryObj(new RequestContext(true, "owner-1"))
                .appendByQueryObj(new CrossClassCondition());

        String statement = dao.genFinalStatement();
        assertTrue(statement.contains("e.ownerId ="), statement);
        assertTrue(dao.getContext().get("_req") instanceof RequestContext, dao.getContext().toString());
        assertTrue(dao.genFinalParamList().stream()
                .filter(Map.class::isInstance)
                .anyMatch(values -> "owner-1".equals(((Map<?, ?>) values).get("ownerId"))), dao.genFinalParamList().toString());
    }

    @Test
    void falseConditionOnWholeRequestDoesNotGenerateRestriction() {
        String statement = dao().appendByQueryObj(new RequestContext(false, "owner-1"))
                .appendByQueryObj(new CrossClassCondition())
                .genFinalStatement();

        assertFalse(statement.contains("e.ownerId"), statement);
    }

    private static SelectDaoImpl<TestEntity> dao() {
        MiniDao miniDao = (MiniDao) Proxy.newProxyInstance(RequestContextExportTest.class.getClassLoader(),
                new Class[]{MiniDao.class}, (proxy, method, args) -> {
                    if ("getParamPlaceholder".equals(method.getName())) return ":?";
                    if ("getNamingStrategy".equals(method.getName())) return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
        return new SelectDaoImpl<>(miniDao, false, TestEntity.class, "e");
    }

    static class TestEntity {
        String ownerId;
    }

    public static class RequestContext {
        @Ignore
        private final boolean restrictOwner;
        @Ignore
        private final String ownerId;

        RequestContext(boolean restrictOwner, String ownerId) {
            this.restrictOwner = restrictOwner;
            this.ownerId = ownerId;
        }

        public boolean isRestrictOwner() {
            return restrictOwner;
        }

        public String getOwnerId() {
            return ownerId;
        }

        @CtxVar(varName = "_req")
        public RequestContext getRequestContext() {
            return this;
        }
    }

    static class CrossClassCondition {
        @Eq(value = "ownerId", condition = "#_req.isRestrictOwner()", paramExpr = "#_req.getOwnerId()")
        boolean applyOwnerRestriction = true;
    }
}
