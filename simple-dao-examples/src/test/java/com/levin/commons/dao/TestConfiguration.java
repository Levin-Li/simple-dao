package com.levin.commons.dao;

import com.levin.commons.dao.proxy.*;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.support.H2JsonFunctions;
import com.levin.commons.service.proxy.EnableProxyBean;
import com.levin.commons.service.proxy.ProxyBeanScan;
import com.levin.commons.service.proxy.ProxyBeanScans;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;


@SpringBootConfiguration
@EnableAutoConfiguration

@ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com .levin .", " .. .", "com.lev", "com.aaa. .. ..", " aa. bb .. . cc ", " . . ", " . "})

@ProxyBeanScans({
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"org .dao .test", " ", ""}),
        @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                , basePackages = {"com. levin. commons . dao.."}),

        @ProxyBeanScan(scanType = API.class, invocationHandlerClass = JdkProxyHandler.class
                , basePackages = {"com. levin."}),

        @ProxyBeanScan(scanType = API2.class, invocationHandlerClass = CglibProxyHandler.class
                , basePackages = {"com. levin."}),


        @ProxyBeanScan(scanType = API3.class, invocationHandlerClass = AopProxyHandler.class, onlyScan = true
                , basePackages = {"com. levin."}),
})

//@EnableProxyBean(registerTypes = {API.class, API2.class, API3.class})
@EnableProxyBean

@EntityScan({"com.levin.commons.dao"})

@ComponentScan("com.levin.commons.dao")
public class TestConfiguration {

    @Bean
    public InitializingBean registerH2JsonFunctions(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                DatabaseMetaData metaData = connection.getMetaData();
                String databaseName = metaData.getDatabaseProductName();
                if (databaseName == null || !databaseName.toLowerCase().contains("h2")) {
                    return;
                }
                statement.execute("CREATE ALIAS IF NOT EXISTS JSON_EXTRACT FOR '" + H2JsonFunctions.class.getName() + ".jsonExtract'");
                statement.execute("CREATE ALIAS IF NOT EXISTS JSON_UNQUOTE FOR '" + H2JsonFunctions.class.getName() + ".jsonUnquote'");
                statement.execute("CREATE ALIAS IF NOT EXISTS JSON_CONTAINS_PATH FOR '" + H2JsonFunctions.class.getName() + ".jsonContainsPath'");
            } catch (Exception e) {
                throw new IllegalStateException("failed to register H2 JSON function aliases", e);
            }
        };
    }

}
