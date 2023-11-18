package com.levin.commons.dao.starter;

import com.levin.commons.dao.DaoContext;
import com.levin.commons.service.support.VariableResolverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
//@AutoConfigureBefore(JpaDaoConfiguration.class)
@ConditionalOnClass(VariableResolverManager.class)
//@ConditionalOnBean(VariableResolverManager.class)
@Slf4j
public class JpaDaoVariableResolverConfiguration {

    @Autowired
    VariableResolverManager variableResolverManager;

    @PostConstruct
    void init() {
        //设置变量解析器
        DaoContext.setDefaultVariableResolverSupplier(variableResolverManager);
        log.info("设置DaoContext默认的变量解析器");
    }

}
