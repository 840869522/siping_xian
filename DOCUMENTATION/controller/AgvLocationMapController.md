# AgvLocationMapController

`AgvLocationMapController` 是一个 Spring REST 控制器，用于管理 AGV（自动导引车）与其在仓库内的位置之间的映射关系。它提供了查询和更新这些映射的端点。

## API 端点

### 1. 分页获取 AGV 库位映射

- **URL:** `/AgvLocationMap/GetAGVMapByPage`
- **HTTP 方法:** `POST`
- **描述:** 根据指定的过滤条件，检索 AGV 库位映射的分页列表。
- **请求体:**
  - `agvLocationMap`: 一个用于过滤的 `AgvLocationMap` 对象。
    - `location_code` (String, 可选): 用于过滤的库位代码。
    - `pallet_code` (String, 可选): 用于过滤的托盘代码。
    - `status` (String, 可选): 用于过滤的占用状态。
  - `pageNo` (int): 要检索的页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个 `InterReturn` 对象，包含：
    - `status` (boolean): `true` 表示成功，`false` 表示失败。
    - `message` (String): 描述结果的消息。
    - `result` (ResReturn):
      - `totalCount` (int): 匹配过滤器的总记录数。
      - `totalPage` (int): 总页数。
      - `anything` (List<AgvLocationMap>): 请求页面的 `AgvLocationMap` 对象列表。

### 2. 按库位更新 AGV 地图

- **URL:** `/AgvLocationMap/UpdateAGVMapByLocation`
- **HTTP 方法:** `POST`
- **描述:** 更新一个现有的 AGV 库位映射。该库位由其 `location_code` 标识。
- **请求体:**
  - `agvLocationMap`: 一个包含更新信息的 `AgvLocationMap` 对象。
    - `location_code` (String, **必需**): 要更新的库位代码。
    - `pallet_code` (String, 可选): 要设置的新托盘代码。
    - `status` (String, 可选): 要设置的新状态。
- **响应:**
  - 一个 `InterReturn` 对象，包含：
    - `status` (boolean): `true` 表示成功，`false` 表示失败。
    - `message` (String): 描述更新操作结果的消息。
