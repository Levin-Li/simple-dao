package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.NamedEntityObject;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.List;

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

    @Schema(title = "拼音名称",description = "拼音，格式Json数组：[全拼,简拼]")
    @Column(length = 128)
    @InjectVar(domain = "dao", expectBaseType = List.class, expectGenericTypes = {String.class}, converter = PrimitiveArrayJsonConverter.class, isRequired = "false")
    protected String pinyinName;

//    查询对象的定义方式
//    @Schema(description = "拼音列表")
//    @OR(autoClose = true)
//    @InjectVar(domain = "dao", converter = JsonStrLikeConverter.class)
//    @Contains
//    private List<String> pinyinNameList;


    @Override
    public String toString() {
        return name;
    }


}
