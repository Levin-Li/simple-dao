package com.levin.commons.dao.uid.baidu;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Properties;

//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Service(ModuleOption.PLUGIN_PREFIX + "HibernateIDGenerator")
@Slf4j
public class HibernateUIDGenerator
        implements IdentifierGenerator {

    @Autowired
    UidGenerator uidGenerator;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        log.info("HibernateUIDGenerator configure.");
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return uidGenerator.getUID();
    }

}
