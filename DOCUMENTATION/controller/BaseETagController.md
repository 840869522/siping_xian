# BaseETagController

`BaseETagController` 是一个 Spring REST 控制器，负责管理仓库内电子标签 (E-Tags) 与其对应的位置和托盘之间的绑定关系。

## API 端点

### 1. 获取电子标签绑定

- **URL:** `/BaseETag/GetBaseETagBy`
- **HTTP 方法:** `POST`
- **描述:** 根据指定的过滤条件，检索电子标签绑定的分页列表。
- **请求体:**
  - `baseETag`: 一个用于过滤的 `BaseETag` 对象。
    - `tag_code` (String, 可选): 用于过滤的电子标签代码。
    - `tag_location` (String, 可选): 用于过滤的电子标签位置。
  - `pageNo` (int): 要检索的页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `BaseETag` 对象分页列表的 `InterReturn` 对象。

### 2. 按位置更新电子标签托盘

- **URL:** `/BaseETag/UpdateETagPalletByLocation`
- **HTTP 方法:** `POST`
- **描述:** 更新特定位置上与电子标签关联的托盘代码。
- **请求体:**
  - `baseETag`: 一个 `BaseETag` 对象。
    - `tag_location` (String, **必需**): 要更新的电子标签的位置。
    - `pallet_code` (String, **必需**): 要与电子标签关联的新托盘代码。
- **响应:**
  - 一个指示更新成功或失败的 `InterReturn` 对象。

### 3. 检查相邻位置是否有托盘

- **URL:** `/BaseETag/IsNextExist`
- **HTTP 方法:** `POST`
- **描述:** 检查一个预定义的相邻位置是否有托盘。位置与其邻居的映射关系在控制器内部是硬编码的。
- **请求体:**
  - `tag_location` (String, 必需): 要检查其邻居的位置。
- **响应:**
  - 一个包含相邻位置托盘的 `pallet_code` 的字符串。如果未找到托盘，则返回空字符串或 null。
