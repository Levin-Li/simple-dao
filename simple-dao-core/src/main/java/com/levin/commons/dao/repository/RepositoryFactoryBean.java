package com.levin.commons.dao.repository;

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
import com.levin.commons.service.proxy.ProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

/**
 * FactoryBean
 */
public class RepositoryFactoryBean<T>
        extends ProxyFactoryBean<T> {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryFactoryBean.class);

    private static String PROMPT = "无操作注解，非接口类需要在方法上显式声明注解，如：@QueryRequest";


    @Autowired
    private MiniDao jpaDao;


    @PostConstruct
    public void afterPropertiesSet() throws Exception {

        if (jpaDao == null)
            throw new IllegalStateException("jpaDao is null");

        if (parameterNameDiscoverer == null)
            parameterNameDiscoverer = new MethodParameterNameDiscoverer();

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


        if(method.getDeclaringClass() == Object.class){
            ;
        }

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

            ResolvableType resolvableType = ResolvableType.forType(method.getGenericReturnType(), ResolvableType.forType(getActualType()));

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

}