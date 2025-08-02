# BaseEquipmentController

`BaseEquipmentController` 是一个 Spring REST 控制器，用于管理仓库内设备的主数据。它提供了查询和添加设备信息的端点。

## API 端点

### 1. 获取设备信息

- **URL:** `/BaseEquipment/GetBaseEquipmentBy`
- **HTTP 方法:** `POST`
- **描述:** 根据指定的过滤条件，检索设备的分页列表。
- **请求体:**
  - `baseEquipment`: 一个用于过滤的 `BaseEquipment` 对象。
    - `equipment_name` (String, 可选): 用于过滤的设备名称。
    - `equipment_code` (String, 可选): 用于过滤的设备代码。
    - `status` (String, 可选): 用于过滤的设备状态。
  - `pageNo` (int): 要检索的页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `BaseEquipment` 对象分页列表的 `InterReturn` 对象。

### 2. 添加新设备

- **URL:** `/BaseEquipment/AddBaseEquipment`
- **HTTP 方法:** `POST`
- **描述:** 向系统中添加一个新设备。
- **请求体:**
  - `baseEquipment`: 一个包含新设备详情的 `BaseEquipment` 对象。
    - `equipment_code` (String, **必需**): 新设备的唯一代码。
    - `equipment_name` (String, **必需**): 新设备的名称。
    - `period` (String, **必需**): 设备的保养周期。
- **响应:**
  - 一个指示添加操作成功或失败的 `InterReturn` 对象。
