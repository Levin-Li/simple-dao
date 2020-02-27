package com.levin.commons.dao.support;

import com.levin.commons.dao.MiniDao;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;

/**
 * 还未完成的 DAO 类
 *
 * @TODO: 2020/2/24
 */

//@Component
//@ConditionalOnMissingBean(MiniDao.class)
//@ConditionalOnBean(DataSource.class)
public class JdbcMiniDao implements MiniDao {

    @Autowired
    DataSource dataSource;


    JdbcOperations jdbcOperations;
    SimpleJdbcInsertOperations insertOperations;


    @PostConstruct
    public void init() {
        jdbcOperations = new JdbcTemplate(dataSource);
        insertOperations = new SimpleJdbcInsert(dataSource);
    }


    @Override
    public Object create(Object entity) {


        String table = entity.getClass().getSimpleName();

//
//        if (annotation != null && StringUtils.hasText(annotation.name())) {
//            table = annotation.name();
//        }

        Number id = insertOperations.withTableName(table).executeAndReturnKey(new BeanPropertySqlParameterSource(entity));


        try {
            BeanUtils.getPropertyDescriptor(entity.getClass(), "id").createPropertyEditor(entity).setValue(id);
        } catch (BeansException e) {

        }


        return entity;
    }

    @Override
    public int update(boolean isNative, int start, int count, String statement, Object... paramValues) {


        statement = addLimit(start, count, statement);

        return jdbcOperations.update(statement, paramValues);
    }

    private String addLimit(int start, int count, String statement) {
        if (count > 0) {
            if (start < 0) {
                start = 0;
            }
        }


        if (start > -1) {
            statement += " limit " + start;
        }

        if (count > 0) {
            statement += " , " + count;
        }
        return statement;
    }

    @Override
    public <T> List<T> find(boolean isNative, Class resultClass, int start, int count, String statement, Object... paramValues) {

        return jdbcOperations.queryForList(addLimit(start, count, statement), resultClass, paramValues);

    }
}
