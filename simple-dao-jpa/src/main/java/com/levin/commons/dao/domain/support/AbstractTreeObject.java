package com.levin.commons.dao.domain.support;


import com.levin.commons.dao.domain.Identifiable;
import com.levin.commons.dao.domain.TreeObject;
import com.levin.commons.service.domain.Desc;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@MappedSuperclass
public abstract class AbstractTreeObject<ID extends Serializable, T extends Identifiable<ID>>
        extends AbstractNamedEntityObject<ID>
        implements TreeObject<ID, T>, Serializable {

    private static final long serialVersionUID = -123456789L;

    @Desc("父ID")
    @Column(name = "parent_id")
    protected ID parentId;

    @Desc("父对象")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    protected T parent;

    @Desc()
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy(value = "orderCode DESC,name ASC")
//    @Fetch(value = FetchMode.JOIN)
    protected Set<T> children;

    @Desc("类型")
    @Column(name = "type")
    protected String type;


    @Desc(name = "ID路径", detail = "id路径，使用|包围，如|1|3|15|")
    @Column(name = "id_path", length = 1800)
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

    @Override
    public ID getParentId() {
        return parentId;
    }

    public void setParentId(ID parentId) {
        this.parentId = parentId;
    }

    @Override
    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        if (parent == null) {
            this.parent = null;
            this.parentId = null;
        } else {

            if (this.equals(parent))
                throw new IllegalArgumentException("parent is self");

            this.parent = parent;
            this.parentId = parent.getId();
        }
    }

    public void setChildren(Set<T> children) {
        this.children = children;
    }

    @Override
    public Set<T> getChildren() {
        return children;
    }

}
