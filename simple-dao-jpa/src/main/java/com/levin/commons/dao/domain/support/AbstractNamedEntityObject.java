package com.levin.commons.dao.domain.support;

import com.levin.commons.annotation.GenNameConstant;
import com.levin.commons.dao.domain.NamedEntityObject;
import com.levin.commons.service.domain.Desc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import java.io.Serializable;

@MappedSuperclass
@Data
@Accessors(chain = true)
@Desc("命名对象")
@FieldNameConstants
//@Table(indexes = {
//        @Index(columnList = AbstractNamedEntityObject.Fields.name),
//})
public abstract class AbstractNamedEntityObject<ID extends Serializable>
        extends AbstractBaseEntityObject<ID>
        implements NamedEntityObject<ID> {

    private static final long serialVersionUID = -123456789L;

    @Schema(description = "名称")
    @Column(nullable = false, length = 512)
    protected String name;


    @Override
    public String toString() {
        return name;
    }

}
