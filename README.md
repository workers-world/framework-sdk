# Books Framework SDK

Books项目通用框架SDK，包含工具类、异常处理、消息类等通用组件。

## 功能特性

### 工具类 (utils)
- **StringUtils**: 字符串处理工具类
- **DateUtils**: 日期时间处理工具类  
- **ExcelUtils**: Excel文件处理工具类

### 异常处理 (exception)
- **AppException**: 自定义应用异常类
- **GlobalExceptionHandler**: 全局异常处理器
- **ErrorCodeEnum**: 框架通用错误码枚举类
- **BusinessErrorCodeEnum**: 业务错误码枚举示例
- **ErrorCodeUtils**: 错误码管理工具类

### 消息类 (message)
- **BaseMessage**: 通用消息基类
- **ResponseResult**: 统一响应结果类

### 常量类 (constants)
- **CommonConstants**: 通用常量定义
- **ErrorCodeEnum**: 错误码枚举

### 注解类 (annotation)
- **ApiName**: API名称注解

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>com.books</groupId>
    <artifactId>framework-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 使用示例

#### 1. 工具类使用

```java
// 字符串工具
String result = StringUtils.cut("Hello World", 5); // "Hello"

// 日期工具
String dateTime = DateUtils.getCurDateTime(); // "20241202220300"

// Excel工具
Workbook workbook = ExcelUtils.readExcel("data.xlsx");
```

#### 2. 异常处理

```java
// 使用框架通用错误码
throw new AppException(ErrorCodeEnum.PARAM_ERROR, "参数不能为空");

// 使用业务错误码
throw new AppException(BusinessErrorCodeEnum.USER_NOT_FOUND, "用户不存在");

// 注册自定义错误码枚举
ErrorCodeUtils.registerErrorCodeEnum(YourCustomErrorCodeEnum.class);

// 全局异常处理器会自动处理异常并返回统一格式的响应
```

#### 3. 消息类使用

```java
// 创建请求消息
BaseMessage request = BaseMessage.newRequest("msg001", "client", "1.0");
request.addDto(userData);

// 创建响应消息
BaseMessage response = BaseMessage.newResponse(request);
response.addDto(resultData);

// 统一响应结果
ResponseResult<User> result = ResponseResult.success(user);
```

#### 4. 注解使用

```java
@ApiName(value = "查询用户", description = "根据ID查询用户信息")
public ResponseResult<User> getUserById(Long id) {
    // 业务逻辑
}
```

## 构建和安装

### 编译

```bash
mvn clean compile
```

### 测试

```bash
mvn test
```

### 打包

```bash
mvn clean package
```

### 安装到本地仓库

```bash
mvn clean install
```

## 版本历史

### v1.0.0
- 初始版本
- 包含基础工具类、异常处理、消息类等核心功能

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 作者: zero
- 邮箱: [your-email@example.com]
- 项目链接: [https://github.com/your-username/books-framework-sdk]
