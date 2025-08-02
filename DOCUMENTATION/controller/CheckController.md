# CheckController

`CheckController` 是一个 Spring REST 控制器，用于协调整个库存盘点（inventory check）流程。它管理盘点订单的生命周期，从创建和库存分配（拣货）到状态更新以及与上游系统的通信。

## API 端点

### 1. 获取盘点订单头

- **URL:** `/check/page`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件检索盘点订单头的分页列表。
- **请求体:**
  - `checkBean`: 一个用于过滤的 `CheckBean` 对象（例如，按 `checkCode`、`status`）。
  - `pageNo` (int): 页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `CheckBean` 对象分页列表的 `InterReturn` 对象。

### 2. 更新盘点订单状态

- **URL:** `/check/update`
- **HTTP 方法:** `POST`
- **描述:** 更新盘点订单的状态。此操作还会将状态更新级联到所有相关的明细行。
- **请求体:**
  - `checkBean`: 一个包含 `checkCode` 和新 `status` 的 `CheckBean` 对象。
- **响应:**
  - 一个指示更新成功或失败的 `InterReturn` 对象。

### 3. 执行盘点拣货

- **URL:** `/check/pick`
- **HTTP 方法:** `POST`
- **描述:** 这是一个核心功能，为盘点流程分配库存。它会检查可用库存，创建拣货任务 (`OutPick`)，并更新库存记录以冻结已分配的数量。
- **请求体:**
  - `checkBean`: 要拣货的订单的 `CheckBean` 对象。订单状态必须是“盘点中”。
  - `ports` (String[]): 用于拣货的出口标识符数组。
- **响应:**
  - 一个指示拣货流程成功或失败的 `InterReturn` 对象。

### 4. 编辑盘点订单头

- **URL:** `/check/edit`
- **HTTP 方法:** `POST`
- **描述:** 修改现有盘点订单头的详细信息。
- **请求体:**
  - `checkBean`: 一个包含 `checkCode` 和要更新字段的 `CheckBean` 对象。
- **响应:**
  - 一个指示编辑结果的 `InterReturn` 对象。

### 5. 添加新盘点订单

- **URL:** `/check/add`
- **HTTP 方法:** `POST`
- **描述:** 创建一个新的盘点订单头。系统会自动生成一个唯一的 `checkCode`。
- **请求体:**
  - `checkBean`: 一个包含所需头信息的 `CheckBean` 对象。
- **响应:**
  - 一个指示创建结果的 `InterReturn` 对象。

### 6. 添加多个盘点订单

- **URL:** `/check/Adds`
- **HTTP 方法:** `POST`
- **描述:** 在单个批处理操作中创建多个盘点订单头。
- **请求体:**
  - 一个 `CheckBean` 对象列表。
- **响应:**
  - 一个指示批量创建结果的 `InterReturn` 对象。

### 7. 从上游系统接收盘点订单

- **URL:** `/check/down/add`
- **HTTP 方法:** `POST`
- **描述:** 一个供外部（上游）系统将盘点订单推送到 WMS 的端点。它会保存订单头及其关联的明细行。
- **请求体:**
  - 一个 `CheckBean` 对象列表，其中每个 `CheckBean` 包含其 `CheckDetailBean` 明细的列表。
- **响应:**
  - 一个指示数据接收成功或失败的 JSON 对象。

### 8. 将已完成的盘点数据发送到上游系统

- **URL:** `/check/up/send`
- **HTTP 方法:** `POST`
- **描述:** 收集尚未报告的已完成盘点订单的数据，对其进行格式化，并将其发送到配置的上游系统 API。
- **请求体:** 无。
- **响应:**
  - 一个指示数据上传成功或失败的 `InterReturn` 对象。

### 9. 删除盘点订单

- **URL:** `/check/delete`
- **HTTP 方法:** `POST`
- **描述:** 删除一个盘点订单头及其所有关联的明细行。这仅适用于处于草稿或预处理状态的订单。
- **请求体:**
  - `checkBean`: 一个包含要删除订单的 `checkCode` 的 `CheckBean` 对象。
- **响应:**
  - 一个指示删除结果的 `InterReturn` 对象。
