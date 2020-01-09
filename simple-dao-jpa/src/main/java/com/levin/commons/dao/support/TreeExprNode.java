package com.levin.commons.dao.support;

import com.levin.commons.dao.util.CollectionHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 树形表达式节点
 */
public class TreeExprNode<OP extends Serializable, E extends Serializable>
        implements Serializable {

    private OP op;

    private boolean valid;

    private final List subNodes = new ArrayList(5);

    //父结点
    private TreeExprNode<OP, E> parentNode;

    //当前活动节点
    private TreeExprNode<OP, E> currentNode;

    ///////////////////////////////////////////////////

    private String prefix = "(";
    private String suffix = ")";

    /////////////////////////////////////////////////////////

    public TreeExprNode(OP op, boolean valid) {
        this(null, op, valid);
    }


    public TreeExprNode(TreeExprNode<OP, E> parentNode, OP op, boolean valid) {

        if (op == null)
            throw new IllegalArgumentException("op is null");

        this.op = op;

        this.valid = valid;

        this.parentNode = parentNode;

        //当前结点等于自己
        this.currentNode = this;
    }

    public synchronized TreeExprNode<OP, E> clear() {

        subNodes.clear();

        currentNode = this;

        return this;
    }


    /**
     * 开始新操作组
     *
     * @param op
     * @param valid
     * @return
     */
    public synchronized TreeExprNode<OP, E> beginGroup(OP op, boolean valid) {

        final TreeExprNode<OP, E> parentNode = currentNode;

        //如果父结点有效，子结束才有效
        valid = valid && parentNode.isValid();

        currentNode = new TreeExprNode<>(op, valid);

        currentNode.parentNode = parentNode;

        //加入节点
        parentNode.subNodes.add(currentNode);

        return currentNode;
    }

    /**
     * 结束当前操作节点
     *
     * @return
     */
    public synchronized TreeExprNode<OP, E> endGroup() {

        //回到自己
        if (currentNode != this)
            currentNode = currentNode.parentNode;

        if (currentNode == null)
            currentNode = this;

        return currentNode;
    }


    public synchronized TreeExprNode<OP, E> add(E element) {

        if (isValid() && element != null) {
            subNodes.add(element);
        }

        return this;
    }

    /**
     * 加入新元素到当前活动结点
     *
     * @param element
     * @return
     */
    public synchronized boolean addToCurrentNode(E element) {

        boolean isAdd = (element != null && currentNode.isValid());

        if (isAdd) {
            currentNode.subNodes.add(element);
        }

        return isAdd;
    }


    @Override
    public String toString() {
        //操作符之间增加空格
        return CollectionHelper.toString((valid ? subNodes : new Object[]{}), null, " " + op + " "
                , false, true, true
                , (parentNode == null ? null : prefix), (parentNode == null ? null : suffix)).toString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////


    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List getSubNodes() {
        return subNodes;
    }

    public void setOp(OP op) {
        this.op = op;
    }

    public OP getOp() {
        return op;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public TreeExprNode<OP, E> getCurrentNode() {
        return currentNode;
    }

    public TreeExprNode<OP, E> getParentNode() {
        return parentNode;
    }


}
