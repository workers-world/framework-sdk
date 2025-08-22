# Framework SDK 使用示例

本文档展示如何在原项目中使用新创建的Framework SDK。

## 1. 在原项目中添加SDK依赖

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.books</groupId>
    <artifactId>framework-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2. 迁移现有代码

### 2.1 更新导入语句

将原来的导入语句从：
```java
import com.books.app.utils.StringUtils;
import com.books.app.utils.DateUtils;
import com.books.app.dto.base.BaseMessage;
import com.books.app.dto.ResponseResult;
import com.books.app.domain.exception.AppException;
import com.books.app.constants.ErrorCodeEnum;
```

更新为：
```java
import com.books.framework.utils.StringUtils;
import com.books.framework.utils.DateUtils;
import com.books.framework.message.BaseMessage;
import com.books.framework.message.ResponseResult;
import com.books.framework.exception.AppException;
import com.books.framework.constants.ErrorCodeEnum;
```

### 2.2 示例：更新工具类使用

**原来的代码：**
```java
// 在 BookManageManager.java 中
import com.books.app.utils.DateUtils;

public class BookManageManager {
    public void insert(Book book) {
        book.setTechMntTime(DateUtils.getCurDateTime());
        // ...
    }
}
```

**更新后的代码：**
```java
// 在 BookManageManager.java 中
import com.books.framework.utils.DateUtils;

public class BookManageManager {
    public void insert(Book book) {
        book.setTechMntTime(DateUtils.getCurDateTime());
        // ...
    }
}
```

### 2.3 示例：更新异常处理

**原来的代码：**
```java
// 在某个Service中
import com.books.app.domain.exception.AppException;
import com.books.app.constants.ErrorCodeEnum;

public class SomeService {
    public void someMethod() {
        if (someCondition) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "参数错误");
        }
    }
}
```

**更新后的代码：**
```java
// 在某个Service中
import com.books.framework.exception.AppException;
import com.books.framework.constants.ErrorCodeEnum;

public class SomeService {
    public void someMethod() {
        if (someCondition) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "参数错误");
        }
    }
}
```

### 2.4 示例：更新消息类

**原来的代码：**
```java
// 在Controller中
import com.books.app.dto.ResponseResult;

@RestController
public class SomeController {
    @GetMapping("/test")
    public ResponseResult<String> test() {
        return ResponseResult.success("Hello World");
    }
}
```

**更新后的代码：**
```java
// 在Controller中
import com.books.framework.message.ResponseResult;

@RestController
public class SomeController {
    @GetMapping("/test")
    public ResponseResult<String> test() {
        return ResponseResult.success("Hello World");
    }
}
```

## 3. 删除原项目中的重复代码

迁移完成后，可以删除原项目中的以下文件：

### 3.1 工具类
- `src/main/java/com/books/app/utils/StringUtils.java`
- `src/main/java/com/books/app/utils/DateUtils.java`
- `src/main/java/com/books/app/utils/ReadUtil.java` (功能已合并到ExcelUtils)

### 3.2 异常处理类
- `src/main/java/com/books/app/domain/exception/AppException.java`
- `src/main/java/com/books/app/exception/GlobalExceptionHandler.java`
- `src/main/java/com/books/app/constants/ErrorCodeEnum.java`

### 3.3 消息类
- `src/main/java/com/books/app/dto/base/BaseMessage.java`
- `src/main/java/com/books/app/dto/ResponseResult.java`

### 3.4 注解类
- `src/main/java/com/books/app/annotation/ApiName.java`

## 4. 使用新的Excel工具类

原来的 `ReadUtil` 功能已经合并到 `ExcelUtils` 中：

```java
import com.books.framework.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import java.util.Map;
import java.util.List;

public class ExcelProcessor {
    public void processExcel(String filePath) {
        try {
            // 读取Excel文件
            Workbook workbook = ExcelUtils.readExcel(filePath);
            
            // 解析Excel数据
            Map<String, List<Row>> sheetMap = ExcelUtils.parseExcel(workbook);
            
            // 处理数据
            for (Map.Entry<String, List<Row>> entry : sheetMap.entrySet()) {
                String sheetName = entry.getKey();
                List<Row> rows = entry.getValue();
                
                for (Row row : rows) {
                    // 获取行数据
                    List<String> values = ExcelUtils.getRowValues(row);
                    // 处理数据...
                }
            }
        } catch (Exception e) {
            throw new AppException(ErrorCodeEnum.FILE_READ_ERROR, "Excel文件读取失败", e);
        }
    }
}
```

## 5. 验证迁移

### 5.1 编译测试
```bash
mvn clean compile
```

### 5.2 运行测试
```bash
mvn test
```

### 5.3 启动应用测试
```bash
mvn spring-boot:run
```

## 6. 注意事项

1. **包名变更**：所有SDK组件的包名从 `com.books.app.*` 变更为 `com.books.framework.*`
2. **功能增强**：新的SDK提供了更多功能和更好的错误处理
3. **向后兼容**：大部分API保持兼容，但建议查看具体的方法签名
4. **测试覆盖**：迁移后请确保所有功能都经过充分测试

## 7. 迁移检查清单

- [ ] 更新所有import语句
- [ ] 测试编译是否成功
- [ ] 运行单元测试
- [ ] 测试主要功能
- [ ] 删除原项目中的重复代码
- [ ] 更新文档和注释
- [ ] 提交代码变更
