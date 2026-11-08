## 1. 模板

- [x] 1.1 从 `MultiTenantReq` 的 `tenantId` 移除 `DataMasking` 并清理无用 import
- [x] 1.2 从 `MultiTenantInfo` 的 `tenantId`、`tenantName` 移除 `DataMasking` 并清理无用 import

## 2. 验证

- [x] 2.1 增加实际渲染、编译及注解契约测试，覆盖三处字段与保留的租户注入和范围
- [x] 2.2 运行代码生成模块与示例兼容测试，修复失败并核对工作区差异
- [x] 2.3 安装本机更新后的 SNAPSHOT，不发布、提交或推送
