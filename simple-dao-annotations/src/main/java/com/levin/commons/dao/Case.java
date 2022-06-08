package com.levin.commons.dao;

import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL case
 * <p>
 * 1、简单 CASE
 * 2、Case 搜索函数
 * <p>
 * <p>
 * 简单 CASE 如下：
 * CASE sex
 * WHEN '1' THEN '男'
 * ELSE '其他'
 * END
 * <p>
 * 统计，选择常用
 *
 * @author llw
 */
@Data
@Accessors(chain = true, fluent = true)
@NoArgsConstructor
//@Builder
@FieldNameConstants

@PrimitiveValue(isExpr = true)
public class Case implements Serializable {

    private final Map<String, String> whenList = new LinkedHashMap<>();

//    @Data
//    @Accessors(chain = true)
//    @FieldNameConstants
//    public class When implements Serializable {
//
//        String when;
//
//        String then;
//
//        @Override
//        public String toString() {
//            return " WHEN " + when + " THEN " + when;
//        }
//    }


    /**
     * 字段名/列名
     *
     * @return
     */
    String column;

    /**
     * @return
     */
    public Case when(String when, String then) {

        whenList.put(when, then);

        return this;
    }

    /**
     * else 表达式
     *
     * @return
     */
    String elseExpr;

    @Override
    public String toString() {

        StringBuilder ql = new StringBuilder();

        ql.append(" CASE ");

        ql.append(nullSafe(column));

        whenList.forEach((k, v) -> ql.append(" WHEN ").append(k).append(" THEN ").append(v));

        if (hasTxt(elseExpr)) {
            ql.append(" ELSE ").append(elseExpr);
        }

        ql.append(" END ");

        return ql.toString();

    }

    private static String nullSafe(String value) {
        return hasTxt(value) ? value : "";
    }

    private static boolean hasTxt(String value) {
        return value != null && value.trim().length() > 0;
    }

}
