package com.levin.commons.dao.starter;

import com.levin.commons.dao.MiniDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Configuration
@AutoConfigureAfter(JpaDaoConfiguration.class)
@Slf4j
public class JpaDaoExConfiguration {

    @Resource
    private EntityManagerFactory entityManagerFactory;

    @Resource
    ApplicationContext applicationContext;

    @Resource
    JdbcOperations jdbcOperations;

    @Resource
    MiniDao miniDao;

    @PostConstruct
    void init() {



    }

}
