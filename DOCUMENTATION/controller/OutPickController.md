# OutPickController

`OutPickController` 是一个复杂的 Spring REST 控制器，它协调整个出库拣货和分配过程。它负责将出库订单转化为一系列具体的拣货任务，管理库存分配（冻结库存），并跟踪货物从存储位置到最终发货容器的移动。

## API 端点

### 1. 获取拣货信息

- **URL:** `/OutPick/GetOutPickBy`
- **HTTP 方法:** `POST`
- **描述:** 使用分页和各种过滤器查询生成的拣货任务（`OutPick` 记录）。
- **请求体:**
  - `outPick`: 一个用于过滤的 `OutPick` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `OutPick` 任务分页列表的 `InterReturn` 对象。

### 2. 编辑拣货任务

- **URL:** `/OutPick/UpdateOutPickByID`
- **HTTP 方法:** `POST`
- **描述:** 修改一个现有的拣货任务。
- **请求体:**
  - `outPick`: 一个包含要更新的 ID 和字段的 `OutPick` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 3. 删除拣货任务

- **URL:** `/OutPick/DelOutPickByID`
- **HTTP 方法:** `POST`
- **描述:** 删除一个拣货任务。
- **请求体:**
  - `outPick`: 一个包含要删除任务 ID 的 `OutPick` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 订单拣货 (分配)

- **URL:** `/OutPick/OrderPick`
- **HTTP 方法:** `POST`
- **描述:** 拣货过程的主要入口点。这个事务性端点接收一个出库订单，检查库存可用性，如果成功，则创建一系列 `OutPick` 任务。它还会更新 `OutOrderDetail` 将其标记为“已配盘”，并增加相应 `Inventory` 记录上的 `frozen_count`。
- **请求体:**
  - `outOrderOld`: 要拣货的 `OutOrder` 头。
  - `ports`: 用于出口/站台的字符串标识符数组。
- **响应:**
  - 一个指示分配过程成功或失败的 `InterReturn` 对象。

### 5. 确认拣货到托盘

- **URL:** `/OutPick/PickPallet`
- **HTTP 方法:** `POST`
- **描述:** 一个代表拣货物理完成的事务性端点。它将 `PickPallet23` 和 `PickPallet33` 的逻辑一起执行。
- **逻辑:**
  1.  将 `OutPick` 任务状态更新为“已完成”。
  2.  从源库存位置减少库存（并减少冻结数量）。
  3.  在代表目标“订单箱”托盘的新库存记录中增加或添加库存。
- **请求体:**
  - `outPick`: 已完成的 `OutPick` 任务。
  - `palletCode`: 目标订单箱/托盘的 ID。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 确认出库

- **URL:** `/OutPick/Checkout`
- **HTTP 方法:** `POST`
- **描述:** 发货流程的最后一步。这个事务性端点确认一个订单箱已经发货。
- **逻辑:**
  1.  增加 `OutOrderDetail` 上的 `actual_count`（已发货数量）。
  2.  删除与订单箱关联的临时 `Inventory` 记录。
- **请求体:**
  - `inventory`: 代表正在发货的订单箱的 `Inventory` 记录。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 专门的拣货端点

- **`/OutPick/PickPallet23`**: 拣货确认的一个特定部分，负责更新拣货任务状态和减少源库存。
- **`/OutPick/PickPallet33`**: 拣货确认的另一部分，负责将库存添加到目标订单箱。
- **`/OutPick/PickPallet33forDM`**: `PickPallet33` 的一个包装器，可能用于与“DM”（可能是 Dematic）分拣机系统集成，该系统报告包裹落入滑槽/箱子的事件。
