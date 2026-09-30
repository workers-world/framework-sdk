# Framework SDK 实现总结

## 版本 1.4.1-SNAPSHOT（当前）

### 新增

- **OutboundHttpLogInterceptor**：`FrameworkHttpClient` 出站 HTTP 访问日志，自动透传 `X-Trace-Id`
- **books.framework.access-log.outbound-***：出站开关、body 日志、URL 排除

---

## 版本 1.4.0-SNAPSHOT

### 新增

- **ApiAccessLogAspect**：Controller + `@ApiName` 双维度访问日志；INFO 元数据、DEBUG 可选脱敏 body
- **books.framework.access-log**：enabled、exclude 路径、mask 字段、body 开关

---

## 版本 1.3.0-SNAPSHOT

### 架构

- **多模块 Maven**：`bom` → `core` → `autoconfigure` → `starter-web` / `starter-redis` → 聚合 `framework-sdk`
- **依赖**：Web 与 Redis 按需引入；`framework-sdk-core` 可单独用于工具/消息层

### 已实现改进

1. **自动装配**：`GlobalExceptionHandler`、`FrameworkWebAutoConfiguration` 等通过 `META-INF/spring/...AutoConfiguration.imports` 加载
2. **模块化**：BOM 统一版本；业务错误码未改动（按约定保留）
3. **Redis**：`RedisIdempotentStorage`、`RedisProgressStore`（`books.framework.redis.enabled=true`）
4. **可观测性**：`TraceIdFilter` 支持 Micrometer `Tracer`；`BaseMessage.traceId` 写入错误/成功响应
5. **主契约**：`BaseMessage` 为主；`ResponseResult` 已 `@Deprecated`
6. **工程化**：`.github/workflows/ci.yml` PR 构建；`FrameworkAutoConfigurationTest` 切片测试

### 技术栈

- Spring Boot **3.4.5**、Java **17**
- Jackson、Lombok、可选 Hutool / POI / EasyExcel / PDF / HanLP

### 构建

```bash
mvn clean verify   # 需 JDK 17+
```

---

## 历史 1.0.x / 1.2.x

单模块 `framework-sdk` JAR，功能见 Git 标签 `v1.2.0` 说明。
