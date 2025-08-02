# TaskAgvController

`TaskAgvController` 是一个 Spring REST 控制器，充当 WMS 与 AGV（自动导引车）控制系统之间的桥梁。它管理 AGV 任务的完整生命周期，包括创建、分派、状态监控和取消。

## API 端点

### 1. 获取 AGV 任务

- **URL:** `/TaskAgv/GetAGVByPage`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件检索 AGV 任务的分页列表。
- **请求体:**
  - `agvTask`: 一个用于过滤的 `AgvTask` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `AgvTask` 对象分页列表的 `InterReturn` 对象。

### 2. 分派新的 AGV 任务

- **URL:** `/TaskAgv/TaskDownload`
- **HTTP 方法:** `POST`
- **描述:** 创建并向 AGV 分派新任务的主要端点。
- **逻辑:**
  1.  验证源和目标位置是否可用。
  2.  生成一个唯一的任务编号。
  3.  调用外部 AGV 系统 API 发送任务命令。
  4.  将任务详情保存到本地 WMS 数据库。
  5.  冻结源和目标的 `AgvLocationMap` 位置，以防止其他任务使用它们。
- **请求体:**
  - `agvTaskSend`: 一个包含任务详情（源、目标、类型）的 `AgvTaskSend` 对象。
  - `palletCode` (String): 正在移动的托盘。
  - `creater` (String): 创建任务的用户。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 3. 取消任务 (来自前端)

- **URL:** `/TaskAgv/TaskCancel`
- **HTTP 方法:** `POST`
- **描述:** 允许用户取消一个正在进行中的 AGV 任务。它调用外部 AGV 系统 API 发出取消命令，并更新本地任务状态。
- **请求体:**
  - `agvTaskSend`: 一个包含要取消的 `id`（任务编号）的 `AgvTaskSend` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 报告任务状态 (来自 AGV 系统)

- **URL:** `/TaskAgv/report-task-status`
- **HTTP 方法:** `POST`
- **描述:** AGV 控制系统报告任务状态的一个关键回调端点。
- **逻辑:**
  1.  接收一个状态更新 (`AgvTaskStatus`)。
  2.  如果任务已完成 (`status == 2`)，它会将本地 `AgvTask` 记录更新为“已完成”。
  3.  然后调用 `FreeStartEndByTask` 来释放源和目标位置，并将托盘绑定到目标位置。
- **请求体:**
  - `agvTaskStatus`: 一个来自 AGV 系统的对象，包含任务 ID 和状态码。
- **响应:**
  - 一个 `AgvRes` 对象，确认收到状态更新。

### 5. 报告任务取消 (来自 AGV 系统)

- **URL:** `/TaskAgv/report-task-cancel`
- **HTTP 方法:** `POST`
- **描述:** AGV 系统用于确认任务已在其端被取消的回调端点。
- **请求体:**
  - `agvTaskSend`: 一个包含已取消任务 `id` 的对象。
- **响应:**
  - 一个 `AgvRes` 对象，确认取消。

### 工具及其他端点

- **`/TaskAgv/report-agvs-status`**: AGV 系统报告其一般车辆状态的回调。
- **`/TaskAgv/GetEmptyLocationByType`**: 在指定的 AGV 区域中查找一个可用的空位置。
- **`/TaskAgv/FreezeStartEndByTask`**: 手动冻结给定任务的起始和结束位置。
- **`/TaskAgv/FreeStartEndByTask`**: 手动释放任务的起始和结束位置，并将托盘绑定到目的地。
