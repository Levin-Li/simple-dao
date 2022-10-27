package com.levin.commons.dao.uid.baidu;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Properties;

@Service(ModuleOption.PLUGIN_PREFIX + "HibernateIDGenerator")
public class HibernateUIDGenerator
        implements IdentifierGenerator, Configurable {

    @Resource
    UidGenerator uidGenerator;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {

    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return uidGenerator.getUID();
    }

    @Override
    public boolean supportsJdbcBatchInserts() {
        return true;
    }

}
