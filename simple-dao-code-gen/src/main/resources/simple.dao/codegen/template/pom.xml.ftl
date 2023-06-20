<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>${parent.groupId}</groupId>
        <artifactId>${parent.artifactId}</artifactId>
<#--        <version>${parent.version}</version>-->
        <version>${r"${revision}"}</version>
    </parent>

<#--  @Author Auto gen by simple-dao-codegen ${now} -->

    <artifactId>${artifactId}</artifactId>

<#--    <groupId>${groupId}</groupId>-->
<#--    <version>${version}</version>-->

    <packaging>${packaging}</packaging>

    <properties>

    </properties>

    <repositories>

    </repositories>


    <dependencyManagement>
        <dependencies>

        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- https://mvnrepository.com/artifact/javax.validation/validation-api -->
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <scope>provided</scope>
        </dependency>

        <#if moduleType == 'service'>
            <dependency>
                <artifactId>${entities.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>

            <dependency>
                <groupId>com.fasterxml.jackson.core</groupId>
                <artifactId>jackson-annotations</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>jakarta.activation</groupId>
                <artifactId>jakarta.activation-api</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>jakarta.annotation</groupId>
                <artifactId>jakarta.annotation-api</artifactId>
                <scope>provided</scope>
            </dependency>
        </#if>

        <#if moduleType == 'starter' || moduleType == 'controller'>
            <dependency>
                <artifactId>${service.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>
        </#if>

        <#if moduleType == 'bootstrap'>
            <dependency>
                <artifactId>${starter.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>

            <dependency>
                <artifactId>${controller.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>
        </#if>


        <#if (moduleType == 'starter' || moduleType == 'controller')>

            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-data-jpa</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-websocket</artifactId>
                <scope>provided</scope>
            </dependency>

        </#if>

        <#if moduleType?? && (moduleType == 'starter')>
            <#-- starter -->
            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-cache</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-openfeign</artifactId>
                <scope>provided</scope>
            </dependency>

        </#if>

        <#if moduleType?? && moduleType == 'controller' >
            <#-- api spring-boot-starter-web -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-webmvc-core</artifactId>
                <scope>provided</scope>
            </dependency>
        </#if>

        <#if moduleType?? && moduleType == 'bootstrap' >

            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo-dependencies-zookeeper</artifactId>
                <type>pom</type>
                <exclusions>
                    <exclusion>
                        <groupId>log4j</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                    <exclusion>
                        <groupId>org.slf4j</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                </exclusions>
            </dependency>

            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo-spring-boot-starter</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-data-jpa</artifactId>
            </dependency>

            <!-- https://mvnrepository.com/artifact/javax.validation/validation-api -->
            <dependency>
                <groupId>javax.validation</groupId>
                <artifactId>validation-api</artifactId>
            </dependency>

            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
            </dependency>

            <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
            </dependency>

            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
            </dependency>

            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-ui</artifactId>
            </dependency>

            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-autoconfigure</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-configuration-processor</artifactId>
            </dependency>

            <dependency>
                <groupId>${r"${levin.service-support.groupId}"}</groupId>
                <artifactId>service-support</artifactId>
            </dependency>

            <dependency>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-jpa-starter</artifactId>
            </dependency>

            <dependency>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-id-generator</artifactId>
            </dependency>

            <dependency>
                <groupId>javax.validation</groupId>
                <artifactId>validation-api</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-cache</artifactId>
            </dependency>

<#--            <dependency>
                <groupId>com.github.ben-manes.caffeine</groupId>
                <artifactId>caffeine</artifactId>
            </dependency>-->

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-data-redis</artifactId>
            </dependency>

            <dependency>
                <groupId>org.redisson</groupId>
                <artifactId>redisson-spring-boot-starter</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-websocket</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-test</artifactId>
                <scope>test</scope>
            </dependency>

            <dependency>
                <groupId>cn.hutool</groupId>
                <artifactId>hutool-all</artifactId>
            </dependency>

            <dependency>
                <groupId>com.google.code.gson</groupId>
                <artifactId>gson</artifactId>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-openfeign</artifactId>
            </dependency>

        </#if>

    </dependencies>

    <build>

        <pluginManagement>
            <plugins>

            </plugins>
        </pluginManagement>

        <plugins>

                <#if moduleType?? && moduleType == 'bootstrap'>

                <plugin>
                    <artifactId>maven-dependency-plugin</artifactId>
                    <configuration>
                        <skip>${r"${spring-boot.repackage-single-jar}"}</skip>
                    </configuration>

                    <executions>

                        <execution>
                            <id>copy-biz-dependencies</id>
                            <phase>package</phase>
                            <goals>
                                <goal>copy-dependencies</goal>
                            </goals>

                            <configuration>
                                <!--  包含的业务包名  -->
                                <includeGroupIds>${r"${parent.groupId}"}</includeGroupIds>
                                <outputDirectory>${r"${project.build.directory}/biz-libs"}</outputDirectory>
                            </configuration>
                        </execution>

                        <execution>
                            <id>copy-third-dependencies</id>
                            <phase>package</phase>
                            <goals>
                                <goal>copy-dependencies</goal>
                            </goals>
                            <configuration>
                                <!--  排除的业务包名  -->
                                <excludeGroupIds>${r"${parent.groupId}"}</excludeGroupIds>
                                <outputDirectory>${r"${project.build.directory}/third-libs"}</outputDirectory>
                            </configuration>
                        </execution>
                    </executions>

                </plugin>

                    <plugin>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-maven-plugin</artifactId>

                        <executions>
                            <execution>
                                <id>repackage</id>
                                <configuration>
                                    <skip>${r"${spring-boot.repackage-not-single-jar}"}</skip>
                                </configuration>
                                <goals>
                                    <goal>repackage</goal>
                                </goals>
                            </execution>

                            <execution>
                                <id>构建非单体应用</id>

                                <configuration>
                                    <skip>${r"${spring-boot.repackage-single-jar}"}</skip>
                                    <!--   ZIP 布局 -->
                                    <layout>ZIP</layout>
                                    <includes>
                                        <include>
                                            <!--   不包含任何依赖 -->
                                            <groupId>nothing</groupId>
                                            <artifactId>nothing</artifactId>
                                        </include>
                                    </includes>
                                </configuration>

                                <goals>
                                    <goal>repackage</goal>
                                </goals>
                            </execution>
                        </executions>

                    </plugin>

                <plugin>
                    <artifactId>maven-resources-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>copy-shell-file</id>
                            <!-- here the phase you need -->
                            <phase>package</phase>
                            <goals>
                                <goal>copy-resources</goal>
                            </goals>
                            <configuration>
                                <outputDirectory>${r"${project.build.directory}"}</outputDirectory>
                                <resources>
                                    <resource>
                                        <directory>${r"${project.basedir}/src/main/resources/shell"}</directory>
                                        <!--    <filtering>true</filtering> -->
                                    </resource>
                                </resources>

                            </configuration>
                        </execution>
                    </executions>
                </plugin>

                <plugin>
                    <artifactId>maven-install-plugin</artifactId>
                    <configuration>
                        <skip>true</skip>
                    </configuration>
                </plugin>

                <plugin>
                    <artifactId>maven-deploy-plugin</artifactId>
                    <configuration>
                        <skip>true</skip>
                    </configuration>
                </plugin>

        </#if>

       </plugins>

    </build>

</project>
