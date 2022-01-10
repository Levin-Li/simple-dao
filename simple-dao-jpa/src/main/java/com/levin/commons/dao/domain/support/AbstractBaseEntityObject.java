package com.levin.commons.dao.domain.support;

import com.levin.commons.annotation.GenNameConstant;
import com.levin.commons.dao.domain.BaseEntityObject;
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
//@Table(indexes = {
//        @Index(columnList = AbstractBaseEntityObject.Fields.creator),
//        @Index(columnList = AbstractBaseEntityObject.Fields.createTime),
//        @Index(columnList = AbstractBaseEntityObject.Fields.lastUpdateTime),
//        @Index(columnList = AbstractBaseEntityObject.Fields.enable),
//        @Index(columnList = AbstractBaseEntityObject.Fields.orderCode),
//})
public abstract class AbstractBaseEntityObject
        implements BaseEntityObject {

    private static final long serialVersionUID = -123456789L;

    @Schema(description = "创建者")
    @Column(length = 128)
    protected String creator;

    @Schema(description = "创建时间")
    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date createTime;

    @Schema(description = "更新时间")
    @Column
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdateTime;

    //@OrderBy
    @Schema(description = "排序代码")
    protected Integer orderCode;

    @Schema(description = "是否允许")
    @Column(nullable = false)
    protected Boolean enable;

    @Schema(description = "是否可编辑")
    @Column(nullable = false)
    protected Boolean editable;

    @Schema(description = "备注")
    @Column(length = 512)
    protected String remark;

    @PrePersist
    public void prePersist() {

        if (createTime == null) {
            createTime = new Date();
        }

        if (orderCode == null) {
            orderCode = 100;
        }

        if (editable == null) {
            editable = true;
        }

        if (enable == null) {
            enable = true;
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
