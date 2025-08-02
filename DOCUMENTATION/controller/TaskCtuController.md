# TaskCtuController

`TaskCtuController` 是一个 Spring REST 控制器，用于管理 CTU（容器传输单元）自动化系统的任务。其结构和功能类似于 `TaskAgvController`，在 WMS 和 CTU 控制系统之间提供了一个桥梁。

## API 端点

### 1. 获取 CTU 任务

- **URL:** `/TaskCtu/GetCTUByPage`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤标准检索 CTU 任务的分页列表。
- **请求体:**
  - `ctuTask`: 一个用于过滤的 `CtuTask` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `CtuTask` 对象分页列表的 `InterReturn` 对象。

### 2. 分派新的 CTU 任务

- **URL:** `/TaskCtu/TaskDownload`
- **HTTP 方法:** `POST`
- **描述:** 创建并向 CTU 控制系统分派一个新任务。
- **逻辑:**
  1.  验证源和目标位置的可用性。
  2.  生成一个唯一的任务编号。
  3.  调用外部 CTU 系统 API 发送任务命令。
  4.  将任务保存到本地数据库。
  5.  冻结源和目标的 `CtuLocationMap` 位置。
- **请求体:**
  - `ctuTaskSend`: 一个包含任务详情的 `CtuTaskSend` 对象。
  - `palletCode` (String): 正在移动的托盘/容器。
  - `creater` (String): 创建任务的用户。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 3. 取消任务 (来自前端)

- **URL:** `/TaskCtu/TaskCancel`
- **HTTP 方法:** `POST`
- **描述:** 允许用户通过调用外部 CTU API 来取消一个正在进行中的 CTU 任务。
- **请求体:**
  - `ctuTaskSend`: 一个包含要取消的 `id`（任务编号）的 `CtuTaskSend` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 报告任务状态 (来自 CTU 系统)

- **URL:** `/TaskCtu/report-task-status`
- **HTTP 方法:** `POST`
- **描述:** CTU 控制系统提供任务状态更新的回调端点。
- **逻辑:**
  - 任务完成后 (`status == 3`)，它会更新本地任务记录并调用 `FreeStartEndByTask` 来释放位置。
- **请求体:**
  - `ctuTaskStatus`: 一个来自 CTU 系统的对象，包含任务 ID 和状态码。
- **响应:**
  - 一个 `CtuRes` 对象，确认更新。

### 5. 报告任务取消 (来自 CTU 系统)

- **URL:** `/TaskCtu/report-task-cancel`
- **HTTP 方法:** `POST`
- **描述:** CTU 系统用于确认任务已被取消的回调。
- **请求体:**
  - `ctuTaskSend`: 一个包含已取消任务 `id` 的对象。
- **响应:**
  - 一个 `CtuRes` 对象，确认取消。

### 工具端点

- **`/TaskCtu/GetEmptyLocationByType`**: 在指定的 CTU 区域中查找一个可用的空位置。
- **`/TaskCtu/FreezeStartEndByTask`**: 手动冻结任务的起始和结束位置。
- **`/TaskCtu/FreeStartEndByTask`**: 手动释放起始和结束位置，并最终确定托盘与目的地的绑定。
