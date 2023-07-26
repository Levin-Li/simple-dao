package com.levin.commons.dao;


import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 查询结果类
 */
@FunctionalInterface
public interface EntityClassSupplier extends Supplier<Class<?>> {

    /**
     * 获取别名
     *
     * @return
     */
    default String getAlias() {
        return getAlias(get());
    }

    /**
     * 获取别名
     *
     * @param entityClass
     * @return
     */
    static String getAlias(Class<?> entityClass) {

        if (entityClass == null) {
            return null;
        }

        final String simpleName = entityClass.getSimpleName();

        String alias = simpleName.chars()
                .filter(c -> Character.isUpperCase((char) c))
                .mapToObj(c -> "" + (char) c)
                .collect(Collectors.joining("_"))
                .toLowerCase();

        if (alias.length() < 2) {
            alias = "_" + simpleName.toLowerCase();
        }

        return alias;
    }
}
