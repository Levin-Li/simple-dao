package com.levin.commons.dao;

import org.springframework.util.ClassUtils;
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
 * @author lilw
 */
@FunctionalInterface
public interface LambdaMethodAttr<T, R> extends Function<T, R>, Supplier<String>, Serializable {

    class Attr implements Serializable {

        private final SerializedLambda serializedLambda;

        private Class<?> attrClass;

        private String attrName;

        public Attr(SerializedLambda serializedLambda) {
            this.serializedLambda = serializedLambda;
            assert serializedLambda != null;
        }

        public String getAttrName() {

            if (attrName == null) {

                attrName = serializedLambda.getImplMethodName();

                attrName = Stream.of("get", "set", "is", "has", "can", "will")
                        //字母是大写
                        .filter(prefix -> attrName.length() > prefix.length() && Character.isUpperCase(attrName.charAt(prefix.length())))
                        .filter(attrName::startsWith)
                        .map(prefix -> Character.toLowerCase(attrName.charAt(prefix.length())) + attrName.substring(prefix.length() + 1))
                        .findFirst()
                        .orElse(attrName);
            }

            return attrName;
        }

        public Class<?> getAttrClass() {
            if (attrClass == null) {

                //(Lcom/levin/commons/dao/domain/support/TestEntity;)Ljava/lang/Object;

                String mSign = serializedLambda.getInstantiatedMethodType();

                if (mSign != null && mSign.startsWith("(L") && mSign.contains(";)")) {
                    mSign = mSign.substring(2, mSign.indexOf(";)"));
                } else {
                    mSign = serializedLambda.getImplClass();
                }

                attrClass = ClassUtils.resolveClassName(mSign.replace('/', '.'), null);
            }
            return attrClass;
        }
    }

    /**
     * 软引用缓存
     */
    Map<Class<?>, Attr> attrNameCache = new ConcurrentReferenceHashMap<>();

    default SerializedLambda getSerializedLambda() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<? extends LambdaMethodAttr> aClass = getClass();
        Method writeReplaceMethod = aClass.getDeclaredMethod("writeReplace");
        writeReplaceMethod.setAccessible(true);
        return (SerializedLambda) writeReplaceMethod.invoke(this);
    }


    default String get() {
        return getAttrName();
    }

    default String getAttrName() {
        return getAttr().getAttrName();
    }

    default Class<?> getAttrClass() {
        return getAttr().getAttrClass();
    }

    default Attr getAttr() {
        // 对类进行缓存
        return attrNameCache.computeIfAbsent(getClass(), cls -> {
            try {
                return new Attr(getSerializedLambda());
            } catch (Exception e) {
                throw new IllegalArgumentException("illegal lambda", e);
            }
        });
    }

}
