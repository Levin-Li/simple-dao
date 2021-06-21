package ${modulePackageName};

import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.service.proxy.ProxyBeanScan;
import lombok.extern.slf4j.Slf4j;
import com.levin.commons.service.support.SimpleVariableInjector;
import com.levin.commons.service.support.VariableInjector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.context.annotation.*;

//Auto gen by simple-dao-codegen ${.now}

@Configuration("${modulePackageName}.${camelStyleModuleName}SpringConfiguration")
@Slf4j
//spring data scan，jpa querydsl entity class ...

@EntityScan({ModuleOption.PACKAGE_NAME})

@ComponentScan({ModuleOption.PACKAGE_NAME})

@ProxyBeanScan(basePackages = {ModuleOption.PACKAGE_NAME}
, scanType = EntityRepository.class
, factoryBeanClass = RepositoryFactoryBean.class)
public class ${camelStyleModuleName}SpringConfiguration {

  public void onInit(){
  }

  @Bean
  @ConditionalOnMissingBean(VariableInjector.class)
  public VariableInjector variableInjector() {
     return SimpleVariableInjector.defaultSimpleVariableInjector;
  }

}
