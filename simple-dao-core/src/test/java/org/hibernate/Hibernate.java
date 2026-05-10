package org.hibernate;

import java.lang.reflect.Field;

public abstract class Hibernate {

    public static boolean isInitialized(Object proxy) {
        if (proxy == null) {
            return true;
        }

        try {
            Field field = proxy.getClass().getDeclaredField("initialized");
            field.setAccessible(true);
            return Boolean.TRUE.equals(field.get(proxy));
        } catch (NoSuchFieldException e) {
            return true;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
