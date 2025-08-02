# 任务相关实体

本文档描述了 `com.example.wmsmp.entity.task` 包及其子包中的实体。这些实体代表了各种自动化系统的任务以及用于与它们通信的数据传输对象 (DTO)。

---

## 通用任务实体

### `TaskSort`

- **表名:** `t_task_sort`
- **描述:** 代表分拣机的单个任务。它将一个出库订单分解为给分拣机的具体指令。
- **关键字段:**
  - `order_no`: 此分拣任务所属的出库订单号。
  - `box_id`: 分拣机上目标滑槽或箱子的标识符。
  - `material_code`: 要分拣的物料。
  - `num`: 要分拣到此箱中的物料数量。

---

## AGV 系统实体 (`task.AGV`)

这些实体与 AGV（自动导引车）系统相关。

- **`AgvTask`**: 一个数据库实体，代表存储在 WMS 中供 AGV 执行的单个任务（例如，将托盘从 A 移动到 B）。
- **`AgvLocationMap`**: 一个数据库实体，代表 AGV 系统可访问的位置。
- **用于 AGV 通信的 DTO:**
  - `AgvTaskSend`: 用于向 AGV WCS 发送新任务命令的 DTO。
  - `AgvTaskStatus`: AGV WCS 用于向 WMS 报告任务状态的 DTO。
  - `AgvStatus`: AGV WCS 用于报告车辆自身状态（位置、电池等）的 DTO。
  - `AgvRes`: 来自 AGV WCS 的通用响应 DTO。

---

## CTU 系统实体 (`task.CTU`)

这些实体与 CTU（容器传输单元）系统相关。它们镜像了 AGV 的实体。

- **`CtuTask`**: 一个用于 CTU 任务的数据库实体。
- **`CtuLocationMap`**: 一个用于 CTU 系统可访问位置的数据库实体。
- **用于 CTU 通信的 DTO:**
  - `CtuTaskSend`: 用于向 CTU WCS 发送任务的 DTO。
  - `CtuTaskStatus`: 用于从 CTU WCS 接收任务状态更新的 DTO。
  - `CtuStatus`: 用于从 CTU WCS 接收车辆状态的 DTO。
  - `CtuRes`: 来自 CTU WCS 的通用响应 DTO。

---

## D 库系统实体 (`task.D`)

这些实体与“D库”（大件库）ASRS 相关。

- **`TaskD`**: 一个数据库实体，代表 D 库 ASRS 的一个任务。
- **用于 D 库 WCS 通信的 DTO:**
  - `Dinware`: WCS 用于请求入库任务的 DTO。
  - `DtaskReceive`: WMS 用于向 WCS 发送新任务的 DTO。包含一个 `Tasks` 列表。
  - `Tasks`: 代表 `DtaskReceive` 载荷中的单个任务。
  - `Dtask`: WCS 用于报告已完成任务状态的 DTO。
  - `DReturn`: 来自 D 库 WCS 的通用响应 DTO。

---

## X 库系统实体 (`task.X`)

这些实体与来自供应商“天地人”的“X库”（箱组库）ASRS 相关。

- **`TaskX`**: 一个数据库实体，代表 X 库 ASRS 的一个任务。
- **用于 X 库 WCS 通信的 DTO:**
  - `SendX`: WMS 用于向天地人 WCS 发送命令（入库、出库）的 DTO。
  - `GetX`: 来自天地人 WCS 的通用响应 DTO。
  - `CarMsgX` / `StatusX`: 代表穿梭车硬件状态的 DTO。

---

## Z 系统 (主 WCS) 和 B 库实体 (`task.Z`)

这个包是“B库”（料箱库）、“P库”（托盘库）以及控制它们的主“Z”WCS 的实体混合体。

- **数据库实体:**
  - **`TaskB`**: 代表 B 库 ASRS 的一个任务（例如，取一个料箱）。
  - **`TaskP`**: 代表 P 库 ASRS 的一个任务（例如，取一个托盘）。
- **用于主 WCS 通信的 DTO:**
  - **`ZApply`**: 主 WCS 发送的用于请求任务的 DTO（`task_type` 字段决定请求哪个系统）。
  - **`ZTask`**: WMS 用于向主 WCS 发送新任务命令的 DTO。
  - **`ZTaskReport`**: 主 WCS 发送的用于报告任务最终状态的 DTO。
  - **`ZTaskSorter1` / `ZTaskSorter2`**: 用于向主 WCS 发送一波分拣任务的 DTO。
  - **`ZTaskLight1` / `ZTaskLight2`**: 用于向主 WCS 发送电子标签亮灯命令的 DTO。
  - **`ZHandBoxTask`**: 用于向机械手发送任务的 DTO。
  - **`ZTrussBoxTask`**: 用于向桁架系统发送任务的 DTO。
  - **`ZReturn`**: 来自主 WCS 的通用响应 DTO。
  - **`ZTag`**: 当按下电子标签按钮时，WCS 发送的 DTO。
