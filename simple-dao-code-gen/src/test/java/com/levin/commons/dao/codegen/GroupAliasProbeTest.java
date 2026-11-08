package com.levin.commons.dao.codegen;
import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.support.UpdateDaoImpl;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class GroupAliasProbeTest {
 static class Row { String id; String name; String orgId; }
 static class Base { @Ignore List<String> allowed=List.of("a"); }
 @DaoLogicGroup(name="visible",logicOp=DaoLogicGroup.LogicOp.OR,minConditions=1,unless="1=2")
 static class Query extends Base { @Update(condition="false") @In(value="orgId",group="visible") List<String> scope; @PostConstruct public void initScope(){ scope=allowed; } }
 static class Set extends Base { @Eq String id="1"; @Update String name="n"; }
 @Test void aliasSharesListAndBuildsBatch() {
  MiniDao dao=(MiniDao)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{MiniDao.class},(p,m,a)->{
   if(m.getName().equals("getSafeModeMaxLimit")) return 10;
   if(m.getName().equals("getParamPlaceholder")) return ":?";
   if(m.getName().equals("getNamingStrategy")) return PhysicalNamingStrategy.DEFAULT_PHYSICAL_NAMING_STRATEGY;
   if(m.getReturnType()==boolean.class) return false;
   if(m.getReturnType()==int.class) return 0;
   return null;
  });
  var update=new UpdateDaoImpl<>(dao,false,Row.class,"r"); update.limit(0,10); var query=new Query(); update.appendByQueryObj(new Set(),query);
  assertSame(query.allowed,query.scope); String sql=update.genFinalStatement(); assertTrue(sql.contains("orgId IN"),sql); assertFalse(sql.contains("scope ="),sql); assertTrue(update.genFinalParamList().toString().contains("a"));
 }
}
