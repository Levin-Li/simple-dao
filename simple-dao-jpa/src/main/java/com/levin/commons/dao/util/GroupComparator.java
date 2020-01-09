package com.levin.commons.dao.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 *
 *
 *
 * @param <T>
 */
public class GroupComparator<T> implements Comparator<T> {

    public interface OrderFieldCallback<T> {
        Comparable[] getOrderFields(T data);
    }

    public abstract static class DefaultOrderFieldCallback<T> implements OrderFieldCallback<T> {

        @Override
        public final Comparable[] getOrderFields(T data) {
            ArrayList<Comparable> fields = new ArrayList<>();

            this.getOrderFields(data, fields);

            return fields.toArray(new Comparable[0]);
        }

        protected abstract void getOrderFields(T data, ArrayList<Comparable> fields);


    }

    public static class NullValueComparator
            implements Comparator<Comparable> {

        final boolean nullValueIsSmall;

        public NullValueComparator(boolean nullValueIsSmall) {
            this.nullValueIsSmall = nullValueIsSmall;
        }

        @Override
        public int compare(Comparable o1, Comparable o2) {

            if (o1 == null
                    && o2 == null)
                return 0;

            if (o1 == null)
                return nullValueIsSmall ? -1 : 1;

            if (o2 == null)
                return nullValueIsSmall ? 1 : -1;

            return o1.compareTo(o2);

        }
    }

    ///////////////////////////////////////////////////////////////

    OrderFieldCallback<T> orderFieldCallback;

    //允许空值比价
    NullValueComparator nullValueComparator;

    private GroupComparator() {
    }

    public GroupComparator<T> supportNullValue(boolean nullValueIsSmall) {
        nullValueComparator = new NullValueComparator(nullValueIsSmall);
        return this;
    }

    public static <T> GroupComparator<T> build(OrderFieldCallback<T> orderFieldCallback) {

        GroupComparator<T> groupComparator = new GroupComparator<>();

        groupComparator.orderFieldCallback = orderFieldCallback;

        return groupComparator;

    }


    public static <T> void sort(T[] data, OrderFieldCallback<T> orderFieldCallback, boolean nullValueIsSmall) {
        Arrays.sort(data, GroupComparator.build(orderFieldCallback).supportNullValue(nullValueIsSmall));
    }

    public static <T> void sort(List<T> data, OrderFieldCallback<T> orderFieldCallback, boolean nullValueIsSmall) {

        Object[] objects = data.toArray();

        GroupComparator groupComparator = GroupComparator.build(orderFieldCallback)
                .supportNullValue(nullValueIsSmall);

        Arrays.sort(objects, groupComparator);

        data.clear();

        for (Object o : objects) {
            data.add((T) o);
        }
    }

    @Override
    public int compare(T t1, T t2) {

        Comparable[] o1Group = orderFieldCallback.getOrderFields(t1);
        Comparable[] o2Group = orderFieldCallback.getOrderFields(t2);

        int i = 0;

        for (Comparable o1 : o1Group) {

            Comparable o2 = o2Group[i++];

            int r = nullValueComparator != null ? nullValueComparator.compare(o1, o2) : o1.compareTo(o2);

            if (r != 0)
                return r;
        }

        return 0;
    }
}


