# InOrderController

`InOrderController` 是一个 Spring REST 控制器，负责管理入库（收货）订单的头信息。它为入库订单头的整个生命周期提供了一套全面的端点。

## API 端点

### 1. 获取入库订单 (模糊搜索)

- **URL:** `/InOrder/GetInOrderByFuzzy`
- **HTTP 方法:** `POST`
- **描述:** 使用各种过滤条件（包括订单ID的模糊匹配）检索入库订单头的分页列表。
- **请求体:**
  - `inOrder`: 一个用于过滤的 `InOrder` 对象（例如，按 `warehouse_name`、`in_order_id`、`order_type`、`status`）。
  - `pageNo` (int): 页码。
  - `pageSize` (int): 每页的项目数。
  - `startTime` (String): 创建日期范围的开始时间。
  - `endTime` (String): 创建日期范围的结束时间。
- **响应:**
  - 一个包含 `InOrder` 对象分页列表的 `InterReturn` 对象。

### 2. 更新入库订单状态

- **URL:** `/InOrder/UpdateInOrderByOrderId`
- **HTTP 方法:** `POST`
- **描述:** 更新特定入库订单的状态。
- **请求体:**
  - `inOrder`: 一个包含 `in_order_id`、新 `status` 和 `updater` 的 `InOrder` 对象。
- **响应:**
  - 一个指示更新结果的 `InterReturn` 对象。

### 3. 批量添加入库订单 (仅限上游系统)

- **URL:** `/InOrder/Adds`
- **HTTP 方法:** `POST`
- **描述:** 在单个批处理操作中添加多个新的入库订单头。此端点特别注明仅供上游系统使用。
- **请求体:**
  - 一个 `InOrder` 对象列表。
- **响应:**
  - 一个指示批量创建结果的 `InterReturn` 对象。

### 4. 提交或审核入库订单

- **URL:** `/InOrder/SubmitInOrder`
- **HTTP 方法:** `POST`
- **描述:** 将入库订单的状态更改为“待审核”或“已审核”。一个关键的验证步骤是确保订单至少有一条明细行，然后才允许状态更改。
- **请求体:**
  - `inOrder`: 一个包含 `in_order_id` 和新 `status` 的 `InOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 删除入库订单

- **URL:** `/InOrder/DelInOrder`
- **HTTP 方法:** `POST`
- **描述:** 删除一个入库订单头，并级联删除其所有关联的明细行。
- **请求体:**
  - `inOrder`: 一个包含要删除订单的 `in_order_id` 的 `InOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 添加新入库订单

- **URL:** `/InOrder/AddInOrder`
- **HTTP 方法:** `POST`
- **描述:** 创建一个单一的新入库订单头。系统会自动为新订单生成一个唯一的 `in_order_id`。
- **请求体:**
  - `inOrder`: 一个包含所需信息（`order_type`、`org_order` 等）的 `InOrder` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 7. 编辑入库订单

- **URL:** `/InOrder/EditInOrder`
- **HTTP 方法:** `POST`
- **描述:** 修改现有入库订单头的字段。
- **请求体:**
  - `inOrder`: 一个包含 `in_order_id` 和任何需要更新的字段的 `InOrder` 对象。
- **响应:**
  - 一个指示编辑结果的 `InterReturn` 对象。
