# TaskHController

`TaskHController` 是一个 Spring REST 控制器，用于管理“H库”（回转库）的任务。该控制器通过一个工具类 (`OpcPlcHelper`) 直接与硬件的 PLC（可编程逻辑控制器）通信，以命令回转库的移动。

## API 端点

### 1. 发出出库任务 (检索货箱)

- **URL:** `/TaskH/OutTask`
- **HTTP 方法:** `POST`
- **描述:** 向 PLC 发送一个底层命令，以将指定的回转库 (`storageNo`) 旋转到指定的层 (`layer`) 以进行出库操作。
- **参数:**
  - `storageNo` (int): 回转库/存储单元编号 (1 或 2)。
  - `layer` (int): 期望的层/货架编号 (1-41)。
- **响应:**
  - 一个 `InterReturn` 对象，指示命令的结果。

### 2. 发出入库任务

- **URL:** `/TaskH/InTask`
- **HTTP 方法:** `POST`
- **描述:** 向 PLC 发送一个命令，以准备一个回转库进行入库（上架）操作。
- **参数:**
  - `storageNo` (int): 回转库/存储单元编号。
- **响应:**
  - 一个 `InterReturn` 对象，指示命令的结果。

### 3. 停止任务

- **URL:** `/TaskH/SetTaskStop`
- **HTTP 方法:** `POST`
- **描述:** 向 PLC 发送一个命令，以立即停止特定回转库上的当前操作。
- **参数:**
  - `storageNo` (int): 回转库/存储单元编号。
- **响应:**
  - 一个 `InterReturn` 对象，指示命令的结果。

### 4. 为物料获取空/半满货箱

- **URL:** `/TaskH/GetStorageEmpty`
- **HTTP 方法:** `POST`
- **描述:** 一个智能端点，用于查找并检索适合存储给定物料的货箱。
- **逻辑:**
  1.  它首先在回转库中搜索一个已包含相同物料且标记为半满的货箱。
  2.  如果未找到，则搜索任何其他半满的货箱。
  3.  如果仍未找到，则搜索一个完全空的货箱。
  4.  一旦确定了合适的货箱，它会自动调用 `OutTask` 方法来检索该货箱。
- **请求体:**
  - `inventory`: 一个 `Inventory` 对象，包含要存储物料的 `material_code` 和 `batch`。
- **响应:**
  - 一个 `InterReturn` 对象，指示已成功请求一个货箱。

### 5. 按库位代码获取特定货箱

- **URL:** `/TaskH/GetStorageOut`
- **HTTP 方法:** `POST`
- **描述:** 一种通过提供完整的库位代码从回转库中直接检索特定货箱的方法。
- **参数:**
  - `storageNo` (String): 货箱的完整库位代码 (例如, "H01L01")。
- **响应:**
  - 一个 `InterReturn` 对象，指示结果。
