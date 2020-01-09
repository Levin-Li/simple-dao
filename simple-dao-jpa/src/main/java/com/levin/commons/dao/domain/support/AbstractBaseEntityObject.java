package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.BaseEntityObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
@Accessors(chain = true)
public abstract class AbstractBaseEntityObject<ID extends Serializable>
//        extends AbstractEntityObject<ID>
        implements BaseEntityObject<ID> {

    private static final long serialVersionUID = -123456789L;


    @Desc("排序代码")
    @Column(name = "order_code")
    protected Integer orderCode;

    @Desc("是否允许")
    @Column(name = "is_enable", nullable = false)
    protected Boolean enable = true;

    @Desc("是否可编辑")
    @Column(name = "is_editable", nullable = false)
    protected Boolean editable = true;

    @Desc("创建时间")
    @Column(name = "create_time")
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date createTime = new Date();

    @Desc("更新时间")
    @Column(name = "last_update_time")
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdateTime;

    @Desc("备注")
    @Column(name = "remark", length = 1000)
    protected String remark;

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
