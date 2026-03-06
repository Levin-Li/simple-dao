package com.levin.commons.dao.support;

import com.levin.commons.service.domain.EnumDesc;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;

import java.sql.Types;

/**
 * 枚举转换 - Hibernate 6 兼容版本
 */
public class EnumDescType extends AbstractSingleColumnStandardBasicType<Enum<?>> {

    private static final long serialVersionUID = 1L;

    private final Class<? extends Enum> enumClass;

    public EnumDescType(Class<? extends Enum> enumClass) {
        super(
                new EnumJdbcTypeDescriptor(enumClass),
                new MyEnumJavaTypeDescriptor<>(enumClass)
        );
        this.enumClass = enumClass;
    }

    @Override
    public Class<Enum<?>> getJavaType() {
        return (Class<Enum<?>>) enumClass;
    }

    @Override
    public String getName() {
        return enumClass.getName();
    }

    static class MyEnumJavaTypeDescriptor<T extends Enum> extends EnumJavaTypeDescriptor<T> {

        private final Class<T> enumClass;

        public MyEnumJavaTypeDescriptor(Class<T> type) {
            super(type);
            this.enumClass = type;
        }

        @Override
        public Class<T> getJavaType() {
            return enumClass;
        }

        @Override
        public <X> X unwrap(T value, Class<X> type, java.util.function.Supplier<X> supplier) {
            if (value == null) {
                return null;
            }

            if (EnumDesc.class.isAssignableFrom(enumClass) && value instanceof EnumDesc) {
                EnumDesc enumDesc = (EnumDesc) value;

                if (String.class.isAssignableFrom(type)) {
                    return (X) enumDesc.name();
                }
                if (Integer.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type)) {
                    return (X) Integer.valueOf(enumDesc.code());
                }
                if (int.class.isAssignableFrom(type)) {
                    return (X) Integer.valueOf(enumDesc.code());
                }
            }

            return super.unwrap(value, type, supplier);
        }

        @Override
        public <X> T wrap(X value, java.util.function.Supplier<T> supplier) {
            if (value == null) {
                return null;
            }

            if (EnumDesc.class.isAssignableFrom(enumClass)) {
                if (value instanceof Number) {
                    return (T) EnumDesc.parse(enumClass, ((Number) value).intValue());
                }
                if (value instanceof String) {
                    return (T) EnumDesc.parse(enumClass, (String) value);
                }
            }

            return super.wrap(value, supplier);
        }
    }
}
