package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * @author lilw
 */ //1、lobmok get set
@Data

//2、必须注解主键字段
//@EqualsAndHashCode(of = {"id"})

//3、必须使用链式设置
@Accessors(chain = true)

//4、必须生成常量字段
@FieldNameConstants

//5、必须注解业务名称
@Schema(title = "平台领域")

@MappedSuperclass
public abstract class AbstractPlatformDomain implements BaseObject, ExpiredObject {

    @Schema(title = "领域名称")
    @Column(length = 512, nullable = false)
    @Contains
    protected String name;

    @Schema(title = "创建时间")
    @Column(nullable = false)
    protected LocalDateTime createTime;

    @Schema(title = "到期时间", description = "")
    protected LocalDateTime expiredTime;

    @Schema(title = "排序代码")
    protected Integer orderCode;

    @Schema(title = "是否启用")
    @Column(nullable = false)
    protected Boolean enable;

    @Schema(title = "是否可编辑")
    @Column(nullable = false)
    protected Boolean editable;

    @Schema(title = "备注")
    @Column(length = 512)
    protected String remark;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    @Override
    @Transient
    public boolean isEnable() {
        return enable == null || enable;
    }

    @Override
    @Transient
    public boolean isEditable() {
        return editable == null || editable;
    }

}
