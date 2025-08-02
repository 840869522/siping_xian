# LocationMapController

`LocationMapController` 是一个 Spring REST 控制器，用于管理仓库库位的主数据，这些数据代表了仓库的物理网格布局。它还包含几个复杂的算法，用于根据不同仓库区域的特定业务规则找到最佳的空库位。

## API 端点

### 1. 按仓库获取库位使用统计

- **URL:** `/LocationMap/GetIsUsedByWarehouse`
- **HTTP 方法:** `POST`
- **描述:** 计算并返回指定仓库的已使用和未使用库位数量。
- **请求体:**
  - `locationMap`: 一个包含 `warehouse` 名称的 `LocationMap` 对象。
- **响应:**
  - 一个 `InterReturn` 对象，其结果中包含一个 `IsUsed` 对象，内含 `used` 和 `unused` 计数。

### 2. 获取所有库位映射 (模糊搜索)

- **URL:** `/LocationMap/GetAllLocationMapByFuzzy`
- **HTTP 方法:** `POST`
- **描述:** 一个通用的查询，用于通过各种过滤器检索库位主数据的分页列表。
- **请求体:**
  - `locationMap`: 一个用于过滤的 `LocationMap` 对象（例如，按 `location_code`、`warehouse`、`status`、`pallet_code`）。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `LocationMap` 对象分页列表的 `InterReturn` 对象。

### 3. 按库位代码更新库位

- **URL:** `/LocationMap/UpdateLocationMapByLocation`
- **HTTP 方法:** `POST`
- **描述:** 更新单个库位的属性，最常用于分配或取消分配托盘，或更改库位状态（例如，“启用”，“禁用”）。
- **请求体:**
  - `locationMap`: 一个包含 `location_code` 和要更新字段的 `LocationMap` 对象。
- **响应:**
  - 一个指示更新结果的 `InterReturn` 对象。

### 4. 在 P 仓库中获取空库位

- **URL:** `/LocationMap/getEmptyLocationP`
- **HTTP 方法:** `POST`
- **描述:** 实现一个专门的搜索，以在“P库”中找到一个最佳的空库位。
- **参数:**
  - `row` (int): 期望的排 (1-12)。
  - `farNear` (String): 邻近偏好，“远”或“近”。
  - `lowHigh` (int): 高度偏好，1 (低, <=7) 或 2 (高, >=8)。
- **响应:**
  - 一个包含最佳匹配的空 `LocationMap` 的 `InterReturn` 对象。

### 5. 在 P 仓库中获取空库位最多的排

- **URL:** `/LocationMap/getEmptyLocationGroupRowP`
- **HTTP 方法:** `POST`
- **描述:** 分析“P库”，根据高度偏好找到可用空库位数量最多的排。
- **参数:**
  - `lowHigh` (int): 高度偏好，1 (低, <=7) 或 2 (高, >=8)。
- **响应:**
  - 一个表示空位数最多的排号的整数。

### 6. 在 B 仓库中获取外侧空库位

- **URL:** `/LocationMap/getEmptyOutLocationB`
- **HTTP 方法:** `POST`
- **描述:** 在“B库”的外侧（深度为 7 或 8）找到一个可用的空库位。
- **参数:**
  - `floor` (int): 期望的层 (1-42)。
  - `farNear` (String): 邻近偏好，“远”或“近”。
- **响应:**
  - 一个包含找到的 `LocationMap` 的 `InterReturn` 对象。

### 7. 在 B 仓库中按托盘分配库位

- **URL:** `/LocationMap/getLocationByPalletcodeB`
- **HTTP 方法:** `POST`
- **描述:** 一个复杂的算法，用于为给定托盘在“B库”中找到一个合适的存储位置。
- **逻辑:**
  - 如果托盘是空的，它会找到一个随机的可用位置。
  - 如果托盘包含物料，它首先尝试在具有相同物料的其他托盘附近找到一个空位置（共置）。
  - 如果无法共置，它会找到一个新的随机可用位置。
- **参数:**
  - `palletcode` (String): 需要位置的托盘。
- **响应:**
  - 一个包含分配的 `LocationMap` 的 `InterReturn` 对象。
