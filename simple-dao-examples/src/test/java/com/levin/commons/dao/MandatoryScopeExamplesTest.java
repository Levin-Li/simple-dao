package com.levin.commons.dao;

import com.levin.commons.dao.domain.ScopePartner;
import com.levin.commons.dao.domain.ScopePartner.Kind;
import com.levin.commons.dao.domain.ScopePartner.Region;
import com.levin.commons.dao.exception.DaoSecurityException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MandatoryScopeExamplesTest extends DaoExamplesTestSupport {
    @AfterEach void clearScope() { MandatoryConditions.setProvider(null); }
    private ScopePartner create(Kind kind, Region region) {
        ScopePartner entity = new ScopePartner(); entity.setKind(kind); entity.setRegion(region); entity.setName("scope-"+kind+region);
        return dao.create(entity,false);
    }
    private void enableScope() {
        MandatoryCondition scope=MandatoryCondition.ofBranches(List.of(
                Map.of("kind",List.of(Kind.Customer),"region",List.of(Region.East)),
                Map.of("kind",List.of(Kind.Supplier),"region",List.of(Region.South))));
        MandatoryConditions.setProvider((entity,action)->entity==ScopePartner.class?scope:MandatoryCondition.unrestricted());
    }
    @Test @Transactional void countListNativeCountAndStatisticsUseSameDnf() {
        create(Kind.Customer,Region.East); create(Kind.Supplier,Region.South);
        ScopePartner hidden=create(Kind.Customer,Region.South); enableScope();
        SelectDao<ScopePartner> query=dao.selectFrom(ScopePartner.class).disableSafeMode();
        assertEquals(2,query.count()); assertEquals(2,query.find().size()); assertEquals(2,query.count());
        assertNull(dao.find(ScopePartner.class,hidden.getId()));
        assertEquals(2,dao.selectFrom(ScopePartner.class).disableSafeMode().select("region").groupBy("region").count());
        assertEquals(2,dao.selectFrom(dao.getTableName(ScopePartner.class)).disableSafeMode().count());
    }
    @Test @Transactional void updateChecksOldAndPostStateWithoutLosingFieldCorrelation() {
        ScopePartner east=create(Kind.Customer,Region.East), south=create(Kind.Supplier,Region.South), hidden=create(Kind.Customer,Region.South);
        enableScope();
        assertEquals(0,dao.updateTo(ScopePartner.class).eq("id",east.getId()).set("kind",Kind.Supplier).update());
        assertEquals(1,dao.updateTo(ScopePartner.class).eq("id",east.getId()).set("kind",Kind.Supplier).set("region",Region.South).update());
        assertEquals(0,dao.updateTo(ScopePartner.class).eq("id",hidden.getId()).set("name","hidden-change").update());
        assertTrue(dao.updateTo(ScopePartner.class).eq("id",south.getId()).set("name","visible-change").singleUpdate());
        assertThrows(DaoSecurityException.class,()->dao.updateTo(ScopePartner.class).set("name","no-target").update());
        assertThrows(DaoSecurityException.class,()->dao.updateTo(ScopePartner.class).eq("id",south.getId()).setByStatement("kind = CASE WHEN 1=1 THEN 'Customer' END").update());
    }
    @Test @Transactional void logicalDeleteAndDirectEntryPointsCannotBypassScope() {
        ScopePartner visible=create(Kind.Customer,Region.East), hidden=create(Kind.Customer,Region.South);
        enableScope();
        assertFalse(dao.deleteById(ScopePartner.class,hidden.getId()));
        assertEquals(0,dao.deleteFrom(ScopePartner.class).eq("id",hidden.getId()).delete());
        assertTrue(dao.deleteFrom(ScopePartner.class).eq("id",visible.getId()).singleDelete());
        assertEquals(0,dao.selectFrom(ScopePartner.class).disableSafeMode().count());
        assertThrows(DaoSecurityException.class,()->dao.save(hidden,false));
        assertThrows(DaoSecurityException.class,()->dao.delete(hidden));
        assertThrows(DaoSecurityException.class,()->create(Kind.Customer,Region.South));
    }
    @Test @Transactional void nativeOrdinalAndStringEnumBindingsRemainTyped() {
        ScopePartner gold=new ScopePartner(); gold.setKind(Kind.Customer); gold.setRegion(Region.East); gold.setRating(ScopePartner.Rating.Gold); gold.setName("gold"); dao.create(gold,false);
        ScopePartner silver=new ScopePartner(); silver.setKind(Kind.Customer); silver.setRegion(Region.East); silver.setRating(ScopePartner.Rating.Silver); silver.setName("silver"); dao.create(silver,false);
        MandatoryConditions.setProvider((entity,action)->entity==ScopePartner.class
                ?MandatoryCondition.ofBranches(List.of(Map.of("kind",List.of(Kind.Customer),"rating",List.of(ScopePartner.Rating.Gold))))
                :MandatoryCondition.unrestricted());
        assertEquals(1,dao.selectFrom(dao.getTableName(ScopePartner.class)).disableSafeMode().count());
        assertEquals(0,dao.updateTo(ScopePartner.class).eq("id",gold.getId()).set("rating",ScopePartner.Rating.Silver).update());
    }
    @Test @Transactional void deniedScopeNeverBecomesAnEmptyIgnoredInPredicate() {
        create(Kind.Customer,Region.East);
        MandatoryConditions.setProvider((entity,action)->entity==ScopePartner.class?MandatoryCondition.deny():MandatoryCondition.unrestricted());
        assertEquals(0,dao.selectFrom(ScopePartner.class).disableSafeMode().count());
        assertEquals(0,dao.selectFrom(ScopePartner.class).disableSafeMode().find().size());
        assertEquals(0,dao.updateTo(ScopePartner.class).eq("name","anything").set("name","changed").update());
    }
}
