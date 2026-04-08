package com.levin.commons.dao.uid.baidu;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManagerFactory;

import java.io.Serializable;
import java.util.Properties;

//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Service(ModuleOption.PLUGIN_PREFIX + "HibernateIDGenerator")
@Slf4j
public class HibernateUIDGenerator
        implements IdentifierGenerator {

    @Autowired
    UidGenerator uidGenerator;

    GeneratorCreationContext creationContext;
    Properties parameters;

    public void configure(GeneratorCreationContext creationContext, Properties parameters) throws MappingException {
        this.creationContext = creationContext;
        this.parameters = parameters;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return uidGenerator.getUID();
    }

}
