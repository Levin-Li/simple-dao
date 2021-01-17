package com.levin.commons.dao;

import java.lang.annotation.*;

/**
 * 实体可选项
 * <p>
 * 用于标注实体类
 *
 * @author levin li
 * @since 2.2.11
 */


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Deprecated
public @interface EntityOption {

    /**
     *
     */
    enum AccessLevel {
        /**
         * 不可读
         */
        Unreadable,

        /**
         * 可读
         */
        Readable,

        /**
         * 可创建
         */
        Creatable,

        /**
         * 可修改
         */
        Writeable,

        /**
         * 可逻辑删除
         * <p>
         * 通过标记某个字段为特定值表示删除
         */
        LogicalDeletable,

        /**
         * 可物理删除
         */
        Deletable;


        /**
         * 是否可读
         *
         * @param accessLevels
         * @return
         */
        boolean canRead(AccessLevel... accessLevels) {
            return canDo(Readable.ordinal(), accessLevels);
        }

        /**
         * 是否可写
         *
         * @param accessLevels
         * @return
         */
        boolean canWrite(AccessLevel... accessLevels) {
            return canDo(Writeable.ordinal(), accessLevels);
        }

        /**
         * 是否可创建写
         *
         * @param accessLevels
         * @return
         */
        boolean canCreate(AccessLevel... accessLevels) {
            return canDo(Creatable.ordinal(), accessLevels);
        }

        /**
         * 是否可逻辑删除
         *
         * @param accessLevels
         * @return
         */
        boolean canLogicalDelete(AccessLevel... accessLevels) {
            return canDo(LogicalDeletable.ordinal(), accessLevels);
        }

        /**
         * 是否可物理删除
         *
         * @param accessLevels
         * @return
         */
        boolean canDelete(AccessLevel... accessLevels) {
            return canDo(Deletable.ordinal(), accessLevels);
        }

        /**
         * 是否可物理删除
         *
         * @param accessLevels
         * @return
         */
        private boolean canDo(int level, AccessLevel... accessLevels) {

            if (accessLevels == null) {
                return false;
            }

            for (AccessLevel accessLevel : accessLevels) {
                if (accessLevel.ordinal() == level) {
                    return true;
                }
            }

            return false;
        }

    }

    /**
     * 可访问的级别
     *
     * @return
     */
    AccessLevel[] accessLevels();

    /**
     * 逻辑删除的字段
     *
     * @return
     */
    String logicalDeleteField() default "";

    /**
     * 逻辑删除的值
     *
     * @return
     */
    String logicalDeleteValue() default "";

}
