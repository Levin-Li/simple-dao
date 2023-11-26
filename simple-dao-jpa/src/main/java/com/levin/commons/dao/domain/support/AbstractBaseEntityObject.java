package com.levin.commons.dao.domain.support;

import com.levin.commons.annotation.GenNameConstant;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.BaseEntityObject;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;

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
//@EntityListeners()
public abstract class AbstractBaseEntityObject
        implements BaseEntityObject {

    private static final long serialVersionUID = -123456789L;

    @Schema(title = "创建者")
    @Column(length = 128)
    @InjectVar(value = InjectConst.USER_ID, isRequired = "false")
    protected String creator;

    @Schema(title = "创建时间")
    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
//    @CreatedDate
    protected Date createTime;

    @Schema(title = "更新时间")
    @Column
    @Temporal(value = TemporalType.TIMESTAMP)
//    @LastModifiedDate
    protected Date lastUpdateTime;

    //@OrderBy
    @Schema(title = "排序代码")
    protected Integer orderCode;

    @Schema(title = "是否允许")
    @Column(nullable = false)
    protected Boolean enable;

    @Schema(title = "是否可编辑")
    @Column(nullable = false)
    protected Boolean editable;

    @Schema(title = "备注")
    @Column(length = 512)
    @Contains
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
