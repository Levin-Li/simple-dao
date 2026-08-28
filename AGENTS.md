# Repository Instructions

This repository is a public Simple DAO component. Follow the project development rules in `docs/project-development-rules.md`.

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
