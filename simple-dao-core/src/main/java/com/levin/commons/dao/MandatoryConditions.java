package com.levin.commons.dao;

import java.util.Objects;

/**
 * 强制数据范围的独立受信配置入口。
 * <p>与可覆盖的DAO表达式上下文分离；应用启动装配提供器，请求不能通过setContext改变它。</p>
 */
public final class MandatoryConditions {
    private static volatile MandatoryConditionProvider provider;
    private MandatoryConditions() { }
    /** 启动时注册；返回旧提供器供受控销毁和测试恢复。禁止按请求替换全局提供器。 */
    public static synchronized MandatoryConditionProvider setProvider(MandatoryConditionProvider value) {
        MandatoryConditionProvider previous = provider;
        provider = value;
        return previous;
    }
    /** 获取已注册的可信提供器，供应用组合和生命周期恢复。 */
    public static MandatoryConditionProvider getProvider() { return provider; }

    /** 获取当前范围；未配置提供器时保持旧行为。 */
    public static MandatoryCondition resolve(Class<?> entityClass, EntityOption.Action action) {
        MandatoryConditionProvider current = provider;
        return current == null || entityClass == null || entityClass == Void.class ? MandatoryCondition.unrestricted()
                : Objects.requireNonNull(current.resolve(entityClass, action), "Mandatory condition provider returned null");
    }
}
