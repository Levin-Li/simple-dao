package com.levin.commons.dao.starter;

import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.util.JdbcUtils;
import com.levin.commons.conditional.ConditionalOn;
import com.levin.commons.conditional.ConditionalOnList;
import com.levin.commons.dao.JpaDao;
import com.levin.commons.dao.MiniDao;
import com.levin.commons.dao.PhysicalNamingStrategy;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.support.EntityNamingStrategy;
import com.levin.commons.dao.support.JpaDaoImpl;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.proxy.ProxyBeanScan;
import com.levin.commons.utils.MapUtils;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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
import javax.annotation.Resource;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.metamodel.Attribute;
import javax.sql.DataSource;
import java.lang.reflect.AccessibleObject;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration

@Role(BeanDefinition.ROLE_SUPPORT)

@ProxyBeanScan(scanType = EntityRepository.class
        , factoryBeanClass = RepositoryFactoryBean.class
        , basePackages = {"com.levin.commons.dao.repository"})

@EntityScan({"com.levin.commons.dao.domain.support"})

@Slf4j
public class JpaDaoConfiguration implements ApplicationContextAware {


//    @DynamicInsert和@DynamicUpdate

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;


    @PersistenceContext
    private EntityManager defaultEntityManager;

    @Resource
    private DataSource dataSource;

    @Resource
    private JpaProperties jpaProperties;

    @Resource
    DataSourceProperties dataSourceProperties;

    public static final String ENABLE_ALTER_TABLE_COMMENT = "enable_alter_table_comment";

    public static final String TABLE_NAME_PREFIX_MAPPINGS = "table_name_prefix_mappings";

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

        String nameMappings = jpaProperties.getProperties().get(TABLE_NAME_PREFIX_MAPPINGS);

        final MapUtils.Builder<String, String> builder = MapUtils.put();

        if (StringUtils.hasText(nameMappings)) {

            Arrays.stream(nameMappings.split(","))
                    .filter(StringUtils::hasText)
                    .map(it -> it.split("="))
                    .filter(it -> it.length == 2)
                    .filter(it -> StringUtils.hasText(it[0]) && StringUtils.hasText(it[1]))
                    .forEachOrdered(it -> builder.put(StringUtils.trimAllWhitespace(it[0]), StringUtils.trimAllWhitespace(it[1])));
        } else {
            log.info("*** you can config like [spring.jpa.properties.{} : com.xxx.base=xxx_base,com.xxx.biz=xxx_biz] to set table name prefix mapping.", TABLE_NAME_PREFIX_MAPPINGS);
        }

        EntityNamingStrategy.setPrefixMapping(builder.build());

        try {
            initTableComments();
        } catch (Exception e) {
            log.warn("update table comments error ", e);
        }

    }

    @SneakyThrows
    void initTableComments() {

        boolean alterTableComment = "true".equalsIgnoreCase(jpaProperties.getProperties().getOrDefault(ENABLE_ALTER_TABLE_COMMENT, "false"));

        if (!alterTableComment) {
            log.info("*** you can config [spring.jpa.properties.{} = true] to enable init table comments.", ENABLE_ALTER_TABLE_COMMENT);
            return;
        }

        final Map<String, String> columnDefinitions = new HashMap<>(loadTableColumnDefinitions());

        if (columnDefinitions.isEmpty()) {
            columnDefinitions.putAll(loadTableColumnDefinitionsByDefault());
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
                sql.append(String.format("alter table `%s` comment '%s';\n", tableName, schemaOnTable.description()));
            }

            entityType.getAttributes().forEach(attribute -> {


                Attribute.PersistentAttributeType type = attribute.getPersistentAttributeType();

                String comment = "";

                Schema schema = ((AccessibleObject) attribute.getJavaMember()).getAnnotation(Schema.class);

                if (!StringUtils.hasText(comment)
                        && schema != null && StringUtils.hasText(schema.description())) {
                    comment = schema.description();
                }

                Desc desc = ((AccessibleObject) attribute.getJavaMember()).getAnnotation(Desc.class);

                if (!StringUtils.hasText(comment)
                        && desc != null && StringUtils.hasText(desc.value())) {
                    comment = desc.value();
                }

                if (StringUtils.hasText(comment)
                        && Attribute.PersistentAttributeType.BASIC.equals(type)) {

                    String columnName = namingStrategy.toPhysicalColumnName(attribute.getName(), null);

                    sql.append(String.format("alter table `%s` modify column `%s` %s comment '%s';\n",
                            tableName, columnName,
                            columnDefinitions.getOrDefault((tableName + "." + columnName).toUpperCase(), ""),
                            comment));
                }

            });

            log.info("*** table and column comments <<< {} >>>", sql);

            if (sql.length() > 0 && jpaProperties.isGenerateDdl()) {
                log.info("*** alter table and column comments...");
                jdbcTemplate(dataSource).execute(sql.toString());
            }

        });

    }

    protected Map<String, String> loadTableColumnDefinitionsByDefault() throws SQLException {

        final Map<String, String> columnDefinitions = new ConcurrentHashMap<>();

        // String dbType = JdbcUtils.getDbType(dataSourceProperties.determineUrl(), dataSourceProperties.determineDriverClassName());

        Connection connection = dataSource.getConnection();

        try {

            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet tablesResultSet = metaData.getTables(connection.getCatalog(), connection.getSchema(), null, new String[]{"TABLE"});

            while (tablesResultSet.next()) {

                String tableName = tablesResultSet.getString("TABLE_NAME");

                ResultSet columnRS = metaData.getColumns(connection.getCatalog(), connection.getSchema(), tableName, null);

                ResultSetMetaData columnRSMetaData = columnRS.getMetaData();
                int n = columnRSMetaData.getColumnCount();

                Map<String, String> colNames = new HashMap<>();

                while (n-- >= 0) {
                    colNames.put(columnRSMetaData.getColumnName(n), columnRSMetaData.getColumnTypeName(n)
                            + ":" + columnRSMetaData.getScale(n) + ":" + columnRSMetaData.getPrecision(n)
                            + ":" + columnRSMetaData.getColumnLabel(n));

                }

                while (columnRS.next()) {

                    log.debug(" " + columnRS.toString());

                }

            }

        } catch (Exception e) {
            log.warn(" Can't load table column definitions " + e.getMessage());
        } finally {
            connection.close();
        }

        return columnDefinitions;

    }

    protected Map<String, String> loadTableColumnDefinitions() throws SQLException {

        final Map<String, String> columnDefinitions = new ConcurrentHashMap<>();

        String dbType = JdbcUtils.getDbType(dataSourceProperties.determineUrl(), dataSourceProperties.determineDriverClassName());

        if (!StringUtils.hasText(dbType)) {
            log.warn("can't recognition db type by {} {}", dataSourceProperties.determineUrl(), dataSourceProperties.determineDriverClassName());
            return columnDefinitions;
        }

        Connection connection = dataSource.getConnection();

        try {

            DatabaseMetaData metaData = connection.getMetaData();

            log.info("*** {} DB  *** Catalog:{} Schema:{} Version:{}.{} userName:{} determineUrl:{} :determineDriverClassName{}", dbType, connection.getCatalog(), connection.getSchema()
                    , metaData.getJDBCMajorVersion(), metaData.getJDBCMinorVersion(), metaData.getUserName()
                    , dataSourceProperties.determineUrl(), dataSourceProperties.determineDriverClassName());

            ResultSet tablesResultSet = metaData.getTables(connection.getCatalog(), connection.getSchema(), null, new String[]{"TABLE"});

            while (tablesResultSet.next()) {

                String tableName = tablesResultSet.getString("TABLE_NAME");

                String tableSql = tablesResultSet.getString("SQL");

                log.debug("*** table {} --> <<< {} >>>", tableName, tableSql);

                tableSql = tableSql.replace("CREATE CACHED TABLE ", "CREATE TABLE ");

                try {

                    SQLCreateTableStatement tableStatement = SQLParserUtils.createSQLStatementParser(tableSql, dbType, true)
                            .getSQLCreateTableParser().parseCreateTable();

                    tableStatement.getTableElementList().stream()
                            .filter(sqlTableElement -> sqlTableElement instanceof SQLColumnDefinition)
                            .map(sqlTableElement -> (SQLColumnDefinition) sqlTableElement)
                            .forEach(sqlTableElement -> {
                                String columnName = sqlTableElement.getColumnName().replace("\"", "");
                                sqlTableElement.setName("");
                                String columnDefinition = sqlTableElement.toString();
                                columnDefinitions.put((tableName + "." + columnName).toUpperCase(), columnDefinition);
                            });

                } catch (Exception e) {
                    log.warn("parse sql error, sql:" + tableSql, e);
                }

            }
        } catch (Exception e) {
            log.warn(" Can't load table column definitions " + e.getMessage());
        } finally {
            connection.close();
        }

        return columnDefinitions;
    }

}
