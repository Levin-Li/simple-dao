package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.BaseEntityObject;
import com.levin.commons.service.domain.Desc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class AbstractBaseEntityObject<ID extends Serializable>
        implements BaseEntityObject<ID> {

    private static final long serialVersionUID = -123456789L;


    @Schema(description = "排序代码")
    protected Integer orderCode;

    @Schema(description = "是否允许")
    @Column(nullable = false)
    protected Boolean enable = true;

    @Schema(description = "是否可编辑")
    @Column(nullable = false)
    protected Boolean editable = true;

    @Schema(description = "创建时间")
    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date createTime;

    @Schema(description = "更新时间")
    @Column
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdateTime;

    @Schema(description = "备注")
    @Column(length = 1000)
    protected String remark;


    @PrePersist
    public void prePersist() {

        if (createTime == null) {
            createTime = new Date();
        }

        if (lastUpdateTime == null) {
            lastUpdateTime = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (lastUpdateTime == null) {
            lastUpdateTime = new Date();
        }
    }

    @Override
    @Transient
    public boolean isEditable() {
        return Boolean.TRUE.equals(editable);
    }

    @Override
    @Transient
    public boolean isEnable() {
        return Boolean.TRUE.equals(enable);
    }

}
