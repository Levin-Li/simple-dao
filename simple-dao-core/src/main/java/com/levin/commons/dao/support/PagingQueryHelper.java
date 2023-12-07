package com.levin.commons.dao.support;

import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.Paging;
import com.levin.commons.dao.SelectDao;
import com.levin.commons.dao.SimpleDao;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.dao.util.ObjectUtil;
import com.levin.commons.dao.util.QueryAnnotationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 暂时不可用
 */
public abstract class PagingQueryHelper {

    private static final Map<String, Map<PageOption.Type, Field>> classFieldCached = new ConcurrentHashMap<>();

    private PagingQueryHelper() {
    }

    /**
     * 分页查询
     * <p>
     * 通过 PageOption 注解 实现分页大小、分页码，是否查询总数的参数的获取，查询成功后，也通过注解自动把查询结果注入到返回对象中。
     *
     * @param simpleDao  dao
     * @param pagingData 查询结果对象，可以是对象实例，也是可以是 Class 对象，对象的字段上有 PageOption {@link com.levin.commons.dao.PageOption}
     *                   为 Null，则会被默认为 PagingData {@link DefaultPagingData}
     * @param queryDto   查询 DTO
     * @param paging     如果 queryDto 本身也是 Paging对象，那么 paging参数将无效
     * @param <T>        查询结果
     * @return
     */
    @Deprecated
    public static <T> T findByPageOption(SimpleDao simpleDao, Class<?> resultType, Object pagingData, Object queryDto, @Nullable Paging paging, Object... otherQueryObjs) {
        return findPageByQueryObj(simpleDao, resultType, pagingData, queryDto, paging, otherQueryObjs);
    }

    public static <T> T findPageByQueryObj(SimpleDao simpleDao, Object pagingData, Object... queryObjs) {
        return findPageByQueryObj(simpleDao, (Class<?>) null, pagingData, queryObjs);
    }

    public static <T> T findPageByQueryObj(SimpleDao dao, Class<?> resultType, Object pagingData, Object... queryObjs) {
        return findPageByQueryObj(params -> dao.countByQueryObj(params),
                (type, params) -> dao.findByQueryObj(type, params)
                , resultType, pagingData, queryObjs);
    }

    /**
     * 分页查询
     * <p>
     * 通过 PageOption 注解 实现分页大小、分页码，是否查询总数的参数的获取，查询成功后，也通过注解自动把查询结果注入到返回对象中。
     *
     * @param dao        dao
     * @param pagingData 查询结果对象，可以是对象实例，也是可以是 Class 对象，对象的字段上有 PageOption {@link com.levin.commons.dao.PageOption}
     **/
    public static <T> T findPageByQueryObj(SelectDao dao, Class<?> resultType, Object pagingData, Object... queryObjs) {
        return findPageByQueryObj(
                (params) -> dao.count(),
                (type, params) -> dao.find(type)
                , resultType, pagingData, queryObjs);
    }

    /**
     * 分页查询
     * <p>
     * 通过 PageOption 注解 实现分页大小、分页码，是否查询总数的参数的获取，查询成功后，也通过注解自动把查询结果注入到返回对象中。
     *
     * @param pagingData 查询结果对象，可以是对象实例，也是可以是 Class 对象，对象的字段上有 PageOption {@link com.levin.commons.dao.PageOption}
     *                   为 Null，则会被默认为 PagingData {@link DefaultPagingData}
     * @param queryObjs  查询 DTO
     * @param <T>        查询结果
     * @return
     */
    public static <T> T findPageByQueryObj(Function<Object[], Long> countFunc, BiFunction<Class<?>, Object[], Object> queryFunc, Class<?> resultType, Object pagingData, Object... queryObjs) {

        List flattenQueryObjs = QueryAnnotationUtil.flattenParams(new LinkedList(), queryObjs);

        Paging paging = (Paging) flattenQueryObjs.stream().filter(o -> o instanceof Paging).findFirst().orElse(null);

        //如果没有分页对象
        if (paging == null) {

            final SimplePaging simplePaging = new SimplePaging();

            flattenQueryObjs.stream()
                    .filter(Objects::nonNull)
                    //忽略消费着
                    .filter(o -> !(o instanceof Consumer))
                    .forEachOrdered(queryObj -> {

                        //试图获取分页参数

                        Map<PageOption.Type, Field> pageOptionFields = getPageOptionFields(queryObj);

                        Field indexField = pageOptionFields.get(PageOption.Type.PageIndex);
                        Field sizeField = pageOptionFields.get(PageOption.Type.PageSize);

                        Object pageIndex = indexField != null ? ReflectionUtils.getField(indexField, queryObj) : null;

                        Object pageSize = sizeField != null ? ReflectionUtils.getField(sizeField, queryObj) : null;

                        if (pageIndex != null) {
                            simplePaging.setPageIndex(ObjectUtil.convert(pageIndex, Integer.class));
                        }

                        if (pageSize != null) {
                            simplePaging.setPageSize(ObjectUtil.convert(pageSize, Integer.class));
                        }
                    });

            //分页
            flattenQueryObjs.add(simplePaging);

            paging = simplePaging;

        }

//        if (paging == null) {
//            throw new IllegalArgumentException("查询对象中没有分页对象，分页查询无法继续");
//        }

        if (pagingData == null) {
            pagingData = DefaultPagingData.class;
        }

        if (pagingData instanceof Class) {
            pagingData = BeanUtils.instantiateClass((Class<T>) pagingData);
        }

        //需要总记录数
        if (isRequireRecordTotals(paging)) {
            setValueByPageOption(pagingData, PageOption.Type.RequireTotals, false, (field) -> countFunc.apply(queryObjs));
        }

        //需要结果集
        if (isRequireResultList(paging)) {

            int index = paging.getPageIndex();
            setValueByPageOption(pagingData, PageOption.Type.PageIndex, true, field -> index);

            int size = paging.getPageSize();
            setValueByPageOption(pagingData, PageOption.Type.PageSize, true, field -> size);

            //查询并且设置查询结果
            setValueByPageOption(pagingData, PageOption.Type.RequireResultList, false, field -> queryFunc.apply(resultType, queryObjs));

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

        if (dto == null) {
            return false;
        }

        if (dto instanceof Paging) {
            return ((Paging) dto).isRequireTotals();
        }

        Field field = getPageOptionFields(dto.getClass()).get(PageOption.Type.RequireTotals);
        return field != null && isEnable(dto, field);
    }

    private static boolean isRequireResultList(Object dto) {

        if (dto == null) {
            return false;
        }

        if (dto instanceof Paging) {
            return ((Paging) dto).isRequireResultList();
        }

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

        return ExprUtils.evalSpEL(queryDto, expr, null);
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
                .forEachOrdered(aClass -> resultMap.putAll(getPageOptionFields(aClass)));

        return resultMap;
    }

    /**
     * @param type
     * @return
     */
    private static Map<PageOption.Type, Field> getPageOptionFields(Class<?> type) {

        return classFieldCached.computeIfAbsent(type.getName(), key -> {

            Map<PageOption.Type, Field> fieldMap = new LinkedHashMap<>();

            ReflectionUtils.doWithFields(type, field -> {

                field.setAccessible(true);

                PageOption option = field.getAnnotation(PageOption.class);
                if (option != null) {
                    fieldMap.put(option.value(), field);
                }
            });

            //放回不可变的结果接
            return Collections.unmodifiableMap(fieldMap);

        });

    }

}
