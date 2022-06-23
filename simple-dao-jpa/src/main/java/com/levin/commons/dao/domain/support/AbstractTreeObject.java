package com.levin.commons.dao.domain.support;


import com.levin.commons.dao.domain.TreeObject;
import com.levin.commons.service.domain.Identifiable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Data
@Accessors(chain = true)
@FieldNameConstants
@MappedSuperclass
public abstract class AbstractTreeObject<ID extends Serializable, T extends Identifiable>
        extends AbstractNamedEntityObject
        implements TreeObject<T, T>, Serializable {

    private static final long serialVersionUID = -123456789L;

    //由子类去定义
//    @Schema(description = "父ID")
//    @Column(length = 128)
//    protected ID parentId;

    public abstract AbstractTreeObject<ID, T> setParentId(ID parentId);

    @Schema(description = "父对象")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId", insertable = false, updatable = false)
    protected T parent;

    @Schema(description = "子节点")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy(value = " orderCode ASC , name  ASC ")
    //@Fetch(value = FetchMode.JOIN)
    protected Set<T> children;

    @Schema(title = "id路径", description = "id路径， 使用|包围，如|1|3|15|")
    @Column(length = 1800)
    protected String idPath;

    protected AbstractTreeObject() {
    }

    protected AbstractTreeObject(ID parentId, String name) {
        setParentId(parentId);
        this.name = name;
    }

    protected void autoCheckParentId() {

        //父ID不能是自己
        if (this.equals(parent)) {
            throw new IllegalArgumentException("parent is self");
        }

        if (getParentId() == null && parent != null) {
            setParentId(parent.getId());
        }

        //父ID不能是自己
        if (getParentId() != null && getParentId().equals(getId())) {
            throw new IllegalArgumentException("parent id is self id");
        }
    }

    @Override
    @PrePersist
    public void prePersist() {
        super.prePersist();
        autoCheckParentId();
    }

    @Override
    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        autoCheckParentId();
    }
}
