package com.levin.commons.dao.codegen;

import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.domain.PersonalObject;
import com.levin.commons.dao.support.SelectDaoImpl;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import static org.junit.jupiter.api.Assertions.*;

class PersonalScopeConditionGenerationTest {
    @Test void unsafeUserWithoutPersonalAccessCanOnlyQueryOwnData() { String s=dao().appendByQueryObj(new PersonalReq().owner("u1").current("u1").unsafe()).genFinalStatement(); assertTrue(s.contains("p.ownerId ="),s); }
    @Test void personalAccessWithoutOwnerScopeAddsNoOwnerCondition() { String s=dao().appendByQueryObj(new PersonalReq().access()).genFinalStatement(); assertFalse(s.contains("p.ownerId"),s); }
    @Test void unsafeUserWithoutAccessUsingAnotherOwnerThrows() { assertThrows(com.levin.commons.dao.exception.StatementBuildException.class,()->dao().appendByQueryObj(new PersonalReq().owner("u2").current("u1").unsafe())); }
    @Test void unsafeNonAdminDeleteOfAnotherOwnerThrows() { PersonalReq r=new PersonalReq().owner("u2").current("u1").unsafe().delete(); assertThrows(IllegalStateException.class,()->r.ownerCondition(false,true)); }
    private static SelectDaoImpl<PersonalEntity> dao(){return new SelectDaoImpl<>(stub(),false,PersonalEntity.class,"p");}
    private static MiniDao stub(){return (MiniDao) Proxy.newProxyInstance(PersonalScopeConditionGenerationTest.class.getClassLoader(),new Class[]{MiniDao.class},(p,m,a)->{if(m.getName().equals("getParamPlaceholder"))return ":?";if(m.getName().equals("getNamingStrategy"))return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;if(m.getReturnType()==boolean.class)return false;if(m.getReturnType()==int.class)return 0;return null;});}
    static class PersonalEntity { String ownerId; }
    static class PersonalReq implements PersonalObject { @OR(autoClose=true) @Eq(condition="ownerCondition(#_isQuery,#_isDelete)") String ownerId; boolean unsafe,access,delete; String current;
        PersonalReq owner(String v){ownerId=v;return this;} PersonalReq current(String v){current=v;return this;} PersonalReq unsafe(){unsafe=true;return this;} PersonalReq access(){access=true;return this;} PersonalReq delete(){delete=true;return this;}
        void check(boolean del){if(unsafe&&(!access||(del))&&(ownerId==null||!ownerId.equals(current)))throw new IllegalStateException("owner");}
        public boolean ownerCondition(boolean query,boolean del){check(del);return ownerId!=null&&(!access||query||del);}
        @Override @SuppressWarnings("unchecked") public <UID extends java.io.Serializable> UID getOwnerId(){return (UID)ownerId;}
    }
}
