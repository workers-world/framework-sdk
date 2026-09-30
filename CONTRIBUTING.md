# Contributing

Thank you for contributing to [workers-world/framework-sdk](https://github.com/workers-world/framework-sdk).

## Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **Docker** (optional): only for `@Tag("docker")` integration tests in `framework-sdk-starter-redis`

## Build and test

From the repository root:

```bash
mvn clean test
```

Docker-backed Redis integration tests are excluded by default. To run them when Docker is available:

```bash
mvn test -Pdocker-it -pl framework-sdk-starter-redis
```

Maintainers in environments that cannot pull Testcontainers images from Docker Hub may use the optional **`docker-it-cn`** profile (Huawei SWR Ryuk mirror). This profile is for private/local CI only and is not required for open-source consumers. See comments in the root `pom.xml`.

## Branch and pull request workflow

1. Fork the repository (when it is public) or push a branch on a fork you control.
2. Create a feature branch from the current integration branch (typically `dev_00_07_00` or `master` for multi-module work).
3. Keep changes focused; match existing code style and module boundaries.
4. Run `mvn clean test` before opening a PR.
5. Open a pull request with a clear description, linked issue (if any), and notes on testing.

## Publishing artifacts (maintainers only)

Open-source clones build and install to the local Maven repository with `mvn install`. **Deploying to GitHub Packages is optional** and gated behind the Maven profile **`github-packages-publish`** (see root `pom.xml` and `framework-sdk-bom/pom.xml`). Do not enable that profile in public CI unless credentials are configured intentionally.

## License

By contributing, you agree that your contributions are licensed under the [MIT License](./LICENSE).
