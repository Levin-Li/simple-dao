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


import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Data
public class ScanPackagesHolder {

    private static final String BEAN = ScanPackagesHolder.class.getName();

    private static final ScanPackagesHolder NONE = new ScanPackagesHolder();


    private Map<String, ScanPair> scanPairs;


    public static ScanPackagesHolder get(BeanFactory beanFactory) {
        try {
            return beanFactory.getBean(BEAN, ScanPackagesHolder.class);
        } catch (NoSuchBeanDefinitionException ex) {
            return NONE;
        }

    }

    /**
     * Register the specified entity scan packages with the system.
     *
     * @param registry the source registry
     * @param scanPair scanPair
     */
    public static void register(BeanDefinitionRegistry registry, ScanPair scanPair) {

        Assert.notNull(registry, "Registry must not be null");
        Assert.notNull(scanPair, "scanPair must not be null");


        String pName = "scanPairs";


        if (!registry.containsBeanDefinition(BEAN)) {
            registry.registerBeanDefinition(BEAN, BeanDefinitionBuilder
                    .genericBeanDefinition(ScanPackagesHolder.class)
                    .addPropertyValue(pName, new ConcurrentHashMap<>())
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                    .getBeanDefinition());
        }


        BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN);


        String key = scanPair.factoryBeanClass.getName() + "_" + scanPair.scanType.getName();


        Map<String, ScanPair> scanPairs = (Map<String, ScanPair>) beanDefinition.getPropertyValues().get(pName);

        ScanPair oldPair = scanPairs.get(key);

        if (oldPair == null) {
            scanPairs.put(key, scanPair);
        } else {
            oldPair.scanPackages.addAll(scanPair.scanPackages);
        }

    }


    /**
     * {@link ImportBeanDefinitionRegistrar} to store the base package from the importing
     * configuration.
     */
    static class Registrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

            AnnotationAttributes attributes1 = AnnotationAttributes.fromMap(
                    metadata.getAnnotationAttributes(ProxyBeanScan.class.getName()));


            AnnotationAttributes attributes2 = AnnotationAttributes.fromMap(
                    metadata.getAnnotationAttributes(ProxyBeanScans.class.getName()));

            if (attributes1 == null && attributes2 == null) {
                throw new IllegalArgumentException(getClass() + " only support [ProxyBeanScan or ProxyBeanScans]");
            }

            Consumer<AnnotationAttributes> consumer = attributes -> {

                if (attributes != null) {
                    String[] basePackages = attributes.getStringArray("basePackages");
                    Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");

                    Class<? extends ProxyFactoryBean> factoryBeanClass = attributes.getClass("factoryBeanClass");
                    Class<? extends Annotation> scanType = attributes.getClass("scanType");


                    addRegister(metadata, registry, basePackages, basePackageClasses, factoryBeanClass, scanType);
                }

            };

            //  consumer.accept(attributes1);

            Optional.ofNullable(attributes1).ifPresent(e -> consumer.accept(e));


            Optional.ofNullable(attributes2).ifPresent(e -> {
                for (AnnotationAttributes attributes : e.getAnnotationArray("value")) {
                    consumer.accept(attributes);
                }
            });


        }

        private void addRegister(AnnotationMetadata metadata, BeanDefinitionRegistry registry, String[] basePackages, Class<?>[] basePackageClasses, Class<? extends ProxyFactoryBean> factoryBeanClass, Class<? extends Annotation> scanType) {

            ScanPair scanPair = new ScanPair();

            Set<String> packagesToScan = scanPair.scanPackages;

            packagesToScan.addAll(Arrays.asList(basePackages));

            for (Class<?> basePackageClass : basePackageClasses) {
                packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
            }

            if (packagesToScan.isEmpty()) {

                String packageName = ClassUtils.getPackageName(metadata.getClassName());

                Assert.state(!StringUtils.isEmpty(packageName),
                        "@ProxyBeanScan cannot be used with the default package");

                packagesToScan.addAll(Collections.singleton(packageName));
            }


            scanPair.setScanType(scanType)
                    .setFactoryBeanClass(factoryBeanClass);


            register(registry, scanPair);
        }

    }


    @Data
    @Accessors(chain = true)
    public static class ScanPair {

        /**
         * 要扫描的注解
         *
         * @return
         */
        Class<? extends Annotation> scanType;

        /**
         * factoryBeanClass
         *
         * @return
         */
        Class<? extends ProxyFactoryBean> factoryBeanClass;

        /**
         *
         */
        final Set<String> scanPackages = new LinkedHashSet<>();

    }

}
