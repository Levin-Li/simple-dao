package com.levin.commons.dao.domain.support;

import com.levin.commons.annotation.GenNameConstant;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.BaseEntityObject;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.domain.ServiceReq;
import com.levin.commons.service.support.InjectConst;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author lilw
 */
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
    protected LocalDateTime createTime;

    @Schema(title = "更新时间")
    protected LocalDateTime lastUpdateTime;

    //@OrderBy
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
    //@Contains
    protected String remark;

    @Schema(title = "乐观锁版本号")
    @Version
    protected Integer optimisticLock;

    @PrePersist
    public void prePersist() {

        if (createTime == null) {
            createTime = LocalDateTime.now();
        }

        if (orderCode == null) {
            orderCode = 1000;
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
            lastUpdateTime = LocalDateTime.now();
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
        //
        return editable == null || editable;
    }

}
