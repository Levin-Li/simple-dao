package com.levin.commons.dao;

import java.lang.annotation.*;

/** DAO 运行时逻辑组定义。 */
@Repeatable(DaoLogicGroup.List.class)
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DaoLogicGroup {

    enum LogicOp { AND, OR }

    String name();

    /** 父组名称；空字符串表示没有父组。 */
    String parent() default "";

    LogicOp logicOp();

    String condition() default "";

    /**
     * 当前组要求的成立直接子元素数量。
     * 一个直接条件节点或局部逻辑子树算一个；子组只要生成有效条件
     * （包括 unless 兜底表达式）就算一个。被忽略或未生成条件的子组不计数。
     * IN 绑定多个值仍算一个。
     */
    int minConditions() default 0;

    /** 组已启用但成立的直接子元素不足时使用的直接条件表达式。 */
    String unless() default "";

    String remark() default "";

    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @interface List {
        DaoLogicGroup[] value();
    }
}
