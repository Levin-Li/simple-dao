package com.levin.commons.dao.domain;


/**
 * 有状态的对象
 */
public interface StatefulObject<STATE> {

    /**
     * 获取对象状态
     *
     * @return
     */
    STATE getState();

}
