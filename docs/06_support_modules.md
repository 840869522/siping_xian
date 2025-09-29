# 模块六：外围设备、支持与工具模块

## 模块概述

本文档涵盖了WMS系统中一系列关键的支持模块。它们虽然不直接处理核心的入库或出库订单流程，但为整个系统的稳定运行、可维护性、数据分析能力和硬件集成能力提供了不可或缺的基础。

主要包括：
1.  **外围设备集成**: 提供了与物理硬件（如LED显示屏、PLC控制器）进行通信的能力。
2.  **数据统计与分析**: 将原始的操作数据转化为有价值的管理视图和KPI指标。
3.  **核心框架配置**: 负责Spring Boot、MyBatis-Plus、Swagger等核心框架的初始化配置。
4.  **通用工具**: 封装了可在项目中多处复用的、与具体业务无关的功能代码。

---

## 一、 外围设备集成

### 1.1 LED显示模块 (`led/`)

该模块是一个设计精良的硬件集成范例，通过清晰的三层结构实现了与物理LED屏幕的可靠通信。

*   **`led/Send2LedController.java` (API层)**
    *   **文件职责**: 提供一个简单的`/led/send/text` REST API入口，允许系统其他部分触发向LED发送消息的操作。
    *   **`materialChart(...)`**: 接收包含文本的请求，并依次调用`LedCmdUtil`的方法来检查在线状态、构建显示内容（文本和日期），并最终将内容发送到屏幕上。

*   **`led/cmd/LedCmdUtil.java` (命令/适配器层)**
    *   **文件职责**: 封装了所有与LED屏幕硬件通信的底层命令和SDK调用逻辑，将高级请求翻译成具体的硬件控制指令。
    *   **`checkOnline()`**: 检查LED控制器状态。**核心健壮性设计在于**：如果检查失败（网络断开），它会立即尝试调用`ledSDKServer.start()`来进行**自动重连**。
    *   **`sendLed(...)`**: 发送内容到屏幕的核心方法。它会创建一个“节目文件”，添加内容，并在发送前**删除屏幕上具有相同ID的旧节目**，以确保内容是被“替换”而不是“追加”。
    *   **`buildText(...)` & `buildDate()`**: “工厂方法”，负责使用`onbon.bx06` SDK创建格式化的文本和日期显示区域，封装了所有样式设置的复杂细节（字体、颜色、特效等）。

*   **`led/server/LedSDKServer.java` (连接层)**
    *   **文件职责**: 作为一个单例`@Component`，负责管理和维护与物理LED屏幕之间的持久化网络连接。
    *   **`start()`**: 核心连接方法。它从配置文件读取IP和端口，初始化SDK，建立TCP连接。连接成功后，立即调用`screen.syncTime()`校准屏幕时间。
    *   **`stop()`**: 提供断开连接的接口，用于优雅地释放资源。

### 1.2 PLC直连驱动 (`util/OpcPlcHelper.java`)

*   **文件职责**: 一个专门用于**直接与西门子S7系列PLC（可编程逻辑控制器）进行通信**的底层硬件驱动，用于控制智能货柜。
*   **技术实现**: 使用了`iot-protocol-s7`第三方库，实现了Java程序对PLC内存地址的直接读写。
*   **关键方法分析**:
    *   `getplc()`: **前置安全检查方法**。在执行任何操作前，通过`s7PLC.readBoolean/readInt16`读取多个PLC内存地址（如`I3.4`, `M96`），检查设备有无物理干涉、故障报警、是否处于远程模式以及是否空闲。这是**至关重要的安全逻辑**，确保了WMS不会在设备状态不正确时强行下发指令。
    *   `setOutplc(int storageNo, int layer)`: **下发取货指令**。遵循经典的PLC控制模式：“设置参数，然后拉动扳机”。它先调用`getplc()`进行安全检查，通过后，将目标层号`layer`写入PLC的数据区（`M208`），然后将“取货执行”的触发位（`M30.3`）置为`true`，启动PLC程序。
    *   `setInPlc(int storageNo)`: **下发存货指令**，逻辑与取货类似。

---

## 二、 数据统计与分析模块 (`statistic/`)

该模块是一个独立的、功能完善的数据分析单元，通过两个不同的Controller和共享的Service层提供服务。

*   **`statistic/controller/DataAnalysisController.java`**
    *   **文件职责**: 为**数据可视化仪表盘（Dashboard）**提供后端数据接口，驱动KPI图表。
    *   **关键API**: 提供了`success/pie`（任务成功率饼图）、`material/ranking`（物料排行榜）、`inventory/use`（库存利用率）、`task/week`（周任务趋势折线图）等专门为图表设计的接口。

*   **`statistic/controller/StatisticController.java`**
    *   **文件职责**: 提供更深入、更具体的**库存相关统计报表**的数据接口。
    *   **关键API**: 提供了`day/chart`（日出入库排行）、`library/statement`（出入库柱状图）、`pie/chart`（库存构成饼图）等用于生成详细报表的接口。

*   **设计模式**: 两个Controller都注入并使用了**同一个`StatisticService`**。所有复杂的SQL聚合查询和数据处理逻辑都被统一封装在Service和Mapper层，Controller层保持了“瘦”和简洁，只负责路由。

---

## 三、 核心框架配置模块 (`config/`)

*   **`config/MyBatisPlusConfig.java`**
    *   **职责**: 配置MyBatis-Plus的**分页拦截器**。
    *   **关键配置**: `new PaginationInnerInterceptor(DbType.KINGBASE_ES)`，指明了项目使用的数据库是**人大金仓**，并为其启用了自动化物理分页。

*   **`config/MyBatisPlusMetaObjectHandler.java`**
    *   **职责**: 实现**公共字段的自动填充**。
    *   **关键方法**: `insertFill`会在插入时自动填充`create_time`和`update_time`；`updateFill`会在更新时自动填充`update_time`。极大地简化了业务代码。

*   **`config/RestTemplateConfig.java`**
    *   **职责**: 向Spring容器注册一个全局的`RestTemplate` Bean，作为整个应用调用外部HTTP API（特别是WCS接口）的基础工具。

*   **`config/SwaggerConfig.java`**
    *   **职责**: 配置和启用**Swagger 2**，实现API文档的自动化生成。
    *   **关键配置**: `.apis(RequestHandlerSelectors.basePackage("com.example.wmsmp.controller"))`，指定了只扫描`controller`包来生成API文档。

*   **`config/SpringUtil.java`**
    *   **职责**: **整个WCS集成架构的关键“粘合剂”**。它通过实现`ApplicationContextAware`接口，提供了一个静态方法`getBean(...)`，允许在**非Spring管理的Bean**（如手动`new`出来的`Thread`子类）中，安全地获取到由Spring容器管理的服务（如`TaskBService`）。

---

## 四、 通用工具模块 (`util/`)

*   **`util/OutHelper.java`**
    *   **职责**: 仓库的“静态地图”，封装了与仓库物理布局相关的硬编码数据。
    *   **关键方法**: `getSet...()`方法返回特定功能区域（如机器人拣选区）的所有端口编号；`getDownPortB(...)`则提供了一个简单的静态路由表，在出库配盘时用于为拣货任务分配合理的目标口。

*   **`util/DateUtil.java`**
    *   **职责**: 标准的日期处理工具类，提供了在`String`和`Date`之间进行安全、统一格式转换的静态方法。

*   **`util/MapHelper.java`**
    *   **职责**: 一个已**废弃**的货位辅助类。
    *   **历史意义**: 其内部的`getEmptyLocation`方法揭示了系统曾经使用过的“寻找可用货位”的策略（过滤空货位，然后按深度排序），为了解系统演进提供了线索。