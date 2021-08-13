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

        <#if entities??>
            <dependency>
                <artifactId>${entities.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>

       <#else>

           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-security</artifactId>
               <scope>provided</scope>
           </dependency>

        </#if>

        <#if services??>
            <dependency>
                <artifactId>${services.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>

        </#if>


        <#if controller??>
            <dependency>
                <artifactId>${controller.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <scope>provided</scope>
            </dependency>

        </#if>

        <#if moduleType?? && moduleType == 'service'>

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

            <!-- https://mvnrepository.com/artifact/javax.validation/validation-api -->
            <dependency>
                <groupId>javax.validation</groupId>
                <artifactId>validation-api</artifactId>
                <scope>provided</scope>
            </dependency>


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
                <artifactId>spring-boot-starter-aop</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-cache</artifactId>
                <scope>provided</scope>
            </dependency>


        </#if>

        <#if moduleType?? && moduleType == 'controller' >

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>io.springfox</groupId>
                <artifactId>springfox-boot-starter</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>javax.validation</groupId>
                <artifactId>validation-api</artifactId>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-aop</artifactId>
                <scope>provided</scope>
            </dependency>

        </#if>

        <#if moduleType?? && moduleType == 'testcase' >

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
                <groupId>com.alibaba</groupId>
                <artifactId>druid</artifactId>
            </dependency>

            <dependency>
                <groupId>io.springfox</groupId>
                <artifactId>springfox-boot-starter</artifactId>
            </dependency>

            <dependency>
                <groupId>com.querydsl</groupId>
                <artifactId>querydsl-jpa</artifactId>
            </dependency>

            <dependency>
                <groupId>com.querydsl</groupId>
                <artifactId>querydsl-apt</artifactId>
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

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-test</artifactId>
                <scope>test</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-security</artifactId>
            </dependency>

        </#if>

    </dependencies>

    <build>


        <pluginManagement>
            <plugins>

            </plugins>
        </pluginManagement>

        <plugins>

                <#if moduleType?? && moduleType == 'testcase'>

                <plugin>
                    <artifactId>maven-dependency-plugin</artifactId>
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

                    <configuration>
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

                    <executions>
                        <execution>
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
        </#if>

       </plugins>

    </build>


</project>