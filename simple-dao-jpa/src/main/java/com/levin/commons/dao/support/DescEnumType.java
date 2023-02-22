package com.levin.commons.dao.support;

import com.levin.commons.service.domain.EnumDesc;
import org.hibernate.metamodel.model.convert.internal.OrdinalEnumValueConverter;
import org.hibernate.type.EnumType;
import org.hibernate.type.descriptor.java.EnumJavaTypeDescriptor;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 *
 */
public class DescEnumType extends EnumType {

    static Field field;

    static {
        try {
            field = EnumType.class.getField("enumValueConverter");
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
        public <E extends Enum> E fromOrdinal(Integer relationalForm) {

            Class<T> javaType = getJavaType();

            if (EnumDesc.class.isAssignableFrom(javaType)) {
                return (E) EnumDesc.parse(javaType, relationalForm);
            }

            return super.fromOrdinal(relationalForm);

        }
    }
}
