package com.levin.commons.dao;

/**
 * 应用级受信范围解析器，与Servlet和具体权限系统无关。
 * <p>未受控实体返回不限制；受控实体缺少身份或许可时必须返回拒绝，不能返回null。</p>
 */
@FunctionalInterface
public interface MandatoryConditionProvider {
    /** 按实体和业务操作解析当前可信上下文的数据范围。 */
    MandatoryCondition resolve(Class<?> entityClass, EntityOption.Action action);
}
