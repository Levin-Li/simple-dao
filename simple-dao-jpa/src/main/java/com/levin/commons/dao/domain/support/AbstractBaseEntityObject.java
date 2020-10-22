package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.BaseEntityObject;
import com.levin.commons.dao.domain.Identifiable;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@MappedSuperclass
@Data
@Accessors(chain = true)
public abstract class AbstractBaseEntityObject<ID extends Serializable>
        implements BaseEntityObject<ID> {

    private static final long serialVersionUID = -123456789L;

    @Desc("排序代码")
    @Column(name = "order_code")
    protected Integer orderCode;

    @Desc("是否允许")
    @Column(name = "is_enable")
    protected Boolean enable = true;

    @Desc("是否可编辑")
    @Column(name = "is_editable")
    protected Boolean editable = true;

    @Desc("创建时间")
    @Column(name = "create_time")
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date createTime;

    @Desc("更新时间")
    @Column(name = "last_update_time")
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdateTime;

    @Desc("备注")
    @Column(name = "remark", length = 1000)
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


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return Objects.equals(getId(), ((Identifiable) o).getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : 0;
    }

}
