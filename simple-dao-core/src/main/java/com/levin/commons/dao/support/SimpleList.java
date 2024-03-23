package com.levin.commons.dao.support;


import com.levin.commons.dao.util.CollectionHelper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 不重复的Collection
 * <p/>
 *
 * @param <E>
 */
public final class SimpleList<E> {

    private List<E> delegate;

    private String delimiter;

    private String prefix;
    private String suffix;

    private boolean canRepeat = true;

    private boolean nullable = false;

    public SimpleList(String delimiter) {
        this(true, null, delimiter);
    }

    public SimpleList(boolean canRepeat, List<E> delegate, String delimiter) {
        this(canRepeat, delegate, delimiter, null, null);
    }

    public SimpleList(boolean canRepeat, List<E> delegate, String delimiter, String prefix, String suffix) {

        this.canRepeat = canRepeat;

        this.delegate = delegate;

        if (this.delegate == null)
            this.delegate = new ArrayList<>(5);

        this.delimiter = delimiter;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public boolean isCanRepeat() {
        return canRepeat;
    }

    public SimpleList<E> setCanRepeat(boolean canRepeat) {
        this.canRepeat = canRepeat;
        return this;
    }

//    public boolean isNullable() {
//        return nullable;
//    }
//
//    public SimpleList<E> setNullable(boolean nullable) {
//        this.nullable = nullable;
//        return this;
//    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * 是否加入成功
     *
     * @param e 不允许空值或是空字符串
     * @return
     */
    public boolean add(E e) {

        //不允许空值或是空字符串，包括全是空格的字符串
        if (!nullable
                && (e == null || (e instanceof CharSequence && !StringUtils.hasText((CharSequence) e))))
            return false;

        if (canRepeat)
            return delegate.add(e);

        //如果是不能重复，并且又是字符串，考虑截取
        if (e instanceof CharSequence) {
            e = (E) StringUtils.trimWhitespace(e.toString());
        }

        if (!delegate.contains(e))
            return delegate.add(e);

        return false;
    }

    public SimpleList<E> clear() {

        delegate.clear();

        return this;
    }

    /**
     * 返回代理
     * <p/>
     * 不安全的方法
     *
     * @return
     */
    public List<E> getList() {
        return delegate;
    }

    public int size() {
        return delegate.size();
    }

    public int length() {
        return delegate.size();
    }

    public boolean isNotEmpty() {
        return !delegate.isEmpty();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public String toString() {
        return toString(null, delimiter, prefix, suffix).toString();
    }

    public StringBuilder toString(StringBuilder buf, String delimiter, String prefix, String suffix) {
        return CollectionHelper.toString(delegate, buf, delimiter, false, true, true, prefix, suffix);
    }

}
