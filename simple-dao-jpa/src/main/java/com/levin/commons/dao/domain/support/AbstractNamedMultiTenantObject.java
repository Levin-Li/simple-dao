package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.NamedEntityObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

//1、lobmok get set
@Data

//2、必须注解主键字段
//@EqualsAndHashCode(of = {"id"})

//3、必须使用链式设置
@Accessors(chain = true)

//4、必须生成常量字段
@FieldNameConstants

//5、必须注解业务名称
@Schema(title = "抽象租户实体")

@MappedSuperclass
public abstract class AbstractNamedMultiTenantObject
        extends AbstractMultiTenantObject
        implements NamedEntityObject {

    @Schema(title = "名称")
    @Column(nullable = false, length = 128)
    @Contains
    protected String name;

}
