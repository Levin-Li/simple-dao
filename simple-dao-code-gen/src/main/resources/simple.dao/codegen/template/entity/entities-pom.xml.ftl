<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <!-- 自动生成标记，请不要删除本行 simple-dao-codegen-flag=${parent__groupId}  -->
    <!-- Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。 -->

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>${parent__groupId}</groupId>
        <artifactId>${parent__artifactId}</artifactId>
        <version>${r"${revision}"}</version>
    </parent>

    <artifactId>${project__artifactId}</artifactId>

    <packaging>${project__packaging}</packaging>

    <dependencies>

        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>${r"${levin.simple-dao.groupId}"}</groupId>
            <artifactId>simple-dao-annotations</artifactId>
            <scope>provided</scope>
        </dependency>

        <#if enableOakBaseFramework>
        <dependency>
            <groupId>com.levin.oak.base</groupId>
            <artifactId>framework-base-entities</artifactId>
            <scope>provided</scope>
        </dependency>
        </#if>

    </dependencies>

    <build>

    </build>

</project>
