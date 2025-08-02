# TaskZController

`TaskZController` 是一个高级别的主控制器，用于协调整个“自动化库”。它充当与主 WCS（仓库控制系统）通信的中心枢纽，接收高级别的请求和状态更新，然后将详细的逻辑委托给适当的特定任务控制器（如 `TaskPController`、`TaskBController` 等）或服务。

## API 端点

### WMS 到 WCS 的命令

这些端点由 WMS 调用，以向主 WCS 发送高级别命令。

- **`/TaskZ/ZDKtaskReceive`**: 一个通用端点，用于向主 WCS 发送任务（例如，移动托盘、移动料箱）。
- **`/TaskZ/ZDKtaskSorter`**: 将给定出库订单的完整一波分拣任务发送给 WCS，由分拣机执行。
- **`/TaskZ/ZDKETagLight`**: 向 WCS 发送命令，以点亮工作站上的特定电子标签，显示数量和颜色。
- **`/TaskZ/trussBoxTask`**: 向“桁架箱”系统发送任务。
- **`/TaskZ/robotHandTask`**: 根据给定料箱的拣货需求，向“机械手”发送一系列拣货任务。

---

## WCS 到 WMS 的回调

这些是主 WCS 调用以与 WMS 通信的主要端点。

### 1. 任务申请 (请求工作)

- **URL:** `/TaskZ/taskApply`
- **HTTP 方法:** `POST`
- **描述:** 这是 WCS 请求新任务的一个主要入口点。WCS 发送一个 `task_type` 来指示请求的上下文。然后，控制器将请求路由到适当的内部处理程序。
- **示例 `task_type` 值:**
  - `robo_in_task`: 一个托盘已到达 P 库入口点。WMS 需要找到其目的地并发出命令。
  - `mfc_in_task`: 一个料箱已到达 B 库入口点。
  - `empty_tray_1f_in`: 一整叠空托盘已准备好入库。
  - `empty_tray_2f_out`: 一个工作站需要一叠空托盘。
  - `mfc_in_machine_2f`: 一个在工作站的料箱已处理完毕，需要返回存储区。
- **请求体:**
  - `zApply`: 一个 `ZApply` 对象，包含 `uuid`、`task_type`、`barcode` 等。
- **响应:**
  - 一个 `ZReturn` 对象，这是给 WCS 执行的命令。

### 2. 任务状态报告

- **URL:** `/TaskZ/taskReport`
- **HTTP 方法:** `POST`
- **描述:** WCS 报告任务最终状态（例如，完成、失败）的另一个主要入口点。控制器根据任务 ID 前缀确定任务属于哪个系统（P库、B库、分拣机等），并调用相应的最终处理逻辑。
- **逻辑:**
  - **完成 (`task_state: "1"`)**: 将 WMS 任务状态更新为“已完成”，并更新 `LocationMap`（例如，将托盘绑定到目的地，从源头清除托盘）。
  - **失败 (`task_state: "2"`, `task_state: "3"`)**: 将 WMS 任务状态更新为错误状态，如“满入”（目的地已满）或“空取”（源头为空），以供人工干预。
- **请求体:**
  - `zTaskReport`: 一个包含 `task_id` 和 `task_state` 的 `ZTaskReport` 对象。
- **响应:**
  - 一个确认报告的 `ZReturn` 对象。
