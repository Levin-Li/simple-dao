package com.levin.commons.dao.proxy;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;

@Slf4j
@Data
public abstract class ProxyFactoryBean<T>
        implements FactoryBean<T>, ApplicationContextAware, InvocationHandler, MethodInterceptor {


    Class<T> actualType;


    boolean isSingleton = true;


    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public T getObject() throws Exception {

        if (actualType.isInterface()) {
            return (T) java.lang.reflect.Proxy.newProxyInstance(actualType.getClassLoader(),
                    new Class[]{actualType}, this);
        } else {

            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(actualType);
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setCallback(this);

            return (T) autowireBean(enhancer.create());
        }

    }

    protected Object autowireBean(final Object proxyBean) {

//        ReflectionUtils.doWithFields(actualType, new ReflectionUtils.FieldCallback() {
//            @Override
//            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
//
//                Class<?> type = ResolvableType.forField(field, actualType).resolve(field.getType());
//
//                if (type == actualType || type.isAssignableFrom(actualType)) {
//                    try {
//                        field.setAccessible(true);
//                        if (field.get(proxyBean) == null) {
//                            ReflectionUtils.setField(field, proxyBean, applicationContext.getBean(type));
//                        }
//                    } catch (Exception e) {
//                        log.warn(actualType + "实例，自动注入属性" + field.getName() + "代理对象时错误", e);
//                    }
//                }
//
//            }
//        });

        try {
            applicationContext.getAutowireCapableBeanFactory().autowireBean(proxyBean);
        } catch (Exception e) {
            log.warn(actualType + "实例，自动装配错误", e);
        }

        return proxyBean;
    }

    @Override
    public Class<T> getObjectType() {
        return actualType;
    }

    @Override
    public boolean isSingleton() {
        return this.isSingleton;
    }
}
