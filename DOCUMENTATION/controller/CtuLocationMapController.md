# CtuLocationMapController

`CtuLocationMapController` 是一个 Spring REST 控制器，用于管理 CTU（容器传输单元）与其在仓库内的位置之间的映射关系。它类似于 `AgvLocationMapController`，但适用于不同类型的车辆/设备。

## API 端点

### 1. 分页获取 CTU 库位映射

- **URL:** `/CtuLocationMap/GetCTUMapByPage`
- **HTTP 方法:** `POST`
- **描述:** 根据指定的过滤条件，检索 CTU 库位映射的分页列表。
- **请求体:**
  - `ctuLocationMap`: 一个用于过滤的 `CtuLocationMap` 对象。
    - `location_code` (String, 可选): 用于过滤的库位代码。
    - `pallet_code` (String, 可选): 用于过滤的托盘代码。
    - `status` (String, 可选): 用于过滤的占用状态。
  - `pageNo` (int): 要检索的页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `CtuLocationMap` 对象分页列表的 `InterReturn` 对象。

### 2. 按库位更新 CTU 地图

- **URL:** `/CtuLocationMap/UpdateCTUMapByLocation`
- **HTTP 方法:** `POST`
- **描述:** 更新一个现有的 CTU 库位映射，由其 `location_code` 标识。
- **请求体:**
  - `ctuLocationMap`: 一个包含更新信息的 `CtuLocationMap` 对象。
    - `location_code` (String, **必需**): 要更新的库位代码。
    - `pallet_code` (String, 可选): 要设置的新托盘代码。
    - `status` (String, 可选): 要设置的新状态。
- **响应:**
  - 一个指示更新操作成功或失败的 `InterReturn` 对象。
