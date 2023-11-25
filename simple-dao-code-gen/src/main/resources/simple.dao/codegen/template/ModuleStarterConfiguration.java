package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;

import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.service.proxy.ProxyBeanScan;

import com.levin.commons.service.support.*;
import com.levin.commons.utils.*;

import javax.annotation.*;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.cloud.openfeign.EnableFeignClients;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.spring.context.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块自举配置
 *
 * 模块需要自举加载的内容都需要配置在该类中
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Configuration(PLUGIN_PREFIX + "${className}")
@Slf4j

// Spring data jpa scan，jpa querydsl entity class ...
@EntityScan({PACKAGE_NAME})

// Spring 扫描
@ConfigurationPropertiesScan({PACKAGE_NAME})
@ComponentScan({PACKAGE_NAME})

// 自定义注解接口 扫描
@ProxyBeanScan(basePackages = {PACKAGE_NAME} , scanType = EntityRepository.class , factoryBeanClass = RepositoryFactoryBean.class)

// FeignClients 扫描
@EnableFeignClients({PACKAGE_NAME})

// Dubbo 扫描，根据现有的 Dubbo 3.1.x 版本的机制， DubboComponentScan 会先自动先扫描 Spring 的注解, 所以 @ComponentScan 可以注释。
<#if !enableDubbo>//</#if>@DubboComponentScan({PACKAGE_NAME})

public class ModuleStarterConfiguration {

    @Autowired
    Environment environment;

}
