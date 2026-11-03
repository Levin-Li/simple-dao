# 设计

由 service-support ExpressionUtils 提供可恢复的同步严格作用域。simple-dao-core 在该作用域下不向条件表达式注入 DaoContext 全局和线程变量，不设置 BeanFactoryResolver，仍保留构造器局部及请求字段变量；普通模式不变。内置函数的最高优先级和线程解析器隔离由 ExpressionUtils 实现。范围只适用于受审计固定表达式，不承诺可安全运行不可信表达式。

下游 Maven 使用者必须可从包内 AGENTS 和 README 找到严格作用域使用指南。simple-dao-core 主包和源码包将随包放置包专用 AGENTS、README、manual 以及指南，保持相对路径可解析；verify/deploy 门禁解压两个实际构件检查文件和索引链接，缺失时构建失败。包内 AGENTS 仅包含适用于下游的使用入口，不携带源仓库运行/发布记录。
