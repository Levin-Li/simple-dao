package com.levin.commons.dao.starter;

import com.levin.commons.conditional.ConditionalOn;
import com.levin.commons.conditional.ConditionalOnList;
import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.support.JpaDaoImpl;
import com.levin.commons.service.proxy.ProxyBeanScan;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.metamodel.Attribute;
import javax.sql.DataSource;
import java.lang.reflect.AccessibleObject;

@Configuration

@Role(BeanDefinition.ROLE_SUPPORT)

@ProxyBeanScan(scanType = EntityRepository.class
        , factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com.levin.commons.dao.repository"})

@EntityScan({"com.levin.commons.dao.domain.support"})

@Slf4j
public class JpaDaoConfiguration implements ApplicationContextAware {

//    @DynamicInsert和@DynamicUpdate

/*    @Bean
    @ConditionalOnList({
            @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = FormattingConversionService.class),
    })
    FormattingConversionServiceFactoryBean formattingConversionServiceFactoryBean() {
        return new FormattingConversionServiceFactoryBean();
    }*/

    //    @Autowired
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    //    @Autowired
    @PersistenceContext
    private EntityManager defaultEntityManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JpaProperties jpaProperties;

    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = JdbcTemplate.class)
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = SimpleJdbcInsert.class)
    SimpleJdbcInsert simpleJdbcInsertOperations(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate);
    }

    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = SimpleJdbcCall.class)
    SimpleJdbcCall simpleJdbcCallOperations(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate);
    }


    /**
     * 因为在注册期 JpaDao bean 已经被引用，所以事务注解不会尝试重试初始化 JpaDao bean
     *
     *
     * <p>
     * 这将导致事务无效，使用代理FactoryBean的方式，可以让@Transactional事务自己加在已有代理对象上面
     *
     * @return
     */
    @Bean("com.levin.commons.dao.JpaDao")
    @ConditionalOnList({
            @ConditionalOn(action = ConditionalOn.Action.OnClass, types = {Eq.class, MiniDao.class, JpaDao.class, JpaDaoImpl.class}),
            @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = JpaDao.class),
    })
    JpaDao newJpaDao() {

        //加入默认的时间格式
//        DaoContext.setGlobalVar("dateFormat", "YYYYMMDD");
//        DaoContext.setGlobalVar("DF_YEAR", "YYYY");
//        DaoContext.setGlobalVar("DF_YYYYMMDD", "YYYYMMDD");

        return new JpaDaoImpl();
    }

    @Bean
    @ConditionalOn(action = ConditionalOn.Action.OnMissingBean, types = JPQLQueryFactory.class)
    JPQLQueryFactory newJPQLQueryFactory() {
        if (defaultEntityManager != null) {
            return new JPAQueryFactory(defaultEntityManager);
        } else {
            return new JPAQueryFactory(new Provider<EntityManager>() {
                @Override
                public EntityManager get() {
                    return entityManagerFactory.createEntityManager();
                }
            });
        }
    }


    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }


    @SneakyThrows
    @PostConstruct
    void init() {

        try {
            initTableComments();
        } catch (Exception e) {
            log.warn("update table comments error " , e);
        }

    }

    @SneakyThrows
    void initTableComments() {

        if (!jpaProperties.isGenerateDdl()) {
            return;
        }

        JpaDao simpleDao = newJpaDao();

        PhysicalNamingStrategy namingStrategy = simpleDao.getNamingStrategy();

        StringBuilder sql = new StringBuilder();

        entityManagerFactory.getMetamodel().getEntities().forEach(entityType -> {


            Class<?> entityClass = entityType.getJavaType();

            Schema schemaOnTable = entityClass.getAnnotation(Schema.class);

            String tableName = simpleDao.getTableName(entityClass);

            if (schemaOnTable != null
                    && StringUtils.hasText(schemaOnTable.description())) {
                sql.append(String.format("alter table `%s` comment '%s';\n" , tableName, schemaOnTable.description()));
            }

            entityType.getAttributes().forEach(attribute -> {

                Schema schema = ((AccessibleObject) attribute.getJavaMember()).getAnnotation(Schema.class);

                Attribute.PersistentAttributeType type = attribute.getPersistentAttributeType();

                if (schema != null
                        && StringUtils.hasText(schema.description())
                        && Attribute.PersistentAttributeType.BASIC.equals(type)) {

                    String columnName = namingStrategy.toPhysicalColumnName(attribute.getName(), null);

                    sql.append(String.format("alter table `%s` modify column `%s` comment '%s';\n" , tableName, columnName, schema.description()));

                }

            });

            if (sql.length() > 0) {
                log.info("*** update table and column comments <<< {} >>>" , sql);
                // jdbcTemplate(dataSource).execute(sql.toString());
            }

        });

    }

}
