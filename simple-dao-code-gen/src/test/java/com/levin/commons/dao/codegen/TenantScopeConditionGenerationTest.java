package com.levin.commons.dao.codegen;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.IsNull;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.MultiTenantPublicObject;
import com.levin.commons.dao.domain.MultiTenantSharedObject;
import com.levin.commons.dao.support.SelectDaoImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class TenantScopeConditionGenerationTest {
    @Test void tenantViewDefaultsToCurrentTenantOnly() {
        String sql = dao().appendByQueryObj(new TenantReq().tenant("t1", "t1")).genFinalStatement();
        assertTrue(sql.contains("t.tenantId ="), sql);
        assertFalse(sql.contains("t.tenantId IS NULL"), sql);
        assertFalse(sql.contains("t.tenantShared"), sql);
    }
    @Test void explicitPublicAndSharedOptionsExpandTenantView() {
        String sql = dao().appendByQueryObj(new TenantReq().tenant("t1", "t1").publicData().sharedData()).genFinalStatement();
        assertTrue(sql.contains("t.tenantId ="), sql);
        assertTrue(sql.contains("t.tenantId IS NULL"), sql);
        assertTrue(sql.contains("t.tenantShared"), sql);
    }
    @Test void unscopedAdminHasNoTenantRestriction() {
        String sql = dao().appendByQueryObj(new TenantReq().admin()).genFinalStatement();
        assertFalse(sql.contains("t.tenantId"), sql);
    }
    @Test void unsafeTenantUserWithDifferentTenantThrows() {
        assertThrows(com.levin.commons.dao.exception.StatementBuildException.class,
                () -> dao().appendByQueryObj(new TenantReq().tenant("other", "current").unsafe()));
    }
    private static SelectDaoImpl<TenantEntity> dao() { return new SelectDaoImpl<>(stub(), false, TenantEntity.class, "t"); }
    private static MiniDao stub() { return (MiniDao) Proxy.newProxyInstance(TenantScopeConditionGenerationTest.class.getClassLoader(), new Class[]{MiniDao.class}, (p,m,a)-> {
        if (m.getName().equals("getParamPlaceholder")) return ":?";
        if (m.getName().equals("getNamingStrategy")) return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
        if (m.getReturnType()==boolean.class) return false; if (m.getReturnType()==int.class) return 0; return null; }); }
    static class TenantEntity { String tenantId; Boolean tenantShared; }
    static class TenantReq implements MultiTenantPublicObject, MultiTenantSharedObject {
        @OR(autoClose=true) @Eq @IsNull(condition="publicCondition(#_isQuery)") @Eq(condition="sharedCondition(#_isQuery)", value="tenantShared", paramExpr="true") String tenantId;
        boolean unsafe, admin, includePublic, includeShared; String currentTenantId;
        TenantReq tenant(String id,String current){tenantId=id;currentTenantId=current;return this;} TenantReq unsafe(){unsafe=true;return this;} TenantReq admin(){admin=true;return this;} TenantReq publicData(){includePublic=true;return this;} TenantReq sharedData(){includeShared=true;return this;}
        void check(){ if(unsafe&&!admin&&(tenantId==null||!tenantId.equals(currentTenantId))) throw new IllegalStateException("tenant"); }
        public boolean publicCondition(boolean q){check(); return q&&tenantId!=null&&includePublic;}
        public boolean sharedCondition(boolean q){check(); return q&&tenantId!=null&&includeShared;}
        public boolean isTenantShared(){return includeShared;}
        @Override @SuppressWarnings("unchecked") public <TID extends java.io.Serializable> TID getTenantId(){return (TID)tenantId;}
    }
}
