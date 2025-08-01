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

### 2.1 `entity` (核心业务实体)

- **`InOrder` / `OutOrder`**: 入库/出库订单头信息。
- **`InOrderDetail` / `OutOrderDetail`**: 入库/出库订单的明细。
- **`Inventory`**: 库存信息，记录物料在特定载具上的数量和状态。
- **`OutPick`**: 出库配盘信息，记录了为出库单分配的具体库存和数量。
- **`CheckBean` / `CheckDetailBean`**: 库存盘点单和盘点明细。
- **`Flow`**: 操作流水记录。
- **`LocationMap`**: 货位与托盘的绑定关系。
- **`InOrderExcel` / `OutOrderExcel`**: 用于导入/导出订单的Excel数据模型。
- **`VMapInv` / `VSelectDplocation`**: 数据库视图对应的实体，用于复杂的关联查询。

### 2.2 `entity.base` (基础数据实体)

- **`BaseMaterial`**: 物料主数据，定义了物料的各种属性。
- **`BasePallet`**: 载具（托盘）主数据。
- **`BaseEquipment` / `BaseEquipmentDetail`**: 设备及其保养/维修记录。
- **`BaseETag`**: 电子标签与货位/料箱的绑定关系。

### 2.3 `entity.system` (系统管理实体)

- **`SysUser`**: 系统用户。
- **`SysRole`**: 用户角色及权限（菜单）。
- **`SysDictionary`**: 数据字典。
- **`InterReturn` / `ResReturn`**: API的标准返回格式。

### 2.4 `entity.task` (自动化任务实体)

此包下的实体用于定义与WCS（仓库控制系统）交互的任务数据结构。

- **`task.AGV` / `task.CTU`**: 分别定义了与AGV（自动导引车）和CTU（箱式堆垛机）相关的任务、状态和位置信息。
- **`task.D`**: 与大件库（D库）相关的任务实体。
- **`task.X`**: 与箱组库（X库）相关的任务实体。
- **`task.Z`**: 这是一个总的自动化任务包，定义了与各种自动化设备交互的通用任务结构，如：
    - `TaskB`: 料箱库任务。
    - `TaskP`: 托盘库任务。
    - `ZTask`: 通用的WCS任务下发结构。
    - `ZTaskLight1`/`ZTaskLight2`: 电子标签亮灯任务。
    - `ZTaskSorter1`/`ZTaskSorter2`: 分拣机任务。

### 2.5 实体关系

- **订单与库存**:
  - 入库单 (`InOrder`) 完成后，其明细 (`InOrderDetail`) 会转化为库存 (`Inventory`) 的增加。
  - 出库单 (`OutOrder`) 会触发配盘 (`OutPick`)，配盘会冻结 `Inventory` 中的库存，出库完成后库存减少。
- **基础数据与业务**:
  - 所有业务实体都依赖于基础数据实体，如 `BaseMaterial` 和 `BasePallet`。
- **任务与业务**:
  - 审核通过的订单（入库/出库/盘点）会转化为具体的自动化任务（如 `TaskB`, `TaskP` 等），由WCS执行。

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

## 5. 控制器(Controller)详解

本节将详细介绍每个控制器的功能和主要的API端点。

### 5.1 核心业务控制器

- **`InOrderController`**:
  - **作用**: 管理入库订单的创建、查询、更新和删除。
  - **主要API**:
    - `/InOrder/GetInOrderByFuzzy`: 根据多种条件（如订单号、类型、状态等）分页模糊查询入库单。
    - `/InOrder/AddInOrder`: 创建一个新的入库单。
    - `/InOrder/SubmitInOrder`: 提交入库单进行审核。
    - `/InOrder/DelInOrder`: 删除入库单及其所有明细。
    - `/InOrder/EditInOrder`: 编辑一个已存在的入库单。

- **`InOrderDetailController`**:
  - **作用**: 管理入库订单的明细项。
  - **主要API**:
    - `/InOrderDetail/GetInOrderDetailBy`: 根据订单ID等查询明细。
    - `/InOrderDetail/AddInOrderDetail`: 为指定的入库单添加明细。
    - `/InOrderDetail/DelInOrderDetail`: 删除一条入库单明细。
    - `/InOrderDetail/EditInOrderDetail`: 编辑一条入库单明细。

- **`OutOrderController`**:
  - **作用**: 管理出库订单。功能与 `InOrderController` 类似。
  - **主要API**:
    - `/OutOrder/GetOutOrderByFuzzy`: 分页模糊查询出库单。
    - `/OutOrder/AddOutOrder`: 创建出库单。
    - `/OutOrder/SubmitOutOrder`: 提交出库单进行审核。

- **`OutOrderDetailController`**:
  - **作用**: 管理出库订单的明细项。功能与 `InOrderDetailController` 类似。
  - **主要API**:
    - `/OutOrderDetail/GetOutOrderDetailBy`: 查询出库明细。
    - `/OutOrderDetail/AddOutOrderDetail`: 添加出库明细。

- **`OutPickController`**:
  - **作用**: 管理出库配盘操作，这是出库流程中的一个关键步骤，用于分配库存和生成拣货任务。
  - **主要API**:
    - `/OutPick/GetOutPickBy`: 查询配盘信息。
    - `/OutPick/OrderPick`: 对整个出库订单进行配盘。
    - `/OutPick/Checkout`: 确认出库，完成拣货操作。

- **`InventoryController`**:
  - **作用**: 管理库存信息。
  - **主要API**:
    - `/Inventory/GetInventoryBy`: 条件查询库存。
    - `/Inventory/AddInventory`: 新增库存记录，通常用于无单收货或库存初始化。
    - `/Inventory/UpdateInventory`: 调整库存数量。
    - `/Inventory/AddReceiveMaterial`: PDA收货接口，在有单收货时更新库存。

- **`CheckController` & `CheckDetailController`**:
    - **作用**: 管理库存盘点操作，包括盘点单的创建、盘点、配盘和完成。
    - **主要API**:
        - `/check/page`: 分页查询盘点单。
        - `/check/add`: 新增盘点单。
        - `/check/pick`: 对盘点单进行配盘，生成拣货任务以核对库存。
        - `/check/detail/update`: 更新盘点明细，记录实际盘点数量。

### 5.2 基础数据控制器

- **`BaseMaterialController`**:
  - **作用**: 管理物料的基础信息。
  - **主要API**:
    - `/BaseMaterial/GetMaterialByFuzzy`: 模糊查询物料。
    - `/BaseMaterial/AddMaterial`: 添加新物料。
    - `/BaseMaterial/UpdateMaterial`: 更新物料信息。

- **`BasePalletController`**:
  - **作用**: 管理载具（托盘）信息。
  - **主要API**:
    - `/BasePallet/GetPalletByFuzzy`: 查询载具。
    - `/BasePallet/AddPallet`: 添加新载具。

- **`BaseEquipmentController` & `BaseEquipmentDetailController`**:
  - **作用**: 管理设备信息及其保养、维修记录。
  - **主要API**:
    - `/BaseEquipment/GetBaseEquipmentBy`: 查询设备。
    - `/BaseEquipmentDetail/AddBaseEquipmentDetail`: 添加设备的保养或维修记录。

- **`LocationMapController`**:
  - **作用**: 管理仓库货位与托盘的绑定关系。
  - **主要API**:
    - `/LocationMap/GetAllLocationMapByFuzzy`: 查询货位绑定关系。
    - `/LocationMap/UpdateLocationMapByLocation`: 更新货位的状态或绑定的托盘。

### 5.3 系统与硬件交互控制器

- **`TaskAgvController`**, **`TaskCtuController`**, **`TaskBController`**, **`TaskPController`**, **`TaskDController`**, **`TaskXController`**:
  - **作用**: 这些控制器分别管理与不同自动化设备（AGV、CTU、料箱库、托盘库等）的任务交互。它们负责创建、查询和更新发送给WCS（仓库控制系统）的任务。
  - **主要API**:
    - `/Task.../TaskDownload`:（AGV/CTU）下发任务给硬件控制系统。
    - `/Task.../Task...Insert`:（B/P/D/X库）创建新的硬件任务。
    - `/Task.../Get...ByPage`: 分页查询任务状态。
    - `/Task.../Update...StatusByTaskNo`: 更新任务状态。

- **`TaskZController`**:
    - **作用**: 这是一个总的自动化库任务控制器，负责与WCS进行更复杂的交互，如电子标签亮灯、任务申请与上报等。
    - **主要API**:
        - `/TaskZ/ZDKtaskReceive`: 下发自动化库任务。
        - `/TaskZ/ZDKETagLight`: 控制电子标签亮灯。
        - `/TaskZ/taskApply`: 接收来自WCS的任务申请（如入库请求）。
        - `/TaskZ/taskReport`: 接收来自WCS的任务状态完成报告。

- **`BarrierGateController`**:
  - **作用**: 与道闸系统集成，当车辆进入时，根据车牌号查询相关订单并可能在LED屏幕上显示信息。
  - **主要API**:
    - `/barrier/gate/car/num`: 接收道闸系统发送的车辆信息。

- **`LedController`**:
  - **作用**: 控制LED显示屏。
  - **主要API**:
    - `/led/addOrUpdate`: 新增或更新LED屏幕上显示的内容。

### 5.4 系统管理与统计控制器

- **`SysUserController`**:
  - **作用**: 管理系统用户。
  - **主要API**:
    - `/User/Login`: 用户登录验证。
    - `/User/GetAllSysUserBy`: 查询用户信息。
    - `/User/AddSysUser`: 添加新用户。

- **`SysRoleController`**:
  - **作用**: 管理用户角色和权限。
  - **主要API**:
    - `/Role/GetAllRoleName`: 获取所有角色名称。
    - `/Role/GetSysRoleByRoleName`: 根据角色名查询其拥有的菜单权限。
    - `/Role/AddSysRoleInfo`: 为新角色分配权限。

- **`SysDictionaryController`**:
  - **作用**: 管理系统的数据字典，用于下拉列表等。
  - **主要API**:
    - `/Dictionary/GetDropDownList`: 根据字典类型获取下拉列表数据。

- **`DataAnalysisController` & `StatisticController`**:
    - **作用**: 提供数据统计和分析的API，用于生成报表和看板。
    - **主要API**:
        - `/analysis/success/pie`: 查询出入库任务成功率。
        - `/analysis/material/ranking`: 查询物料排行榜。
        - `/statistic/library/statement`: 生成出入库物料的柱状图。
