package com.levin.commons.dao.starter;

import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.utils.MapUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.Map;

//@Order(Ordered.LOWEST_PRECEDENCE)
public class SimpleCondition
        implements ConfigurationCondition {

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {


        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOn.class.getName());

        ConditionalOn.Type type = (ConditionalOn.Type) attributes.get("type");
        String value = (String) attributes.get("value");

        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("" + metadata);
        }

        boolean result = false;

        ClassLoader classLoader = context.getClassLoader();
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        if (ConditionalOn.Type.OnBean.equals(type)) {

            result = !beanFactory.getBeansOfType(loadClass(classLoader, value)).isEmpty();

        } else if (ConditionalOn.Type.OnMissingBean.equals(type)) {

            result = beanFactory.getBeansOfType(loadClass(classLoader, value)).isEmpty();


        } else if (ConditionalOn.Type.OnClass.equals(type)) {

            result = loadClass(classLoader, value) != null;

        } else if (ConditionalOn.Type.OnMissingClass.equals(type)) {

            result = loadClass(classLoader, value) == null;

        } else if (ConditionalOn.Type.OnProperty.equals(type)) {

            result = StringUtils.hasText(context.getEnvironment().getProperty(value));

        } else if (ConditionalOn.Type.OnExpr.equals(type)) {

            result = ObjectUtil.evalSpEL(attributes, value
                    , MapUtils.put("env", (Object) context.getEnvironment())
                            .put("beanFactory", context.getBeanFactory()).build());
        }


        return result;
    }


    private Class loadClass(ClassLoader classLoader, String value) {
        try {
            return classLoader.loadClass(value);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
