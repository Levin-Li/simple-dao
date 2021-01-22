<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>${parent.groupId}</groupId>
        <artifactId>${parent.artifactId}</artifactId>
        <version>${parent.version}</version>
    </parent>

    <artifactId>${artifactId}</artifactId>

<#--    <groupId>${groupId}</groupId>-->
<#--    <version>${version}</version>-->

    <packaging>${packaging}</packaging>

    <properties>

    </properties>

    <repositories>

        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>

        <repository>
            <!-- 仓库首页 https://maven.aliyun.com/ -->
            <id>aliyun-central仓和jcenter仓的聚合仓</id>
            <url>https://maven.aliyun.com/repository/public</url>
        </repository>

    </repositories>


    <dependencyManagement>
        <dependencies>

        </dependencies>
    </dependencyManagement>

    <dependencies>

        <#if !entities>
        <dependency>

            <artifactId>${entities.artifactId}</artifactId>
            <groupId>${r"${project.groupId}"}</groupId>
            <version>${r"${project.version}"}</version>

        </dependency>
        </#if>

        <#if !services>
            <dependency>
                <artifactId>${services.artifactId}</artifactId>
                <groupId>${r"${project.groupId}"}</groupId>
                <version>${r"${project.version}"}</version>
            </dependency>
        </#if>

    </dependencies>

    <build>


        <pluginManagement>
            <plugins>

            </plugins>
        </pluginManagement>

    </build>


</project>