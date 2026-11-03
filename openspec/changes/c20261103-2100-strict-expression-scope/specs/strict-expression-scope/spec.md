## ADDED Requirements

### Requirement: 严格作用域隔离 DAO 环境
严格作用域下 DAO 求值 MUST 不使用 DaoContext 的全局/线程变量或 BeanFactoryResolver，且保持本次局部和请求上下文。

#### Scenario: 普通求值保持兼容
- **WHEN** 没有启用严格作用域
- **THEN** 现有全局、线程、局部变量及 BeanFactory 行为保持不变

#### Scenario: 受控求值忽略污染
- **WHEN** 全局或线程上下文包含与内置函数或普通变量冲突的值
- **THEN** 严格条件求值不能读取这些环境值，内置函数仍正常执行
