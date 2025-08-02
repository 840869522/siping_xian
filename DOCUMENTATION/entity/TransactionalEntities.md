# 事务性实体和视图实体

本文档描述了根 `com.example.wmsmp.entity` 包中的实体。这些实体代表了 WMS 的核心事务数据，如订单、库存和位置，以及用于查询的几个数据库视图。

---

### 订单和拣货实体

- **`InOrder` / `OutOrder`**: 分别代表入库和出库订单的头信息。它们包含订单号、类型、状态和关联仓库等高级信息。
- **`InOrderDetail` / `OutOrderDetail`**: 代表入库和出库订单的明细行。每个明细行对应订单上的特定物料和数量。
- **`OutPick`**: 代表从 `OutOrderDetail` 生成的单个具体的拣货任务。它指定从哪个托盘拣货、拣货数量以及目标工作站。
- **`InOrderExcel` / `OutOrderExcel`**: 这些不是数据库实体，而是用于从/向 Excel 文件导入和导出订单数据的 DTO。它们是结合了头和明细信息的扁平化结构。

---

### 库存和库位实体

- **`Inventory`**: 核心库存表。每条记录代表特定托盘/料箱上特定批号的特定物料的数量。
  - `inventory_count`: 物料的总数量。
  - `frozen_count`: 已分配给出口订单但尚未拣货的数量。可用数量为 `inventory_count - frozen_count`。
- **`LocationMap`**: 代表仓库中单个物理位置（或单元）的主数据。如果位置被占用，它会将 `location_code` 与 `pallet_code` 关联起来。
- **`CheckBean` / `CheckDetailBean`**: 代表盘点（库存盘点）订单的头和明细行。
- **`Flow`**: 代表系统审计追踪（“作业流水”）中的单个条目。它记录了收货、发货和转移等操作。
- **`LedModel`**: 代表要在 LED 屏幕上显示的内容。

---

### 数据库视图实体

- **`InvenMaterialLocationmap`**: 映射到 `v_inven_material_locationmap` 视图的实体。此视图可能连接了库存、物料和位置数据，以提供物料所在位置的全面快照。
- **`VMapInv`**: 映射到 `v_inven_localtion_material` 视图的实体。与上一个类似，它提供了库存和位置数据的连接视图。
- **`VSelectDplocation`**: 映射到 `v_select_dplocation` 视图的实体。它似乎是用于在 D 库中寻找合适位置的专用视图。

---

### 其他 DTO

- **`IsUsed`**: 一个简单的 DTO，用于返回已使用和未使用库位的计数。
- **`AgvLocationMap`**: 这似乎是一个重复或放错位置的实体。一个更完整的版本存在于 `entity.task.AGV` 中。
