package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.SimpleDao;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.ObjectUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

/**
 * 暂时不可用
 */
public abstract class PagingQueryHelper {

    private static final Map<String, Map<PageOption.Type, Field>> classFieldCached = new ConcurrentReferenceHashMap<>();

    private PagingQueryHelper() {
    }

    /**
     * 分页查询
     * <p>
     * 通过 PageOption 注解 实现分页大小、分页码，是否查询总数的参数的获取，查询成功后，也通过注解自动把查询结果注入到返回对象中。
     *
     * @param simpleDao     dao
     * @param queryResponse 查询结果，可以是对象实例，也是可以是 Class 对象
     * @param queryDto      查询 DTO
     * @param <T>           查询结果
     * @return
     */
    public static <T> T findByPageOption(SimpleDao simpleDao, Object queryResponse, Object queryDto) {

        if (queryResponse instanceof Class) {
            queryResponse = BeanUtils.instantiateClass((Class<T>) queryResponse);
        }

        if (queryResponse == null) {
            return (T) simpleDao.findByQueryObj(queryDto);
        }

        //需要总记录数
        if (isRequireRecordTotals(queryDto)) {
            setValueByPageOption(queryResponse,
                    PageOption.Type.RequireTotals, (field) -> simpleDao.countByQueryObj(queryDto));
        }

        //需要结果集
        if (isRequireResultList(queryDto)) {

            Object resultList = null;

            Map<PageOption.Type, Field> pageOptionFields = getPageOptionFields(queryDto.getClass());

            Field indexField = pageOptionFields.get(PageOption.Type.PageIndex);
            Field sizeField = pageOptionFields.get(PageOption.Type.PageSize);

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

                PagingQueryReq paging = new PagingQueryReq(-1, -1);

                if (index != null) {
                    paging.setPageIndex(ObjectUtil.convert(index, Integer.class));
                }

                if (size != null) {
                    paging.setPageSize(ObjectUtil.convert(size, Integer.class));
                }

                resultList = simpleDao.findByQueryObj(queryDto, paging);
            }

            //设置结果集
            final Object resultListCopy = resultList;

            setValueByPageOption(queryResponse, PageOption.Type.RequireResultList, field -> resultListCopy);

        }

        return (T) queryResponse;
    }


    private static void setValueByPageOption(Object target, Object key, Function<Field, Object> fun) {

        Field field = getPageOptionFields(target.getClass()).get(key);

        if (field == null) {
            throw new IllegalArgumentException(" 对象[" + target.getClass().getName() + "] 没有 " + key.getClass().getName() + "注解");
        }

        field.setAccessible(true);

        ReflectionUtils.setField(field, target, ObjectUtil.convert(fun.apply(field), field.getType()));
    }

    private static boolean isRequireRecordTotals(Object dto) {
        Field field = getPageOptionFields(dto.getClass()).get(PageOption.Type.RequireTotals);
        return field != null && isEnable(dto, field);
    }

    private static boolean isRequireResultList(Object dto) {
        Field field = getPageOptionFields(dto.getClass()).get(PageOption.Type.RequireResultList);
        return field == null || isEnable(dto, field);
    }


    /**
     * 注解是否允许
     *
     * @param queryDto
     * @param field
     * @return
     */
    private static boolean isEnable(Object queryDto, Field field) {

        String expr = field.getAnnotation(PageOption.class).condition();

        if (!StringUtils.hasText(expr)) {

            field.setAccessible(true);

            Object value = ReflectionUtils.getField(field, queryDto);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else {
                return value != null;
            }

        }

        return Boolean.TRUE.equals(ExprUtils.evalSpEL(queryDto, expr, null));

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

        synchronized (getSyncLock(type)) {

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

    private static Object getSyncLock(Class type) {
        return PagingQueryHelper.class.getName() + "_" + type.getName();
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
