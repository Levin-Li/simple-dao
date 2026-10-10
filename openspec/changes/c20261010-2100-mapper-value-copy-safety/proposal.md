# Mapper 值复制与编译安全

## 目标

保持一个 JPA 实体对应一个 Mapper，所有被引用的非实体值对象复制和字符串双向转换均在该实体 Mapper 内完成。修复通用生成规则中循环容器类型不匹配、浅复制、跨包值对象遗漏以及同名实体方法擦除冲突。

## 验收

- 共享或自引用的 Map、List、Set、数组复制可终止，并保留同一目标对象身份。
- 无 setter 或 setter 重载的属性不共享可变源对象。
- 能访问的跨包非实体值对象可生成复制方法；Map 值对象能复制元素。
- JPA 映射超类的泛型关联必须相对当前具体实体解析；能解析为 JPA 实体时使用该实体的 Mapper 并共享上下文。不得仅因 `@MappedSuperclass` 就跳过复制；真正无法解析的抽象引用必须明确报错，不能浅复制。
- 两个不同包的同名实体被同一实体引用时，生成的集合方法仍能编译。
- 字符串与对象的双向 JSON 转换默认使用 Fastjson2；独立接口 `MapperJsonUtils` 在 `simple-dao-core` 中定义静态嵌套类 `JsonCodec`（可重写 `stringify(Object)` 与 `parse(String, Type)`）和一个可配置的静态 final `AtomicReference<JsonCodec> codec`。接口仅提供 `toJson(Object)` 与 `fromJson(String, Type)` 两个静态转换方法。实体 Mapper 中仍定义实际转换方法并委托该接口，完整声明类型传入 codec；调用方可暂存原实例并在测试后恢复。
- `toJson(null)` 返回 Java `null` 字符串引用，`fromJson(null, Type)` 与 `fromJson("", Type)` 返回 `null` 对象；这些输入不调用 codec。非空但格式错误的 JSON 抛出解析异常；不隐式裁剪空白。
- 生成器测试及项目规定的示例兼容性测试通过。
