# Mapper 生成规范

## 要求

- 每个 JPA `@Entity` 只生成其对应的实体 Mapper。
- 非实体可变对象的复制方法位于引用实体 Mapper 内，直接对象字段及已知 List/Set 元素不得别名源对象。
- 相同类型 `Map<K,V>` 及可构造的具体 Map 子类的复制必须产生新容器。声明的 K/V 均为具体类型时，已知可变非实体 K/V 使用对应单对象复制方法，复制键后再插入目标 Map；不可变标量直接复用。`Map<String,Object>` 等未知 V 保留原键和值引用，不能对 `Object` 做运行时类型分派。`String` 与 `Map<K,V>` 的 JSON 转换仍保留声明的完整泛型类型。
- 实体关联映射委托目标实体 Mapper，并传递同一上下文。
- JSON 字符串与声明的完整 Java 类型可双向转换。
- 所有生成的 JSON 双向方法统一调用 core 的 `MapperJsonUtils`；仅公开 `toJson(Object)`、`fromJson(String, Type)` 转换入口，默认 Fastjson2，可经同一原子引用替换嵌套 `JsonCodec`。空对象序列化为 Java `null` 字符串，空或 `null` JSON 输入解析为 `null` 对象。
- 值类型候选必须复用已生成 Info 字段的解析类型，避免与实体泛型解析结果分歧。
- Map 以外不支持的动态可变类型在运行时显式失败；已知不兼容字段在生成或编译阶段明确失败，不得静默跳过。
