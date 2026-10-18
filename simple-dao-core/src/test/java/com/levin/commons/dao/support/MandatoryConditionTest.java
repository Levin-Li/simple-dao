package com.levin.commons.dao.support;

import com.levin.commons.dao.*;
import com.levin.commons.dao.exception.DaoSecurityException;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MandatoryConditionTest {
    enum Kind { Customer, Supplier }
    enum Region { East, South }
    @Entity static class Partner { Long id; Kind kind; Region region; String name; }
    @AfterEach void clear() { MandatoryConditions.setProvider(null); }
    private MandatoryCondition scope() {
        return MandatoryCondition.ofBranches(List.of(Map.of("kind",List.of(Kind.Customer),"region",List.of(Region.East)),
                Map.of("kind",List.of(Kind.Supplier),"region",List.of(Region.South))));
    }
    private SelectDaoImpl<Partner> select() {
        SelectDaoImpl<Partner> dao = new SelectDaoImpl<>(mini(), false, Partner.class,"p");
        dao.disableSafeMode(); return dao;
    }
    @Test void immutableDnfRetainsCombinationsAndRejectsNonEnums() {
        Map<String,Collection<?>> branch = new HashMap<>(); branch.put("kind",new ArrayList<>(List.of(Kind.Customer)));
        MandatoryCondition value = MandatoryCondition.ofBranches(List.of(branch,branch));
        branch.clear(); assertEquals(1,value.getBranches().size());
        assertThrows(UnsupportedOperationException.class,()->value.getBranches().clear());
        assertThrows(IllegalArgumentException.class,()->MandatoryCondition.ofBranches(List.of(Map.of("kind",List.of("Customer")))));
        assertTrue(MandatoryCondition.ofBranches(List.of(Map.of("kind",List.of()))).isDenied());
    }
    @Test void independentProvidersIntersectWithoutFlatteningBranches() {
        MandatoryCondition left=scope();
        MandatoryCondition east=MandatoryCondition.ofBranches(List.of(Map.of("region",List.of(Region.East))));
        MandatoryCondition result=left.and(east);
        assertEquals(1,result.getBranches().size());
        assertEquals(List.of(Kind.Customer),result.getBranches().get(0).get("kind"));
        assertTrue(result.and(MandatoryCondition.ofBranches(List.of(Map.of("kind",List.of(Kind.Supplier))))).isDenied());
        assertSame(left,left.and(MandatoryCondition.unrestricted()));
        assertTrue(left.and(MandatoryCondition.deny()).isDenied());
        List<Map<String, List<?>>> reversed=new ArrayList<>(left.getBranches());
        Collections.reverse(reversed);
        assertEquals(left,MandatoryCondition.ofBranches(reversed));
    }
    @Test void scopeIsPreparedBeforeBusinessConditionsAndNullResolverFailsClosed() {
        java.util.concurrent.atomic.AtomicInteger calls=new java.util.concurrent.atomic.AtomicInteger();
        MandatoryConditions.setProvider((entity,action)->{ calls.incrementAndGet(); assertEquals(EntityOption.Action.Read,action); return scope(); });
        SelectDaoImpl<Partner> dao=select();
        assertEquals(1,calls.get());
        dao.where("p.id = :?",1L); dao.genFinalStatement(); dao.genFinalStatement();
        assertTrue(calls.get() >= 3);
        MandatoryConditions.setProvider((entity,action)->null);
        assertThrows(NullPointerException.class,this::select);
    }
    @Test void builderRejectsScopeChangesAtBothSqlAndParameterBoundaries() {
        java.util.concurrent.atomic.AtomicReference<MandatoryCondition> current=new java.util.concurrent.atomic.AtomicReference<>(scope());
        MandatoryConditions.setProvider((entity,action)->current.get());
        SelectDaoImpl<Partner> sqlFirst=select(); sqlFirst.genFinalStatement();
        SelectDaoImpl<Partner> paramsFirst=select(); paramsFirst.genFinalParamList();
        current.set(MandatoryCondition.ofBranches(List.of(Map.of("kind",List.of(Kind.Customer)))));
        assertThrows(DaoSecurityException.class,sqlFirst::genFinalStatement);
        assertThrows(DaoSecurityException.class,sqlFirst::genFinalParamList);
        assertThrows(DaoSecurityException.class,paramsFirst::genFinalStatement);
        current.set(MandatoryCondition.unrestricted());
        SelectDaoImpl<Partner> top=select(); top.genFinalStatement();
        current.set(MandatoryCondition.deny());
        assertThrows(DaoSecurityException.class,top::genFinalStatement);
        assertThrows(DaoSecurityException.class,top::genFinalParamList);
    }
    @Test void repeatRenderingDoesNotAccumulateAndBusinessOrIsParenthesized() {
        MandatoryConditions.setProvider((entity,action)->scope());
        SelectDaoImpl<Partner> dao=select(); dao.where("p.id = :? OR p.id = :?",1L,2L);
        List<?> beforeRender=values(dao.genFinalParamList());
        String first=dao.genFinalStatement(); List<?> params=values(dao.genFinalParamList());
        assertEquals(beforeRender,params);
        assertTrue(first.contains("OR p.id")); assertTrue(first.contains("AND (("));
        assertEquals(first,dao.genFinalStatement()); assertEquals(params,values(dao.genFinalParamList()));
        assertEquals(List.of(1L,2L,Kind.Customer,Region.East,Kind.Supplier,Region.South),params);
    }
    @Test void permissionDoesNotSatisfyBusinessSafeMode() {
        MandatoryConditions.setProvider((entity,action)->scope());
        UpdateDaoImpl<Partner> dao=new UpdateDaoImpl<>(mini(),false,Partner.class,"p");
        dao.set("name","changed"); assertThrows(DaoSecurityException.class,dao::genFinalStatement);
    }
    @Test void partialUpdateRetainsUnchangedFieldPredicatesAndRejectsExpressions() {
        MandatoryConditions.setProvider((entity,action)->scope());
        UpdateDaoImpl<Partner> dao=new UpdateDaoImpl<>(mini(),false,Partner.class,"p");
        dao.disableSafeMode(); dao.set("kind",Kind.Supplier); dao.where("p.id = :?",1L);
        String statement=dao.genFinalStatement();
        assertTrue(statement.contains("p.region IN"));
        assertEquals(List.of(Kind.Supplier,1L,Kind.Customer,Region.East,Kind.Supplier,Region.South,Region.South),values(dao.genFinalParamList()));
        UpdateDaoImpl<Partner> raw=new UpdateDaoImpl<>(mini(),false,Partner.class,"p");
        raw.disableSafeMode(); raw.setByStatement("p.kind = CASE WHEN 1=1 THEN 'Supplier' END");
        assertThrows(DaoSecurityException.class,raw::genFinalStatement);
        UpdateDaoImpl<Partner> compound=new UpdateDaoImpl<>(mini(),false,Partner.class,"p");
        compound.disableSafeMode(); compound.setByStatement("p.name = :?, p.kind = :?", "name",Kind.Supplier);
        assertThrows(DaoSecurityException.class,compound::genFinalStatement);
        UpdateDaoImpl<Partner> comment=new UpdateDaoImpl<>(mini(),false,Partner.class,"p");
        comment.disableSafeMode(); comment.setByStatement("p.name = :? --", "name");
        assertThrows(DaoSecurityException.class,comment::genFinalStatement);
    }
    @Test void denyIsFalseAndNoProviderIsUnchanged() {
        SelectDaoImpl<Partner> plain=select(); plain.where("p.id = :?",9L);
        String before=plain.genFinalStatement(); List<?> params=values(plain.genFinalParamList());
        MandatoryConditions.setProvider((entity,action)->MandatoryCondition.unrestricted());
        assertEquals(before,plain.genFinalStatement()); assertEquals(params,values(plain.genFinalParamList()));
        MandatoryConditions.setProvider((entity,action)->MandatoryCondition.deny());
        SelectDaoImpl<Partner> denied=select(); denied.where("p.id = :?",9L);
        assertTrue(denied.genFinalStatement().contains("1 = 0")); assertEquals(List.of(9L),values(denied.genFinalParamList()));
    }
    private List<?> values(List<?> list) { return list.stream().filter(value -> !(value instanceof Map)).toList(); }
    private MiniDao mini() {
        return (MiniDao) Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{MiniDao.class},(proxy,method,args)->{
            switch(method.getName()) {
                case "getParamPlaceholder": return ":?";
                case "getSafeModeMaxLimit": return 2000;
                case "getNamingStrategy": return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
                case "isEntityClass": return args[0]==Partner.class;
            }
            if(method.getReturnType()==boolean.class)return false;
            if(method.getReturnType()==int.class)return 0;
            return null;
        });
    }
}
