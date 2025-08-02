# BaseEquipmentDetailController

`BaseEquipmentDetailController` 是一个 Spring REST 控制器，用于管理仓库设备的保养和维修记录。它提供了查询和添加这些记录的端点。

## API 端点

### 1. 获取设备保养/维修记录

- **URL:** `/BaseEquipmentDetail/GetBaseEquipmentDetailBy`
- **HTTP 方法:** `POST`
- **描述:** 检索特定设备保养和维修记录的分页列表。
- **请求体:**
  - `baseEquipmentDetail`: 一个用于过滤的 `BaseEquipmentDetail` 对象。
    - `equipment_code` (String, 可选): 要检索记录的设备代码。
  - `pageNo` (int): 要检索的页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `BaseEquipmentDetail` 对象分页列表的 `InterReturn` 对象。

### 2. 添加设备保养/维修记录

- **URL:** `/BaseEquipmentDetail/AddBaseEquipmentDetail`
- **HTTP 方法:** `POST`
- **描述:** 为一个设备添加一条新的保养或维修记录。
- **请求体:**
  - `baseEquipmentDetail`: 一个包含记录详情的 `BaseEquipmentDetail` 对象。
    - `equipment_code` (String, **必需**): 设备的代码。
    - `equipment_name` (String, **必需**): 设备的名称。
    - `maintenance_content` (String, **必需**): 保养/维修工作的内容。
    - `maintenance_person` (String, **必需**): 执行保养/维修的人员。
    - `maintenance_date` (String, **必需**): 保养/维修的日期。
- **响应:**
  - 一个指示添加操作成功或失败的 `InterReturn` 对象。
