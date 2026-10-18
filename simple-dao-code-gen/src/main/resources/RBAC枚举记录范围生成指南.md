# RBAC 枚举记录范围生成指南

## 1. 声明与前置条件

本能力与属性可见性 `RbacDataAttrGroup` 独立。实体使用 `com.levin.commons.rbac.RbacDataAttr` 标记需要按值限制记录的 Java 枚举字段；需要安装支持强制条件扩展的 DAO 及 RBAC 运行时提供器。生成器自身不授予权限。

```java
@Schema(title = "合作类型")
@RbacDataAttr
@Enumerated(EnumType.STRING)
private PartnerType type;

public enum PartnerType { CUSTOMER, SUPPLIER }
```

JPA 实体仅支持 FIELD 访问，受控字段必须直接声明 `@Enumerated(EnumType.STRING)`；PROPERTY/getter 映射、ORDINAL 和自定义 Convert 不在本期支持范围。多个字段可分别标记，继承字段也会校验。字符串即使有表单枚举选项也不是 Java 枚举，标注后生成应明确失败。选项的业务名称和身份由共享元数据解析，生成器不另行推导授权值。

## 2. 生成时机与结果

先编译实体模块，再执行已有 `gen-code`。生成器读取完整实体字节码并校验注解，不依赖实体源码是否可见。

查询、ID、更新、删除及 Info 模型继续使用已有 `TargetOption.entityClass` 指向源实体。权限范围通过可信上下文和 DAO 强制条件处理，不生成 `allowedTypes` 等授权值字段，也不向请求/Info 属性复制 `RbacDataAttr`。原有 `RbacDataAttrGroup` 传递规则保持不变。

## 3. 缓存行为

仅针对存在有效 `RbacDataAttr` 的实体，生成服务的按 ID、唯一键和通用 `getCache` 加载器不启用共享读取缓存；每次读取进入 DAO，避免缓存命中跳过当前用户的记录范围检查。原有缓存清理、事件维护入口仍保留，无注解实体的生成行为不变。受控实体的 `getCacheByKeySuffix` 仅查缓存且没有加载器，因此返回缓存未命中的 `null`；不要依赖它获取受控业务记录。

这是生成模板行为，已有生成服务必须重新生成才会改变。业务自行实现的缓存、原生 SQL、直接 JPA 和关联加载仍须遵循运行时安全边界，不能认为此模板自动保护所有自定义入口。

## 4. 最小验证

运行 `RbacDataAttrCodegenTest`、`RbacDataAttrGroupCodegenTest` 及模块既有测试。测试从无源码实体 JAR 生成并编译各模型，检查继承、严格枚举错误、实体来源、无授权 DTO 字段、受控快捷缓存停用及旧属性分组兼容。应用还必须验证实际 DAO 和分页 COUNT/修改/删除，不以生成测试替代运行时验收。
