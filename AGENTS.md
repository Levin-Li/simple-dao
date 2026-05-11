# Repository Instructions

This repository is a public Simple DAO component. Follow the project development rules in `docs/project-development-rules.md`.

## Required Verification

- Treat example tests as compatibility tests, not as optional demos.
- After every code change, run the core tests relevant to the touched module.
- After every code change that can affect DAO behavior, annotation parsing, query/update generation, JSON support, JPA behavior, examples, or public APIs, also run:

```bash
mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'
```

- `DaoExamplesTest` is the key end-to-end usage test for this project. Do not claim a code change is fully verified unless this test has been run successfully, or clearly report why it could not run.
- Documentation-only changes do not require `DaoExamplesTest`, but if code changes are already present in the same work session, run it before completion.
