package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.NamedEntityObject;
import com.levin.commons.service.domain.Desc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Data
@Accessors(chain = true)
@Desc("命名对象")
@FieldNameConstants
//@Table(indexes = {
//        @Index(columnList = AbstractNamedEntityObject.Fields.name),
//})
public abstract class AbstractNamedEntityObject
        extends AbstractBaseEntityObject
        implements NamedEntityObject {

    private static final long serialVersionUID = -123456789L;

    @Schema(description = "名称")
    @Column(nullable = false, length = 128)
    @Contains
    protected String name;

    @Schema(description = "拼音，格式：全拼(简拼)")
    @Column(length = 128)
    @Contains
    protected String pinyinName;

    @Override
    public String toString() {
        return name;
    }


}
