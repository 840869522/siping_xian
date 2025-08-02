# OutOrderDetailController

`OutOrderDetailController` 是一个 Spring REST 控制器，用于管理出库（发货）订单的明细行。它提供了一套与 `InOrderDetailController` 功能上平行的端点。

## API 端点

### 1. 获取出库订单明细

- **URL:** `/OutOrderDetail/GetOutOrderDetailBy`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件检索出库订单明细行的分页列表。
- **请求体:**
  - `outOrderDetail`: 一个用于过滤的 `OutOrderDetail` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `OutOrderDetail` 对象分页列表的 `InterReturn` 对象。

### 2. 按订单 ID 更新出库订单明细

- **URL:** `/OutOrderDetail/UpdateOutOrderDetailByOrderId`
- **HTTP 方法:** `POST`
- **描述:** 更新与特定出库订单 ID 关联的所有明细行的状态。
- **请求体:**
  - `outOrderDetail`: 一个包含 `out_order_id` 和新 `status` 的 `OutOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 3. 批量添加出库订单明细 (仅限上游系统)

- **URL:** `/OutOrderDetail/Adds`
- **HTTP 方法:** `POST`
- **描述:** 在一个批处理中添加多个新的出库订单明细行，旨在供上游系统使用。
- **请求体:**
  - 一个 `OutOrderDetail` 对象列表。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 添加单个出库订单明细

- **URL:** `/OutOrderDetail/AddOutOrderDetail`
- **HTTP 方法:** `POST`
- **描述:** 向现有出库订单添加一个新明细行。父订单状态必须为“创建中”。
- **请求体:**
  - `outOrderDetail`: 要添加的 `OutOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 删除出库订单明细

- **URL:** `/OutOrderDetail/DelOutOrderDetail`
- **HTTP 方法:** `POST`
- **描述:** 删除单个出库订单明细行。
- **请求体:**
  - `outOrderDetail`: 一个包含 `out_order_detail_id` 的 `OutOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 编辑出库订单明细

- **URL:** `/OutOrderDetail/EditOutOrderDetail`
- **HTTP 方法:** `POST`
- **描述:** 修改现有出库订单明细行的字段。
- **请求体:**
  - `outOrderDetail`: 一个包含 `out_order_detail_id` 和要更新字段的 `OutOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 7. 将出库订单导出到 Excel

- **URL:** `/OutOrderDetail/GetOutOrderExcel`
- **HTTP 方法:** `POST`
- **描述:** 收集并格式化给定出库订单列表的所有头和明细信息，以便导出到 Excel。
- **请求体:**
  - 一个 `OutOrder` 对象（头）列表。
- **响应:**
  - 一个包含 `OutOrderExcel` 对象列表的 `InterReturn` 对象。

### 8. 从 Excel 导入出库订单

- **URL:** `/OutOrderDetail/ImportOutOrderExcel`
- **HTTP 方法:** `POST`
- **描述:** 从 `OutOrderExcel` 对象列表导入出库订单，在一个事务中同时创建头和明细。
- **请求体:**
  - 一个 `OutOrderExcel` 对象列表。
- **响应:**
  - 一个指示导入结果的 `InterReturn` 对象。
