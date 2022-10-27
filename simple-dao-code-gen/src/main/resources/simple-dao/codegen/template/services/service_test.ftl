package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import ${entityClassPackage}.*;
import ${entityClassName};

import ${servicePackageName}.*;
import ${servicePackageName}.req.*;
import ${servicePackageName}.info.*;

<#list fields as field>
    <#if (field.lzay)??>
import ${field.classType.package.name}.${field.classType.simpleName};
    </#if>
    <#if (field.infoClassName)??>
import ${field.infoClassName};
    </#if>
</#list>

////////////////////////////////////
//自动导入列表
<#list importList as imp>
import ${imp};
</#list>
////////////////////////////////////

import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import org.springframework.util.*;
import java.util.Date;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  ${desc}测试
 *
 *  @author auto gen by simple-dao-codegen ${.now}
 *
 */

//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
//@Transactional(rollbackFor = {Throwable.class})
@Slf4j
public class ${className} {


    @Resource
    private ${serviceName} ${serviceName?uncap_first};

<#if pkField?exists>
    private ${pkField.typeName} ${pkField.name};
</#if>

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }


    @Test
    public void create${entityName}Test() {

        Create${entityName}Req req = new Create${entityName}Req();

<#list fields as field>
    <#if (!field.notUpdate && field.testValue?? && field.baseType && !field.hasDefValue && !field.jpaEntity) >
        <#if field.name!="id">
            // req.set${field.name?cap_first}(${field.testValue!'null'});//${field.desc} ${field.required?string('必填','')}
        </#if>

    </#if>
</#list>

<#if pkField?exists>
       ${pkField.typeName} ${pkField.name}  = ${serviceName?uncap_first}.create(req);

        log.debug("新增${desc}->" + ${pkField.name});

        Assert.assertTrue(${pkField.name} != null);
<#else>
        Assert.assertTrue(${serviceName?uncap_first}.create(req));
</#if>

    }


    @Test
    public void query${entityName}Test() {

        Query${entityName}Req req = new Query${entityName}Req();

<#list fields as field>
    <#if field.typeName=='Date'>
        // req.setGte${field.name?cap_first}(DateUtils.getZoneHour(new Date()));//最小${field.desc}
        // req.setLte${field.name?cap_first}(DateUtils.getEndHour(new Date()));//最大${field.desc}
    <#elseif !field.jpaEntity && field.baseType>
        // req.set${field.name?cap_first}(${(!field.testValue?? || field.uk || field.pk)?string('null',field.testValue!'null')});//${field.desc}
    <#elseif field.lazy!>
        // req.setLoad${field.name?cap_first}(true);//加载${field.desc}
    </#if>
</#list>

        PagingData<${entityName}Info> resp = ${serviceName?uncap_first}.query(req,null);

        log.debug("查询${desc}->" + resp);

        Assert.assertTrue(!resp.isEmpty());
    }

    @Test
    public void update${entityName}Test() {

         Update${entityName}Req req = new Update${entityName}Req();

    <#if pkField?exists>
         req.set${pkField.name?cap_first}(${pkField.name});
    </#if>


    <#list fields as field>
        <#if !field.notUpdate && field.testValue?? && field.baseType>
           // req.set${field.name?cap_first}(${field.testValue});//${field.desc} ${field.required?string('必填','')}
        </#if>
    </#list>

          int resp = ${serviceName?uncap_first}.update(req);

          log.debug("更新${desc}-> " + resp);

          Assert.assertTrue(resp > 0);
    }

    @Test
    public void delete${entityName}Test() {

        ${entityName}IdReq req = new ${entityName}IdReq();

    <#if pkField?exists>
        req.set${pkField.name?cap_first}(${pkField.name});
    </#if>

        int n = ${serviceName?uncap_first}.delete(req);

        log.debug("删除${desc}->" + n);

        Assert.assertTrue(n > 0);
    }
}
