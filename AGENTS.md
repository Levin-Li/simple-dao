# Repository Instructions

This repository is a public Simple DAO component. Follow the project development rules in `docs/project-development-rules.md`.

## 项目边界

- 当前工作区之外的任何文件、相邻仓库、独立前端仓库、依赖源码目录和本机其它项目默认均不属于本项目。
- 发现需求实际归属外部项目时，必须先明确告知用户该项目归属和当前工作区不包含对应实现；仅可进行只读定位，不得修改、生成、格式化、构建可能写入文件的命令或执行 Git 操作。
- 只有用户明确指定外部项目为本次目标，或明确授权跨项目修改后，才可以在该项目中写入。用户对功能归属的说明本身不构成跨项目修改授权。
- 即使外部项目代码已经被修改，也必须先向用户说明实际改动范围；除非用户明确要求，不得自行继续扩展、提交、发布或撤销外部项目改动。

## Required Verification

- Treat example tests as compatibility tests, not as optional demos.
- After every code change, run the core tests relevant to the touched module.
- After every code change that can affect DAO behavior, annotation parsing, query/update generation, JSON support, JPA behavior, examples, or public APIs, also run:

```bash
mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest,DaoJsonExamplesTest,DaoQueryExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'
```

- `DaoExamplesTest`、`DaoJsonExamplesTest` 和 `DaoQueryExamplesTest` 共同构成关键端到端使用测试 for this project. Do not claim a code change is fully verified unless this test has been run successfully, or clearly report why it could not run.
- Documentation-only changes do not require `DaoExamplesTest`, but if code changes are already present in the same work session, run it before completion.

## Git 提交说明

- 本仓库的 git 提交说明默认必须使用中文，包括提交标题、正文说明，以及有实际内容的 git trailers。
- 继续遵守现有结构化提交协议，但除非用户明确要求其它语言，否则协议中的内容也要翻译成中文。

## 发布规则

- 用户要求发布版本时，发布阶段只允许执行 Maven 的 `deploy` 任务。
- 不要自动修改版本号、POM、Git 标签或其他任何文件；版本管理由用户显式决定。
