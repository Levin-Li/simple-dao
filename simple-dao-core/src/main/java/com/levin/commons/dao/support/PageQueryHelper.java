package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.SimpleDao;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.ObjectUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 暂时不可用
 */
public abstract class PageQueryHelper {

    private static final Map<String, Map<PageOption.Type, Field>> classFieldCached = new ConcurrentReferenceHashMap<>();

    private PageQueryHelper() {
    }

    /**
     * 通过分页注解，或是生成并返回结果对象
     *
     * @param simpleDao
     * @param pageClassOrInstance
     * @param queryDto
     * @param <T>
     * @return
     */
    private static <T> T find(SimpleDao simpleDao, T pageClassOrInstance, Object queryDto) {

        if (pageClassOrInstance instanceof Class) {
            pageClassOrInstance = BeanUtils.instantiateClass((Class<T>) pageClassOrInstance);
        }

        if (pageClassOrInstance == null) {
            return (T) simpleDao.findByQueryObj(queryDto);
        }

        Map<PageOption.Type, Field> fields = getPageOptionFields(queryDto.getClass());

        Field totalsField = fields.get(PageOption.Type.RecordTotals);
        Field resultListField = fields.get(PageOption.Type.ResultList);

        if (totalsField == null
                && resultListField == null) {
            throw new IllegalArgumentException("查询对象没有通过PageOption注解指定结果集的存放字段");
        }

        boolean requireRecordTotals = totalsField != null && isEnable(queryDto, totalsField);
        boolean requireResultList = resultListField != null && isEnable(queryDto, resultListField);

        if (requireRecordTotals) {
            ReflectionUtils.setField(totalsField, pageClassOrInstance,
                    ObjectUtil.convert(simpleDao.countByQueryObj(queryDto), totalsField.getType()));
        }

        if (requireResultList) {

            Object resultList = null;

            Field indexField = fields.get(PageOption.Type.PageIndex);
            Field sizeField = fields.get(PageOption.Type.PageSize);

            if ((queryDto instanceof Paging)
                    || indexField == null
                    || sizeField == null
                    || !isEnable(queryDto, indexField)
                    || !isEnable(queryDto, sizeField)
            ) {

                resultList = simpleDao.findByQueryObj(queryDto);

            } else {

                Object index = ReflectionUtils.getField(indexField, queryDto);
                Object size = ReflectionUtils.getField(sizeField, queryDto);

                DefaultPaging paging = new DefaultPaging(-1, -1);

                if (index != null) {
                    paging.setPageIndex(ObjectUtil.convert(index, Integer.class));
                }

                if (size != null) {
                    paging.setPageSize(ObjectUtil.convert(size, Integer.class));
                }

                resultList = simpleDao.findByQueryObj(queryDto, paging);
            }

            //设置结果集
            ReflectionUtils.setField(resultListField, pageClassOrInstance, resultList);
        }

        return pageClassOrInstance;
    }

    private static boolean isEnable(Object queryDto, Field field) {
        return Boolean.TRUE.equals(ExprUtils.evalSpEL(queryDto, field.getAnnotation(PageOption.class).condition(), null));
    }

    /**
     * 通过注解设置值
     *
     * @param pageObj
     * @param totals
     * @param resultList
     * @param <T>
     * @return
     */
    public static <T> T setResult(T pageObj, long totals, List resultList) {

        return pageObj;
    }

    private static Map<PageOption.Type, Field> getPageOptionFields(Object... dtos) {

        List<Object> list = Arrays.asList(dtos);

        if (list.size() == 1) {
            return getPageOptionFields(list.get(0).getClass());
        }

        Collections.reverse(list);

        final Map<PageOption.Type, Field> resultMap = new LinkedHashMap<>();

        list.stream()
                .filter(Objects::nonNull)
                .map(o -> o.getClass())
                .forEach(aClass -> resultMap.putAll(getPageOptionFields(aClass)));

        return resultMap;
    }

    /**
     * @param type
     * @return
     */
    private static Map<PageOption.Type, Field> getPageOptionFields(Class type) {

        synchronized (type) {

            Map<PageOption.Type, Field> fieldMap = classFieldCached.get(type.getName());

            if (fieldMap == null) {

                final Map tempMap = new LinkedHashMap<>();

                ReflectionUtils.doWithFields(type, field -> {
                    PageOption option = field.getAnnotation(PageOption.class);
                    if (option != null) {
                        tempMap.put(option.value(), field);
                    }
                });

                fieldMap = tempMap.isEmpty() ? Collections.emptyMap() : tempMap;
            }

            //为了性能不做拷贝
            return fieldMap;
        }

    }


    /**
     * 通过注解提取
     *
     * @param queryDto
     * @return
     */
    public static Paging extractPaging(Object... queryDto) {

        if (queryDto == null) {
            return null;
        }

        for (Object dto : queryDto) {
            if (dto instanceof Paging) {
                return (Paging) dto;
            }
        }


        return null;
    }

}
