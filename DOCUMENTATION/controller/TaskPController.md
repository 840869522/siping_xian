# TaskPController

`TaskPController` 是一个 Spring REST 控制器，负责管理“P库”（托盘库）内的任务，这是一个自动化的基于托盘的存储系统。其职责和结构与其他特定任务的控制器（如 `TaskBController` 和 `TaskDController`）类似。

## API 端点

### 1. 创建 P 库任务

- **URL:** `/TaskP/TaskPInsert`
- **HTTP 方法:** `POST`
- **描述:** 为 P 库创建一个新任务，例如“入库”、“出库”或“回库”。该端点执行全面的验证，以确保库位和托盘状态对于请求的操作是正确的。它还根据库位的排号将任务分配给特定的“堆垛机”。
- **请求体:**
  - `taskP`: 一个包含要创建任务详情的 `TaskP` 对象。
- **响应:**
  - 一个指示任务创建结果的 `InterReturn` 对象。

### 2. 获取 P 库任务

- **URL:** `/TaskP/GetTaskPByPage`
- **HTTP 方法:** `POST`
- **描述:** 根据各种过滤条件检索 P 库任务的分页列表。
- **请求体:**
  - `taskP`: 一个用于过滤的 `TaskP` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `TaskP` 对象分页列表的 `InterReturn` 对象。

### 3. 更新 P 库任务状态

- **URL:** `/TaskP/UpdateTaskPStatusByTaskNo`
- **HTTP 方法:** `POST`
- **描述:** 手动更新特定 P 库任务的状态。
- **请求体:**
  - `taskP`: 一个包含 `task_no` 和新 `status` 的 `TaskP` 对象。
- **响应:**
  - 一个指示更新结果的 `InterReturn` 对象。

### 4. 将拣货列表转换为 P 任务

- **URL:** `/TaskP/Pick2TaskP`
- **HTTP 方法:** `POST`
- **描述:** 一个批量创建端点，它接收一个 `OutPick` 记录列表，按托盘对它们进行分组，找到每个托盘的位置，并为每个托盘创建一个相应的 `TaskP` 出库任务。
- **请求体:**
  - 一个 `OutPick` 对象列表。
- **响应:**
  - 一个指示批量任务创建结果的 `InterReturn` 对象。
