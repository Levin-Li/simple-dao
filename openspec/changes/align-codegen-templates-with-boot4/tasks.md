## 1. OpenSpec 建模

- [x] 1.1 为代码生成模板 Boot 4 对齐补齐 proposal、design 和规格增量
- [x] 1.2 用任务清单约束本次实现范围，仅覆盖模板依赖与遗留 Swagger 模板

## 2. 模板依赖收敛

- [x] 2.1 修改 `root-pom.xml.ftl`，移除或替换明确不兼容 Spring Boot 4 的依赖坐标
- [x] 2.2 修改 `pom.xml.ftl`，同步收敛模块模板中对应的旧 starter 与文档依赖

## 3. 遗留模板清理与验证

- [x] 3.1 移除 SpringFox 相关模板或兼容代码，避免继续生成旧文档栈
- [x] 3.2 编译验证 `simple-dao-code-gen` 模块并执行 `openspec validate` 校验本次 change
