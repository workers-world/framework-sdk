# Books Framework SDK

Books 项目通用框架 SDK（**1.3+ 多模块**），包含工具类、异常处理、消息类、校验、回收站、进度、Redis 分布式能力等。

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
      <version>1.4.0-SNAPSHOT</version>
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
| `swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/testcontainers/ryuk:0.13.0` | Ryuk 侧车（华为云 SWR） |

可预先拉取 Ryuk：`docker pull swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/testcontainers/ryuk:0.13.0`

## 版本

- **1.4.0-SNAPSHOT**：ApiAccessLogAspect 外部请求访问日志切面、`books.framework.access-log` 配置
- **1.3.0-SNAPSHOT**：多模块拆分、BOM、Redis 存储、Micrometer trace、BaseMessage 主契约
- **1.2.0**：单模块 JAR + 进度 SSE 等

详见 [PROGRESS_GUIDE.md](./PROGRESS_GUIDE.md)、[ERROR_CODE_GUIDE.md](./ERROR_CODE_GUIDE.md)。
