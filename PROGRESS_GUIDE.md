# 批量任务进度模块对接指南

> 适用版本：`framework-sdk` **≥ 1.2.0**  
> 包路径：`com.books.framework.progress`

## 1. 解决什么问题

批量操作（解析、对账、回归扫描等）往往耗时较长。本模块提供：

- **后端**：统一的上报 API（`ProgressContext` / `ProgressRunner` / `@ProgressTask`）
- **传输**：SSE 实时推送 + 内存快照轮询兜底
- **前端**：`api-common.js` 中的 `initProgressSubscribe` / `renderProgressBarHtml`

业务代码只需在循环里 **一行 tick**，或在方法上 **一个注解**，不必各自维护 SSE 与节流逻辑。

## 2. 依赖与自动配置

### Maven

```xml
<dependency>
    <groupId>com.books</groupId>
    <artifactId>framework-sdk</artifactId>
    <version>1.2.0</version>
</dependency>
```

项目需引入 `spring-boot-starter-aop`（与 `@ApiName` 校验切面相同）。

### 开关

```properties
# 默认 true；设为 false 可关闭整套进度 Bean 与 /api/framework/progress 接口
books.progress.enabled=true
```

### 自动注册的 Bean

| Bean | 作用 |
|------|------|
| `ProgressContext` | 线程内绑定任务，上报 tick / complete / fail |
| `ProgressRunner` | 对 `List` 循环自动 tick |
| `ProgressTaskAspect` | 处理 `@ProgressTask` 生命周期 |
| `ProgressStore` | 内存快照（默认 `InMemoryProgressStore`） |
| `SseProgressHub` | SSE 推送 |
| `ProgressController` | HTTP 订阅与查询接口 |

## 3. 核心概念

### ProgressSnapshot（推送 payload）

| 字段 | 说明 |
|------|------|
| `taskId` | 任务唯一标识，建议用业务主键（fileId、runNo）或 UUID |
| `taskType` | 任务类型，前端按此过滤（如 `bill-parse`） |
| `phase` | 当前阶段，业务自定义字符串（如 `MATCH`、`SCAN`） |
| `processed` | 已完成量 |
| `total` | 总量；未知时可 `null`（前端显示不确定进度） |
| `status` | `RUNNING` / `COMPLETED` / `FAILED` |
| `message` | 失败时的错误信息 |
| `updatedAtMs` | 更新时间戳 |

### taskType 命名建议

使用 **kebab-case**，与业务一一对应，全项目统一维护：

```
bill-parse                  账单解析
bank-reconciliation         银行对账
category-regression-trigger 分类回归手动触发
category-regression-rerun   未分类重跑
```

## 4. 后端对接（三种模式）

### 模式 A：方法级注解（粗粒度，适合 List 入参）

```java
@Service
public class MyBatchService {

    @ProgressTask(type = "my-batch", taskId = "#batchId")
    public BatchResult run(Long batchId, List<Item> items) {
        // 若需细粒度，循环内再 tick
        return progressRunner.forEach(items, "PROCESS", this::processOne);
    }
}
```

- `taskId` 支持 **SpEL**（如 `#fileId`、`#x1.runNo`）；留空则自动生成 UUID
- 若第一个参数为 `List` / 数组，切面会尝试推导 `total`
- 方法正常返回 → `COMPLETED`；抛异常 → `FAILED`

### 模式 B：手动 bind + tick（细粒度，推荐用于线程池 / 多阶段）

```java
@Autowired
private ProgressContext progressContext;

public void doWork(Long fileId) {
    progressContext.bind("bill-parse", String.valueOf(fileId), null, 120);
    try {
        progressContext.tick("CONVERT", 0, total);
        for (int i = 0; i < rows.size(); i++) {
            convert(rows.get(i));
            if (shouldEmit(total, i)) {
                progressContext.tick("CONVERT", i + 1, total);
            }
        }
        progressContext.complete("PERSIST", total, total);
    } catch (RuntimeException e) {
        progressContext.fail(e.getMessage());
        throw e;
    } finally {
        progressContext.unbind();
    }
}
```

要点：

- **bind / unbind 必须成对**，尤其在 `ExecutorService` 子线程中
- `tick` 内置 **时间节流**（默认 120ms），避免 SSE 刷屏
- `phase`、`total` 的语义由业务定义（如分母切换时更新 total，避免进度条回退）

### 模式 C：ProgressRunner 循环封装

```java
progressRunner.forEach(tasks, "EXECUTE", task -> llmClient.call(task));

// 需要下标时
progressRunner.forEachIndexed(rows, "SCAN", (i, row) -> scan(row));
```

等价于：先 `tick(phase, 0, total)`，每处理一条 `tick(phase, i+1, total)`。

### 线程池辅助

```java
ProgressContext.runInTask(progressContext, "bill-parse", taskId, total, 120, () -> {
    billParseFlow.parse(...);
});
```

在 `Runnable` 内可直接 `progressContext.tick(...)`，结束时自动 `complete` / `fail`。

## 5. 循环 tick 频率建议

SDK 已做时间节流，循环内可每条都调 `tick`；若数据量极大，可额外按条数抽样：

```java
private static boolean shouldEmitProgress(int total, int index) {
    if (index <= 2 || index >= total - 1) return true;
    return index % 40 == 0;
}
```

## 6. HTTP 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/framework/progress/subscribe` | SSE，接收全部 task 的进度 |
| GET | `/api/framework/progress/subscribe/{taskId}` | SSE，仅接收指定 taskId |
| GET | `/api/framework/progress/{taskId}` | 轮询最新快照（断线兜底） |

### SSE 事件名

| 事件 | 时机 | data |
|------|------|------|
| `connected` | 连接建立 | `"ok"` |
| `progress` | 进行中 | `ProgressSnapshot` JSON |
| `progress-complete` | 成功结束 | `ProgressSnapshot` JSON |
| `progress-failed` | 失败 | `ProgressSnapshot` JSON |
| `ping` | 心跳（15s） | 时间戳 |

> **与业务回调分离**：如「解析完成通知」仍可用业务自己的 SSE（如 `parse-result`）；进度模块只负责 `processed/total/phase`。

## 7. 前端对接

### 引入

```html
<script src="/js/api-common.js"></script>
```

`api-common.js` 由 SDK 打包，随应用静态资源发布。

### 订阅（页面加载时）

```javascript
const progressEs = initProgressSubscribe({
    // taskId: '42',  // 可选：仅订阅单个任务
    onProgress(snapshot) {
        if (snapshot.taskType !== 'bank-reconciliation') return;
        document.getElementById('execProgress').innerHTML = renderProgressBarHtml(snapshot);
    },
    onComplete(snapshot) {
        document.getElementById('execProgress').innerHTML = '';
    },
    onFailed(snapshot) {
        toast(snapshot.message || '任务失败', 'err');
    }
});
```

### 同步 HTTP + 实时进度

典型流程（对账、回归等「点按钮等结果」场景）：

1. 页面 `init` 时已 `initProgressSubscribe`
2. 用户点击执行 → 显示「等待开始…」
3. 发起同步 `POST`（请求阻塞期间，SSE 持续收到 `progress`）
4. `onComplete` 清空进度条，展示最终结果

无需先把任务改成异步返回 taskId（异步模式可作为后续增强）。

### 渲染

```javascript
container.innerHTML = renderProgressBarHtml(snapshot);
```

- `total == null` → 不确定进度条（`<progress>` 无 max）
- 阶段中文映射见 `progressPhaseLabel()`，可按业务扩展

## 8. 完整示例：Service + 前端

### 后端

```java
@Service
public class DemoBatchService {

    @Autowired
    private ProgressContext progressContext;

    public DemoResult execute(int limit) {
        String taskId = "DEMO-" + System.currentTimeMillis();
        progressContext.bind("demo-batch", taskId, null, 120);
        try {
            List<Row> rows = loadRows(limit);
            int total = rows.size();
            for (int i = 0; i < rows.size(); i++) {
                process(rows.get(i));
                progressContext.tick("RUN", i + 1, total);
            }
            progressContext.complete("DONE", total, total);
            return new DemoResult(total);
        } catch (RuntimeException e) {
            progressContext.fail(e.getMessage());
            throw e;
        } finally {
            progressContext.unbind();
        }
    }
}
```

### 前端

```javascript
initProgressSubscribe({
    onProgress(s) {
        if (s.taskType === 'demo-batch') {
            el.innerHTML = renderProgressBarHtml(s);
        }
    }
});

btn.onclick = async () => {
    el.innerHTML = renderProgressBarHtml(null);
    await api.call('/api/demo/execute', 'DemoExecuteX1', { limit: 1000 });
};
```

## 9. 扩展：自定义 ProgressReporter

默认 `CompositeProgressReporter` 同时写入 `ProgressStore` 和 `SseProgressHub`。  
若需 Redis 广播、落库审计等，实现接口并注册 Bean：

```java
@Component
public class RedisProgressReporter implements ProgressReporter {
    @Override
    public void report(ProgressSnapshot snapshot) {
        // 推送到 Redis / MQ
    }
}
```

自定义 `ProgressReporter` Bean 会 **替换** 默认组合实现；若需并存，自行组装 `CompositeProgressReporter`。

## 10. 注意事项

| 项 | 说明 |
|----|------|
| 切面 vs tick | `@ProgressTask` 只管任务开/关/异常；**循环内细粒度仍需 tick** |
| phase / total | 属于业务语义（多阶段、分母变化），SDK 不推断 |
| 线程池 | 子线程必须 `bind`，且 `finally` 里 `unbind` |
| 多实例部署 | 内存 `ProgressStore` + 本地 SSE 仅适合单机；集群需自定义 `ProgressReporter` |
| 编译参数 | SpEL `#paramName` 依赖 `-parameters`（SDK 已开启） |

## 11. 业务接入提示

消费方按场景选择稳定的 `taskType` / `taskId`（如业务主键、批次号），在长任务入口加 `@ProgressTask`，循环内 `tick`，前端用 `/api/progress/subscribe/{taskType}/{taskId}` 订阅即可。

---

SDK 变更后执行：

```bash
mvn clean install -DskipTests
```

业务项目更新 `framework-sdk` 版本并重新编译即可。
