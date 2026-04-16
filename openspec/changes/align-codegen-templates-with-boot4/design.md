## Context

`simple-dao-code-gen` 既包含 Maven 插件自身的运行依赖，也包含用于生成新工程的 POM 与代码模板。前者只需要能够在当前仓库中正常编译运行，后者则直接决定新生成工程是否站在 Spring Boot 4 / Jakarta 基线上。

当前发现的问题主要集中在模板输出层：`j2cache-spring-boot2-starter`、`sharding-jdbc-spring-boot-starter:4.1.1`、旧 `sa-token-spring-boot-starter`、非 Jakarta 的 Knife4j starter，以及一套已经不应继续生成的 SpringFox 模板与路径兼容代码。

## Goals / Non-Goals

**Goals:**
- 让代码生成器输出的根 POM、模块 POM 与文档模板符合 Spring Boot 4 基线。
- 移除高置信不兼容依赖和明显过时的 SpringFox 模板资产。
- 尽量保持生成器模块自身依赖改动最小，避免把“模板修复”扩大为“全生态升级”。

**Non-Goals:**
- 不在本次变更中全面升级所有可选三方生态版本。
- 不处理 `simple-dao-code-gen` 之外模块的功能改造。
- 不为尚未确认支持 Boot 4 的可选生态强行引入新依赖族，只收敛明确不匹配项。

## Decisions

### 1. 区分“插件运行依赖”和“模板输出依赖”

只要 `simple-dao-code-gen` 模块自身能够编译运行，就不因为模板里存在旧 starter 而强行改它的插件运行依赖。真正要修的是 `root-pom.xml.ftl`、`pom.xml.ftl` 和相关 Swagger 模板，因为它们会污染新生成工程。

备选方案是同步大规模调整插件自身和模板依赖，但这会把问题从“模板兼容修复”扩大成“生成器运行时升级”，风险更高，因此不采用。

### 2. 对高置信不兼容项直接替换或移除

本次对以下项目做确定性处理：
- `j2cache-spring-boot2-starter`：直接移除 Boot 2 专用 starter 依赖。
- `sharding-jdbc-spring-boot-starter` 4.1.1：从默认模板依赖中移除，避免继续输出旧 Spring Boot 线。
- `sa-token-spring-boot-starter`：切换到 Boot 3/Jakarta 线 starter。
- `knife4j-openapi3-spring-boot-starter`：切换到 Jakarta 线 starter。
- SpringFox 模板：移除模板文件与相关兼容路径。

备选方案是继续保留这些旧依赖并在文档中标注“用户自行替换”，但这会让默认生成结果仍然不兼容，因此不采用。

### 3. 保留已确认可沿用的 Boot 4 基线

`spring-boot.version`、`spring-cloud.version`、`springdoc`、`hypersistence-utils-hibernate-71` 等已在当前仓库中形成一致基线，本次不重做建模，只在模板中围绕这些基线消除冲突项。

## Risks / Trade-offs

- [移除旧 starter 后，部分历史生成工程选项不再“开箱即用”] → 通过保留必要属性或留空默认依赖，优先保证新工程默认可编译。
- [Sa-Token、Knife4j 切换 Jakarta 坐标后，个别旧模板代码可能需要联动] → 同步清理 SpringFox 遗留模板，并保留 SpringDoc 作为默认文档栈。
- [ShardingSphere 与 J2Cache 功能不再默认注入模板] → 这是有意收敛，避免继续生成明显不兼容依赖；若后续需要，可单独以新 change 重新引入兼容实现。

## Migration Plan

1. 更新 OpenSpec 规格增量，明确模板输出必须使用 Boot 4 对齐的依赖坐标。
2. 修改 `root-pom.xml.ftl`、`pom.xml.ftl` 和 Swagger 相关模板。
3. 编译 `simple-dao-code-gen` 模块验证生成器本身未被破坏。
4. 使用 `openspec validate` 校验新增 change。

## Open Questions

- `lock4j`、`dynamic-datasource`、`powerjob`、`liteflow` 等可选生态是否要进一步升级到更明确的 Boot 4 推荐坐标，本次暂不处理，后续单独建变更。
