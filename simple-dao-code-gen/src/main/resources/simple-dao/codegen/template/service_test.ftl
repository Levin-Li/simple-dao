package ${packageName};

import ${packageName}.evt.*;
import ${packageName}.info.${entityName}Info;
<#list fields as field>
    <#if (field.lzay)??>
import ${field.classType.package.name}.${field.classType.simpleName};
    </#if>
    <#if (field.infoClassName)??>
import ${field.infoClassName};
    </#if>
</#list>
<#list fields as field>
    <#if !field.baseType && field.enums>
import ${field.classType.name};
    </#if>
</#list>


import com.oaknt.common.service.support.model.ServiceQueryResp;
import com.oaknt.common.service.support.model.ServiceResp;
import com.oaknt.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.com.oaknt.yunxin.TestServiceMain;

/**
 *  ${desc}测试
 *  ${.now}
 *@author auto gen by oaknt
 *
 */
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestServiceMain.class})
//@Transactional(rollbackFor = {Throwable.class})
@Slf4j
public class ${className}Test {


    @Autowired
    private ${className} ${className?uncap_first};

    private ${pkField.type} ${pkField.name};

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void ${className?uncap_first}Test(){
        create${entityName}Test();
        edit${entityName}Test();
        find${entityName}Test();
        query${entityName}Test();
        del${entityName}Test();
    }

    @Test
    public void create${entityName}Test() {

        Create${entityName}Evt evt = new Create${entityName}Evt();
<#list fields as field>
    <#if (!field.notUpdate && field.testValue?? && !field.hasDefValue && !field.complex) || (field.identity?? && !field.identity)>
        <#if field.name!="id">
             evt.set${field.name?cap_first}(${field.testValue});//${field.desc} ${field.required?string('必填','')}
        </#if>

    </#if>
</#list>
        ServiceResp<${pkField.type}> serviceResp = ${className?uncap_first}.create${entityName}(evt);
        log.debug("创建${desc}->" + serviceResp);

        ${pkField.name} = serviceResp.getData();
        Assert.assertTrue(serviceResp.isSuccess());

    }

    @Test
    public void edit${entityName}Test() {

        Edit${entityName}Evt evt = new Edit${entityName}Evt();
        evt.set${pkField.name?cap_first}(${pkField.name});
<#list fields as field>
    <#if !field.notUpdate && field.testValue??>
        evt.set${field.name?cap_first}(${field.testValue});//${field.desc} ${field.required?string('必填','')}
    </#if>
</#list>
        ServiceResp serviceResp = ${className?uncap_first}.edit${entityName}(evt);
        log.debug("修改${desc}->" + serviceResp);
        Assert.assertTrue(serviceResp.isSuccess());
    }

    @Test
    public void find${entityName}Test() {

        Find${entityName}Evt evt = new Find${entityName}Evt();
        evt.set${pkField.name?cap_first}(${pkField.name});
        ServiceResp<${entityName}Info> serviceResp = ${className?uncap_first}.find${entityName}(evt);
        log.debug("查找${desc}["+${pkField.name}+"]->" + serviceResp);
        Assert.assertTrue(serviceResp.isSuccess());
    }

    @Test
    public void query${entityName}Test() {

        Query${entityName}Evt evt = new Query${entityName}Evt();
<#list fields as field>
    <#if field.type=='Date'>
        evt.setMin${field.name?cap_first}(DateUtils.getZoneHour(new Date()));//最小${field.desc}
        evt.setMax${field.name?cap_first}(DateUtils.getEndHour(new Date()));//最大${field.desc}
    <#elseif !field.complex>
        evt.set${field.name?cap_first}(${(!field.testValue?? || field.uk || field.pk)?string('null',field.testValue)});//${field.desc}
    <#elseif field.lazy!>
        evt.setLoad${field.name?cap_first}(true);//加载${field.desc}
    </#if>
</#list>
        ServiceQueryResp<${entityName}Info> serviceQueryResp = ${className?uncap_first}.query${entityName}(evt);
        log.debug("查询${desc}->" + serviceQueryResp);
        Assert.assertTrue(serviceQueryResp.isSuccess());
    }

    @Test
    public void del${entityName}Test() {
        Del${entityName}Evt evt = new Del${entityName}Evt();
        evt.set${pkField.name?cap_first}(${pkField.name});
        ServiceResp serviceResp = ${className?uncap_first}.del${entityName}(evt);
        log.debug("删除${desc}->" + serviceResp);
        Assert.assertTrue(serviceResp.isSuccess());
    }
}
