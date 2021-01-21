<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>


    <parent>
        <groupId>com.levin.commons</groupId>
        <artifactId>simple-dao-parent</artifactId>
        <version>${levin.simple-dao.version}</version>
    </parent>


    <artifactId>simple-dao-codegen</artifactId>

    <packaging>maven-plugin</packaging>

    <properties>
        <mavenVersion>3.6.3</mavenVersion>
    </properties>


    <dependencyManagement>
        <dependencies>

        </dependencies>
    </dependencyManagement>

    <dependencies>

        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-artifact</artifactId>
            <version>${mavenVersion}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-plugin-api</artifactId>
            <version>${mavenVersion}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-model</artifactId>
            <version>${mavenVersion}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-core</artifactId>
            <version>${mavenVersion}</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-repository-metadata</artifactId>
            <version>${mavenVersion}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-settings</artifactId>
            <version>${mavenVersion}</version>
            <exclusions>
                <exclusion>
                    <groupId>org.codehaus.plexus</groupId>
                    <artifactId>plexus-utils</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.apache.maven.plugin-tools</groupId>
            <artifactId>maven-plugin-annotations</artifactId>
            <version>3.6.0</version>
            <scope>provided</scope>
        </dependency>


        <dependency>
            <groupId>org.apache.maven.wagon</groupId>
            <artifactId>wagon-http-lightweight</artifactId>
            <version>3.4.0</version>
        </dependency>


        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </dependency>

        <dependency>
            <groupId>com.github.Levin-Li</groupId>
            <artifactId>service-support</artifactId>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.maven.shared</groupId>
            <artifactId>maven-common-artifact-filters</artifactId>
            <version>3.1.0</version>
        </dependency>

        <dependency>
            <groupId>org.apache.maven.shared</groupId>
            <artifactId>maven-artifact-transfer</artifactId>
            <version>0.13.1</version>
        </dependency>

        <dependency>
            <groupId>org.apache.maven.shared</groupId>
            <artifactId>maven-shared-utils</artifactId>
            <version>3.3.3</version>
        </dependency>

        <!-- plexus -->
        <dependency>
            <groupId>org.codehaus.plexus</groupId>
            <artifactId>plexus-archiver</artifactId>
            <version>4.2.2</version>
        </dependency>

        <dependency>
            <groupId>org.codehaus.plexus</groupId>
            <artifactId>plexus-utils</artifactId>
            <version>3.3.0</version>
        </dependency>

        <dependency>
            <groupId>org.codehaus.plexus</groupId>
            <artifactId>plexus-io</artifactId>
            <version>3.2.0</version>
        </dependency>


        <dependency>
            <groupId>${project.groupId}</groupId>
            <artifactId>simple-dao-annotations</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
        </dependency>

        <dependency>
            <groupId>io.swagger.core.v3</groupId>
            <artifactId>swagger-annotations</artifactId>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>javax.persistence</groupId>
            <artifactId>javax.persistence-api</artifactId>
        </dependency>


        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
        </dependency>

        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-all</artifactId>
            <version>2.4.17</version>
        </dependency>

        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
        </dependency>

        <dependency>
            <groupId>com.trilead</groupId>
            <artifactId>trilead-ssh2</artifactId>
            <version>1.0.0-build221</version>
        </dependency>

        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.8.0</version>
        </dependency>

    </dependencies>

    <build>

        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-plugin-plugin</artifactId>
                    <version>3.6.0</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>


</project>