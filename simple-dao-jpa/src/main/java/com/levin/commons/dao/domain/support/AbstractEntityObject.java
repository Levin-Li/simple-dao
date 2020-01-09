package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.EntityObject;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;


/**
 * 实现默认相等的比较
 *
 * @param <ID>
 */
@MappedSuperclass
public abstract class AbstractEntityObject<ID extends Serializable>
        implements EntityObject<ID> {

    private static final long serialVersionUID = 1234567890L;

//    @Id
//    @GeneratedValue
//    protected ID id;

//    @Version
//    @Column(name = "optimistic_lock_version", nullable = false)
//    Integer optimisticLockVersion = 0;

    //
//    public ID getId() {
//        return id;
//    }
//

    /**
     * 设置实体 id
     *
     * @param id
     */
    public abstract void setId(ID id);

//    public Integer getOptimisticLockVersion() {
//        return optimisticLockVersion;
//    }
//
//    public void setOptimisticLockVersion(Integer optimisticLockVersion) {
//        this.optimisticLockVersion = optimisticLockVersion;
//    }

    @Override
    public EntityObject<ID> clone() throws CloneNotSupportedException {
        return (EntityObject<ID>) super.clone();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getId() + "]";
    }

    @Override
    public boolean equals(Object target) {

        if (this == target) return true;

        //如果比较对象为null或是，类定义不相同，则视为不相等
        //必须是同一个类加裁器加载的类
        if (target == null || this.getClass() != target.getClass()) return false;

        EntityObject<ID> that = (EntityObject<ID>) target;

        //如果有一个对象的ID为null或ID不相等，就认为对象不相等
        if (this.getId() == null || that.getId() == null || !this.getId().equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        ID entityId = getId();
        return entityId != null ? entityId.hashCode() : super.hashCode();
    }

}
