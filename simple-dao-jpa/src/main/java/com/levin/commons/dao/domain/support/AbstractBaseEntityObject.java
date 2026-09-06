package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.BaseEntityObject;
import com.levin.commons.dao.domain.DomainObject;
import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.InjectConst;
import com.levin.commons.ui.annotation.Options;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

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
        implements BaseEntityObject , DomainObject {

    private static final long serialVersionUID = -123456789L;

    @Options(dictCode = "framework_domainId")
    @Schema(title = "领域标识", description = "全局管理;通常是模块Id或是应用Id，是一个比较大的范围;超过租户的概念；")
    @Column(length = 384)
    protected String domainId;

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
            //如果是租户为空的记录,默认普通用户不可编辑
            editable = (this instanceof MultiTenantObject) && ((MultiTenantObject) this).getTenantId() != null;
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
