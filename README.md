# Books Framework SDK

通用 Spring Boot 框架 SDK（**1.6.x 多模块**），包含工具类、异常处理、消息契约、校验、回收站、进度、Redis 分布式能力等。`groupId` 为历史命名 `com.books`。

## 模块结构

| 模块 | 说明 |
|------|------|
| `framework-sdk-bom` | 统一版本 BOM |
| `framework-sdk-core` | 核心 API（无 `spring-boot-starter-web`） |
| `framework-sdk-autoconfigure` | Spring Boot 自动配置 |
| `framework-sdk-starter-web` | Web 层（Filter、拦截器、Controller） |
| `framework-sdk-starter-redis` | Redis 幂等 / 进度存储 |
| `framework-sdk` | **聚合 POM**（兼容旧坐标，等价于 web + 可选 redis） |

## 快速开始

### 推荐依赖（Web 应用）

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.books</groupId>
      <artifactId>framework-sdk-bom</artifactId>
      <version>1.6.1-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.books</groupId>
    <artifactId>framework-sdk-starter-web</artifactId>
  </dependency>
</dependencies>
```

### 仅需核心工具（无 Web）

```xml
<dependency>
  <groupId>com.books</groupId>
  <artifactId>framework-sdk-core</artifactId>
</dependency>
```

### 分布式 Redis（多实例）

```yaml
books:
  framework:
    redis:
      enabled: true
    idempotent:
      store-type: redis
    progress:
      store-type: redis
```

```xml
<dependency>
  <groupId>com.books</groupId>
  <artifactId>framework-sdk-starter-redis</artifactId>
</dependency>
```

需自行提供 `StringRedisTemplate`（`spring-boot-starter-data-redis`）。

## API 主契约：BaseMessage

- 控制器与 **GlobalExceptionHandler** 统一返回 `BaseMessage`
- 响应体含 `traceId`（与 `X-Trace-Id` / Micrometer 对齐）
- `ResponseResult` 自 **1.3.0** 起标记 `@Deprecated`，请迁移至 `BaseMessage`

```java
@ApiName("查询用户")
public BaseMessage getUser(@RequestBody BaseMessage request) {
    User user = ...;
    BaseMessage response = request.newResponse();
    response.addDto(user);
    return response;
}
```

## 自动配置说明

引入 `framework-sdk-starter-web` 后**无需** `@ComponentScan("com.books.framework")`：

- `GlobalExceptionHandler` 通过 `FrameworkExceptionAutoConfiguration` 注册
- `TraceIdFilter`、幂等拦截器、进度/回收站 Controller 由 `FrameworkWebAutoConfiguration` 注册

## 构建与测试

需 **JDK 17+**：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean test
```

默认 **不跑** 需 Docker 的 `@Tag("docker")` 集成测试。

本地已有 `redis:7.2-alpine` 时，可显式跑 Redis 集成测试：

```bash
mvn test -Pdocker-it -pl framework-sdk-starter-redis
```

Testcontainers 镜像（见 `framework-sdk-starter-redis/src/test/resources/.testcontainers.properties`）：

| 镜像 | 说明 |
|------|------|
| `redis:7.2-alpine` | Redis 集成测试（本地已有则不拉取） |
| Testcontainers 默认 Ryuk 镜像 | 侧车（`-Pdocker-it` 时由 Testcontainers 拉取） |

若无法访问 Docker Hub，维护者可在本地 CI 使用 `-Pdocker-it-cn`（见 [CONTRIBUTING.md](./CONTRIBUTING.md)）。

## 公共 API 与稳定性

- **稳定公共 API**：`com.books.framework` 下各模块的 Java 类型（工具类、消息/异常模型、注解、自动配置入口类），以及文档中描述的 `BaseMessage` 请求/响应契约、`books.framework.*` 配置项。
- **集成面**：`framework-sdk-starter-web` 注册的 Filter、拦截器与 Controller（进度、回收站等）视为可替换的默认实现；行为扩展优先通过 Spring Bean 替换或配置开关，而非 fork 内部类。
- **非公共 / 可能变更**：`*.internal` 包（若有）、测试专用类、静态 HTML/JS 演示页，以及未在 README 或模块 Javadoc 中说明的 REST 路径。SemVer 主要约束 Maven 模块与 documented API；小版本可能增加配置项或可选 Bean。

## 版本

- **1.6.x-SNAPSHOT**：出站超时属性绑定修复等（见 changelog / commits）
- **1.4.1-SNAPSHOT**：FrameworkHttpClient 出站 HTTP 访问日志（outbound_http）
- **1.4.0-SNAPSHOT**：ApiAccessLogAspect 入站访问日志切面、`books.framework.access-log` 配置
- **1.3.0-SNAPSHOT**：多模块拆分、BOM、Redis 存储、Micrometer trace、BaseMessage 主契约
- **1.2.0**：单模块 JAR + 进度 SSE 等

详见 [PROGRESS_GUIDE.md](./PROGRESS_GUIDE.md)、[ERROR_CODE_GUIDE.md](./ERROR_CODE_GUIDE.md)、[NOTICE](./NOTICE)。
