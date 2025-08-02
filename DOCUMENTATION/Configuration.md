# 应用程序配置

本文档描述了 WMS 应用程序的主要配置文件。

---

## `main/resources/application.properties`

这是 Spring Boot 应用程序的主要配置文件。它包含几个关键部分：

### 服务器配置
- `server.address`: 应用程序将绑定的 IP 地址。当前设置为 `127.0.0.1`，但有用于不同环境的注释掉的备用选项。
- `server.port`: 应用程序将运行的端口，设置为 `9999`。

### 数据库配置
- 应用程序配置为连接到 **Kingbase8** (人大金仓) 数据库。
- `spring.datasource.driver-class-name`: `com.kingbase8.Driver`
- `spring.datasource.url`: `jdbc:kingbase8://127.0.0.1:54321/sp_wms`
- `spring.datasource.username`: `system`
- `spring.datasource.password`: `123456`
- 文件中还包含被注释掉的 MySQL 和 SQL Server 配置，表明该应用程序可以适应不同的数据库后端。

### MyBatis-Plus 配置
- `mybatis-plus.configuration.map-underscore-to-camel-case=false`: 这是一个值得注意的设置。它禁用了 `snake_case` (下划线命名) 的数据库列到 `camelCase` (驼峰命名) 的实体字段的自动映射，这意味着实体中的 `@TableField` 注解必须与列名完全匹配。

### 外部 API 端点
该文件的很大部分专门用于硬编码各种外部系统的 URL，这对于 WCS 集成至关重要：
- **`apiX.*`**: 用于 "XZK" (天地人) 穿梭板系统的端点。
- **`apiD.*`**: 用于 "DJK" (大件库) WCS 的端点。
- **`apiZ.*`**: 用于 "ZDH" (主自动化库) WCS 的端点。
- **`apiAGV.*`**: 用于 AGV 控制系统的端点。
- **`apiCTU.*`**: 用于 CTU 控制系统的端点。
- **`check.up`**: 用于向上游系统报告已完成盘点订单的端点。

### LED 屏幕配置
- **`led.*`**: 定义了 OnBon LED 屏幕的连接详情（IP、端口、超时）和物理尺寸（x、y、宽度、高度）。

---

## `main/resources/log4j.properties`

该文件使用 Log4j 配置应用程序的日志记录。

- **根记录器:** 根记录器的级别设置为 `DEBUG`。
- **Appenders (输出目的地):**
  - `stdout`: 将日志消息输出到控制台。
  - `D`: 一个 `DailyRollingFileAppender`，它将所有 `DEBUG` 级别及以上的消息记录到 `E://logs/log.txt`，日志文件每天轮换。
  - `E`: 一个 `DailyRollingFileAppender`，它仅将 `ERROR` 级别及以上的消息记录到 `E://logs/error.log`。
- **注意:** 文件路径被硬编码到 `E:` 驱动器，这可能需要根据部署环境进行更改。
