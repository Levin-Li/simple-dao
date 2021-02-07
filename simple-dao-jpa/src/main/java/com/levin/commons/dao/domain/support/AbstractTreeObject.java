package com.levin.commons.dao.domain.support;


import com.levin.commons.dao.domain.Identifiable;
import com.levin.commons.dao.domain.TreeObject;
import com.levin.commons.service.domain.Desc;
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
public abstract class AbstractTreeObject<ID extends Serializable, T extends Identifiable<ID>>
        extends AbstractNamedEntityObject<ID>
        implements TreeObject<ID, T>, Serializable {

    private static final long serialVersionUID = -123456789L;

    @Schema(description = "父ID")
    @Column(name = "parent_id")
    protected ID parentId;

    @Schema(description = "父对象")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    protected T parent;

    @Schema(description = "子节点")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy(value = "orderCode DESC,name ASC")
//    @Fetch(value = FetchMode.JOIN)
    protected Set<T> children;

    @Schema(description = "类型")
    @Column
    protected String type;

    @Schema(description = "ID路径， 使用|包围，如|1|3|15|")
    @Column(length = 1800)
    protected String idPath;




    protected AbstractTreeObject() {
    }

    protected AbstractTreeObject(String name, ID parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public AbstractTreeObject(ID parentId, String name, String type) {
        this.parentId = parentId;
        this.name = name;
        this.type = type;
    }

    public void setParent(T parent) {

        if (parent == null) {
            this.parent = null;
            this.parentId = null;
        } else {

            if (this.equals(parent)) {
                throw new IllegalArgumentException("parent is self");
            }

            this.parent = parent;
            this.parentId = parent.getId();
        }
    }

}
