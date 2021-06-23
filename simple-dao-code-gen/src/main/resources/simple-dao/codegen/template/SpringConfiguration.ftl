package ${modulePackageName};

import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.service.proxy.ProxyBeanScan;

import com.levin.commons.service.support.*;
import com.levin.commons.utils.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;


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


    @Autowired
    Environment environment;

    @Bean
    public VariableResolverConfigurer variableResolverConfigurer() {
        return variableResolverManager -> {

            //加入全局变量
            variableResolverManager.addVariableResolverByCtx(false,
                    MapUtils.putFirst(ModuleOption.ID+"_x1", "x1_value")
                            .put(ModuleOption.ID+"_x2", "x2_value")
                            .build());

            //@todo 增加自定义变量解析器
            //加入
            variableResolverManager.addVariableResolvers(false, new VariableResolver() {
                @Override
                public <T> ValueHolder<T> resolve(String key, T oldValue, boolean required, Class<?>... classes) throws VariableNotFoundException {

                    if (!key.startsWith("env:")) {
                        return ValueHolder.notValue();
                    }

                    return (ValueHolder<T>) new ValueHolder<>()
                            .setValue(environment.getProperty(key.substring(4)))
                            .setHasValue(environment.containsProperty(key));
                }
            });
        };
    }

}
