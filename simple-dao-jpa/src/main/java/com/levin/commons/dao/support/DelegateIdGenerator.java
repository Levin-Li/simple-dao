package com.levin.commons.dao.support;

import com.levin.commons.service.support.SpringContextHolder;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

/**
 * 代理的ID生成器
 */
public class DelegateIdGenerator implements IdentifierGenerator, Configurable {

    IdentifierGenerator identifierGenerator;

    transient Properties params;
    transient Type type;
    transient ServiceRegistry serviceRegistry;

    private IdentifierGenerator getIdentifierGenerator() {

        if (this.identifierGenerator == null) {

            this.identifierGenerator = SpringContextHolder.getBeanFactory()
                    .getBeanProvider(IdentifierGenerator.class)
                    .getIfAvailable();
        }

        if (this.identifierGenerator == null) {
            //配置
            identifierGenerator = configure(new UUIDGenerator());
        }

        return identifierGenerator;
    }

    public <T extends IdentifierGenerator> T configure(T generator) {

        if (generator instanceof Configurable) {
            ((Configurable) generator).configure(type, params, serviceRegistry);
        }

        return generator;
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
            } else if (Long.class == returnedClass) {
                return Long.parseLong(id.toString());
            } else if (Integer.class == returnedClass) {
                return Integer.parseInt(id.toString());
            }
        }

        return id;
    }

}