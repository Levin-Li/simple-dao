package com.levin.commons.dao.util;


import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.Iterator;

public abstract class CollectionHelper {


    /**
     * 集合数据toString 工具类
     *
     * @param dataSet   数组或是集合对象
     * @param buf
     * @param delimiter
     * @return
     */
    public static StringBuilder toString(Object dataSet, StringBuilder buf, String delimiter) {
        return toString(dataSet, buf, delimiter, true, true, true, null, null);
    }


    /**
     * 集合数据toString 工具类
     *
     * @param dataSet
     * @param buf
     * @param delimiter
     * @param allowEmptyOrNull
     * @param isIgnorePrefixAndSuffixIfDataSetOnlyOneElement
     * @param isIgnorePrefixAndSuffixIfNoContent
     * @param prefix
     * @param suffix
     * @return
     */
    public static StringBuilder toString(Object dataSet, StringBuilder buf, String delimiter, boolean allowEmptyOrNull
            , boolean isIgnorePrefixAndSuffixIfDataSetOnlyOneElement, boolean isIgnorePrefixAndSuffixIfNoContent
            , String prefix, String suffix) {

        if (buf == null) {
            buf = new StringBuilder();
        }

        int eleCount = 0;

        if (dataSet == null) {
            //如果没有数据，不加入任何东西
        } else if (dataSet instanceof Iterable) {

            Iterator iterator = ((Iterable) dataSet).iterator();

            while (iterator.hasNext()) {
                appendData(buf, delimiter, allowEmptyOrNull, eleCount == 0, iterator.next());
                eleCount++;
            }
        } else if (dataSet.getClass().isArray()) {

            eleCount = Array.getLength(dataSet);

            for (int i = 0; i < eleCount; i++) {
                appendData(buf, delimiter, allowEmptyOrNull, i == 0, Array.get(dataSet, i));
            }
        }

        boolean isIgnore = (isIgnorePrefixAndSuffixIfDataSetOnlyOneElement && eleCount <= 1)
                || (isIgnorePrefixAndSuffixIfNoContent && buf.length() == 0);

        //增加前缀
        if (prefix != null && !isIgnore) {
            buf.insert(0, prefix);
        }

        //增加后缀
        if (suffix != null && !isIgnore) {
            buf.append(suffix);
        }

        return buf;
    }

    private static void appendData(StringBuilder buf, String delimiter, boolean allowEmptyOrNull, boolean isFirstElement, Object data) {

        if (!allowEmptyOrNull && data == null)
            return;

        String txt = "" + data;

        if (allowEmptyOrNull || StringUtils.hasText(txt)) {

            if (!isFirstElement
                    && delimiter != null
                    && (allowEmptyOrNull || buf.length() > 0)) {
                buf.append(delimiter);
            }

            buf.append(txt);
        }
    }

}
