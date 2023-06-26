package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.NamedEntityObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Data
@Accessors(chain = true)
@Schema(title = "命名对象")
@FieldNameConstants
//@Table(indexes = {
//        @Index(columnList = AbstractNamedEntityObject.Fields.name),
//})
public abstract class AbstractNamedEntityObject
        extends AbstractBaseEntityObject
        implements NamedEntityObject {

    private static final long serialVersionUID = -123456789L;

    @Schema(title = "名称")
    @Column(nullable = false, length = 128)
    @Contains
    protected String name;

    @Schema(title = "拼音名", description = "简拼或全拼，逗号隔开")
    @Column(length = 128)
    @Contains
    protected String pinyinName;

    @Override
    public String toString() {
        return name;
    }

}
