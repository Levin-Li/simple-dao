<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <!-- 自动生成标记，请不要删除本行 simple-dao-codegen-flag=${parent__groupId}  -->
    <!-- Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。 -->
    <modelVersion>4.0.0</modelVersion>

    <groupId>${project__groupId}</groupId>
    <artifactId>${project__artifactId}</artifactId>
    <version>${r"${revision}"}</version>

    <packaging>pom</packaging>

    <properties>

        <revision>${project__version}</revision>

        <dubbo.version>3.2.4</dubbo.version>

        <spring-boot.version>2.7.12</spring-boot.version>

        <levin.simple-dao.groupId>${codegen_plugin__groupId}</levin.simple-dao.groupId>
        <levin.simple-dao.version>${codegen_plugin__version}</levin.simple-dao.version>

        <levin.service-support.groupId>${service_support__groupId}</levin.service-support.groupId>
        <levin.service-support.version>${service_support__version}</levin.service-support.version>

        <levin-framework-base.version>2.0.6</levin-framework-base.version>

        <easyexcel.version>3.2.1</easyexcel.version>
        <druid.version>1.1.24</druid.version>
        <hutool.version>5.8.11</hutool.version>

        <sa-token.version>1.37.0</sa-token.version>
        <fastjson.version>1.2.83</fastjson.version>

        <j2cache.version>2.8.5-release</j2cache.version>
        <j2cache_starter.version>2.8.0-release</j2cache_starter.version>

        <knife4j.version>4.4.0</knife4j.version>
        <springdoc.version>1.6.14</springdoc.version>
        <swagger.version>2.2.7</swagger.version>
        <swagger.enable>true</swagger.enable>

        <redission.version>3.19.3</redission.version>
        <redission.version>3.16.8</redission.version>

        <net.java.dev.jna.version>5.2.0</net.java.dev.jna.version>

        <com.github.oshi.version>3.12.2</com.github.oshi.version>

        <openfeign.version>3.1.5</openfeign.version>

        <mysql-driver.version>8.0.32</mysql-driver.version>

        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>

        <!-- 兼容 jdk-21-->
        <!-- <lombok.version>1.18.30</lombok.version> -->

        <maven.test.skip>false</maven.test.skip>
        <maven.javadoc.skip>true</maven.javadoc.skip>

        <maven.source.skip>true</maven.source.skip>
        <maven.source.attach>false</maven.source.attach>

        <maven-jar-plugin.addMavenDescriptor>false</maven-jar-plugin.addMavenDescriptor>

        <spring-boot.repackage-not-single-jar>false</spring-boot.repackage-not-single-jar>
        <spring-boot.repackage-single-jar>true</spring-boot.repackage-single-jar>

        <mica-auto.version>2.3.0</mica-auto.version>

    </properties>


    <modules>

        ${modules}

    </modules>

    <profiles>

        <profile>
            <id>01-跳过测试</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>

            <properties>
                <maven.test.skip>true</maven.test.skip>
            </properties>

        </profile>

        <profile>
            <id>02-上传源代码到 Maven</id>
            <properties>
                <maven.source.skip>false</maven.source.skip>
            </properties>
        </profile>


        <profile>
            <id>03-上传JavaDoc到 Maven</id>
            <properties>
                <maven.javadoc.skip>false</maven.javadoc.skip>
            </properties>
        </profile>


        <profile>
            <id>env0-local</id>
            <properties>
                <spring.profiles.active>local</spring.profiles.active>
                <logback.profiles.level>DEBUG</logback.profiles.level>
                <logback.profiles.loghub.level>OFF</logback.profiles.loghub.level>
            </properties>
        </profile>

        <profile>
            <id>env1-dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>

            <properties>
                <spring.profiles.active>dev</spring.profiles.active>
                <logback.profiles.level>DEBUG</logback.profiles.level>
                <logback.profiles.loghub.level>OFF</logback.profiles.loghub.level>
            </properties>

        </profile>

        <profile>
            <id>env2-test</id>
            <properties>
                <spring.profiles.active>test</spring.profiles.active>
                <logback.profiles.level>DEBUG</logback.profiles.level>
                <logback.profiles.loghub.level>ERROR</logback.profiles.loghub.level>
            </properties>
        </profile>

        <profile>
            <id>env3-prod</id>
            <properties>
                <spring.profiles.active>prod</spring.profiles.active>
                <logback.profiles.level>INFO</logback.profiles.level>
                <logback.profiles.loghub.level>ERROR</logback.profiles.loghub.level>
                <swagger.enable>false</swagger.enable>
            </properties>
        </profile>

        <profile>
            <id>构建SpringBoot非单体应用</id>
            <properties>
                <spring-boot.repackage-not-single-jar>true</spring-boot.repackage-not-single-jar>
                <spring-boot.repackage-single-jar>false</spring-boot.repackage-single-jar>
            </properties>
        </profile>

    </profiles>

    <distributionManagement>

        <!--具体变量来自 Setting 文件-->
        <repository>
            <id>${r"${dist-repo}"}</id>
            <url>${r"${dist-repo.releases.url}"}</url>
        </repository>

        <snapshotRepository>
            <id>${r"${dist-repo}"}</id>
            <url>${r"${dist-repo.snapshots.url}"}</url>
        </snapshotRepository>

    </distributionManagement>

    <repositories>

        <repository>
            <!-- 仓库首页 https://maven.aliyun.com/ , 如果在 gitpack.io 打包时会非常的慢 -->
            <id>aliyun-central仓和jcenter仓的聚合仓</id>
            <url>https://maven.aliyun.com/repository/public</url>
        </repository>

        <repository>
            <id>jitpack.io</id>
            <url>https://www.jitpack.io</url>
        </repository>

    </repositories>

    <pluginRepositories>

        <pluginRepository>
            <!-- 仓库首页 https://maven.aliyun.com/ , 如果在 gitpack.io 打包时会非常的慢 -->
            <id>aliyun-central仓和jcenter仓的聚合仓</id>
            <url>https://maven.aliyun.com/repository/public</url>
        </pluginRepository>

        <pluginRepository>
            <!--  插件库 -->
            <id>jitpack.io</id>
            <url>https://www.jitpack.io</url>
        </pluginRepository>

    </pluginRepositories>

    <dependencyManagement>
        <dependencies>

            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${r"${spring-boot.version}"}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- dubbo3 https://cn.dubbo.apache.org/zh-cn/overview/core-features/ecosystem/-->
            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo-bom</artifactId>
                <version>${r"${dubbo.version}"}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.apache.dubbo</groupId>
                <artifactId>dubbo-dependencies-zookeeper</artifactId>
                <version>${r"${dubbo.version}"}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-annotations</artifactId>
                <version>${r"${levin.simple-dao.version}"}</version>
            </dependency>

            <dependency>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-core</artifactId>
                <version>${r"${levin.simple-dao.version}"}</version>
            </dependency>

            <dependency>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-jpa</artifactId>
                <version>${r"${levin.simple-dao.version}"}</version>
            </dependency>

            <dependency>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-id-generator</artifactId>
                <version>${r"${levin.simple-dao.version}"}</version>
            </dependency>

            <dependency>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-jpa-starter</artifactId>
                <version>${r"${levin.simple-dao.version}"}</version>
            </dependency>

            <dependency>
                <groupId>${r"${levin.service-support.groupId}"}</groupId>
                <artifactId>service-support</artifactId>
                <version>${r"${levin.service-support.version}"}</version>
            </dependency>

            <#if enableOakBaseFramework>
                 <dependency>
                     <groupId>com.levin.oak.base</groupId>
                     <artifactId>framework-base-services</artifactId>
                     <version>${r"${levin-framework-base.version}"}</version>
                     <scope>provided</scope>
                 </dependency>

                 <dependency>
                     <groupId>com.levin.oak.base</groupId>
                     <artifactId>framework-base-api</artifactId>
                     <version>${r"${levin-framework-base.version}"}</version>
                     <scope>provided</scope>
                 </dependency>

                 <dependency>
                     <groupId>com.levin.oak.base</groupId>
                     <artifactId>framework-base-services-impl</artifactId>
                     <version>${r"${levin-framework-base.version}"}</version>
                     <scope>provided</scope>
                 </dependency>

                 <dependency>
                     <groupId>com.levin.oak.base</groupId>
                     <artifactId>framework-base-admin-ui</artifactId>
                     <version>${r"${levin-framework-base.version}"}</version>
                     <scope>provided</scope>
                 </dependency>

                 <dependency>
                     <groupId>com.levin.oak.base</groupId>
                     <artifactId>framework-base-starter</artifactId>
                     <version>${r"${levin-framework-base.version}"}</version>
                     <scope>provided</scope>
                 </dependency>
            </#if>

            <dependency>
                <groupId>net.oschina.j2cache</groupId>
                <artifactId>j2cache-core</artifactId>
                <version>${r"${j2cache.version}"}</version>
                <exclusions>
                    <exclusion>
                        <groupId>com.alibaba</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>

                    <exclusion>
                        <groupId>org.slf4j</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                    <exclusion>
                        <groupId>redis.clients</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>

                    <exclusion>
                        <groupId>com.github.ben-manes.caffeine</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                </exclusions>
            </dependency>

            <dependency>
                <groupId>net.oschina.j2cache</groupId>
                <artifactId>j2cache-spring-boot2-starter</artifactId>
                <version>${r"${j2cache_starter.version}"}</version>

                <exclusions>
                    <exclusion>
                        <groupId>org.springframework</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>

                    <exclusion>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                </exclusions>

            </dependency>

            <dependency>
                <groupId>cn.hutool</groupId>
                <artifactId>hutool-all</artifactId>
                <version>${r"${hutool.version}"}</version>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>com.github.oshi</groupId>
                <artifactId>oshi-core</artifactId>
                <version>${r"${com.github.oshi.version}"}</version>
                <exclusions>
                    <exclusion>
                        <groupId>org.slf4j</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                    <exclusion>
                        <groupId>net.java.dev.jna</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                </exclusions>
            </dependency>

            <dependency>
                <groupId>net.java.dev.jna</groupId>
                <artifactId>jna-platform</artifactId>
                <version>${r"${net.java.dev.jna.version}"}</version>
            </dependency>

            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid</artifactId>
                <version>${r"${druid.version}"}</version>
            </dependency>

            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version>${r"${fastjson.version}"}</version>
            </dependency>

            <dependency>
                <groupId>org.springframework.session</groupId>
                <artifactId>spring-session-data-redis</artifactId>
            </dependency>

            <dependency>
                <groupId>org.redisson</groupId>
                <artifactId>redisson-spring-boot-starter</artifactId>
                <version>${r"${redission.version}"}</version>
            </dependency>

            <!-- Sa-Token 权限认证, 在线文档：http://sa-token.dev33.cn/ -->
            <dependency>
                <groupId>cn.dev33</groupId>
                <artifactId>sa-token-spring-boot-starter</artifactId>
                <version>${r"${sa-token.version}"}</version>
            </dependency>

            <!-- Sa-Token 整合 Redis （使用 jackson 序列化方式） -->
            <dependency>
                <groupId>cn.dev33</groupId>
                <artifactId>sa-token-redis-jackson</artifactId>
                <version>${r"${sa-token.version}"}</version>
            </dependency>

            <dependency>
                <groupId>cn.dev33</groupId>
                <artifactId>sa-token-redisson-jackson</artifactId>
                <version>${r"${sa-token.version}"}</version>
            </dependency>

            <!-- Sa-Token 整合 SpringAOP 实现注解鉴权 -->
            <dependency>
                <groupId>cn.dev33</groupId>
                <artifactId>sa-token-spring-aop</artifactId>
                <version>${r"${sa-token.version}"}</version>
            </dependency>

            <!-- Sa-Token-Quick-Login 插件 -->
            <dependency>
                <groupId>cn.dev33</groupId>
                <artifactId>sa-token-quick-login</artifactId>
                <version>${r"${sa-token.version}"}</version>
            </dependency>


            <!-- 引入Swagger3依赖 -->
            <dependency>
                <groupId>io.swagger.core.v3</groupId>
                <artifactId>swagger-annotations</artifactId>
                <version>${r"${swagger.version}"}</version>
            </dependency>

            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-webmvc-core</artifactId>
                <version>${r"${springdoc.version}"}</version>
            </dependency>

            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-ui</artifactId>
                <version>${r"${springdoc.version}"}</version>
            </dependency>

            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
                <version>${r"${knife4j.version}"}</version>
                <exclusions>
                    <exclusion>
                        <groupId>org.springdoc</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>

                    <exclusion>
                        <groupId>io.swagger.core.v3</groupId>
                        <artifactId>*</artifactId>
                    </exclusion>
                </exclusions>
            </dependency>

            <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
                <version>${r"${mysql-driver.version}"}</version>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-openfeign</artifactId>
                <version>${r"${openfeign.version}"}</version>
            </dependency>

            <dependency>
                <groupId>net.dreamlu</groupId>
                <artifactId>mica-auto</artifactId>
                <version>${r"${mica-auto.version}"}</version>
                <scope>provided</scope>
            </dependency>

            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>easyexcel</artifactId>
                <version>${r"${easyexcel.version}"}</version>
            </dependency>

        </dependencies>
    </dependencyManagement>

    <dependencies>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>javax.persistence</groupId>
            <artifactId>javax.persistence-api</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
            <artifactId>simple-dao-jpa</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>${r"${levin.service-support.groupId}"}</groupId>
            <artifactId>service-support</artifactId>
            <scope>provided</scope>
        </dependency>

        <#if enableOakBaseFramework>
        <dependency>
            <groupId>com.levin.oak.base</groupId>
            <artifactId>framework-base-services</artifactId>
            <scope>provided</scope>
        </dependency>
        </#if>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>io.swagger.core.v3</groupId>
            <artifactId>swagger-annotations</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <build>

        <resources>
            <!--   过滤SpringBoot profile文件   -->
            <resource>
                <directory>src/main/resources</directory>
                <excludes>
                    <!--  1、  先排除所有profile文件-->
                    <exclude>application-*.yml</exclude>
                </excludes>
            </resource>

            <resource>
                <!--  开启替换变量-->
                <filtering>true</filtering>

                <directory>src/main/resources</directory>
                <!--      2、  加入当前profile文件-->
                <includes>
                    <include>application.properties</include>
                    <include>application.yml</include>
                    <include>application-${r"${spring.profiles.active}"}.yml</include>
                </includes>
            </resource>
        </resources>

        <plugins>

            <plugin>
                <artifactId>maven-source-plugin</artifactId>
                <executions>
                    <execution>
                        <id>attach-source</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                        <inherited>true</inherited>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
                <artifactId>simple-dao-codegen</artifactId>
                <version>${r"${levin.simple-dao.version}"}</version>

                <configuration>
                    <!-- 生成的控制器代码是否包括目录-->
                    <!--  <isCreateControllerSubDir>false</isCreateControllerSubDir> -->

                    <!-- 是否生成BizController -->
                    <isCreateBizController>${isCreateBizController?c}</isCreateBizController>

                    <!-- 生成的DTO的Schema注解中描述的配置是否使用类引用-->
                    <isSchemaDescUseConstRef>${isSchemaDescUseConstRef?c}</isSchemaDescUseConstRef>

                    <enableOakBaseFramework>${enableOakBaseFramework?c}</enableOakBaseFramework>

                    <enableDubbo>${enableDubbo?c}</enableDubbo>

                </configuration>

                <dependencies>
                    <dependency>
                        <groupId>${r"${levin.service-support.groupId}"}</groupId>
                        <artifactId>service-support</artifactId>
                        <version>${r"${levin.service-support.version}"}</version>
                    </dependency>
                </dependencies>
            </plugin>

            <plugin>
                <!--  flatten-maven-plugin 插件由 spring boot 进行版本管理-->
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>flatten-maven-plugin</artifactId>

                <configuration>
                    <updatePomFile>true</updatePomFile>
                    <flattenMode>resolveCiFriendliesOnly</flattenMode>
                </configuration>

                <executions>

                    <execution>
                        <id>flatten</id>
                        <phase>process-resources</phase>
                        <goals>
                            <goal>flatten</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${r"${project.build.directory}"}</outputDirectory>
                        </configuration>
                    </execution>

                    <execution>
                        <id>flatten.clean</id>
                        <phase>clean</phase>
                        <goals>
                            <goal>clean</goal>
                        </goals>
                    </execution>

                </executions>

            </plugin>

        </plugins>
    </build>

</project>
