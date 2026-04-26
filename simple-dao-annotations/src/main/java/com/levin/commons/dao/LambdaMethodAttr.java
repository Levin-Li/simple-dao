package com.levin.commons.dao;

import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.beans.Introspector;
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
                attrName = resolveAttrName(serializedLambda);
            }

            return attrName;
        }

        private String resolveAttrName(SerializedLambda lambda) {

            String attrName = methodToAttrName(lambda.getImplMethodName());

            if (attrName != null) {
                return attrName;
            }

            for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
                SerializedLambda nestedLambda = tryExtractSerializedLambda(lambda.getCapturedArg(i));

                if (nestedLambda != null) {
                    String nestedAttrName = resolveAttrName(nestedLambda);

                    if (nestedAttrName != null) {
                        return nestedAttrName;
                    }
                }
            }

            return lambda.getImplMethodName();
        }

        private String methodToAttrName(String methodName) {
            return Stream.of("get", "set", "is", "has", "can", "will")
                    //字母是大写
                    .filter(prefix -> methodName.length() > prefix.length() && Character.isUpperCase(methodName.charAt(prefix.length())))
                    .filter(methodName::startsWith)
                    .map(prefix -> Introspector.decapitalize(methodName.substring(prefix.length())))
                    .findFirst()
                    .orElse(null);
        }

        private SerializedLambda tryExtractSerializedLambda(Object target) {

            if (!(target instanceof Serializable serializable)) {
                return null;
            }

            try {
                Method writeReplaceMethod = serializable.getClass().getDeclaredMethod("writeReplace");
                writeReplaceMethod.setAccessible(true);
                Object result = writeReplaceMethod.invoke(serializable);
                return (result instanceof SerializedLambda) ? (SerializedLambda) result : null;
            } catch (Exception e) {
                return null;
            }
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
