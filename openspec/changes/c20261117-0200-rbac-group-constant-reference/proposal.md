## Why
生成的属性分组在可证明语义一致时应引用实体字段常量。

## What Changes
仅引用实体 E_ 类中公开静态 final 且具有匹配 ConstantValue 的字符串字段；DTO 别名、缺失常量或值不等时保留字面量。
