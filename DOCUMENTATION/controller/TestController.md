# TestController

`TestController` 是一个简单的 Spring REST 控制器，可能用于基本的调试、连接性检查或开发测试。

## API 端点

### 1. 简单测试端点

- **URL:** `/test`
- **HTTP 方法:** `GET`
- **描述:** 一个测试端点，它接受一个字符串作为请求参数，并将其值打印到系统控制台。它不返回任何响应体。
- **请求参数:**
  - `testStr` (String): 要打印到控制台的字符串。
- **响应:**
  - 无 (void)。
