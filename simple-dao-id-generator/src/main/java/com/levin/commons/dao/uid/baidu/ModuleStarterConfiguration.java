package com.levin.commons.dao.uid.baidu;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;

import org.springframework.beans.factory.annotation.*;

import static com.levin.commons.dao.uid.baidu.ModuleOption.*;

@Role(BeanDefinition.ROLE_SUPPORT)
@Configuration(PLUGIN_PREFIX + "ModuleStarterConfiguration")
@ComponentScan({PACKAGE_NAME})
public class ModuleStarterConfiguration {

    @Autowired
    Environment environment;

}
