# LED 集成包 (`led`)

该包包含负责与 OnBon BX-06 系列 LED 屏幕集成和控制的类。它处理从底层网络连接到为其他服务提供高级 REST 端点的所有事务。

## 类分解

### `led.server.LedSDKServer`

- **目的:** 管理与物理 LED 屏幕的网络连接的生命周期。
- **关键逻辑:**
  - 从 `application.properties` 读取屏幕的 IP 地址、端口和连接超时时间。
  - `start()` 方法使用 `onbon.bx06.Bx6GScreenClient` 库建立与屏幕的连接。
  - `stop()` 方法断开与屏幕的连接。
  - 它持有活动 `screen` 客户端对象，其他类使用该对象与硬件通信。

### `led.cmd.LedCmdUtil`

- **目的:** 一个工具类，它抽象了 OnBon LED 库的复杂性。它提供了创建和向屏幕发送内容的便捷方法。
- **关键逻辑:**
  - **`sendLed(...)`**: 核心发送方法。它接收一个或多个 `BxArea` 对象（如文本或时钟），将它们包装在 `ProgramBxFile` 中，删除屏幕上具有相同 ID 的任何旧节目，并写入新节目。
  - **`buildText(...)` / `buildCarText(...)`**: 创建 `TextCaptionBxArea` 的辅助方法。它们配置文本内容、对齐方式、字体、颜色和显示效果（如滚动或静态显示）。
  - **`buildDate()`**: 一个创建 `DateTimeBxArea` 以显示当前日期和时间的辅助方法。
  - **`checkOnline()`**: 检查屏幕是否已连接。如果没有，它会尝试通过调用 `ledSDKServer.start()` 来重新连接。

### `led.Send2LedController`

- **目的:** 一个简单的 REST 控制器，它将 LED 功能暴露给 WMS 的其余部分或外部系统。
- **端点:**
  - `POST /led/send/text`:
    - **描述:** 在请求体中接收一个文本字符串。
    - **逻辑:** 它调用 `ledCmdUtil.checkOnline()` 以确保连接。然后，它使用 `ledCmdUtil` 构建一个文本区域和一个日期/时间区域，并将它们作为两个独立的节目发送到 LED 屏幕。
