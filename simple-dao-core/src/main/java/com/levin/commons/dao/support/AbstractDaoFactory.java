package com.levin.commons.dao.support;

import com.levin.commons.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterNameDiscoverer;

import static org.springframework.util.StringUtils.hasText;

//@Component
//@ConditionalOnMissingBean(DaoFactory.class)
//@ConditionalOnBean(MiniDao.class)
public abstract class AbstractDaoFactory implements DaoFactory {

//    @Autowired
//    MiniDao dao;

    @Autowired(required = false)
    private ParameterNameDiscoverer parameterNameDiscoverer;


    //    @Override
    abstract public MiniDao getDao();


    public ParameterNameDiscoverer getParameterNameDiscoverer() {
        return parameterNameDiscoverer;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    // @Override
    public <DAO extends ConditionBuilder> DAO newDao(Class<DAO> daoClass, Object... queryObjs) {


        if (daoClass == null) {
            throw new IllegalArgumentException("daoClass is null");
        }

        if (SelectDao.class.isAssignableFrom(daoClass)) {
            return (DAO) new SelectDaoImpl(getDao(), false).appendByQueryObj(queryObjs);

        } else if (UpdateDao.class.isAssignableFrom(daoClass)) {
            return (DAO) new UpdateDaoImpl(false, getDao()).appendByQueryObj(queryObjs);

        } else if (DeleteDao.class.isAssignableFrom(daoClass)) {
            return (DAO) new DeleteDaoImpl(false, getDao()).appendByQueryObj(queryObjs);
        } else {
            throw new IllegalArgumentException("action  " + daoClass.getName() + " is not support");
        }

    }

    @Override
    public <T> SelectDao<T> forSelect(Object... queryObjs) {
        return newDao(SelectDao.class, queryObjs);
    }

    @Override
    public <T> UpdateDao<T> forUpdate(Object... queryObjs) {
        return newDao(UpdateDao.class, queryObjs);
    }

    @Override
    public <T> UpdateDao<T> forDelete(Object... queryObjs) {
        return newDao(UpdateDao.class, queryObjs);
    }

    /**
     * 返回值类型
     *
     * @param nativeQL
     * @param fromStatement
     */
    @Override
    public <T> SelectDao<T> selectFrom(boolean nativeQL, String fromStatement) {
        return new SelectDaoImpl<T>(getDao(), nativeQL, fromStatement);
    }

    @Override
    public <T> SelectDao<T> selectFrom(Class<T> clazz, String... alias) {
        return new SelectDaoImpl<T>(getDao(), clazz, checkAlias(alias));
    }

    @Override
    public <T> UpdateDao<T> updateTo(Class<T> clazz, String... alias) {
        return new UpdateDaoImpl<T>(getDao(), clazz, checkAlias(alias));
    }


    @Override
    public <T> DeleteDao<T> deleteFrom(Class<T> clazz, String... alias) {
        return new DeleteDaoImpl<T>(getDao(), clazz, checkAlias(alias))
                .setParameterNameDiscoverer(getParameterNameDiscoverer());
    }


    /**
     * @param tableName
     * @param alias
     * @return
     */
    @Override
    public <T> SelectDao<T> selectFrom(String tableName, String... alias) {
        return new SelectDaoImpl<T>(getDao(), checkTableName(tableName), checkAlias(alias))
                .setParameterNameDiscoverer(getParameterNameDiscoverer());
    }

    /**
     * 创建一个指定类型的更新dao
     *
     * @param tableName 表名，不允许为null
     * @param alias     实体类别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     * @return
     * @throws IllegalArgumentException 如果别名多于一个将会抛出异常
     */
    @Override
    public <T> UpdateDao<T> updateTo(String tableName, String... alias) {
        return new UpdateDaoImpl<T>(getDao(), checkTableName(tableName), checkAlias(alias))
                .setParameterNameDiscoverer(getParameterNameDiscoverer());
    }

    /**
     * 原生类
     * alias别名，为了接口使用更方便，使用可变参，但只获取第一个别名
     *
     * @param tableName
     * @param alias
     * @return
     */
    @Override
    public <T> DeleteDao<T> deleteFrom(String tableName, String... alias) {
        return new DeleteDaoImpl<T>(getDao(), checkTableName(tableName), checkAlias(alias))
                .setParameterNameDiscoverer(getParameterNameDiscoverer());
    }


    private String checkAlias(String... alias) {

        if (alias != null && alias.length > 1) {
            throw new IllegalArgumentException("alias only allow one");
        }

        return (alias != null && alias.length > 0) ? alias[0] : null;
    }

    private String checkTableName(String tableName) {

        if (!hasText(tableName)) {
            throw new IllegalArgumentException("tableName is null");
        }

        return tableName;
    }

}
