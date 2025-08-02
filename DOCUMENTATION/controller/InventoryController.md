# InventoryController

`InventoryController` 是一个 Spring REST 控制器，提供管理核心库存记录的功能。这包括查询库存、执行调整以及处理如收货和托盘间转移等复杂的事务性操作。

## API 端点

### 1. 获取库存记录

- **URL:** `/Inventory/GetInventoryBy`
- **HTTP 方法:** `POST`
- **描述:** 一个通用的端点，用于通过分页和各种过滤器查询库存记录。
- **请求体:**
  - `inventory`: 一个用于过滤的 `Inventory` 对象（例如，按 `material_code`、`pallet_code`、`batch`）。
  - `pageNo` (int), `pageSize` (int), `startTime` (String), `endTime` (String)。
- **响应:**
  - 一个包含 `Inventory` 记录分页列表的 `InterReturn` 对象。

### 2. 添加新库存 (手动/无订单收货)

- **URL:** `/Inventory/AddInventory`
- **HTTP 方法:** `POST`
- **描述:** 创建一条新的库存记录。这通常用于没有前序订单的流程，例如手动发现库存或初始库存加载。它包含一个 `@FlowAnnotation`，表明它会为“无单收货”创建一个审计追踪条目。
- **请求体:**
  - `tInventory`: 一个包含所有必填字段（托盘、单元格、物料、批次、数量、创建者）的 `Inventory` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 3. 更新库存数量

- **URL:** `/Inventory/UpdateInventory`
- **HTTP 方法:** `POST`
- **描述:** 对现有库存记录的数量进行直接调整。
- **请求体:**
  - `inventory`: 一个包含 `inventory_id` 和新 `inventory_count` 的 `Inventory` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. PDA 货物接收

- **URL:** `/Inventory/AddReceiveMaterial`
- **HTTP 方法:** `POST`
- **描述:** 一个关键的、事务性的端点，用于处理通过 PDA 接收的货物。这个单一的调用协调了多个更新：
  1.  增加相应 `InOrderDetail` 行上的 `actual_count`（已收货数量）。
  2.  如果已收到全部数量，则将 `InOrderDetail` 状态更新为“已完成”。
  3.  将收到的数量添加到库存中，可以是创建一条新的 `Inventory` 记录，也可以是增加现有记录（针对相同的物料、批次和托盘）的数量。
  4.  根据其所有明细行的状态，将父 `InOrder` 头的状态更新为“收货中”或“已完成”。
- **请求体:**
  - `inventory`: 代表已收货货物的 `Inventory` 对象。
  - `inOrderId` (String): 正在收货的入库订单的 ID。
- **响应:**
  - 一个指示整个事务结果的 `InterReturn` 对象。

### 5. 绑定/解绑托盘 (库存转移)

- **URL:** `/Inventory/BindPallet`
- **HTTP 方法:** `POST`
- **描述:** 一个事务性的端点，用于将指定数量的物料从一个源托盘/单元格移动到目标托盘/单元格。
- **逻辑:**
  1.  从源库存记录中减少数量。如果数量变为零，则删除源记录。
  2.  在目标库存记录上增加数量。如果目标托盘上不存在该物料/批次的记录，则创建一条新记录。
- **请求体:**
  - `inventory`: 一个代表**目标**和要移动数量的 `Inventory` 对象。
  - `palletCodeOld` (String): **源**的托盘代码。
  - `cellIdOld` (String): **源**的单元格 ID。
- **响应:**
  - 一个指示转移结果的 `InterReturn` 对象。
