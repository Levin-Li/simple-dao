package com.levin.commons.dao.support;

import com.levin.commons.dao.SimpleDao;
import org.springframework.beans.BeanUtils;

public abstract class PageQueryHelper {

    private PageQueryHelper() {
    }

    /**
     * 通过分页注解，或是生成并返回结果对象
     *
     * @param simpleDao
     * @param classOrInstance
     * @param queryDto
     * @param <T>
     * @return
     */
    public static <T> T findByPageObject(SimpleDao simpleDao, T classOrInstance, Object... queryDto) {

        //@todo

        if (classOrInstance instanceof Class) {
            classOrInstance = BeanUtils.instantiateClass((Class<T>) classOrInstance);
        }

        if (classOrInstance == null) {
            return (T) simpleDao.findByQueryObj(queryDto);
        }


        throw new UnsupportedOperationException();

    }

}
