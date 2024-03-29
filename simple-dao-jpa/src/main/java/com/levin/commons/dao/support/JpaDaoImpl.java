package com.levin.commons.dao.support;


import com.levin.commons.dao.*;
import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.dao.domain.OrganizedObject;
import com.levin.commons.dao.domain.support.TestEntity;
import com.levin.commons.dao.util.ExceptionUtils;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.dao.util.QLUtils;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.service.support.Locker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.hibernate.boot.model.naming.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.*;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import javax.persistence.Parameter;
import javax.persistence.metamodel.EntityType;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.levin.commons.dao.util.QueryAnnotationUtil.*;
import static com.levin.commons.dao.util.QueryAnnotationUtil.expandAndFilterNull;


/**
 * JPA实体状态
 * 1. new/transient
 * 新建的对象，或者是说这个对象没有加入到持久化上下文当中。
 * 2 managed
 * 对象存在持久化上下文中
 * 3 detach
 * 对象脱离了持久化上下文
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * 1.persist(Object entity)方法
 * 这个方法把一个实体加入持久化上下文中，也就是缓存中，在事务提交或者调用flush()方法的时候，把这个实体保存到数据库中（执行insert语句），如果实体已存在，则抛出EntityExistsException异常，缓存则不存在了。
 * <p/>
 * 2.find(Class<T> entityClass,Object primaryObject)
 * 根据主键从数据库中查询一个实体，这个方法首先从缓存中去查找，如果找不到，就从数据库中去找，并把它加入到缓存中。
 * 3.merge(T object)
 * 把一个对象加入到当前的持久化上下文中，就是把一个对象从detach转变为managed，并返回这个对象。当一个对象设置了主键，并调用此方法，就会从数据库中根据主键查找到该对象把它放到持久化上下文中，当事物提交的时候，如果对象发生了改变，更新该对象的改变到数据库中，如果对象没有改变，则什么也不做，如果对象没有设置主键，则插入该对象到数据库中。
 * 4.remove(Object entity)
 * 根据主键从数据库中删除一个对象，这个对象的状态必须是managed，否则会抛出IllegalArgumentException,在一个事务中删除一个对象,假如实体管理器对象为em,
 * em.remove(em.merge(entity)),如果直接调用em.remove(entity);会抛出异常
 * IllegalArgumentException。
 * <p/>
 * <p/>
 * public void persist(Object entity)
 * persist 方法可以将实例转换为 managed( 托管 ) 状态。在调用 flush() 方法或提交事物后，实例将会被插入到数据库中。
 * <p/>
 * <p/>
 * <p/>
 * 对不同状态下的实例 A ， persist 会产生以下操作 :
 * <p/>
 * 1.       如果 A 是一个 new 状态的实体，它将会转为 managed 状态；
 * <p/>
 * 2.       如果 A 是一个 managed 状态的实体，它的状态不会发生任何改变。但是系统仍会在数据库执行 INSERT 操作；
 * <p/>
 * 3.       如果 A 是一个 removed( 删除 ) 状态的实体，它将会转换为受控状态；
 * <p/>
 * 4.       如果 A 是一个 detached( 分离 ) 状态的实体，该方法会抛出 IllegalArgumentException 异常，具体异常根据不同的 JPA 实现有关。
 * <p/>
 * public void merge(Object entity)
 * merge 方法的主要作用是将用户对一个 detached 状态实体的修改进行归档，归档后将产生一个新的 managed 状态对象。
 * <p/>
 * <p/>
 * <p/>
 * 对不同状态下的实例 A ， merge 会产生以下操作 :
 * <p/>
 * 1.       如果 A 是一个 detached 状态的实体，该方法会将 A 的修改提交到数据库，并返回一个新的 managed 状态的实例 A2 ；
 * <p/>
 * 2.       如果 A 是一个 new 状态的实体，该方法会产生一个根据 A 产生的 managed 状态实体 A2 ;
 * <p/>
 * 3.       如果 A 是一个 managed 状态的实体，它的状态不会发生任何改变。但是系统仍会在数据库执行 UPDATE 操作；
 * <p/>
 * 4.       如果 A 是一个 removed 状态的实体，该方法会抛出 IllegalArgumentException 异常。
 * <p/>
 * public void refresh(Object entity)
 * refresh 方法可以保证当前的实例与数据库中的实例的内容一致。
 * <p/>
 * <p/>
 * <p/>
 * 对不同状态下的实例 A ， refresh 会产生以下操作 :
 * <p/>
 * 1.       如果 A 是一个 new 状态的实例，不会发生任何操作，但有可能会抛出异常，具体情况根据不同 JPA 实现有关；
 * <p/>
 * 2.       如果 A 是一个 managed 状态的实例，它的属性将会和数据库中的数据同步；
 * <p/>
 * 3.       如果 A 是一个 removed 状态的实例，不会发生任何操作 ;
 * <p/>
 * 4.       如果 A 是一个 detached 状态的实体，该方法将会抛出异常。
 * <p/>
 * public void remove(Object entity)
 * remove 方法可以将实体转换为 removed 状态，并且在调用 flush() 方法或提交事物后删除数据库中的数据。
 * <p/>
 * <p/>
 * <p/>
 * 对不同状态下的实例 A ， remove 会产生以下操作 :
 * <p/>
 * 1.       如果 A 是一个 new 状态的实例， A 的状态不会发生任何改变，但系统仍会在数据库中执行 DELETE 语句；
 * <p/>
 * 2.       如果 A 是一个 managed 状态的实例，它的状态会转换为 removed ；
 * <p/>
 * 3.       如果 A 是一个 removed 状态的实例，不会发生任何操作 ;
 * <p/>
 * 4.       如果 A 是一个 detached 状态的实体，该方法将会抛出异常。
 */

//@ConditionalOnBean({EntityManagerFactory.class})
//@Repository
//@Transactional
public class JpaDaoImpl
        extends AbstractDaoFactory
        implements JpaDao, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JpaDaoImpl.class);
    private final Integer hibernateVersion;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager defaultEntityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired(required = false)
    private ParameterNameDiscoverer parameterNameDiscoverer;

    @Autowired(required = false)
    private Validator validator;

    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private FormattingConversionService formattingConversionService;

    @Value("${com.levin.commons.dao.param.placeholder:#{T(com.levin.commons.dao.JpaDao).DEFAULT_JPQL_PARAM_PLACEHOLDER}}")
    private String paramPlaceholder = JpaDao.DEFAULT_JPQL_PARAM_PLACEHOLDER;

    @Value("${com.levin.commons.dao.safeModeMaxLimit:2000}")
    private int safeModeMaxLimit = 2000;

    private static final ThreadLocal<Integer> safeModeMaxLimitThreadLocal = new ThreadLocal<>();

    @Autowired
    private HibernateProperties hibernateProperties;

    private static final Map<String, List<UniqueField>> uniqueFieldMap = new ConcurrentHashMap<>();

    //注意这个是数据库的函数支持缓存，不能静态化
    private final Map<String, Boolean> funSupportMap = new ConcurrentHashMap<>();

    private static final Map<String, String> idAttrNames = new ConcurrentHashMap<>();

    private static final Map<String, Object> idFields = new ConcurrentHashMap<>(256);

    private static final ContextHolder<String, Object> autoFlushThreadContext = ContextHolder.buildThreadContext(true);

    private final DeepCopier deepCopier = this::copy;

    private final String AUTO_FLUSH = getClass().getName() + ".session.flush_" + hashCode();

    private final String AUTO_CLEAR = getClass().getName() + ".session.clear_" + hashCode();

    @Data
    @Accessors(chain = true)
    static class UniqueField {

        String group;
        String key;
        String title;
        final List<Field> fieldList = new ArrayList<>(3);

        UniqueField addField(Field field) {
            if (!fieldList.contains(field)) {
                fieldList.add(field);
            }
            return this;
        }

        UniqueField finish() {

            fieldList.forEach(field -> field.setAccessible(true));

            key = fieldList.stream().map(Field::getName).filter(Objects::nonNull).collect(Collectors.joining(","));

            title = fieldList.stream().map(JpaDaoImpl::getDesc).filter(Objects::nonNull).collect(Collectors.joining("+"));

            return this;
        }

        @Override
        public String toString() {
            return key + ":" + title;
        }

    }

    @Data
    @Accessors(chain = true)
    private static class TRS<T> implements RS<T> {
        long totals;
        List<T> resultList;
    }


    @PostConstruct
    public void init() {

        entityManagerFactory.getMetamodel().getEntities().forEach(entityType -> {

            Class<?> entityClass = entityType.getJavaType();

            //加载缓存
            getTableName(entityClass);

            //加载缓存
            getColumnName(entityClass, "id");

        });

        logger.info("jpa dao init.");
    }

    public JpaDaoImpl() {
        hibernateVersion = getHibernateVersion();
    }

    public static Integer getHibernateVersion() {

        try {
            //5.2.10.Final

            String version = (String) ClassUtils
                    .forName("org.hibernate.Version", JpaDaoImpl.class.getClassLoader())
                    .getDeclaredMethod("getVersionString")
                    .invoke(null);

            //
            logger.info("*** org.hibernate ***  Version:" + version);

            return Integer.parseInt(version.substring(0, version.indexOf(".")));

        } catch (IllegalAccessException e) {
            logger.error("getHibernateVersion error" + ExceptionUtils.getRootCauseInfo(e));
        } catch (InvocationTargetException e) {
            logger.error("getHibernateVersion error" + ExceptionUtils.getRootCauseInfo(e));
        } catch (NoSuchMethodException e) {
            logger.error("getHibernateVersion error" + ExceptionUtils.getRootCauseInfo(e));
        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
            logger.warn("getHibernateVersion " + ExceptionUtils.getRootCauseInfo(e));
        }

        return null;
    }

    @Override
    public boolean isJpa() {
        return true;
    }

    @Override
    public int getSafeModeMaxLimit() {
        Integer limit = safeModeMaxLimitThreadLocal.get();
        return limit != null ? limit : safeModeMaxLimit;
    }

    @Override
    public void setCurrentThreadMaxLimit(Integer maxLimit) {
        safeModeMaxLimitThreadLocal.set(maxLimit);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager getDefaultEntityManager() {
        return defaultEntityManager;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public ParameterNameDiscoverer getParameterNameDiscoverer() {
        return parameterNameDiscoverer;
    }

    @Override
    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @PostConstruct
    public void initCheck() {

        if (logger.isDebugEnabled()) {
            logger.debug("emf:" + entityManagerFactory + ",em:" + defaultEntityManager + ",tm:" + transactionManager);
        }

        if (parameterNameDiscoverer == null) {
            parameterNameDiscoverer = new MethodParameterNameDiscoverer();
        }

        if (defaultEntityManager == null
                && entityManagerFactory != null) {
            defaultEntityManager = entityManagerFactory.createEntityManager();
        }

        if (defaultEntityManager == null || entityManagerFactory == null) {
            throw new IllegalStateException("entityManager or entityManagerFactory must be set");
        }

    }

    @SneakyThrows
    @Override
    public PhysicalNamingStrategy getNamingStrategy() {

        PhysicalNamingStrategy namingStrategy = DaoContext.getValue(PhysicalNamingStrategy.class.getName(), null);

        if (namingStrategy == null && hibernateProperties.getNaming() != null) {

            String physicalStrategy = hibernateProperties.getNaming().getPhysicalStrategy();

            if (!StringUtils.hasText(physicalStrategy)) {
                physicalStrategy = EntityNamingStrategy.class.getName();
            }

            Class<?> aClass = ClassUtils.forName(physicalStrategy, this.getClass().getClassLoader());

            namingStrategy = new PhysicalNamingStrategy() {

                final Map<String, String> columnNameMapCaches = new ConcurrentHashMap<>();

                org.hibernate.boot.model.naming.PhysicalNamingStrategy springPhysicalNamingStrategy = (org.hibernate.boot.model.naming.PhysicalNamingStrategy) BeanUtils.instantiateClass(aClass);

                @Override
                public String toPhysicalTableName(String name, Object jdbcEnvironment) {
                    return springPhysicalNamingStrategy.toPhysicalTableName(Identifier.toIdentifier(name), null).getText();
                }

                @Override
                public String toPhysicalColumnName(String name, Object jdbcEnvironment) {

                    String newName = columnNameMapCaches.get(name);

                    if (!StringUtils.hasText(newName)) {
                        newName = springPhysicalNamingStrategy.toPhysicalColumnName(Identifier.toIdentifier(name), null).getText();
                        columnNameMapCaches.put(name, newName);
                    }

                    return newName;
                }
            };

            DaoContext.setGlobalValue(PhysicalNamingStrategy.class.getName(), namingStrategy);

        }

        return namingStrategy;
    }

    public <V> V getThreadVar(String key, V defaultValue) {
        return autoFlushThreadContext.getOrDefault(key, defaultValue);
    }

    @Override
    public Validator getValidator() {
        return validator;
    }

    @Override
    public JpaDao setParamPlaceholder(String paramPlaceholder) {

        this.paramPlaceholder = paramPlaceholder;

        return this;
    }


    @Override
    public Boolean isSupportFunction(String funName) {
        return funSupportMap.computeIfAbsent(funName.toUpperCase(), this::testSupportFunction);
    }

    private Boolean testSupportFunction(String funcName) {

        if ("IFNULL".equalsIgnoreCase(StringUtils.trimWhitespace(funcName))) {
            try {  //使用哑表测试
                List<Object> objects = find(true, null, -1, 1, "select IFNULL('Yes','No') from dual");
                return "Yes".equals(objects.get(0));
            } catch (Exception e) {
                //防止一直查询
                return false;
            }
        }

        return null;
    }


    @Override
    public DeepCopier getDeepCopier() {
        return deepCopier;
    }

    /**
     * 强制让对象脱管
     *
     * @param object
     */
    @Override
    public JpaDao detach(Object object) {

        if (object != null) {
            try {
                getEntityManager().detach(object);
            } catch (IllegalArgumentException e) {
                logger.warn("detach entity object " + e.getLocalizedMessage());
            }
        }

        return this;
    }

    @Override
    public JpaDao clearSessionCache() {

        getEntityManager().clear();

        return this;
    }

    @Override
    public EntityManager getEntityManager() {

        if (defaultEntityManager != null) {

            return defaultEntityManager;
        }

        if (entityManagerFactory != null) {

            logger.info("默认实体管理器没有注入，将使用entityManagerFactory创建");

            EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);

            if (em == null) {
                em = entityManagerFactory.createEntityManager();
            }

            return em;
        }

        throw new IllegalStateException("can't find entityManager instance");
    }

    /**
     * 为了兼容 JPA 和 普通 SQL 方式混用问题, 在发生更新后，立刻发送语句
     * <p>
     * 会带来性能问题
     *
     * @param em
     */
    protected void tryAutoFlushAndDetachAfterUpdate(EntityManager em, boolean isFlush, boolean isClear, Object entity) {

        //是否自动发送语句，默认先发送语句
        if (DaoContext.isAutoFlushAndClearBeforeQuery(true)
                && em.isJoinedToTransaction()) {

            if (isFlush) {
                //em.flush();
                //需要去FLUSH
                logger.debug("*** tryAutoFlushAndDetachAfterUpdate AUTO_FLUSH");
                autoFlushThreadContext.put(AUTO_FLUSH, true);
            }

            if (isClear) {
                // em.clear();
                //需要去 CLEAR
                logger.debug("*** tryAutoFlushAndDetachAfterUpdate AUTO_CLEAR");
                autoFlushThreadContext.put(AUTO_CLEAR, true);
            } else if (entity != null && em.contains(entity)) {
                em.detach(entity);
            }

        }
    }


    /**
     * 为了确保查询数据的正确性，在查询前自动清楚缓存
     * <p>
     * 当通过 update 语句更新，并且更新记录数大于 0 时，表示数据库被实际变更，则自动 flush，并且清除 session 缓存
     *
     * @param em
     */
    protected void autoFlushAndClearBeforeQuery(EntityManager em) {

        if (DaoContext.isAutoFlushAndClearBeforeQuery(true)
                && em.isJoinedToTransaction()) {

            if (autoFlushThreadContext.getOrDefault(AUTO_FLUSH, false)) {
                try {
                    logger.debug("*** autoFlushAndClearBeforeQuery AUTO_FLUSH ...");
                    em.flush();
                } finally {
                    autoFlushThreadContext.put(AUTO_FLUSH, false);
                }
            }

            if (autoFlushThreadContext.getOrDefault(AUTO_CLEAR, false)) {
                try {
                    logger.debug("*** autoFlushAndClearBeforeQuery AUTO_CLEAR ...");
                    em.clear();
                } finally {
                    autoFlushThreadContext.put(AUTO_CLEAR, false);
                }
            }

        }
    }

//    /**
//     * 检查访问级别
//     * 暂不实现
//     *
//     * @param entity
//     */
//    void checkAccessLevel(Object entity, EntityOption.AccessLevel accessLevel) {
//        EntityOption entityOption = entity.getClass().getAnnotation(EntityOption.class);
//
//        if (entityOption != null) {
//
//        }
//    }

    protected <E> E tryConvertToEntityObject(Object entityOrDto, boolean isInitPrePersistMethod) {

        if (entityOrDto == null) {
            throw new PersistenceException("persist object is null");
        }

        //如果不是实体类
        Class<?> entityOrDtoClass = entityOrDto.getClass();

        //如果是实体类，不做处理
        if (entityOrDtoClass.isAnnotationPresent(Entity.class)) {
            return (E) entityOrDto;
        }

        TargetOption targetOption = entityOrDtoClass.getAnnotation(TargetOption.class);

        //如果没有指定实体类，不做处理
        if (targetOption == null) {
            return (E) entityOrDto;
        }

        Class<?> entityClass = targetOption.entityClass();

        //如果不是有效的实体类，不做处理
        if (!isValidClass(entityClass)) {
            return (E) entityOrDto;
        }

        //执行初始化方法
        com.levin.commons.utils.ClassUtils.invokePostConstructMethod(entityOrDto);

        if (isInitPrePersistMethod) {
            //执行初始化方法
            com.levin.commons.utils.ClassUtils.invokeMethodByAnnotationTag(entityOrDto, false, PrePersist.class);
        }

        //                if (countByQueryObj(entityOrDto) > 0) {
        //                    throw new org.springframework.dao.DataIntegrityViolationException("数据已经存在");
        //                }

        Object old = entityOrDto;

        // 1、获取实体对象，需要注入的属性
        String[] daoInjectAttrs = QueryAnnotationUtil.getDaoInjectAttrs(entityClass);

        //2、不拷贝注入属性
        entityOrDto = copy(entityOrDto, BeanUtils.instantiateClass(entityClass), 1, daoInjectAttrs);

        if (daoInjectAttrs != null
                && daoInjectAttrs.length > 0) {
            //3、注入变量,需要注入的变量
            DaoContext.injectValues(entityOrDto, old);
        }

        //新对象初始化参数
        com.levin.commons.utils.ClassUtils.invokePostConstructMethod(entityOrDto);

        return (E) entityOrDto;
    }

    @Transactional
    public List<Object> batchCreate(List<Object> entityOrDtoList) {

        List<Object> result = new ArrayList<>(entityOrDtoList.size());

        entityOrDtoList.forEach(data -> result.add(create(data)));

        return result;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Object> batchCreate(List<Object> entityOrDtoList, int commitBatchSize) {

        if (commitBatchSize < 1) {
            commitBatchSize = 512;
        } else if (commitBatchSize > 512 * 10) {
            commitBatchSize = 5120;
        }

        if (entityOrDtoList.size() < commitBatchSize) {
            getDao().batchCreate(entityOrDtoList);
        } else {

            final List<Object> tempList = new ArrayList<>(commitBatchSize);

            int i = 0;
            for (Object data : entityOrDtoList) {

                tempList.add(data);
                //如果批次满
                if (i++ % commitBatchSize == 0) {
                    getDao().batchCreate(tempList);
                    tempList.clear();
                }
            }

            if (!tempList.isEmpty()) {
                getDao().batchCreate(tempList);
                tempList.clear();
            }

        }

        return entityOrDtoList;
    }

    @Override
    @Transactional
    public <E> E create(Object entityOrDto, boolean isCheckUniqueValue) {

        E entity = tryConvertToEntityObject(entityOrDto, true);

        if (isCheckUniqueValue) {
            //查询重复
            checkUniqueEntity(entityOrDto);
        }

//        checkAccessLevel(entity, EntityOption.AccessLevel.Creatable);
        //如果有ID对象，将会抛出异常
        EntityManager em = getEntityManager();

        try {
            em.persist(entity);
        } catch (EntityExistsException ex) {

            if (getEntityId(entity) != null) {
                entity = em.merge(entity);
            } else {
                throw ex;
            }

            // tryAutoFlushAndDetachAfterUpdate(em, entity);
        }

        return entity;
    }

    @Override
    @Transactional(rollbackFor = {PersistenceException.class})
    public <E> E save(Object entityOrDto, boolean isCheckUniqueValue) {

        E entity = tryConvertToEntityObject(entityOrDto, true);

        EntityManager em = getEntityManager();

        boolean mergeOk = false;

        try {
            //如果是一个 removed 状态的实体，该方法会抛出 IllegalArgumentException 异常。

            Object entityId = getEntityId(entity);

            if (entityId != null) {
//                checkAccessLevel(entity, EntityOption.AccessLevel.Writeable);

                if (isCheckUniqueValue) {
                    checkUniqueEntity(entityOrDto);
                }

                entity = em.merge(entity);

                // tryAutoFlushAndDetachAfterUpdate(em, entity);
                mergeOk = true;
            }

        } catch (IllegalArgumentException e) {
            //不做异常处理尝试使用persist进行保存
        } catch (EntityNotFoundException e) {
            //不做异常处理尝试使用，persist进行保存
        }

        if (!mergeOk) {

            //removed 状态的实体，persist可以处理
//            checkAccessLevel(entity, EntityOption.AccessLevel.Creatable);
            if (isCheckUniqueValue) {
                checkUniqueEntity(entityOrDto);
            }

            em.persist(entity);
        }

        return entity;
    }

    /**
     * 唯一约束检查
     *
     * @param entityOrDto
     */
    protected void checkUniqueEntity(Object entityOrDto) {

        //默认不检查，否则耗费性能
        findUniqueEntityId(entityOrDto, null, (id, info) -> {
            throw new NonUniqueResultException("[" + info + "]已经存在");
        });

    }


    @Override
    @Transactional(rollbackFor = PersistenceException.class)
    public void delete(Object entity) {

        EntityManager em = getEntityManager();

        if (entity != null && !em.contains(entity)) {
            entity = em.find(entity.getClass(), getEntityId(entity));
        }

        if (entity != null) {
//            checkAccessLevel(entity, EntityOption.AccessLevel.Deletable);
            em.remove(entity);
            //tryAutoFlushAndDetachAfterUpdate(em, null);
        }
    }

    @Override
    public boolean deleteById(Class entityClass, Object id) {

//        if (id != null) {
//            checkAccessLevel(entityClass, EntityOption.AccessLevel.Deletable);
//        }

        return update("delete from " + entityClass.getName()
                + " where " + getEntityIdAttrName(entityClass) + " = " + getParamPlaceholder(false), id) > 0;
    }

    @Override
    public int updateByQueryObj(Object... queryObjs) {
        return newDao(UpdateDao.class, queryObjs).update();
    }

    @Override
    public boolean singleUpdateByQueryObj(Object... queryObjs) {
        return newDao(UpdateDao.class, queryObjs).singleUpdate();
    }

    @Override
    public void uniqueUpdateByQueryObj(Object... queryObjs) {
        newDao(UpdateDao.class, queryObjs).uniqueUpdate();
    }

    @Override
    public int deleteByQueryObj(Object... queryObjs) {
        return newDao(DeleteDao.class, queryObjs).delete();
    }

    @Override
    public boolean singleDeleteByQueryObj(Object... queryObjs) {
        return newDao(DeleteDao.class, queryObjs).singleDelete();
    }

    @Override
    public void uniqueDeleteByQueryObj(Object... queryObjs) {
        newDao(DeleteDao.class, queryObjs).uniqueDelete();
    }

    /**
     * 替换默认的占位符
     * <p>
     * hibernate 要去
     *
     * @param sql
     * @return
     */
    public String replacePlaceholder(boolean isNative, String sql) {

        String placeholder = this.getParamPlaceholder(isNative).trim();

        //如果JDBC 是一样的，就不做处理
        if (MiniDao.DEFAULT_JDBC_PARAM_PLACEHOLDER.trim().equals(placeholder)) {
            return sql;
        }

        return QLUtils.replaceParamPlaceholder(sql, placeholder, getParamStartIndex(isNative), "?", null);
    }

    protected void checkLimit(int count) {
        if (count < 1 || count > this.getSafeModeMaxLimit()) {
            throw new DaoSecurityException("no limit or limit over " + getSafeModeMaxLimit());
        }
    }

    @Override
    @Transactional(rollbackFor = PersistenceException.class)
    public int update(boolean isNative, int start, int count, String statement, Object... paramValues) {

        checkLimit(count);

        List paramValueList = flattenParams(null, paramValues);

        if (!paramValueList.isEmpty()) {
            statement = replacePlaceholder(isNative, statement);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Jpa Ql:[" + statement + "], Param placeholder:" + getParamPlaceholder(isNative)
                    + ", native: " + isNative + ", limits :" + start + "," + count
                    + " , StartIndex: " + getParamStartIndex(isNative) + " , Params:" + paramValueList);
        }

        EntityManager em = getEntityManager();

        Query query = isNative ? em.createNativeQuery(statement) : em.createQuery(statement);

        setParams(isNative, getParamStartIndex(isNative), query, paramValueList);

        setRange(query, start, count);

        int n = query.executeUpdate();

        //非常重要
        //当通过 update 语句更新，并且更新记录数大于 0 时，表示数据库被实际变更，则自动 flush，并且清除 session 缓存
        tryAutoFlushAndDetachAfterUpdate(em, n > 0, n > 0, null);

        return n;
    }

    @Override
    public void flush() {
        getDefaultEntityManager().flush();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object id) {

        EntityManager em = getEntityManager();

        //如果当前有事务，发送语句，在清除缓存，以保证数据正确加载
        autoFlushAndClearBeforeQuery(em);

        return em.find(entityClass, id);
    }


    @Override
    public <ID> ID getEntityId(Object entity) {

        Class<?> entityClass = entity.getClass();

        String idAttrName = getEntityIdAttrName(entityClass);

        if (!StringUtils.hasText(idAttrName)) {
            throw new IllegalArgumentException("class " + entityClass.getName() + " is not a jpa entity object");
        }

        Object fieldOrMethod = getFieldOrMethodByName(entityClass, idAttrName);

        Exception ex = null;

        if (fieldOrMethod instanceof Field) {
            try {
                return (ID) ((Field) fieldOrMethod).get(entity);
            } catch (Exception e) {
                ex = e;
            }
        } else if (fieldOrMethod instanceof Method) {
            try {
                return (ID) ((Method) fieldOrMethod).invoke(entity);
            } catch (Exception e) {
                ex = e;
            }
        }

        throw new IllegalArgumentException("class " + entityClass.getName() + " can't get property '" + idAttrName + "' value", ex);

    }

    /**
     * @param entityClass
     * @param idAttrName
     * @return
     */
    private static Object getFieldOrMethodByName(Class<?> entityClass, String idAttrName) {

        final String key = entityClass.getName() + "." + idAttrName;

        Object fieldOrMethod = idFields.get(key);


        if (fieldOrMethod == null) {
            String mName = "get" + Character.toUpperCase(idAttrName.charAt(0)) + idAttrName.substring(1);
            fieldOrMethod = ReflectionUtils.findMethod(entityClass, mName);
            if (fieldOrMethod != null) {
                idFields.put(key, fieldOrMethod);
            }
        }

        if (fieldOrMethod == null) {
            fieldOrMethod = ReflectionUtils.findField(entityClass, idAttrName);
            if (fieldOrMethod != null) {
                ((Field) fieldOrMethod).setAccessible(true);
                idFields.put(key, fieldOrMethod);
            }
        }

        return fieldOrMethod;
    }

    static Optional<String> findFirst(String... texts) {
        return Arrays.stream(texts)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    static String getDesc(Field field) {

        Class<?> fieldClass = field.getClass();

        //特别处理，如何过多租户 或是 跨部门对象
        if (MultiTenantObject.class.isAssignableFrom(fieldClass) || OrganizedObject.class.isAssignableFrom(fieldClass)) {
            return null;
        }

        return Optional.ofNullable(field.getAnnotation(Schema.class))
                .map(schema -> findFirst(schema.title(), schema.description()).orElse(field.getName()))
                .orElse(field.getName());
    }


    /**
     * 通过查询对象查询唯一约束的对象ID
     *
     * @param queryObj
     * @param entityClass
     * @return
     */
    @Override
    public <ID> ID findUniqueEntityId(@NotNull Object queryObj, Class<?> entityClass, BiConsumer<ID, String> onFind) {

        Assert.notNull(queryObj, "查询对象为空");

        //尝试自动获取实体类
        if (entityClass == null) {

            TargetOption targetOption = queryObj.getClass().getAnnotation(TargetOption.class);

            if (targetOption != null) {
                entityClass = targetOption.entityClass();
            }

            if (!isEntityClass(entityClass)
                    && isEntityClass(queryObj.getClass())) {
                entityClass = queryObj.getClass();
            }
        }

        Assert.isTrue(isEntityClass(entityClass), "查询实体未明确");


        Class<?> finalEntityClass = entityClass;
        List<UniqueField> uniqueFields = uniqueFieldMap.computeIfAbsent(entityClass.getName(),
                key -> getUniqueFields(finalEntityClass));

        //开始查找
        for (UniqueField uniqueField : uniqueFields) {

            ID id = findIdByUniqueField(entityClass, queryObj, uniqueField);

            if (id != null) {
                if (onFind != null) {
                    onFind.accept(id, uniqueField.getTitle());
                }
                return id;
            }
        }

        return null;
    }

    @SneakyThrows
    private <ID> ID findIdByUniqueField(Class<?> entityClass, Object queryObj, UniqueField uniqueField) {

        SelectDao<?> selectDao = selectFrom(entityClass)
                .disableEmptyValueFilter()
                .select(getEntityIdAttrName(entityClass));

//        String idAttrName = getEntityIdAttrName(entityClass);
//        Object id = ObjectUtil.getValue(queryObj, idAttrName, false);
//        //增加ID不等于自己
//        if (id != null) {
//            selectDao.notEq(idAttrName, id);
//        }

        boolean hasWhere = false;

        for (Field field : uniqueField.fieldList) {

            String fieldName = field.getName();

            Object value = ObjectUtil.getValue(queryObj, fieldName, true);

            if (value == null) {

                //部分数据库支持空字符串等同于Null空值

                //@todo 考虑根据数据库类型进行优化处理

                //目前 MySql 支持空值忽略，唯一约束
                //暂时 忽略空值
                // selectDao.isNull(fieldName);

                //暂时做法，只要有一个空值，就忽略本功能
                hasWhere = false;
                break;
            } else {
                selectDao.eq(fieldName, value);
                hasWhere = true;
            }
        }

        //只查ID
        return hasWhere ? selectDao.findOne() : null;
    }

    private static List<UniqueField> getUniqueFields(Class<?> entityClass) {

        List<UniqueField> uniqueFields = new ArrayList<>(3);

        Map<String, UniqueField> tmp = new LinkedHashMap<>();

        //1、获取Unique注解要求的唯一约束
        getFieldsFromCache(entityClass)
                .stream()
                .filter(field -> field.isAnnotationPresent(Unique.class))
                .forEachOrdered(field -> {

                    Unique unique = field.getAnnotation(Unique.class);

                    //如果字段
                    if (StringUtils.hasText(unique.value())) {
                        try {
                            field = getRequireField(entityClass, unique.value().trim());
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(entityClass + field.getName() + "字段上的注解 Unique(value ="
                                    + unique.value() + ") 指定的字段不存在", e);
                        }
                    }

                    if (StringUtils.hasText(unique.group())) {

                        UniqueField uniqueField = tmp.get(unique.group());

                        if (uniqueField == null) {
                            uniqueField = new UniqueField().setGroup(unique.group());
                            tmp.put(unique.group(), uniqueField);
                            uniqueFields.add(uniqueField);
                        }

                        uniqueField.addField(field);

                    } else {
                        uniqueFields.add(new UniqueField().addField(field));
                    }

                });

        //2、获取 Jpa Table 注解定义的唯一约束
        Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(table -> table.uniqueConstraints())
                .ifPresent(uniqueConstraints -> {
                    for (UniqueConstraint constraint : uniqueConstraints) {
                        UniqueField uniqueField = new UniqueField();
                        //唯一约束的列名必须和字段名相同
                        for (String column : constraint.columnNames()) {
                            try {
                                uniqueField.addField(getRequireField(entityClass, column));
                            } catch (NoSuchFieldException e) {
                                throw new RuntimeException(entityClass + " UniqueConstraint 注解 columnNames{"
                                        + column + "} 中必须填入类的字段名而不是数据库的字段名", e);
                            }
                        }
                        if (!uniqueField.fieldList.isEmpty()) {
                            uniqueFields.add(uniqueField);
                        }
                    }
                });


        //3、查找 unique 属性
        ReflectionUtils.doWithFields(entityClass, field -> {

            Boolean unique = (Boolean) Stream.of(field.getAnnotations())
                    .filter(Objects::nonNull)
                    //1、过滤出JPA 注解
                    .filter(annotation -> annotation.annotationType().getName().startsWith(Column.class.getPackage().getName()))
                    //2、读取unique属性值
                    .map(annotation -> com.levin.commons.utils.ClassUtils.getValue(annotation, "unique", false))
                    .filter(v -> v instanceof Boolean || boolean.class.isInstance(v))
                    .findFirst()
                    .orElse(null);

            if (Boolean.TRUE.equals(unique)) {
                //加入字段
                uniqueFields.add(new UniqueField().addField(field));
            }

        }, field -> !Modifier.isStatic(field.getModifiers()));

        //4、自动生成描述信息
        uniqueFields.forEach(UniqueField::finish);

        return uniqueFields;
    }

    /**
     * 获取字段
     *
     * @param entityClass
     * @param fieldNames
     * @return
     * @throws NoSuchFieldException
     */
    private static Field getRequireField(Class<?> entityClass, String... fieldNames) throws NoSuchFieldException {
        return getFieldsFromCache(entityClass)
                .stream()
                .filter(field -> Arrays.stream(fieldNames).anyMatch(n -> field.getName().equals(n)))
                .findFirst()
                .orElseThrow(() -> new NoSuchFieldException("" + Arrays.asList(fieldNames)));
    }

    /**
     * @fix 修复实体类继承时无法
     */
    @Override
    public String getEntityIdAttrName(Object entityOrClass) {

        Assert.notNull(entityOrClass, "entityOrClass is null");

        Class entityClass = null;

        if (entityOrClass instanceof Class) {
            entityClass = (Class) entityOrClass;
        } else {
            entityClass = entityOrClass.getClass();
        }

        EntityType<?> entityType = getEntityManager().getMetamodel().entity(entityClass);

        if (entityType == null) {
            throw new IllegalArgumentException("class " + entityClass.getName() + " can't find  @" + Id.class.getName() + " method or field");
        }

        return entityType.getId(entityType.getIdType().getJavaType()).getName();
    }

    // @Override

    /**
     * @fix 修复实体类继承时无法
     */
    public String getEntityIdAttrName_OLD(Object entity) {

        final Class<?> entityClass = (entity instanceof Class) ? (Class<?>) entity : entity.getClass();

        String idAttrName = idAttrNames.get(entityClass.getName());

        if (StringUtils.hasText(idAttrName)) {
            return idAttrName;
        }

        Class type = entityClass;

        while (type != Object.class) {

            String attrName = getIdAttrName(type);

            if (attrName != null) {
                idAttrNames.put(entityClass.getName(), attrName);
                return attrName;
            }

            type = type.getSuperclass();
        }

        throw new IllegalArgumentException("class " + entityClass.getName() + " can't find  @" + Id.class.getName() + " method or field");
    }

    private String getIdAttrName(Class<?> entityClass) {

        //获取声明的所有字段，包括私有属性
        for (Field field : entityClass.getDeclaredFields()) {

            Object annotation = field.getAnnotation(Id.class);

            if (annotation == null) {
                annotation = field.getAnnotation(EmbeddedId.class);
            }

            if (annotation != null) {
                return field.getName();
            }
        }

        //获取声明的所有方法，包括私有方法
        for (Method method : entityClass.getDeclaredMethods()) {

            Object annotation = method.getAnnotation(Id.class);

            if (annotation == null) {
                annotation = method.getAnnotation(EmbeddedId.class);
            }

            if (annotation != null) {
                //去除get
                String name = method.getName().substring(3);
                //首字母变小写
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

                return name;
            }
        }

        return null;
    }

    @Override
    public <T> List<T> find(int start, int count, String statement, Object... paramValues) {
        return find(false, null, start, count, statement, paramValues);
    }

    /**
     * @param isNative    是否是原生查询
     * @param resultClass 可以为null(结果集将返回对象数组)，或是java.util.Map 或是具体的类
     * @param start
     * @param count
     * @param statement
     * @param paramValues 数组中的元素可以是map，数组，或是list,或值对象
     * @return
     */
    @Override
    public <T> List<T> find(boolean isNative, Class<T> resultClass, int start, int count, String statement, Object... paramValues) {

        checkLimit(count);

        List paramValueList = flattenParams(null, paramValues);

        String oldStatement = statement;

        if (!paramValueList.isEmpty()) {
            statement = replacePlaceholder(isNative, statement);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Select JPQL:[" + statement + "] ResultClass: " + resultClass + " , Param placeholder:" + getParamPlaceholder(isNative)
                    + ", native: " + isNative + ", limits :" + start + "," + count
                    + " , StartIndex: " + getParamStartIndex(isNative) + " , Params:" + paramValueList);
        }

        EntityManager em = getEntityManager();

        Query query = null;

        //@todo hibernate 5.2.17 对结果类的映射，不支持自定义的类型
        // setResultTransformer 实际使用时无法获取到字段名，也许是 hibernate bug
        // query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);


        if (isNative) {
            query = (resultClass == null) ? em.createNativeQuery(statement) : em.createNativeQuery(statement, resultClass);
        } else {
            query = (resultClass == null) ? em.createQuery(statement) : em.createQuery(statement, resultClass);
        }

        org.hibernate.query.Query hibernateQuery = query.unwrap(org.hibernate.query.Query.class);

//        hibernateQuery.setFlushMode(FlushModeType.AUTO);

        //hibernateQuery.setHibernateFlushMode(null);

//        query.setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
//        query.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
//        query.setHint("org.hibernate.cacheMode", "REFRESH");

        setParams(isNative, getParamStartIndex(isNative), query, paramValueList);

        setRange(query, start, count);

        autoFlushAndClearBeforeQuery(em);

        return (List<T>) query.getResultList();

    }

    @Override
    public String getPKName(Object tableOrEntityClass) {

        if (tableOrEntityClass instanceof Class) {
            return getEntityIdAttrName(tableOrEntityClass);
        }

        return null;
    }

    @Override
    public String getParamPlaceholder(boolean isNative) {

//        return isNative ? MiniDao.DEFAULT_JDBC_PARAM_PLACEHOLDER : this.paramPlaceholder;

        return this.paramPlaceholder;
    }


    public int getParamStartIndex(boolean isNative) {

        //   return (isNative && paramStartIndex < 1) ? (paramStartIndex + 1) : paramStartIndex;

        boolean isJdbcParamPlaceholder = MiniDao.DEFAULT_JDBC_PARAM_PLACEHOLDER.trim().equals(this.getParamPlaceholder(isNative).trim());


        //如果是？占位符 并且不是原生查询才返回0

        return (isJdbcParamPlaceholder && !isNative) ? 0 : 1;

    }

    /**
     * 尝试使用代理的DAO类
     * <p/>
     * 解决
     *
     * @return
     */

    private transient JpaDao proxyDao = null;

    @Override
    public JpaDao getDao() {

        if (applicationContext != null && proxyDao == null) {
            proxyDao = applicationContext.getBean(JpaDao.class);
        }

        if (proxyDao != null) {
            return proxyDao;
        }

        return this;
    }


    private Class tryFindResultClass(Object... queryObjs) {

        Class resultClass = null;

        if (queryObjs == null
                || queryObjs.length == 0) {
            return resultClass;
        }

        List<Object> queryObjList = tryGetEntityClassAndClear(
                //展开嵌套参数，过滤简单的类型，
                filterQueryObjSimpleType(expandAndFilterNull(null, Arrays.asList(queryObjs)), null), null
        );

        for (Object queryObj : queryObjList) {

            if (queryObj == null) {
                continue;
            }

            if (queryObj instanceof ResultClassSupplier) {
                resultClass = ((ResultClassSupplier) queryObj).get();
                if (isValidClass(resultClass)) {
                    break;
                }
            }

            if (queryObj instanceof QueryOption) {
                resultClass = ((QueryOption) queryObj).getResultClass();
                if (isValidClass(resultClass)) {
                    break;
                }
            }

            TargetOption targetOption = queryObj.getClass().getAnnotation(TargetOption.class);

            //注解优先
            if (targetOption != null) {
                resultClass = targetOption.resultClass();
                if (isValidClass(resultClass)) {
                    break;
                }
            }

            ResultOption resultOption = queryObj.getClass().getAnnotation(ResultOption.class);

            //注解优先
            if (resultOption != null) {
                resultClass = resultOption.resultClass();
                if (isValidClass(resultClass)) {
                    break;
                }
            }

        }

        return resultClass;
    }

    private boolean isValidClass(Class clazz) {
        return clazz != null && clazz != Void.class;
    }

    private boolean isEntityClass(Class clazz) {
        return isValidClass(clazz)
                && (clazz.isAnnotationPresent(Entity.class));
    }

    @Override
    public <T> SelectDao<T> forSelect(Object... queryObjs) {
        return forSelect(null, queryObjs);
    }

    public <T> SelectDao<T> forSelect(Class<?> resultType, Object... queryObjs) {

        if (resultType == null) {
            resultType = tryFindResultClass(queryObjs);
        }

        SelectDao selectDao = null;

        if (!hasType(resultType, queryObjs)
                && hasSelectStatementField(resultType)) {
            selectDao = newDao(SelectDao.class, queryObjs, resultType);
        } else {
            selectDao = newDao(SelectDao.class, queryObjs);
        }

        return selectDao;
    }

    @Override
    public long countByQueryObj(Object... queryObjs) {
        return forSelect(queryObjs).count();
    }


    @Override
    public <E> List<E> findByQueryObj(Object... queryObjs) {
        return findByQueryObj(tryFindResultClass(queryObjs), queryObjs);
    }


    /**
     * 查询分页数据
     * <p>
     * 参考注解类PageOption {@link PageOption}
     * <p>
     * 参考 PagingData  {@link PagingData}
     *
     * @param pagingHolderInstanceOrClass 分页结果存放对象，分页对象必须使用 PageOption 进行注解
     * @param queryObjs                   查询对象
     * @return 返回分页对象
     * @since 2.5.1 新增方法
     */
    @Override
    public <P> P findPageByQueryObj(Class<?> resultType, Object pagingHolderInstanceOrClass, Object... queryObjs) {
        return PagingQueryHelper.findPageByQueryObj(this, resultType, pagingHolderInstanceOrClass, queryObjs);
    }

    @Override
    public <E> RS<E> findTotalsAndResultList(Object... queryObjs) {

        Class type = tryFindResultClass(queryObjs);

        SelectDao selectDao = forSelect(queryObjs);

        return new TRS<E>()
                .setTotals(selectDao.count())
                .setResultList((List<E>) selectDao.find(type));
    }


    @Override
    public <E> List<E> findByQueryObj(Class<E> resultType, Object... queryObjs) {

        if (resultType == null) {
            resultType = tryFindResultClass(queryObjs);
        }

        return forSelect(resultType, queryObjs).find(resultType);
    }


    @Override
    public <E> E findOneByQueryObj(boolean isExpectUnique, Class<E> resultType, Object... queryObjs) {

        if (resultType == null) {
            resultType = tryFindResultClass(queryObjs);
        }

        if (!hasType(resultType, queryObjs)
                && hasSelectStatementField(resultType)) {
            return (E) newDao(SelectDao.class, queryObjs, resultType).findOne(isExpectUnique, resultType);
        }

        return (E) newDao(SelectDao.class, queryObjs).findOne(isExpectUnique, resultType);
    }


    /**
     * @param type
     * @param queryObjs
     * @return
     */
    private static boolean hasType(Class<?> type, Object... queryObjs) {

        if (type == null || queryObjs == null || queryObjs.length == 0) {
            return false;
        }

        return Stream.of(queryObjs)
                .filter(Objects::nonNull)
                .map(o -> o instanceof Class ? (Class<?>) o : o.getClass())
                .anyMatch(t -> t == type || type.isAssignableFrom(t));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private Query setRange(Query query, int index, int count) {

        if (index > 0) {
            query.setFirstResult(index);
        }

        if (count > 0) {
            query.setMaxResults(count);
        }

        return query;
    }

    private int setParams(boolean isNative, int pIndex, Query query, List paramValueList) {

        Map<Object, Parameter> parameterMap = new LinkedHashMap<>();

        Set<Parameter<?>> parameters = query.getParameters();

        for (Parameter<?> p : parameters) {
            parameterMap.put(StringUtils.hasText(p.getName()) ? p.getName() : p.getPosition(), p);
        }

        for (Object paramValue : paramValueList) {
            if (paramValue instanceof Map) {
                //如果是Map，就设置命名参数
                for (Map.Entry<Object, Parameter> entry : parameterMap.entrySet()) {
                    try {
                        //没有属性会抛出异常
                        Object value = ObjectUtil.getIndexValue(paramValue, (String) entry.getKey(), true);


                        query.setParameter((String) entry.getKey(), tryAutoConvertParamValue(isNative, parameterMap, entry.getKey(), value));

                    } catch (Exception e) {

                    }
                }

            } else {

                paramValue = tryAutoConvertParamValue(isNative, parameterMap, pIndex, paramValue);

                //尝试使用命名参数
                if (parameterMap.containsKey("" + pIndex)) {
                    query.setParameter("" + pIndex, paramValue);
                } else {
                    query.setParameter(pIndex, paramValue);
                }

                //关键步骤
                pIndex++;
            }
        }


        return pIndex;
    }

    private Object tryAutoConvertParamValue(boolean isNative, Map<Object, Parameter> parameterMap, Object paramKey, Object paramValue) {
        //自动转换数据类型
        //@todo 观察，需要关注性能问题
        //@todo 数据自动转换，关注 ConditionBuilderImpl.tryToConvertValue

        if (paramValue == null) {
            return paramValue;
        }

        Parameter parameter = parameterMap.get(paramKey);

        if (parameter == null && (paramKey instanceof Number)) {
            parameter = parameterMap.get(paramKey.toString());
        }

        try {
            Class<?> parameterType = parameter != null ? parameter.getParameterType() : null;

            if (parameterType != null
                    && !parameterType.equals(paramValue.getClass())) {

                paramValue = ObjectUtil.convert(paramValue, parameterType);
            }

        } catch (Exception e) {
            logger.warn(" try to convert param [" + paramKey + "] value error: " + ExceptionUtils.getRootCauseInfo(e));
        }

        //关键点，如果是原生查询，枚举要转换城对应的
//        if (isNative && paramValue != null
//                && paramValue.getClass().isEnum()) {
//        }

        return paramValue;
    }

    /**
     * 关键方法，把数组或是集合嵌套的参数，按顺序提取出来，抚平
     *
     * @param valueHolder
     * @param paramValues
     * @return
     */
    public List flattenParams(List valueHolder, Object... paramValues) {

        if (valueHolder == null) {
            valueHolder = new ArrayList();
        }

        if (paramValues == null) {
            return valueHolder;
        }

        //扁平化 Map
        Map<String, Object> namedParams = new LinkedHashMap<>();

        for (Object paramValue : paramValues) {
            if (paramValue instanceof Collection) {
                for (Object pv : ((Collection) paramValue)) {
                    flattenParams(valueHolder, pv);
                }
            } else if (paramValue != null && paramValue.getClass().isArray()) {
                int length = Array.getLength(paramValue);
                for (int i = 0; i < length; i++) {
                    flattenParams(valueHolder, Array.get(paramValue, i));
                }
            } else if (paramValue instanceof Map) {
                //扁平化 Map，所有的都合并到同一个 Map
                namedParams.putAll(Map.class.cast(paramValue));
            } else {
                valueHolder.add(paramValue);
            }
        }

        if (namedParams.size() > 0) {
            valueHolder.add(namedParams);
        }

        return valueHolder;
    }

}


