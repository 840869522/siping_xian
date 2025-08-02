# CheckDetailController

`CheckDetailController` 是一个 Spring REST 控制器，用于管理盘点订单的明细行。它与 `CheckController` 紧密协作，处理盘点的整个生命周期。

## API 端点

### 1. 获取盘点明细行

- **URL:** `/check/detail/page`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件检索盘点明细行的分页列表。
- **请求体:**
  - `checkDetailBean`: 一个用于过滤的 `CheckDetailBean` 对象（例如，按 `checkCode`、`materialCode`）。
  - `pageNo` (int): 页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `CheckDetailBean` 对象分页列表的 `InterReturn` 对象。

### 2. 查询物料数据

- **URL:** `/check/query/material`
- **HTTP 方法:** `POST`
- **描述:** 一个特定的查询，用于检索与盘点明细相关的物料数据。
- **请求体:**
  - `checkDetail`: 一个 `CheckDetailBean` 对象。
- **响应:**
  - 一个包含物料数据的 `InterReturn` 对象。

### 3. 更新盘点明细

- **URL:** `/check/detail/update`
- **HTTP 方法:** `POST`
- **描述:** 更新特定盘点明细行的 `checkCount`（盘点数量）。更新后，它会检查订单的所有明细行是否都已完成，如果是，则将主订单头的状态更新为“已完成”。它还会释放与该行关联的任何冻结库存。
- **请求体:**
  - `checkDetailBean`: 一个包含 `checkDetailId` 和新 `checkCount` 的 `CheckDetailBean` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 批量添加盘点明细行

- **URL:** `/check/detail/Adds`
- **HTTP 方法:** `POST`
- **描述:** 在单个批处理操作中向盘点订单添加多个新的明细行。
- **请求体:**
  - 一个 `CheckDetailBean` 对象列表。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 添加单个盘点明细行

- **URL:** `/check/detail/add`
- **HTTP 方法:** `POST`
- **描述:** 向现有盘点订单添加一个新明细行。订单状态必须为“草稿”。
- **请求体:**
  - `checkDetailBean`: 要添加的 `CheckDetailBean`。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 删除盘点明细行

- **URL:** `/check/detail/delete`
- **HTTP 方法:** `POST`
- **描述:** 删除单个盘点明细行。
- **请求体:**
  - `checkDetailBean`: 一个包含要删除的 `checkDetailId` 的 `CheckDetailBean` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 7. 编辑盘点明细行

- **URL:** `/check/detail/edit`
- **HTTP 方法:** `POST`
- **描述:** 编辑一个盘点明细行并将其状态标记为“已完成”。它还会触发逻辑来检查父订单是否已完全完成。
- **请求体:**
  - `checkDetailBean`: 包含更新信息的 `CheckDetailBean`。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 8. 将盘点数据导出到 Excel

- **URL:** `/check/detail/GetOutOrderExcel`
- **HTTP 方法:** `POST`
- **描述:** 收集给定盘点订单头列表的所有明细行，并将组合数据格式化以供 Excel 导出。
- **请求体:**
  - 一个 `CheckBean` 对象（头）列表。
- **响应:**
  - 一个包含格式化为 Excel 的 `CheckDetailBean` 对象列表的 `InterReturn` 对象。

### 9. 从 Excel 导入盘点数据

- **URL:** `/CheckDetailBean/ImportOutOrderExcel`
- **HTTP 方法:** `POST`
- **描述:** 从 Excel 文件导入盘点记录列表，在系统中创建订单头和明细行。
- **请求体:**
  - 一个 `CheckDetailBean` 对象列表，推测是从 Excel 文件解析的。
- **响应:**
  - 一个指示导入结果的 `InterReturn` 对象。
