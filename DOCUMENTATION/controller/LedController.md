# LedController

`LedController` 是一个 Spring REST 控制器，负责管理与 WMS 集成的物理 LED 屏幕上显示的内容。

## API 端点

### 1. 获取 LED 显示内容

- **URL:** `/led/find`
- **HTTP 方法:** `POST`
- **描述:** 检索 LED 显示的当前内容配置。它旨在获取单个主要配置，返回在数据库中找到的第一条记录。
- **请求体:** 无。
- **响应:**
  - 一个 `InterReturn` 对象，其 `result` 字段包含一个带有当前显示文本的 `LedModel` 对象。

### 2. 添加或更新 LED 显示内容

- **URL:** `/led/addOrUpdate`
- **HTTP 方法:** `POST`
- **描述:** 创建一个新的 LED 内容配置或更新现有配置。数据成功保存到数据库后，此端点会立即触发一个命令，将新文本发送到物理 LED 屏幕进行显示。
- **请求体:**
  - `ledModel`: 一个包含 `ledId` 和新 `ledText` 的 `LedModel` 对象。
- **响应:**
  - 一个指示操作成功或失败的 `InterReturn` 对象。
