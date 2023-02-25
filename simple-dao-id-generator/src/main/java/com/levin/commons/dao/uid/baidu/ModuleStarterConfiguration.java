package com.levin.commons.dao.uid.baidu;

import com.levin.commons.conditional.ConditionalOn;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;

import static com.levin.commons.dao.uid.baidu.ModuleOption.*;

@Role(BeanDefinition.ROLE_SUPPORT)
@Configuration(PLUGIN_PREFIX + "ModuleStarterConfiguration")

@EntityScan({ PACKAGE_NAME+".worker.entity"})
@ComponentScan({PACKAGE_NAME})
public class ModuleStarterConfiguration {

    @Autowired
    Environment environment;


    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = JdbcTemplate.class)
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = SimpleJdbcInsert.class)
    SimpleJdbcInsert simpleJdbcInsertOperations(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate);
    }

}
