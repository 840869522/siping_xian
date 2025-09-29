# 模块四：出库管理模块

## 模块概述

出库管理模块是整个WMS系统业务逻辑最复杂、设计最精巧的核心模块。它负责处理从接收出库订单到拣货、再到最终发货的全过程。该模块不仅管理订单本身，更重要的是，它扮演着**出库策略引擎**的角色，将抽象的订单需求转化为一系列具体的、可执行的、与物理库存联动的拣货任务。

主要职责包括：
1.  **出库单生命周期管理**：管理出库单从“创建”到“审核”、“已配盘”直至“已完成”的完整工作流。
2.  **库存分配与预留（配盘）**：这是本模块的核心。它包含一套复杂的算法，用于检查库存可用性、防止超卖，并为订单**冻结**库存，生成具体的拣货任务。
3.  **拣货与发运模拟**：通过一系列原子性的API，模拟了物料从存储位被拣选到周转箱，再从周转箱被确认发运的全过程。
4.  **高度的可追溯性与自动化基础**：通过`@FlowAnnotation`和`OutPick`任务实体的设计，为每一次库存的分配、移动和发出都留下了详细的审计日志，并为与WCS（仓库控制系统）集成、调度自动化设备执行拣货提供了标准的数据接口。

该模块的设计充分体现了现代WMS在订单履行、库存控制和自动化集成方面的先进理念。

---

## 文件深度分析

### 1. `controller/OutOrderController.java` & `controller/OutOrderDetailController.java`

*   **文件职责**: 这两个控制器负责管理出库单头（`OutOrder`）和明细（`OutOrderDetail`），其API结构和设计与入库模块高度一致，体现了良好的设计复用和统一的订单管理框架。
*   **关键方法分析**:
    *   `AddOutOrder`/`AddOutOrderDetail`: 手动创建出库单头和明细。
    *   `Adds` (头/明细): 供上游系统高性能批量创建出库单和明细的集成接口。
    *   `SubmitOutOrder`: 提交/审核出库单，在更新状态前会校验必须存在明细。
    *   `DelOutOrder`: 事务性地删除出库单及其所有明细。
    *   `Get...Excel`/`Import...Excel`: 强大的Excel导入导出功能。

### 2. `controller/OutPickController.java`

*   **文件职责**: **出库模块的大脑和发动机**。它不直接面向用户，而是作为出库流程的“计划与执行”引擎，负责将订单需求转化为物理世界的作业任务。
*   **关键方法分析**:
    *   `OrderPick(...)` -> `DetailPick(...)` -> `GetPickPalletResult(...)`: **核心配盘算法链路**。这是一个由外到内层层调用的方法链，共同完成了从接收订单到生成任务的全过程。
        1.  **`OutPickDetails` (需求计算)**: 算法的第一步。它会过滤出所有真正需要配盘的订单明细（即`计划出库数 > 已出库数 + 已配盘数`），并精确计算出每种物料（物料编码+批次）还需配盘的具体数量。
        2.  **`GetPickPalletResult` (库存检查)**: **这是防止超卖的核心安全门**。算法遍历所有待配物料，查询`v_map_inv`视图（一个预先join了货位信息的库存视图），计算每种物料的**可用库存** (`inventory_count - frozen_count`)。如果任何一种物料的可用库存总和小于订单需求，则配盘失败并立即返回错误信息，终止后续操作。
        3.  **`GetPickPalletResult` (任务生成与库存冻结)**: 如果库存充足，算法进入执行阶段。它会按货位（`location_area`）排序以优化拣货路径，然后**贪心分配**库存。每成功分配一个库存源，就会：
            *   **创建拣货任务**: `new OutPick()`，生成一条包含“从哪里、拿什么、拿多少、到哪里”的原子指令。
            *   **冻结库存**: 以事务方式更新`Inventory`表的`frozen_count`字段（`frozen_count = frozen_count + 拣货数`），将这部分库存“锁定”。
            *   **更新明细进度**: 更新`OutOrderDetail`的`pick_count`字段（`pick_count = pick_count + 拣货数`），记录已分配进度。
    *   `PickPallet(@RequestBody OutPick outPick, String palletCode)`: **物理拣货模拟**。该方法模拟了WCS或工人执行一条`OutPick`任务的物理过程，是一次典型的“一减一增”库存移动。
        1.  **源头扣减 (LOSS)**: 调用`PickPallet23`子方法，它会同时减少源`Inventory`记录的`inventory_count`和`frozen_count`。如果库存全部拣完，则删除该条库存记录。
        2.  **目标增加 (ADD)**: 调用`PickPallet33`子方法，在`palletCode`（代表订单箱/周转箱）上创建或增加一条新的`Inventory`记录。这条新记录同样是冻结状态并关联了出库单。
    *   `Checkout(@RequestBody Inventory inventory)`: **确认发运**。模拟货物在出库口被扫描并最终发货的场景。
        1.  **更新明细**: 增加`OutOrderDetail`的`actual_count`（实际出库数量）。
        2.  **删除库存**: 调用`inventoryService.removeById(...)`，将代表已装入周转箱的`Inventory`记录从数据库中**彻底删除**，标志着该批货物已经物理上离开了仓库，其库存生命周期在WMS中正式终结。

### 3. `entity/OutOrder.java` & `entity/OutOrderDetail.java`

*   **文件职责**: 出库单头和明细的实体。
*   **关键字段**:
    *   `OutOrder.status`: **核心流程状态机**，其值的变化（创建中 -> 已审核 -> 已配盘 -> 已完成）严格控制着出库流程的推进。
    *   `OutOrderDetail`的三个核心数量字段，共同提供了对出库流程的精细化进度跟踪：
        *   `order_count`: **计划出库数**（总目标）。
        *   `pick_count`: **已配盘/已分配数**（已为之冻结了多少库存）。
        *   `actual_count`: **实际已出库数**（已确认发运了多少）。

### 4. `entity/OutPick.java`

*   **文件职责**: **拣货任务实体**，是配盘算法的最终产出，也是连接WMS与WCS的桥梁。
*   **关键字段**:
    *   **任务来源**: `out_order_id`, `out_order_detail_id` (为了哪个订单)。
    *   **任务源**: `inventory_id`, `pallet_code`, `cell_id` (从哪个库存、哪个物理位置拣货)。
    *   **任务内容**: `material_code`, `pick_qty` (拣什么、拣多少)。
    *   **任务目的地**: `pickstation_no`, `target_no` (送到哪个拣货台的哪个口)。
    *   `status`: 任务本身的执行状态（待执行/已完成）。

在自动化仓库中，WCS系统会持续监控`t_out_pick`表，一旦发现有新的“待执行”任务，就会立即调度AGV或堆垛机去执行这些物理操作。