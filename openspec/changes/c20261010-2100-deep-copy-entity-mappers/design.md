# 设计

1. 复用生成 Info 时已经解析的 `FieldModel` 字段类型建立类型图，以 `@Entity` 区分实体和值类型；进入独立值类后才递归其属性。不得再次独立扫描实体字段并推导泛型。
2. 值类型复制方法放在引用实体的 Mapper 中；嵌套实体继续委托自己的 Mapper。List/Set 的已知非实体元素和实体 Info 递归复制。`Map<K,V>` 及可构造的具体 Map 子类始终新建容器：声明键和值的具体类型都可解析时，按静态类型调用已有单对象复制方法，复制键后再插入新 Map 以遵循目标 Map 的相等性及哈希语义；`Map<String,Object>` 等未知值类型保留原键值引用，不做运行时 `Object` 类型分派。不单独生成值 Mapper。
3. 对 JSON 字符串声明的完整目标类型生成 `String` 双向转换，避免原始 List/Map 泛型丢失。转换方法只委托 `simple-dao-core` 的 `MapperJsonUtils.toJson(Object)` 与 `<T>fromJson(String, Type)`；其公开嵌套 `JsonCodec` 经唯一 `AtomicReference` 注入，默认 Fastjson2，保留空值语义。
4. 对已知值对象和实体 Info 的循环与共享对象使用公共 `CycleAvoidingMappingContext`。未知类型 Map 的值共享是明确契约；其它无法安全复制的动态可变类型显式失败。
5. 用可执行的生成器、核心 JSON 工具与下游生成编译测试检查 Mapper 方法选择和复制行为。
