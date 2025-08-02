# TaskXController

`TaskXController` 是一个 Spring REST 控制器，用于管理“X库”（箱组库）的任务，这是一个由名为“天地人”的供应商提供的基于穿梭车的 ASRS（自动存储与检索系统）。

## 内部 WMS API 端点

这些端点由 WMS 前端和其他内部服务使用。

### 1. 创建 X 库任务

- **URL:** `/TaskX/TaskXInsert`
- **HTTP 方法:** `POST`
- **描述:** 为 X 库创建一个新任务（例如，入库、出库）。它执行验证以防止重复任务或冲突。
- **请求体:**
  - `taskX`: 一个包含任务详情的 `TaskX` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 2. 叉车/AGV 获取第一个任务

- **URL:** `/TaskX/CarGetFirstTask`
- **HTTP 方法:** `POST`
- **描述:** 允许一个运输单元（如叉车或 AGV，由 `carNo` 标识）请求下一个可用任务。它找到状态为“待执行”的最早的任务，将其状态更新为“已获取”，并将 `carNo` 分配给它。
- **参数:**
  - `carNo` (String): 请求任务的单元的标识符。
- **响应:**
  - 一个包含分配的 `TaskX` 对象的 `InterReturn` 对象。

### 3. 获取 X 库任务

- **URL:** `/TaskX/GetTaskXByPage`
- **HTTP 方法:** `POST`
- **描述:** 使用分页和过滤器查询 X 库的任务。
- **请求体:**
  - `taskX`: 一个用于过滤的 `TaskX` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `TaskX` 对象分页列表的 `InterReturn` 对象。

### 4. 更新 X 库任务状态

- **URL:** `/TaskX/UpdateTaskXStatusByTaskNo`
- **HTTP 方法:** `POST`
- **描述:** 手动更新特定 X 任务的状态。
- **请求体:**
  - `taskX`: 一个包含 `task_no` 和新 `status` 的 `TaskX` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 将拣货列表转换为 X 任务

- **URL:** `/TaskX/Pick2TaskX`
- **HTTP 方法:** `POST`
- **描述:** 将 `OutPick` 记录列表转换为一批 `TaskX` 出库任务。
- **请求体:**
  - 一个 `OutPick` 对象列表。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

---

## 外部 WCS (天地人) API 端点

这些端点用于与天地人穿梭车系统通信。

### 1. 检查并完成任务

- **URL:** `/TaskX/TaskXFinish`
- **HTTP 方法:** `POST`
- **描述:** WMS 调用此端点以确认穿梭车任务的完成。它调用天地人 API 获取穿梭车的状态。如果穿梭车空闲（任务完成），此端点将 WMS `TaskX` 状态更新为“已完成”，并更新 `LocationMap` 以反映新状态（例如，入库时绑定托盘，出库时清除）。
- **请求体:**
  - `sendXFromWeb`: 一个包含 `shuttleId` 的 `SendX` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 2. 发送入库命令

- **URL:** `/TaskX/XZKIn`
- **HTTP 方法:** `POST`
- **描述:** 向天地人穿梭车系统发送一个“入库”命令。
- **请求体:**
  - `sendXFromWeb`: 一个包含 `wmsTaskId` 和 `shuttleId` 的 `SendX` 对象。
- **响应:**
  - 一个包含天地人 API 响应的 `InterReturn` 对象。

### 3. 发送出库命令

- **URL:** `/TaskX/XZKOut`
- **HTTP 方法:** `POST`
- **描述:** 向天地人穿梭车系统发送一个“出库”命令。
- **请求体:**
  - `sendXFromWeb`: 一个包含 `wmsTaskId` 和 `shuttleId` 的 `SendX` 对象。
- **响应:**
  - 一个包含天地人 API 响应的 `InterReturn` 对象。

### 其他 WCS 通信端点

- **`/TaskX/XZKStatus`**: 查询穿梭车的实时状态。
- **`/TaskX/XZKTask`**: 查询特定穿梭车上当前的任务列表。
- **`/TaskX/XZKCmdopt`**: 为特定任务发送“强制完成”命令。
