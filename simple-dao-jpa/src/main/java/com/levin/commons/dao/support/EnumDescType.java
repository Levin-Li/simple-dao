package com.levin.commons.dao.support;

import com.levin.commons.service.domain.EnumDesc;
import org.hibernate.metamodel.model.convert.internal.OrdinalEnumValueConverter;
import org.hibernate.type.EnumType;
import org.hibernate.type.descriptor.java.EnumJavaTypeDescriptor;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * 枚举装换
 */
public class EnumDescType extends EnumType {

    static Field field;

    static {
        try {
            field = EnumType.class.getDeclaredField("enumValueConverter");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setParameterValues(Properties parameters) {

        super.setParameterValues(parameters);

        Class<? extends Enum> enumClass = returnedClass();

        if (EnumDesc.class.isAssignableFrom(enumClass)) {
            try {
                field.set(this, new OrdinalEnumValueConverter(new MyEnumJavaTypeDescriptor(enumClass)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    static class MyEnumJavaTypeDescriptor<T extends Enum> extends EnumJavaTypeDescriptor<T> {
        public MyEnumJavaTypeDescriptor(Class<T> type) {
            super(type);
        }

        @Override
        public <E extends Enum> Integer toOrdinal(E domainForm) {

            if (domainForm instanceof EnumDesc) {
                return ((EnumDesc) domainForm).code();
            }

            return super.toOrdinal(domainForm);
        }

        @Override
        public String toName(T domainForm) {
            return super.toName(domainForm);
        }

        @Override
        public <E extends Enum> E fromOrdinal(Integer relationalForm) {
            return (E) EnumDesc.parse(getJavaType(), relationalForm);
        }

        @Override
        public T fromName(String relationalForm) {
            return EnumDesc.parse(getJavaType(), relationalForm);
        }

        @Override
        public T fromString(String relationalForm) {
            return EnumDesc.parse(getJavaType(), relationalForm);
        }

    }
}
