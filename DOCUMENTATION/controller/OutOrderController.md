# OutOrderController

`OutOrderController` 是一个 Spring REST 控制器，用于管理出库（发货）订单的头信息。它为出库流程镜像了 `InOrderController` 的功能。

## API 端点

### 1. 获取出库订单 (模糊搜索)

- **URL:** `/OutOrder/GetOutOrderByFuzzy`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件检索出库订单头的分页列表。
- **请求体:**
  - `outOrder`: 一个用于过滤的 `OutOrder` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `OutOrder` 对象分页列表的 `InterReturn` 对象。

### 2. 更新出库订单状态

- **URL:** `/OutOrder/UpdateOutOrderByOrderId`
- **HTTP 方法:** `POST`
- **描述:** 更新特定出库订单的状态。
- **请求体:**
  - `outOrder`: 一个包含 `out_order_id`、新 `status` 和 `updater` 的 `OutOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 3. 编辑出库订单

- **URL:** `/OutOrder/EditOutOrder`
- **HTTP 方法:** `POST`
- **描述:** 修改现有出库订单头的字段。
- **请求体:**
  - `outOrder`: 一个包含 `out_order_id` 和要更新字段的 `OutOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 添加新出库订单

- **URL:** `/OutOrder/AddOutOrder`
- **HTTP 方法:** `POST`
- **描述:** 创建一个新的出库订单头。它内部调用 `GenerateOutOrderNo` 来获取一个唯一的 ID。
- **请求体:**
  - `outOrder`: 包含头信息的 `OutOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 批量添加出库订单 (仅限上游系统)

- **URL:** `/OutOrder/Adds`
- **HTTP 方法:** `POST`
- **描述:** 在一个批处理中添加多个新的出库订单头，旨在供上游系统使用。
- **请求体:**
  - 一个 `OutOrder` 对象列表。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 提交或审核出库订单

- **URL:** `/OutOrder/SubmitOutOrder`
- **HTTP 方法:** `POST`
- **描述:** 将出库订单的状态更改为“待审核”或“已审核”。要求订单必须有明细行。
- **请求体:**
  - `outOrder`: 一个包含 `out_order_id` 和新 `status` 的 `OutOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 7. 删除出库订单

- **URL:** `/OutOrder/DelOutOrder`
- **HTTP 方法:** `POST`
- **描述:** 删除一个出库订单头及其所有关联的明细行。
- **请求体:**
  - `outOrder`: 一个包含 `out_order_id` 的 `OutOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 8. 生成出库订单号

- **URL:** `/OutOrder/GenerateOutOrderNo`
- **HTTP 方法:** `POST`
- **描述:** 根据当前日期和顺序计数器生成一个新的、唯一的出库订单号。
- **请求体:** 无。
- **响应:**
  - 一个 `InterReturn` 对象，其 `result` 字段中包含新的订单号。
