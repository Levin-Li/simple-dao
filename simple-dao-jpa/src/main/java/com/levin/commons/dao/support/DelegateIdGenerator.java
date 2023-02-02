package com.levin.commons.dao.support;

import com.levin.commons.service.support.SpringContextHolder;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

/**
 * 代理的ID生成器
 */
public class DelegateIdGenerator implements IdentifierGenerator {

    IdentifierGenerator identifierGenerator;

    transient Properties params;
    transient Type type;
    transient ServiceRegistry serviceRegistry;

    static boolean isLoaded = false;
    static IdentifierGenerator ctxIdentifierGenerator;

    private IdentifierGenerator getIdentifierGenerator() {

        //立刻返回
        if (this.identifierGenerator != null) {
            return this.identifierGenerator;
        }

        //如果没加载过，只会加载一次
        if (!isLoaded && ctxIdentifierGenerator == null) {

            ctxIdentifierGenerator = SpringContextHolder.getBeanFactory()
                    .getBeanProvider(IdentifierGenerator.class)
                    .getIfAvailable();

            isLoaded = true;

            if (ctxIdentifierGenerator != null) {
                //初始一次
                ctxIdentifierGenerator.configure(type, params, serviceRegistry);
            }
        }

        this.identifierGenerator = ctxIdentifierGenerator;

        if (this.identifierGenerator == null) {
            //使用默认的生成器
            this.identifierGenerator = new UUIDGenerator();
            this.identifierGenerator.configure(type, params, serviceRegistry);
        }

        return identifierGenerator;
    }


    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        this.params = new Properties();
        this.params.putAll(params);
        this.type = type;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {

        Serializable id = getIdentifierGenerator().generate(session, object);

        Class returnedClass = type.getReturnedClass();

        if (!returnedClass.isInstance(id)) {

            if (String.class == returnedClass) {
                return id.toString();
            } else if (Integer.class == returnedClass) {
                return Integer.parseInt(id.toString());
            } else if (Long.class == returnedClass) {
                return Long.parseLong(id.toString());
            }

        }

        return id;
    }

}
