## 1. 项目现状梳理

- [x] 1.1 阅读根 `pom.xml`、`README.md` 和关键模块入口，确认运行时与代码生成器的职责边界
- [x] 1.2 确认 `openspec/` 当前只有配置文件，没有现成规格与变更文档

## 2. 当前态规格补齐

- [x] 2.1 新建 `simple-dao-runtime` 规格，沉淀运行时能力与 Spring Boot 4 基线要求
- [x] 2.2 新建 `simple-dao-codegen` 规格，沉淀代码生成器能力与模板输出约束

## 3. 本轮变更建模

- [x] 3.1 新建本次 change 的 `proposal.md`，说明补齐 OpenSpec 的原因与影响
- [x] 3.2 新建本次 change 的 `design.md`，记录为什么采用“两份规格 + 一个变更”的建模方式
- [x] 3.3 为 change 增补对应的规格增量文件，确保当前文档工作可被 OpenSpec 识别

## 4. 校验

- [x] 4.1 使用 `openspec list --specs` 检查当前规格是否可发现
- [ ] 4.2 使用 `openspec validate` 校验规格与 change 结构
