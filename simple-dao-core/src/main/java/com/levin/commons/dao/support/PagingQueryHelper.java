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
     * @param simpleDao  dao
     * @param pagingData 查询结果，可以是对象实例，也是可以是 Class 对象
     * @param queryDto   查询 DTO
     * @param <T>        查询结果
     * @return
     */
    public static <T> T findByPageOption(SimpleDao simpleDao, Object pagingData, Object queryDto) {

        if (pagingData instanceof Class) {
            pagingData = BeanUtils.instantiateClass((Class<T>) pagingData);
        }

        if (pagingData == null) {
            return (T) simpleDao.findByQueryObj(queryDto);
        }

        //需要总记录数
        if (isRequireRecordTotals(queryDto)) {
            setValueByPageOption(pagingData,
                    PageOption.Type.RequireTotals, false, (field) -> simpleDao.countByQueryObj(queryDto));
        }

        //需要结果集
        if (isRequireResultList(queryDto)) {

            Object resultList = null;

            Map<PageOption.Type, Field> pageOptionFields = getPageOptionFields(queryDto.getClass());

            Field indexField = pageOptionFields.get(PageOption.Type.PageIndex);
            Field sizeField = pageOptionFields.get(PageOption.Type.PageSize);


            Object pageIndex = null;
            Object pageSize = null;


            if ((queryDto instanceof Paging)
                    || indexField == null
                    || sizeField == null
                    || !isEnable(queryDto, indexField)
                    || !isEnable(queryDto, sizeField)) {

                if (queryDto instanceof Paging) {
                    pageIndex = ((Paging) queryDto).getPageIndex();
                    pageSize = ((Paging) queryDto).getPageSize();
                }

                resultList = simpleDao.findByQueryObj(queryDto);

            } else {

                pageIndex = ReflectionUtils.getField(indexField, queryDto);
                pageSize = ReflectionUtils.getField(sizeField, queryDto);

                PagingQueryReq paging = new PagingQueryReq(-1, -1);

                if (pageIndex != null) {
                    paging.setPageIndex(ObjectUtil.convert(pageIndex, Integer.class));
                }

                if (pageSize != null) {
                    paging.setPageSize(ObjectUtil.convert(pageSize, Integer.class));
                }

                resultList = simpleDao.findByQueryObj(queryDto, paging);

            }


            final Object pagingDataRef = pagingData;

            Optional.ofNullable(pageIndex).ifPresent(index -> setValueByPageOption(pagingDataRef, PageOption.Type.PageIndex, true, field -> index));

            Optional.ofNullable(pageSize).ifPresent(size -> setValueByPageOption(pagingDataRef, PageOption.Type.PageSize, true, field -> size));

            //设置结果集
            final Object resultListRef = resultList;

            setValueByPageOption(pagingData, PageOption.Type.RequireResultList, false, field -> resultListRef);

        }

        return (T) pagingData;
    }


    private static void setValueByPageOption(Object target, Object key, boolean allowFieldNotExist, Function<Field, Object> fun) {

        Field field = getPageOptionFields(target.getClass()).get(key);

        if (field == null
                && allowFieldNotExist) {
            return;
        }

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
