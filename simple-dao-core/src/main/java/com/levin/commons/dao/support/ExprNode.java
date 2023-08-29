package com.levin.commons.dao.support;

import com.levin.commons.dao.StatementBuildException;
import com.levin.commons.dao.util.CollectionHelper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * 树形表达式节点
 */

public class ExprNode<OP extends Serializable, E extends Serializable>
        implements Serializable {

    @Getter
    private OP op;

    @Getter
    private boolean valid;

    /**
     * 子节点
     */
    @Getter
    private final List subNodes = new ArrayList(7);

    /**
     * 该节点的参数
     */
//    @Getter
//    private final List params = new ArrayList(7);

    //父结点
    @Getter
    private ExprNode<OP, E> parentNode;

    //当前活动节点
    private ExprNode<OP, E> currentNode;

    ///////////////////////////////////////////////////

    @Getter
    @Setter
    private String prefix = "(";

    @Getter
    @Setter
    private String suffix = ")";

    /////////////////////////////////////////////////////////

    public ExprNode(OP op, boolean valid) {
        this(null, op, valid);
    }


    public ExprNode(ExprNode<OP, E> parentNode, OP op, boolean valid) {

        if (op == null) {
            throw new IllegalArgumentException("op is null");
        }

        this.op = op;


        this.valid = valid;

        this.parentNode = parentNode;

        //当前结点等于自己
        this.currentNode = this;

    }

    protected boolean isNotOp() {
        return "NOT".equals(op);
    }

    protected String getDelimiter() {
        return "" + op;
    }

    protected String getPrefix() {

        if (isNotOp()) {
            return op + prefix;
        }

        //操作符之间增加空格
        //如果不需要小刮号
        return isNotNeedParentheses() ? null : prefix;

    }

    protected String getSuffix() {

        if (isNotOp()) {
            return suffix;
        }

        //操作符之间增加空格
        //如果不需要小刮号
        return isNotNeedParentheses() ? null : suffix;

    }

    /**
     * 是否需要小挂号
     *
     * @return
     */
    public boolean isNotNeedParentheses() {
        return parentNode == null || this.op.equals(parentNode.op) || subNodes.size() < 2;
    }

    public synchronized ExprNode<OP, E> clear() {

        subNodes.clear();
//        params.clear();

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
    public synchronized ExprNode<OP, E> beginGroup(OP op, boolean valid) {

        final ExprNode<OP, E> parentNode = currentNode;

        //如果父结点有效，子结束才有效
        valid = valid && parentNode.isValid();

        currentNode = new ExprNode<>(op, valid);

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
    public synchronized ExprNode<OP, E> endGroup(boolean isContainLastElement) {

        //如果不包含最后一个节点，则把最后一个节点移动到父节点
        if (!isContainLastElement
                && currentNode != null
                && !currentNode.getSubNodes().isEmpty()) {

            //把最后一个节点移出，加给父节点
            List tempList = currentNode.getSubNodes();

            Object lastNode = tempList.remove(tempList.size() - 1);

            ExprNode<OP, E> p = currentNode.parentNode;

            if (p != null) {
                p.getSubNodes().add(lastNode);
            }
        }

        //回到自己
        if (currentNode != this) {
            currentNode = currentNode.parentNode;
        }

        if (currentNode == null) {
            currentNode = this;
        }

        //把

        return currentNode;
    }

    /**
     * 切换当前节点为自己
     *
     * @return
     */
    public ExprNode<OP, E> switchCurrentNodeToSelf() {
        currentNode = this;
        return this;
    }

    /**
     * 获取根节点
     *
     * @return
     */
    public final synchronized ExprNode<OP, E> getRootNode() {

        ExprNode<OP, E> parent = parentNode;

        ExprNode<OP, E> top = this;

        while (parent != null) {
            top = parent;
            parent = parent.parentNode;
        }

        return top;
    }


    /**
     * 增加节点元素
     *
     * @param element
     * @return
     */
    public synchronized boolean add(E element) {
        return add(false, element);
    }

    /**
     * 增加节点元素和参数
     *
     * @param element    节点元素
     * @param addToFirst //     * @param params     节点参数
     * @return
     */
    public synchronized boolean add(boolean addToFirst, E element) {

        if (element == null || !isValid()) {
            return false;
        }

        if (isNotOp() && subNodes.size() >= 1) {
            throw new StatementBuildException("NOT操作只允许一个操作数");
        }

        if (addToFirst) {
            //加到头部去

            subNodes.add(0, element);

//            if (params != null && params.length > 0) {
//
//                if (params.length == 1) {
//                    this.params.add(0, params[0]);
//                } else {
//                    this.params.addAll(0, Arrays.asList(params));
//                }
//            }

        } else {

            subNodes.add(element);

//            if (params != null
//                    && params.length > 0) {
//
//                if (params.length == 1) {
//                    this.params.add(params[0]);
//                } else {
//                    this.params.addAll(Arrays.asList(params));
//                }
//
//            }

        }

        return true;
    }


    /**
     * 获取当前节点
     *
     * @return
     */
    public synchronized ExprNode<OP, E> currentNode() {
        return currentNode != null ? currentNode : this;
    }

    @Override
    public String toString() {
        return CollectionHelper.toString(
                (valid ? subNodes : new Object[]{}), null, " " + getDelimiter() + " "
                , false, !isNotOp(), true
                , getPrefix(), getSuffix()
        ).toString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
}
