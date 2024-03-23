package com.levin.commons.dao;

import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * 用于获取属性名称
 *
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface PFunction<T, R> extends Function<T, R>, Supplier<String>, Serializable {

    Map<Class<?>, String> attrNameCache = new ConcurrentReferenceHashMap<>();

    default SerializedLambda getSerializedLambda() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<? extends PFunction> aClass = getClass();
        Method writeReplaceMethod = aClass.getDeclaredMethod("writeReplace");
        writeReplaceMethod.setAccessible(true);
        return (SerializedLambda) writeReplaceMethod.invoke(this);
    }

    default String get() {

        // 对类进行缓存
        return attrNameCache.computeIfAbsent(getClass(), cls -> {

            SerializedLambda serializedLambda = null;

            try {
                serializedLambda = getSerializedLambda();
            } catch (Exception e) {
                throw new IllegalArgumentException("illegal lambda", e);
            }

            String implMethodName = serializedLambda.getImplMethodName();

            return Stream.of("get", "is", "has", "can", "will")
                    //字母是大写
                    .filter(prefix -> implMethodName.length() > prefix.length() && Character.isUpperCase(implMethodName.charAt(prefix.length())))
                    .filter(implMethodName::startsWith)
                    .map(prefix -> Character.toUpperCase(implMethodName.charAt(prefix.length())) + implMethodName.substring(prefix.length()))
                    .findFirst()
                    .orElse(implMethodName);


        });
    }

}
