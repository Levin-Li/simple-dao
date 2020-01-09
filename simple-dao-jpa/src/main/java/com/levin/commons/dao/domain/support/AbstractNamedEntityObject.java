package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.NamedEntityObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@Data
@Accessors(chain = true)
@Desc("命名对象")
public abstract class AbstractNamedEntityObject<ID extends Serializable>
        extends AbstractBaseEntityObject<ID>
        implements NamedEntityObject<ID> {

    private static final long serialVersionUID = -123456789L;

    @Desc("名称")
    @Column(name = "name", nullable = false)
    protected String name;


    @Override
    public String toString() {
        return name;
    }

}
