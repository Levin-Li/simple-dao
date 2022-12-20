package com.levin.commons.dao.codegen.db.util;

import org.springframework.util.StringUtils;

import java.util.Objects;

public class FieldUtil {

    public static final String UNDER_LINE = "_";

    /**
     * 下划线字段转驼峰 <br/>
     *
     * <pre>
     * user_age -> userAge
     * user_address_detail -> userAddressDetail
     * user__age -> userAge
     * name -> name
     * _name -> _name
     * _name_ -> _name_
     * _user_age -> _userAge
     * _user__age -> _userAge
     * user_age_ -> userAge_
     * _user_age_ -> _userAge_
     * name__ -> name__
     * __name -> __name
     * </pre>
     *
     *
     * @param field 字段
     * @return 返回转换后的字段
     */
    public static String underlineFilter(String field) {
        if (StringUtils.hasText(field)) {
            char underLine = '_';
            int underLineCountLeading = findCharCount(field, underLine, false);
            int underLineCountTailing = findCharCount(field, underLine, true);
            // 去除首尾'_'
            field = StringUtils.trimLeadingCharacter(field, underLine);
            field = StringUtils.trimTrailingCharacter(field, underLine);
            if (isSingleAllUpper(field)) {
                return field.toLowerCase();
            }
            if (field.contains(UNDER_LINE)) {
                field = field.toLowerCase();
            }
            String[] arr = field.split("_+");
            return join(arr, underLineCountLeading, underLineCountTailing);
        }
        return "";
    }

    /**
     * 是不是全部大写的单词，如：NAME, ADDRESS
     * @param name 单词
     * @return true：是
     */
    private static boolean isSingleAllUpper(String name) {
        if (name.contains(UNDER_LINE)) {
            return false;
        }
        return Objects.equals(name, name.toUpperCase());
    }

    private static String join(String[] arr, int underLineCountLeading, int underLineCountTailing) {
        if (arr.length > 1) {
            for (int i = 1; i < arr.length; i++) {
                arr[i] = upperFirstLetter(arr[i]);
            }
        }
        StringBuilder ret = new StringBuilder();
        char underLine = '_';
        for (int i = 0; i < underLineCountLeading; i++) {
            ret.append(underLine);
        }
        ret.append(String.join("", arr));
        for (int i = 0; i < underLineCountTailing; i++) {
            ret.append(underLine);
        }
        return ret.toString();
    }

    private static int findCharCount(String str, char searchChar, boolean reverse) {
        if (StringUtils.isEmpty(str)) {
            return 0;
        }
        int count = 0;
        char[] chars = str.toCharArray();

        if (reverse) {
            for (int i = chars.length - 1; i >= 0; i--) {
                if (chars[i] == searchChar) {
                    count++;
                } else {
                    break;
                }
            }
        } else {
            for (char aChar : chars) {
                if (aChar == searchChar) {
                    count++;
                } else {
                    break;
                }
            }
        }
        return count;
    }

    public static String convertString(Object object) {
        if (object == null) {
            return "";
        }
        return String.valueOf(object);
    }

    /**
     * 过滤"."
     *
     * @param field 字段
     * @return 返回新字段
     */
    public static String dotFilter(String field) {
        if (StringUtils.hasText(field)) {
            if (field.contains(".")) {
                String[] words = field.split("\\.");
                StringBuilder ret = new StringBuilder();
                for (String str : words) {
                    ret.append(upperFirstLetter(str));
                }
                return ret.toString();
            }
        }
        return field;
    }

    /**
     * 将第一个字母转换成大写。 name -> Name
     *
     * @param str 字符串
     * @return 返回新字段
     */
    public static String upperFirstLetter(String str) {
        if (StringUtils.hasText(str)) {
            String firstUpper = str.substring(0, 1).toUpperCase();
            str = firstUpper + str.substring(1);
        }
        return str;
    }

    /**
     * 将第一个字母转换成小写。Name -> name
     *
     * @param str 字符串
     * @return 返回新内容
     */
    public static String lowerFirstLetter(String str) {
        if (StringUtils.hasText(str)) {
            String firstLower = str.substring(0, 1).toLowerCase();
            str = firstLower + str.substring(1);
        }
        return str;
    }


}
