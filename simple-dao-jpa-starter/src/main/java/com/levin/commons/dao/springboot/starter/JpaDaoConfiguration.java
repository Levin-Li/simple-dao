package com.levin.commons.dao.springboot.starter;

import com.levin.commons.dao.starter.EnableJpaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableJpaDao
@Slf4j
public class JpaDaoConfiguration {

    //@SpringBootApplication
    //@EnableDiscoveryClient
    //@ComponentScan(basePackages = {"com.aaa.*"})
    //@EntityScan(basePackages = {"com.aaa"})
    //public class CqgtApplication {

}
