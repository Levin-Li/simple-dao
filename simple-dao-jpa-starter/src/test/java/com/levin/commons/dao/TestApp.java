package com.levin.commons.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;


@SpringBootApplication
public class TestApp  { //extends SpringBootServletInitializer


    @Autowired
    JpaDao jpaDao;

    public static void main(String[] args) throws Exception {

        System.out.println(TestApp.class.getSimpleName() + " WORK DIR:" + new File("").getAbsolutePath());

        SpringApplication.run(TestApp.class, args);


        Thread.sleep(100 * 1000L);

    }

//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        return builder.sources(TestApp.class);
//    }


}
