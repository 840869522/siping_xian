# TaskDController

`TaskDController` 是一个 Spring REST 控制器，用于管理“D库”（大件库）的任务，这是一个用于大型物品的自动化系统。它作为 WMS 和控制该特定仓库区域的 WCS（仓库控制系统）之间的主要接口。

## 内部 WMS API 端点

这些端点由 WMS 前端用于管理任务。

### 1. 创建 D 库任务

- **URL:** `/TaskD/TaskDInsert`
- **HTTP 方法:** `POST`
- **描述:** 为 D 库创建一个新任务（例如，入库、出库、移库）。它执行广泛的验证，并在 `LocationMap` 中冻结源/目标位置。
- **请求体:**
  - `taskD`: 一个包含任务详情的 `TaskD` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 2. 获取 D 库任务

- **URL:** `/TaskD/GetTaskDByPage`
- **HTTP 方法:** `POST`
- **描述:** 使用分页和过滤器查询 D 库的任务。
- **请求体:**
  - `taskD`: 一个用于过滤的 `TaskD` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `TaskD` 对象分页列表的 `InterReturn` 对象。

### 3. 更新 D 库任务状态

- **URL:** `/TaskD/UpdateTaskDStatusByTaskNo`
- **HTTP 方法:** `POST`
- **描述:** 手动更新特定 D 任务的状态。
- **请求体:**
  - `taskD`: 一个包含 `task_no` 和新 `status` 的 `TaskD` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 将拣货列表转换为 D 任务

- **URL:** `/TaskD/Pick2TaskD`
- **HTTP 方法:** `POST`
- **描述:** 接收一个大型物品的 `OutPick` 记录列表，并将其转换为一批 `TaskD` 出库任务。
- **请求体:**
  - 一个 `OutPick` 对象列表。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

---

## 外部 WCS API 端点

这些端点设计为由控制 D 库硬件的外部 WCS 调用。

### 1. 入库任务请求 (来自 WCS)

- **URL:** `/TaskD/fromwcs/inwaretask`
- **HTTP 方法:** `POST`
- **描述:** 当一个托盘到达入口点时，WCS 调用此端点。WMS 找到该托盘对应的“待执行”入库任务，并向 WCS 发回一个带有目标位置的命令。
- **请求体:**
  - `dinware`: 一个包含 `barCode`（托盘代码）和 `fromPort` 的 `Dinware` 对象。
- **响应:**
  - 一个 `DReturn` 对象。`returnStatus: 0` 表示成功，`1` 表示失败。

### 2. 任务状态报告 (来自 WCS)

- **URL:** `/TaskD/fromwcs/task`
- **HTTP 方法:** `POST`
- **描述:** WCS 调用此端点来报告任务的状态。当任务被报告为完成 (`taskStatus: 8`) 时，WMS 更新其内部任务状态并最终确定库位状态（例如，对于入库任务，将托盘绑定到目的地）。
- **请求体:**
  - `dtask`: 一个包含 `taskId` 和 `taskStatus` 的 `Dtask` 对象。
- **响应:**
  - 一个确认状态更新的 `DReturn` 对象。

### WMS 到 WCS 的命令端点

这些端点由 WMS 用于向 WCS 发送命令。

- **`/TaskD/DJKtaskReceive`**: 向 WCS 发送一个新任务（入库、出库等）。
- **`/TaskD/DJKtaskChange`**: 发送一个命令来更改活动任务的目的地。
- **`/TaskD/DJKtaskCancel`**: 发送一个命令来取消一个活动任务。
- **`/TaskD/DJKcargoLocationSyn`**: 发送一个命令来同步库位的占用状态。
- **`/TaskD/DJKdeviceStatus`**: 从 WCS 查询特定设备（例如，穿梭车）的状态。
