package com.levin.commons.dao.codegen.example;

import com.levin.commons.dao.SimpleDao;
import com.levin.commons.plugin.PluginManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.*;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;

import com.levin.commons.dao.codegen.example.entities.User;
import com.levin.commons.dao.codegen.example.entities.Group;
import com.levin.commons.dao.codegen.example.entities.Task;

import com.levin.commons.dao.codegen.example.services.user.UserService;
import com.levin.commons.dao.codegen.example.services.group.GroupService;
import com.levin.commons.dao.codegen.example.services.task.TaskService;

import com.levin.commons.dao.codegen.example.controller.user.UserController;
import com.levin.commons.dao.codegen.example.controller.group.GroupController;
import com.levin.commons.dao.codegen.example.controller.task.TaskController;

@Component
@Slf4j
public class ModuleDataInitializer implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    SimpleDao dao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    TaskService taskService;


    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("[ {} ] on applicationContext ..." , ModuleOption.ID);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext() == applicationContext) {
            initData();
        }
    }

    void initData() {

        log.info("[ {} ] on init ..." , ModuleOption.ID);

        Random random = new Random(this.hashCode());

        //@todo 初始化数据

        log.info("***** {} 数据初始化完成 ******" , ModuleOption.ID);
    }

}
