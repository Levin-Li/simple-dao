package com.levin.commons.dao.support;


import com.levin.commons.service.domain.EnumDesc;
import org.hibernate.type.descriptor.java.EnumJavaType;

/**
 * Hibernate 7 custom type for EnumDesc enums.
 *
 * @author lilw
 */
public class EnumDescType<T extends Enum<T>> extends EnumJavaType<T> {

    public EnumDescType(Class<T> type) {
        super(type);
    }

    @Override
    public T fromString(CharSequence str) {
        return fromName(str == null ? null : str.toString());
    }

    @Override
    public T fromName(String relationalForm) {
        return EnumDesc.parse(getJavaTypeClass(), relationalForm);
    }
}
