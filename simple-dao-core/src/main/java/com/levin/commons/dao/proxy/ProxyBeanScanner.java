/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.levin.commons.dao.proxy;

import com.levin.commons.dao.util.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * 代理额扫描
 *
 */
public abstract class ProxyBeanScanner {

    /**
     * 重要说明 ImportBeanDefinitionRegistrar 的实现类可以加
     * BeanFactoryAware, EnvironmentAware
     * BeanClassLoaderAware, ResourceLoaderAware
     * 以获取环境变量
     */
    public static class Registrar implements BeanFactoryAware, EnvironmentAware
            , BeanClassLoaderAware, ResourceLoaderAware
            , ImportBeanDefinitionRegistrar {

        private ClassLoader classLoader;

        private ResourceLoader resourceLoader;

        private BeanFactory beanFactory;

        private Environment environment;

        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public void setEnvironment(Environment environment) {

            this.environment = environment;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {

            this.resourceLoader = resourceLoader;
        }


        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

            //获取收集到的扫描包列表
            ScanPackagesHolder scanPackagesHolder = ScanPackagesHolder.get(beanFactory);

            scanPackagesHolder.getScanPairs().values().stream().forEachOrdered(
                    scanPair -> {

                        scan(scanPair.getScanPackages(), scanPair.scanType).stream().forEachOrdered(
                                type -> {
                                    //注册 bean
                                    registry.registerBeanDefinition(type.getName(), BeanDefinitionBuilder
                                            .genericBeanDefinition(scanPair.factoryBeanClass)
                                            .addPropertyValue("actualType", type).getBeanDefinition());

                                }

                        );

                    }
            );

        }


        private Set<Class<?>> scan(Set<String> packages, Class<? extends Annotation>... annotationTypes) {

            if (packages.isEmpty()) {
                return Collections.emptySet();
            }

            List<String> minList = ObjectUtil.formatPackages(packages);

            //扫描类

            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                    false) {
                @Override
                protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                    return true;
                }
            };

            scanner.setEnvironment(environment);
            scanner.setResourceLoader(resourceLoader);

            for (Class<? extends Annotation> annotationType : annotationTypes) {
                scanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
            }

            Set<Class<?>> entitySet = new LinkedHashSet<>();

            for (String basePackage : minList) {
                if (StringUtils.hasText(basePackage)) {
                    for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                        try {
                            //加载类
                            entitySet.add(ClassUtils.forName(candidate.getBeanClassName(), classLoader));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            return entitySet;
        }


    }
}
