# 模块二：库存管理模块

## 模块概述

库存管理模块是整个WMS系统的心脏。其核心职责是实时、精确地追踪和管理仓库内所有物料的数量、位置和状态，并通过周期性的盘点来保证账实相符。该模块的设计直接决定了仓库运营的效率和准确性。

主要职责包括：
1.  **库存生命周期管理**：提供对库存的增、删、改、查、移动等全生命周期操作。
2.  **库存收货与上架**：与入库模块紧密集成，在收货环节创建或更新库存记录。
3.  **库存盘点与调整**：提供完整的盘点流程，包括盘点单创建、库存冻结、盘点执行、结果录入和库存解冻，为后续的库存调整提供依据。
4.  **库存冻结与预留**：通过 `frozen_count` 字段实现先进的库存预留机制，为出库订单分配库存，有效防止超卖。

该模块的实现充分利用了数据库事务和AOP，确保了所有关键操作的原子性和可追溯性。

---

## 文件深度分析

### 1. `controller/InventoryController.java`

*   **文件职责**: 库存操作的核心API入口，提供了对库存的直接增、删、改、查和移动功能。
*   **关键方法分析**:
    *   `GetInventoryBy(...)`: 提供一个强大的、多条件的**库存分页查询**功能。使用MyBatis-Plus的 `LambdaQueryWrapper` 动态构建SQL查询条件。
    *   `AddInventory(@RequestBody Inventory tInventory)`: **无单收货**或手动新增库存。在严格的参数校验后，直接调用 `inventoryService.save()` 创建新的库存记录。通过 `@FlowAnnotation` 标记，自动记录“增加”类型的业务流水。
    *   `AddReceiveMaterial(@RequestBody Inventory inventory, String inOrderId)`: **PDA收货核心事务**。这是一个复杂的、原子性的操作，用于处理有明确入库订单的收货场景。
        1.  **前置校验**: 检查订单号、托盘号、物料、数量等参数的合法性。
        2.  **校验订单状态**: 查询 `InOrder`，确保其状态是“已审核”或“收货中”，防止对未审核或已完成的订单进行收货。
        3.  **更新入库单明细**: 使用 `LambdaUpdateWrapper` 和 `setSql("actual_count=actual_count+" + ...)` 以增量方式安全地更新 `in_order_detail` 的实收数量。同时判断 `实收数 + 本次收货数` 是否等于或超出 `订单数`，以决定是否将明细状态更新为“已完成”。
        4.  **更新库存**: 查询 `t_inventory` 中该托盘上是否存在相同物料和批次。若存在，则累加数量；若不存在，则 `inventoryService.save(inventory)` 创建一条新库存记录。
        5.  **更新入库单头状态**: 检查该订单下所有明细的状态，如果全部为“已完成”，则将 `InOrder` 主单状态更新为“已完成”，否则更新为“收货中”，实现了流程的自动闭环。
    *   `BindPallet(@RequestBody Inventory inventory, String palletCodeOld, String cellIdOld)`: **组托/拆托核心事务**，即库存移动。
        1.  **源头扣减**: 查询 `palletCodeOld`（源托盘）的库存，校验移动数量是否充足。若充足，则通过 `set("inventory_count", ...)` 更新库存；若移动数量等于全部库存，则调用 `inventoryService.removeById(...)` 直接删除源库存记录。
        2.  **目标增加**: 查询 `inventory.getPallet_code()`（目标托盘）是否存在相同物料。若存在，则累加数量；若不存在，则创建一条新的库存记录。
        3.  **注解驱动流水**: 该方法被 `@FlowAnnotation(..., businessType = BusinessType.ADD_AND_LOSS)` 标记，`FlowAspect` 会自动为其生成“一减一增”两条详细的库存移动流水。

### 2. `controller/CheckController.java`

*   **文件职责**: 管理库存盘点任务的全生命周期，是保证账实相符的关键。
*   **关键方法分析**:
    *   `OrderPick(@RequestBody CheckBean checkBean, String[] ports)`: **盘点配盘**。
        *   **设计模式**: 这是整个项目中最精妙的设计之一，完美体现了**逻辑复用**的思想。
        *   **实现**: 它将盘点明细（`CheckDetailBean`）在内存中**伪装**成出库订单明细（`OutOrderDetail`），然后直接调用与真实出库完全相同的`DetailPick`方法。
        *   **效果**: 这使得盘点业务能够完全复用出库模块最复杂的**库存分配和冻结逻辑**，而无需重复开发。配盘算法会为盘点任务找到可用库存，生成`OutPick`拣货任务，并增加`Inventory`表的`frozen_count`，将这部分库存锁定，防止在盘点期间被其他出库订单占用。
    *   `downAdd(@RequestBody List<CheckBean> checkBeanList)`: **上游系统下发盘点任务**。接收一个包含头和明细的复杂JSON，通过遍历列表，同时向`t_check`和`t_check_detail`两张表中写入数据，快速创建完整的盘点任务。
    *   `sendUp()`: **盘点结果上传至上游系统**。查询状态为“已完成”且`type=1`（上游下发）的盘点单，将盘点结果（`checkCount`）组装成上游系统要求的JSON格式，通过`RestTemplate`回传数据。成功后，将盘点单的`type`改为`3`（已回传），防止重复发送。

### 3. `controller/CheckDetailController.java`

*   **文件职责**: 管理盘点明细，负责录入盘点结果，并完成库存状态的闭环。
*   **关键方法分析**:
    *   `EditCheckDetailBean(@RequestBody CheckDetailBean checkDetailBean)`: **录入盘点结果与库存解冻**。
        1.  **更新明细**: 将前端传入的实际盘点数`checkCount`更新到`t_check_detail`表中，并将该明细行的状态设为“已完成”。
        2.  **触发库存解冻**: 调用`callbackInventory(checkDetailBean)`方法。
        3.  **检查并关闭主单**: 检查该盘点单下的所有明细是否都已“已完成”，如果是，则自动将`CheckBean`主单状态也更新为“已完成”，实现流程自动化。
    *   `callbackInventory(CheckDetailBean checkDetailBean)`: **库存解冻核心逻辑**。当一个物料盘点完成后，此方法被调用。它会找到在盘点配盘时被冻结的所有相关库存（根据物料编码和批次），并使用`LambdaUpdateWrapper`将它们的`frozen_count`**重置为0**，从而释放库存，使其重新变为可用状态，形成“冻结-解冻”的完整闭环。

### 4. `entity/Inventory.java`

*   **文件职责**: 库存实体，定义了`t_inventory`表，是库存管理模块的原子数据单元。
*   **关键字段**:
    *   **业务唯一键**: `pallet_code` (载具), `cell_id` (格子), `material_code` (物料), `batch` (批次) 的组合构成了库存记录的唯一业务标识。
    *   `inventory_count`: **可用库存**。
    *   `frozen_count`: **冻结库存**。这是实现订单预留、防止超卖的核心字段。当出库订单配盘时，这部分库存会被“锁定”，虽然物理上仍在库，但在逻辑上已不可用。
    *   `out_order_id`, `out_order_detail_id`: 当库存被冻结时，记录是哪个出库订单预定了这批库存，提供了清晰的追溯链。

### 5. `entity/CheckBean.java` & `entity/CheckDetailBean.java`

*   **文件职责**: 盘点单头和明细的实体。
*   **关键字段**:
    *   `CheckBean.status`: 驱动盘点单生命周期（草稿 -> 盘点中 -> 已配盘 -> 已完成）的状态机。
    *   `CheckBean.type`: 任务来源标识，`1`代表上游下发，`2`代表手动创建，是`sendUp`回传逻辑的关键。
    *   `CheckDetailBean.inventoryCount`: 账面数量，盘点基准。
    *   `CheckDetailBean.checkCount`: 实际盘点数量，由人工录入。
    *   `CheckBean.data`: 一个`@TableField(exist = false)`的非持久化字段，用于在接收上游系统下发的复杂JSON时，临时承载明细列表。