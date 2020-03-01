package com.levin.commons.dao.repository.support;

import com.levin.commons.dao.Converter;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.repository.annotation.DeleteRequest;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.repository.annotation.QueryRequest;
import com.levin.commons.dao.repository.annotation.UpdateRequest;
import com.levin.commons.dao.support.DeleteDaoImpl;
import com.levin.commons.dao.support.MethodParameterNameDiscoverer;
import com.levin.commons.dao.support.SelectDaoImpl;
import com.levin.commons.dao.support.UpdateDaoImpl;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

/**
 * FactoryBean
 */
public class RepositoryFactoryBean<T>
        implements InitializingBean, ApplicationContextAware, FactoryBean<T>, InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryFactoryBean.class);

    private static String PROMPT = "无操作注解，非接口类需要在方法上显式声明注解，如：@QueryRequest";

    private static final Object RESULT_TAKED = new Object();

    @Autowired
    private MiniDao jpaDao;

    @Autowired(required = false)
    private ParameterNameDiscoverer parameterNameDiscoverer;

    private Class<T> serviceType;

    private ApplicationContext applicationContext;

    //用于传输方法执行结果
    private static final ThreadLocal threadContext = new ThreadLocal<>();

    public void setServiceType(Class<T> serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (jpaDao == null)
            throw new IllegalStateException("jpaDao is null");

        if (parameterNameDiscoverer == null)
            parameterNameDiscoverer = new MethodParameterNameDiscoverer();

    }

    public ParameterNameDiscoverer getParameterNameDiscoverer() {
        return parameterNameDiscoverer;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }


    /**
     * 如果代理对象发生异常，这个方法将返回异常对象，否则返回正常的方法返回对象
     * <p/>
     * 可能获得的结果：
     * 1、正常返回
     * 2、方法执行异常，返回异常实体
     * 3、方法不需要执行代理，返回PROXY_NOOP
     *
     * @return
     */
    public static <T> T getProxyInvokeResult() {

        Object result = threadContext.get();

        threadContext.set(RESULT_TAKED);

        if (result == null)
            return null;

        if (result == RESULT_TAKED)
            throw new IllegalStateException("执行结果已经被取走");

        if (result instanceof InvokeExceptionDesc)
            throw new ProxyMethodInvokeException(((InvokeExceptionDesc) result).ex);

        if (result instanceof NOOP)
            throw new ProxyMethodInvokeException(((NOOP) result).info);

        return (T) result;

    }

    protected Annotation findOpAnnotation(Object methodOrClass, Annotation[] annotations) {

        List<Annotation> annotationsList = QueryAnnotationUtil.getAnnotationsByPackage(QueryRequest.class.getPackage().getName()
                , annotations, EntityRepository.class, TargetOption.class);

        if (annotationsList.size() > 1)
            throw new IllegalStateException(methodOrClass + ",在同一个方法或类上不能同时定义多个操作的注解:" + annotations);

        if (annotationsList.size() > 0)
            return annotationsList.get(0);

        return null;
    }


    static Converter findFirstConverter(Object[] args) {

        if (args != null)
            for (Object arg : args) {
                if (arg instanceof Converter)
                    return (Converter) arg;
            }

        return null;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //获取方法上面的注解
        Annotation opAnnotation = findOpAnnotation(method, method.getAnnotations());

        boolean isInterface = method.getDeclaringClass().isInterface();

        //如果代理的不是接口，方法次是抽象，且没有注解，则忽略这个方法的执行，直接返回noop
        if (!isInterface
                && !Modifier.isAbstract(method.getModifiers())
                && opAnnotation == null) {
            return new NOOP(proxy, method, args, PROMPT);
        }

        //如果方法上没有注解，则获以类上面的注解
        if (opAnnotation == null)
            opAnnotation = findOpAnnotation(method.getDeclaringClass(), method.getDeclaringClass().getAnnotations());

        // DaoContext.setThreadVar("p",);

        if (opAnnotation instanceof QueryRequest || opAnnotation == null) {

//        if (opAnnotation instanceof QueryRequest) {

            final QueryRequest queryRequest = (QueryRequest) opAnnotation;

            SelectDaoImpl<Object> selectDao = new SelectDaoImpl<>(jpaDao, false);

            selectDao.setParameterNameDiscoverer(getParameterNameDiscoverer());

            if (queryRequest != null) {
                selectDao.appendWhere(queryRequest.fixedCondition());
            }

            selectDao.appendByMethodParams(proxy, method, args);

            if (queryRequest != null) {
                selectDao.setQueryRequest(queryRequest);
            }

            ResolvableType resolvableType = ResolvableType.forType(method.getGenericReturnType(), ResolvableType.forType(serviceType));

            Class<?> returnType = resolvableType.resolve(method.getReturnType());

            // ResolvableType.forMethodReturnType(method,serviceType).resolve(method.getReturnType());

            Converter converter = findFirstConverter(args);

            //如果是集合对象
            boolean isCollection = Collection.class.isAssignableFrom(returnType);

            if (isCollection)
                returnType = resolvableType.resolveGeneric(0);

            //以注解的优先
            if (queryRequest != null) {
                if (queryRequest.resultClass() != null
                        && queryRequest.resultClass() != Void.class) {
                    returnType = queryRequest.resultClass();
                }

                if (converter == null
                        && queryRequest.resultConverterClass() != null
                        && queryRequest.resultConverterClass() != Void.class) {
                    converter = (Converter) queryRequest.resultConverterClass().newInstance();
                }
            }

            if (returnType == null)
                returnType = Object.class;

            //集合
            if (isCollection) {
                return converter != null ? selectDao.find(converter) : selectDao.find(returnType);
            } else {
                return converter != null ? selectDao.findOne(converter) : selectDao.findOne(returnType);
            }

        } else if (opAnnotation instanceof UpdateRequest) {

            UpdateDaoImpl<Object> updateDao = new UpdateDaoImpl<>(false, jpaDao);

            return updateDao
                    .setParameterNameDiscoverer(getParameterNameDiscoverer())
                    .appendWhere(((UpdateRequest) opAnnotation).fixedCondition())
                    .appendColumns(((UpdateRequest) opAnnotation).updateStatement())
                    .appendByMethodParams(proxy, method, args)
                    .update();

        } else if (opAnnotation instanceof DeleteRequest) {

            DeleteDaoImpl<Object> deleteDao = new DeleteDaoImpl<>(false, jpaDao);

            return deleteDao
                    .setParameterNameDiscoverer(getParameterNameDiscoverer())
                    .appendWhere(((DeleteRequest) opAnnotation).fixedCondition())
                    .appendByMethodParams(proxy, method, args)
                    .delete();

        } else {
            throw new RuntimeException("unknown operation annotation : " + opAnnotation);
        }

    }


    protected Object autoInjectProxy(final Object proxyBean) {

        ReflectionUtils.doWithFields(serviceType, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                Class<?> type = ResolvableType.forField(field, serviceType).resolve(field.getType());

                if (type == serviceType || type.isAssignableFrom(serviceType)) {
                    try {
                        field.setAccessible(true);
                        if (field.get(proxyBean) == null) {
                            ReflectionUtils.setField(field, proxyBean, applicationContext.getBean(type));
                        }
                    } catch (Exception e) {
                        logger.warn(serviceType + "实例，自动注入属性" + field.getName() + "代理对象时错误", e);
                    }
                }

            }
        });

        try {
            applicationContext.getAutowireCapableBeanFactory().autowireBean(proxyBean);
        } catch (Exception e) {
            logger.warn(serviceType + "实例，自动注入属性代理对象时错误", e);
        }

        return proxyBean;
    }


    @Override
    public T getObject() throws Exception {

        if (serviceType.isInterface()) {
            return (T) java.lang.reflect.Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[]{serviceType}, this);
        } else {

            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(serviceType);
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setCallback(methodInterceptor);

            return (T) autoInjectProxy(enhancer.create());
        }
    }

    private static class InvokeDesc {

        public final Object invokeTarget;
        public final Method method;
        public final Object[] args;

        private InvokeDesc(Object invokeTarget, Method method, Object[] args) {
            this.invokeTarget = invokeTarget;
            this.method = method;
            this.args = args;
        }

        @Override
        public String toString() {
            return method.toString();
        }
    }

    public static class NOOP extends InvokeDesc {

        public final String info;

        private NOOP(Object invokeTarget, Method method, Object[] args, String info) {
            super(invokeTarget, method, args);
            this.info = info;
        }

        @Override
        public String toString() {
            return info + ":" + super.toString();
        }
    }

    public static class InvokeExceptionDesc extends InvokeDesc {

        public final MethodProxy methodProxy;
        public final Throwable ex;

        private InvokeExceptionDesc(Object invokeTarget, Method method, Object[] args, MethodProxy methodProxy, Throwable ex) {
            super(invokeTarget, method, args);
            this.methodProxy = methodProxy;
            this.ex = ex;
        }

        @Override
        public String toString() {
            return ex.getMessage() + ":" + super.toString();
        }
    }

    private final MethodInterceptor methodInterceptor = new MethodInterceptor() {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

            //如果是抽象方法
            boolean anAbstract = Modifier.isAbstract(method.getModifiers());

            try {
                Object returnValue = invoke(obj, method, args);

                if (anAbstract)
                    return returnValue;
                else
                    threadContext.set(returnValue);

            } catch (Throwable e) {
                if (anAbstract)
                    throw new ProxyMethodInvokeException(method.toGenericString(), e);
                else
                    threadContext.set(new InvokeExceptionDesc(obj, method, args, methodProxy, e));
            }

            //继续执行父类的方法
            return methodProxy.invokeSuper(obj, args);
        }
    };
}