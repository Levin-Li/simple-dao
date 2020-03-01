package com.levin.commons.dao.starter;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//@Order(Ordered.LOWEST_PRECEDENCE)
public class SimpleCondition
        implements ConfigurationCondition {

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {


        AnnotationAttributes attributes1 = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(ConditionalOn.class.getName(), true));
        AnnotationAttributes attributes2 = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(ConditionalOnList.class.getName(), true));

//        List<AnnotationAttributes> attributesList = new LinkedList<>();


        //一个注解未匹配，直接返回
        if (attributes1 != null && !matchOne(context, attributes1)) {
            return false;
        }

        if (attributes2 == null) {
            return false;
        }

        boolean requireAllMatch = attributes2.getBoolean("requireAllMatch");
        AnnotationAttributes[] values = attributes2.getAnnotationArray("value");

        int matchedCnt = 0;

        for (AnnotationAttributes value : values) {

            boolean matchOne = matchOne(context, value);

            if (matchOne) {
                matchedCnt++;
            }

            //如果要求匹配所有，发现一个不匹配就返回匹配不成功
            if (requireAllMatch && !matchOne) {
                return false;
            }

            //如果匹配成功，并不要求匹配所有，直接返回
            if (matchOne && !requireAllMatch) {
                return true;
            }
        }

        return true;

        // return matchedCnt == values.length;
    }

    private boolean matchOne(ConditionContext context, AnnotationAttributes attributes) {

        ConditionalOn.Action action = (ConditionalOn.Action) attributes.get("action");

        String[] values = attributes.getStringArray("value");

        String[] types = attributes.getStringArray("types");

        final boolean requireAllMatch = attributes.getBoolean("requireAllMatch");


        ClassLoader classLoader = context.getClassLoader();
        Environment env = context.getEnvironment();

        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        List<String> list = new LinkedList<>();

        list.addAll(Arrays.asList(values));

        list.addAll(Arrays.asList(types));


        boolean result = false;

        if (ConditionalOn.Action.OnBean.equals(action)) {

            result = isMatched(requireAllMatch, list,
                    p -> beanFactory.getBeansOfType(loadClass(classLoader, p)).size() > 0);

        } else if (ConditionalOn.Action.OnMissingBean.equals(action)) {

            result = isMatched(requireAllMatch, list,
                    p -> beanFactory.getBeansOfType(loadClass(classLoader, p)).isEmpty());


        } else if (ConditionalOn.Action.OnClass.equals(action)) {

            result = isMatched(requireAllMatch, list,
                    p -> loadClass(classLoader, p) != null);


        } else if (ConditionalOn.Action.OnMissingClass.equals(action)) {

            result = isMatched(requireAllMatch, list,
                    p -> loadClass(classLoader, p) == null);


        } else if (ConditionalOn.Action.OnProperty.equals(action)) {
            result = isMatched(requireAllMatch, list,
                    p -> StringUtils.hasText(env.resolvePlaceholders(env.getProperty(p))));

        }


        return result;
    }

    /**
     * @param requireAllMatch 所有条件匹配才是成功
     * @param values
     * @param predicate
     * @return
     */
    private boolean isMatched(boolean requireAllMatch, List<String> values, Predicate<String> predicate) {

        values = values.stream()
                .filter(StringUtils::hasText).collect(Collectors.toList());

        long cnt = values.stream().filter(predicate).count();

//        return requireAllMatch ? (values.size() > 0 && cnt == values.size()) : (cnt > 0);

        //重要逻辑，允许没有要匹配的选项时，也返回匹配成功

        return requireAllMatch ? (cnt == values.size()) : (cnt > 0);

    }


    private Class loadClass(ClassLoader classLoader, String value) {
        try {
            return classLoader.loadClass(value);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
