# 错误码使用指南

## 设计理念

Framework SDK 采用了分层错误码设计理念：

1. **框架通用错误码** (`ErrorCodeEnum`): 适用于所有项目的通用错误场景
2. **业务错误码** (`BusinessErrorCodeEnum`): 项目特定的业务错误场景
3. **自定义错误码**: 项目可以根据需要定义自己的错误码

## 错误码分类

### 1. 框架通用错误码 (ErrorCodeEnum)

这些错误码适用于所有Java项目，包括：

#### 系统级错误码
- `SYSTEM_ERROR`: 系统异常
- `PARAM_ERROR`: 参数错误
- `CONFIG_ERROR`: 配置错误
- `DATABASE_ERROR`: 数据库错误

#### 通用业务错误码
- `DATA_NOT_FOUND`: 数据不存在
- `DATA_ALREADY_EXISTS`: 数据已存在
- `DATA_INVALID`: 数据格式不正确

#### 权限相关错误码
- `PERMISSION_DENIED`: 权限不足
- `TOKEN_INVALID`: 无效的token
- `AUTHENTICATION_FAILED`: 认证失败

#### 网络相关错误码
- `NETWORK_ERROR`: 网络连接错误
- `TIMEOUT_ERROR`: 请求超时
- `SERVICE_UNAVAILABLE`: 服务不可用

### 2. 业务错误码示例 (BusinessErrorCodeEnum)

这是一个示例，展示如何定义业务相关的错误码：

```java
public enum BusinessErrorCodeEnum {
    // 用户相关错误码
    USER_NOT_FOUND("USER_001", "用户不存在"),
    USER_ALREADY_EXISTS("USER_002", "用户已存在"),
    
    // 图书相关错误码
    BOOK_NOT_FOUND("BOOK_001", "图书不存在"),
    BOOK_BORROWED("BOOK_010", "图书已被借出"),
    
    // 订单相关错误码
    ORDER_NOT_FOUND("ORDER_001", "订单不存在"),
    ORDER_CANCELLED("ORDER_004", "订单已取消");
}
```

## 使用方法

### 1. 基本使用

```java
// 使用框架通用错误码
throw new AppException(ErrorCodeEnum.PARAM_ERROR, "参数不能为空");

// 使用业务错误码
throw new AppException(BusinessErrorCodeEnum.USER_NOT_FOUND, "用户不存在");
```

### 2. 注册自定义错误码

```java
// 定义自定义错误码枚举
public enum CustomErrorCodeEnum {
    CUSTOM_ERROR_001("CUSTOM_001", "自定义错误1"),
    CUSTOM_ERROR_002("CUSTOM_002", "自定义错误2");
    
    private final String code;
    private final String message;
    
    // 构造函数和getter方法...
}

// 在应用启动时注册
@PostConstruct
public void init() {
    ErrorCodeUtils.registerErrorCodeEnum(CustomErrorCodeEnum.class);
}
```

### 3. 错误码管理

```java
// 检查错误码是否存在
boolean exists = ErrorCodeUtils.exists("USER_001");

// 获取错误码统计信息
String stats = ErrorCodeUtils.getStatistics();

// 验证错误码格式
boolean isValid = ErrorCodeUtils.isValidFormat("USER_001");

// 生成错误码建议
String suggestion = ErrorCodeUtils.suggestErrorCode("USER", "not found");
// 结果: "USER_NOT_FOUND"
```

## 错误码命名规范

### 1. 格式要求
- 使用大写字母和下划线
- 长度在3-50个字符之间
- 不能包含特殊字符（除了下划线和数字）
- 必须以字母开头

### 2. 命名建议
- **框架错误码**: 使用描述性名称，如 `PARAM_ERROR`, `SYSTEM_ERROR`
- **业务错误码**: 使用 `模块_操作_状态` 格式，如 `USER_NOT_FOUND`, `BOOK_BORROWED`
- **自定义错误码**: 使用 `前缀_描述` 格式，如 `CUSTOM_001`, `API_TIMEOUT`

### 3. 示例

```java
// ✅ 正确的命名
USER_NOT_FOUND
BOOK_ALREADY_BORROWED
PAYMENT_FAILED
API_CALL_TIMEOUT

// ❌ 错误的命名
user_not_found    // 小写
USER-NOT-FOUND    // 包含连字符
1USER_NOT_FOUND   // 以数字开头
```

## 最佳实践

### 1. 错误码设计原则

1. **唯一性**: 每个错误码应该是唯一的
2. **可读性**: 错误码应该能够清楚地表达错误含义
3. **层次性**: 按照模块和功能进行分组
4. **扩展性**: 预留足够的空间供未来扩展

### 2. 错误消息设计

1. **简洁明了**: 错误消息应该简洁明了
2. **用户友好**: 面向用户的错误消息应该友好易懂
3. **技术细节**: 面向开发者的错误消息可以包含技术细节
4. **国际化**: 考虑多语言支持

### 3. 错误处理策略

```java
// 1. 使用框架错误码处理通用错误
if (param == null) {
    throw new AppException(ErrorCodeEnum.PARAM_ERROR, "参数不能为空");
}

// 2. 使用业务错误码处理业务错误
if (user == null) {
    throw new AppException(BusinessErrorCodeEnum.USER_NOT_FOUND, "用户不存在");
}

// 3. 使用自定义错误码处理特定错误
if (customCondition) {
    throw new AppException(CustomErrorCodeEnum.CUSTOM_ERROR_001, "自定义错误");
}

// 4. 在全局异常处理器中统一处理
@ExceptionHandler(AppException.class)
public ResponseResult handleAppException(AppException e) {
    log.error("业务异常：{}", e.getMessage(), e);
    return ResponseResult.error(e.getErrorCode(), e.getMessage());
}
```

## 迁移指南

### 从原项目迁移

1. **保留框架通用错误码**: 继续使用 `ErrorCodeEnum`
2. **迁移业务错误码**: 将业务相关的错误码迁移到 `BusinessErrorCodeEnum`
3. **更新导入语句**: 更新所有错误码的导入语句
4. **测试验证**: 确保所有错误处理逻辑正常工作

### 示例迁移

```java
// 原来的代码
throw new AppException(ErrorCodeEnum.BOOK_NOT_FOUND, "图书不存在");

// 迁移后的代码
throw new AppException(BusinessErrorCodeEnum.BOOK_NOT_FOUND, "图书不存在");
```

## 总结

通过分层设计，Framework SDK 提供了灵活而强大的错误码管理机制：

- **框架通用错误码**: 提供跨项目的通用错误处理
- **业务错误码**: 处理项目特定的业务逻辑错误
- **自定义错误码**: 满足项目的特殊需求
- **错误码管理工具**: 提供统一的错误码管理功能

这种设计既保证了错误码的通用性，又提供了足够的灵活性来满足不同项目的需求。
