package com.levin.commons.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 事件
 */
@Getter
//@Setter
@Accessors(chain = true)
@ToString
@AllArgsConstructor
public class EntityEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    EntityOption.Action action;

    Object entity;

}
