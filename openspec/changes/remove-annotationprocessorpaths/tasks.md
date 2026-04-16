## 1. 构建配置调整

- [x] 1.1 从父 POM 中移除 `annotationProcessorPaths`
- [x] 1.2 在不恢复 `annotationProcessorPaths` 的前提下，为 JDK 23+ 显式开启 `proc=full`
- [x] 1.3 保留并核对模块级直接依赖，确保删除后仍具备所需编译依赖

## 2. 验证

- [x] 2.1 执行主源码编译和全仓 `test-compile`，验证移除显式处理器路径后构建链可工作
- [x] 2.2 执行 `openspec validate` 校验本次 change
