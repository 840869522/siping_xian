# 项目说明文档

本文档旨在详细说明本仓库管理系统（WMS）项目的结构、核心功能、实体关系和主要业务流程。

## 1. 项目文件夹结构

本节将详细介绍项目的主要代码包及其功能。

- **`com.example.wmsmp.config`**:
  - **作用**: 存放应用的配置类。
  - **关键文件**:
    - `MyBatisPlusConfig.java`: 配置MyBatisPlus分页插件和相关设置。
    - `SwaggerConfig.java`: 配置Swagger2，用于生成API文档。
    - `RestTemplateConfig.java`: 配置Spring的 `RestTemplate`，用于进行HTTP请求。

- **`com.example.wmsmp.controller`**:
  - **作用**: MVC模式中的控制器层，负责接收HTTP请求，调用 `service` 层处理业务逻辑，并返回响应。
  - **命名约定**: 通常以 `...Controller.java` 结尾。
  - **示例**: `InOrderController.java` 负责处理所有与入库订单相关的请求。

- **`com.example.wmsmp.entity`**:
  - **作用**: 数据实体（模型）层，定义了与数据库表对应的Java对象。
  - **关键特征**: 通常包含字段、getter/setter方法，并可能使用JPA或MyBatisPlus的注解。
  - **示例**: `InOrder.java` 类代表了数据库中的入库订单表。

- **`com.example.wmsmp.mapper`**:
  - **作用**: 数据访问对象（DAO）层，定义了与数据库进行交互的接口。MyBatisPlus会根据这些接口自动生成SQL。
  - **命名约定**: 通常以 `...Mapper.java` 结尾。
  - **示例**: `InOrderMapper.java` 提供了对入库订单表的CRUD（创建、读取、更新、删除）操作。

- **`com.example.wmsmp.service`**:
  - **作用**: 业务逻辑层。它被 `controller` 调用，用于执行核心业务操作，如数据处理、事务管理等。
  - **关键文件**:
    - `...Service.java`: 业务逻辑接口。
    - `impl/...ServiceImpl.java`: 业务逻辑的实现。
  - **示例**: `InOrderService.java` 定义了与入库订单相关的业务操作，`InOrderServiceImpl.java` 是其具体实现。

- **`com.example.wmsmp.wcs`**:
  - **作用**: 仓库控制系统（Warehouse Control System）相关的功能。这部分代码可能负责与仓库中的硬件设备（如AGV、堆垛机）进行通信和协调。
  - **关键文件**: `Thread...java` 文件（如 `ThreadB.java`, `ThreadD.java`）表明系统使用多线程来处理与特定设备或任务的实时通信。

- **`com.example.wmsmp.led`**:
  - **作用**: 与LED显示屏集成的相关代码。
  - **关键文件**:
    - `Send2LedController.java`: 用于向LED屏幕发送数据的控制器。
    - `LedSDKServer.java`: 可能是一个与LED硬件SDK交互的服务。

- **`com.example.wmsmp.statistic`**:
  - **作用**: 负责数据统计和报表功能。
  - **关键文件**:
    - `StatisticController.java`: 提供统计数据的API接口。
    - `StatisticMapper.java`: 用于执行复杂的统计查询。

- **`com.example.wmsmp.util`**:
  - **作用**: 存放各种工具类，提供公共功能，如日期格式化、HTTP请求帮助等。

## 2. 核心实体与关系

本节将介绍项目中的核心数据实体及其之间的关系。

### 2.1 核心实体说明

- **`BaseMaterial` (物料基础信息)**
  - **作用**: 定义了仓库中存储的物料的基础属性。
  - **关键字段**:
    - `material_code`: 物料的唯一编号。
    - `material_name`: 物料名称。
    - `material_spec`: 物料规格。
    - `material_unit`: 单位。
    - `min_stock`: 最低库存警告线。

- **`InOrder` (入库订单)**
  - **作用**: 记录了入库操作的头部信息。
  - **关键字段**:
    - `in_order_id`: 入库订单的唯一ID。
    - `order_type`: 订单类型（例如，采购入库、退货入库）。
    - `status`: 订单状态（1:创建中, 2:已审核, 3:收货中, 4:已完成）。
    - `org_order`: 原始单号，用于追溯。

- **`InOrderDetail` (入库订单明细)**
  - **作用**: 记录了入库订单中具体的物料项和数量。
  - **关键字段**:
    - `in_order_id`: 关联到 `InOrder` 的外键。
    - `material_code`: 物料编号。
    - `order_count`: 计划入库数量。
    - `actual_count`: 实际入库数量。

- **`OutOrder` (出库订单)**
  - **作用**: 记录了出库操作的头部信息。
  - **关键字段**:
    - `out_order_id`: 出库订单的唯一ID。
    - `order_type`: 订单类型（例如，销售出库、调拨出库）。
    - `status`: 订单状态（1:创建中, 2:待审核, 3:已配盘, 4:已完成）。

- **`OutOrderDetail` (出库订单明细)**
  - **作用**: 记录了出库订单中具体的物料项和数量。
  - **关键字段**:
    - `out_order_id`: 关联到 `OutOrder` 的外键。
    - `material_code`: 物料编号。
    - `order_count`: 计划出库数量。
    - `actual_count`: 实际出库数量。
    - `pick_count`: 已拣货（配盘）数量。

- **`Inventory` (库存)**
  - **作用**: 记录了每个物料在仓库中的具体存储信息，是库存管理的核心。
  - **关键字段**:
    - `pallet_code`: 载具（托盘）编号，表示物料存放在哪个托盘上。
    - `material_code`: 物料编号。
    - `batch`: 批次号。
    - `inventory_count`: 当前库存数量。
    - `frozen_count`: 冻结数量（例如，在出库过程中被锁定的数量）。

### 2.2 实体关系

- **`InOrder` 与 `InOrderDetail`**:
  - 一对多关系。一个入库订单 (`InOrder`) 可以包含多个物料明细 (`InOrderDetail`)。通过 `in_order_id` 字段进行关联。

- **`OutOrder` 与 `OutOrderDetail`**:
  - 一对多关系。一个出库订单 (`OutOrder`) 可以包含多个物料明细 (`OutOrderDetail`)。通过 `out_order_id` 字段进行关联。

- **`BaseMaterial` 与 `Inventory`**:
  - 一对多关系。一种物料 (`BaseMaterial`) 可以在库存 (`Inventory`) 中有多条记录（例如，不同批次、存放在不同托盘上）。通过 `material_code` 关联。

- **`Inventory` 与订单**:
  - **入库**: 当入库完成后，会根据 `InOrderDetail` 的信息，增加或创建 `Inventory` 中的记录。
  - **出库**: 创建出库订单时，系统会检查 `Inventory` 中的可用库存。拣货（配盘）时，会冻结 `Inventory` 中的相应数量。出库完成后，`Inventory` 中的数量会减少。

## 3. 主要业务流程

本节将概述系统中的两个核心业务流程：入库和出库。

### 3.1 入库流程

入库流程指的是将物料接收并存放到仓库中的一系列操作。

1.  **创建入库单**:
    - 用户通过调用 `InOrderController` 中的 `AddInOrder` 接口来创建一个新的入库单 (`InOrder`)。
    - 系统会自动生成一个唯一的入库单号 (`in_order_id`)，并将初始状态设置为 "创建中"。
    - 用户需要提供订单类型、来源单号等基本信息。

2.  **添加入库明细**:
    - 用户通过调用 `InOrderDetailController` (未在本次分析中读取，但功能可以推断) 的接口，为入库单添加具体的物料明细 (`InOrderDetail`)，包括物料编号、数量等。

3.  **提交与审核**:
    - 创建完所有明细后，用户调用 `SubmitInOrder` 接口提交入库单。
    - 系统会检查该入库单是否有关联的明细。如果没有，提交将失败。
    - 提交成功后，订单状态变为 "待审核"。
    - 有权限的用户可以再次调用此接口，将状态更新为 "已审核"，表示入库单已批准。

4.  **收货与上架**:
    - （此部分逻辑在 `wcs` 包中，此处为高层推断）
    - 订单审核通过后，仓库操作员开始接收实体物料。
    - WCS系统会根据 `InOrderDetail` 的信息，指导操作员进行收货。
    - 收货完成后，系统会更新 `InOrderDetail` 中的 `actual_count` (实际收货数量)。
    - 同时，系统会更新 `Inventory` 表，增加相应物料和批次的库存数量。
    - 当所有明细都收货完成，`InOrder` 的状态会更新为 "已完成"。

### 3.2 出库流程

出库流程指的是根据客户或生产需求，从仓库中拣选并发出物料的一系列操作。

1.  **创建出库单**:
    - 与入库类似，通过调用 `OutOrderController` 中的 `AddOutOrder` 接口创建出库单 (`OutOrder`)。
    - 初始状态为 "创建中"。

2.  **添加出库明细**:
    - 为出库单添加具体的物料明细 (`OutOrderDetail`)。

3.  **提交与审核**:
    - 调用 `SubmitOutOrder` 接口提交并审核出库单。流程与入库单审核类似。

4.  **配货与拣货 (配盘)**:
    - （此部分逻辑在 `wcs` 包中，此处为高层推断）
    - 订单审核通过后，WCS系统开始进行配货。
    - 系统会根据 `OutOrderDetail` 的需求，在 `Inventory` 中查找可用的库存（物料、批次）。
    - 找到库存后，系统会"冻结" `Inventory` 中的相应数量（增加 `frozen_count`），防止这部分库存在同一时间被其他订单使用。
    - 同时，系统会更新 `OutOrderDetail` 中的 `pick_count` (已配盘数量)。
    - 当所有明细都完成配货，`OutOrder` 的状态会更新为 "已配盘"。

5.  **出库确认**:
    - 物料被物理地移出仓库后，操作员在系统中进行确认。
    - 系统会减少 `Inventory` 中的 `inventory_count` 和 `frozen_count`。
    - 更新 `OutOrderDetail` 中的 `actual_count` (实际出库数量)。
    - 当所有明细都出库完成，`OutOrder` 的状态会更新为 "已完成"。

## 4. 其他关键功能

本节将简要介绍系统中的其他重要模块。

### 4.1 WCS (仓库控制系统)

- **`com.example.wmsmp.wcs`**
- **作用**: 这是系统的“大脑”和“双手”，负责与物理世界的仓库硬件进行交互。
- **推断功能**:
  - **任务调度**: 根据审核通过的订单，创建并调度硬件任务（例如，AGV搬运、堆垛机存取）。
  - **硬件通信**: `Thread...java` 文件（如 `ThreadB`, `ThreadD`）暗示了系统为不同类型的设备或流程（可能对应不同的库区，如B库、D库）维护着独立的通信线程。这允许系统与硬件进行并行的、实时的状态更新和指令发送。
  - **路径规划与执行**: 对于AGV等移动设备，此模块可能还包含路径规划和任务执行监控的逻辑。

### 4.2 LED 集成

- **`com.example.wmsmp.led`**
- **作用**: 该模块负责与仓库内的LED显示屏进行集成，用于信息可视化。
- **推断功能**:
  - **信息发布**: `Send2LedController.java` 提供了一个接口，允许系统的其他部分向LED屏幕发送消息。
  - **显示内容**: 可能用于显示实时状态，如“月台X，正在装载订单Y”、“货位Z，等待上架”等，提高仓库作业的透明度和效率。

### 4.3 统计与报表

- **`com.example.wmsmp.statistic`**
- **作用**: 提供数据分析和报表功能，帮助管理层了解仓库运营状况。
- **推断功能**:
  - **数据接口**: `StatisticController.java` 提供了用于查询统计数据的API。
  - **分析维度**: 可能包括库存周转率、订单完成时间、库区利用率、操作员绩效等关键绩效指标（KPI）的统计。
