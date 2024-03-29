package ${packageName};

import static ${modulePackageName}.ModuleOption.*;
import ${entityClassPackage}.*;
import ${entityClassName};

import ${bizServicePackageName}.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ${entityTitle}测试
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
//@Transactional(rollbackFor = {Throwable.class})
@Slf4j
public class ${className} {

    @Autowired
    private ${serviceName} ${serviceName?uncap_first};

    @Autowired
    private Biz${serviceName} biz${serviceName};

<#if pkField?exists>
    private ${pkField.typeName} ${pkField.name};
</#if>

    @BeforeAll
    public static void beforeAll() throws Exception {
    }

    @AfterAll
    public static void afterAll() throws Exception {
    }

    @BeforeEach
    public void beforeEach() throws Exception {
    }

    @AfterEach
    public void afterEach() throws Exception {
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

        log.debug("新增${entityTitle}->" + ${pkField.name});

        Assert.isTrue(${pkField.name} != null, "${entityTitle}");
<#else>
        Assert.isTrue(${serviceName?uncap_first}.create(req), "${entityTitle}");
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

        log.debug("查询${entityTitle}->" + resp);

        Assert.isTrue(!resp.isEmpty(), "${entityTitle}");
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

          boolean ok = ${serviceName?uncap_first}.update(req);

          log.debug("更新${entityTitle}-> " + ok);

          Assert.isTrue(ok, "${entityTitle}");
    }

    @Test
    public void delete${entityName}Test() {

        ${entityName}IdReq req = new ${entityName}IdReq();

    <#if pkField?exists>
        req.set${pkField.name?cap_first}(${pkField.name});
    </#if>

        boolean ok = ${serviceName?uncap_first}.delete(req);

        log.debug("删除${entityTitle}->" + ok);

        Assert.isTrue(ok , "${entityTitle}");
    }
}
