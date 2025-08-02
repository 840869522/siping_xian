# InOrderDetailController

`InOrderDetailController` 是一个 Spring REST 控制器，用于管理入库（收货）订单的明细行。它提供了创建、查询、更新和删除单个订单行以及进行如 Excel 导入/导出等批量操作的端点。

## API 端点

### 1. 获取入库订单明细

- **URL:** `/InOrderDetail/GetInOrderDetailBy`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件检索入库订单明细行的分页列表。
- **请求体:**
  - `inOrderDetail`: 一个用于过滤的 `InOrderDetail` 对象（例如，按 `in_order_id`、`material_code`）。
  - `pageNo` (int): 页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `InOrderDetail` 对象分页列表的 `InterReturn` 对象。

### 2. 按订单 ID 更新入库订单明细

- **URL:** `/InOrderDetail/UpdateInOrderDetailByOrderId`
- **HTTP 方法:** `POST`
- **描述:** 更新与特定入库订单 ID 关联的所有明细行的状态。
- **请求体:**
  - `inOrderDetail`: 一个包含 `in_order_id` 和新 `status` 的 `InOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 3. 批量添加入库订单明细 (仅限上游系统)

- **URL:** `/InOrderDetail/Adds`
- **HTTP 方法:** `POST`
- **描述:** 在单个批处理操作中添加多个新的入库订单明细行。此端点注明仅供上游系统使用。
- **请求体:**
  - 一个 `InOrderDetail` 对象列表。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 添加单个入库订单明细

- **URL:** `/InOrderDetail/AddInOrderDetail`
- **HTTP 方法:** `POST`
- **描述:** 向现有入库订单添加一个新明细行。父订单状态必须为“创建中”。
- **请求体:**
  - `inOrderDetail`: 要添加的 `InOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 删除入库订单明细

- **URL:** `/InOrderDetail/DelInOrderDetail`
- **HTTP 方法:** `POST`
- **描述:** 根据主键删除单个入库订单明细行。
- **请求体:**
  - `tInOrderDetail`: 一个包含 `in_order_detail_id` 的 `InOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 编辑入库订单明细

- **URL:** `/InOrderDetail/EditInOrderDetail`
- **HTTP 方法:** `POST`
- **描述:** 修改现有入库订单明细行的字段。
- **请求体:**
  - `inOrderDetail`: 一个包含 `in_order_detail_id` 和要更新字段的 `InOrderDetail` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 7. 将入库订单导出到 Excel

- **URL:** `/InOrderDetail/GetInOrderExcel`
- **HTTP 方法:** `POST`
- **描述:** 收集给定入库订单列表的所有头和明细信息，并将其格式化为适合导出到 Excel 文件的结构。
- **请求体:**
  - 一个 `InOrder` 对象（头）列表。
- **响应:**
  - 一个包含 `InOrderExcel` 对象列表的 `InterReturn` 对象。

### 8. 从 Excel 导入入库订单

- **URL:** `/InOrderDetail/ImportInOrderExcel`
- **HTTP 方法:** `POST`
- **描述:** 从 `InOrderExcel` 对象列表（推测是从文件解析的）导入一组入库订单。它在一个事务中同时创建 `InOrder` 头和 `InOrderDetail` 行。
- **请求体:**
  - 一个 `InOrderExcel` 对象列表。
- **响应:**
  - 一个指示导入结果的 `InterReturn` 对象。
