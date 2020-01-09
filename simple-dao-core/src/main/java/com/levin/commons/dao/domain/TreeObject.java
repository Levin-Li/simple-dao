package com.levin.commons.dao.domain;

import java.util.Set;


/**
 * 树对象
 * 要注意检查，是否会死锁
 *
 * @param <ID>
 * @param <P>
 */
public interface TreeObject<ID, P extends Identifiable<ID>>
        extends Identifiable<ID> {

    /**
     * 获取父结点id
     *
     * @return parentId
     */
    ID getParentId();

    /**
     * 获取父对象
     *
     * @return parent
     */
    P getParent();

    /**
     * 获取所有的孩子对象
     *
     * @return children
     */
    <C extends P> Set<C> getChildren();

}
