# BaseMaterialController

`BaseMaterialController` 是一个 Spring REST 控制器，用于管理仓库中物料的主数据。它提供了查询、添加和更新物料信息的端点。

## API 端点

### 1. 获取物料信息 (模糊搜索)

- **URL:** `/BaseMaterial/GetMaterialByFuzzy`
- **HTTP 方法:** `POST`
- **描述:** 使用物料名称的模糊匹配来检索物料的分页列表。
- **请求体:**
  - `baseMaterial`: 一个用于过滤的 `BaseMaterial` 对象。
    - `material_code` (String, 可选): 用于过滤的物料代码。
    - `org_material_code` (String, 可选): 用于过滤的原始物料代码。
    - `material_name` (String, 可选): 要搜索的物料名称 (模糊匹配)。
    - `status` (String, 可选): 用于过滤的状态。
  - `pageNo` (int): 页码。
  - `pageSize` (int): 每页的项目数。
  - `startTime` (String): 创建日期范围的开始时间。
  - `endTime` (String): 创建日期范围的结束时间。
- **响应:**
  - 一个包含 `BaseMaterial` 对象分页列表的 `InterReturn` 对象。

### 2. 获取物料信息 (精确搜索)

- **URL:** `/BaseMaterial/GetMaterialBy`
- **HTTP 方法:** `POST`
- **描述:** 使用物料名称的精确匹配来检索物料的分页列表。
- **请求体:**
  - `baseMaterial`: 一个用于过滤的 `BaseMaterial` 对象。
    - `material_code` (String, 可选): 用于过滤的物料代码。
    - `material_name` (String, 可选): 要搜索的物料名称 (精确匹配)。
    - `status` (String, 可选): 用于过滤的状态。
  - `pageNo` (int): 页码。
  - `pageSize` (int): 每页的项目数。
  - `startTime` (String): 创建日期范围的开始时间。
  - `endTime` (String): 创建日期范围的结束时间。
- **响应:**
  - 一个包含 `BaseMaterial` 对象分页列表的 `InterReturn` 对象。

### 3. 添加单个物料

- **URL:** `/BaseMaterial/AddMaterial`
- **HTTP 方法:** `POST`
- **描述:** 向系统中添加一个新物料。
- **请求体:**
  - `baseMaterial`: 一个包含新物料详情的 `BaseMaterial` 对象。
    - `material_code` (String, **必需**): 新物料的唯一代码。
    - `material_name` (String, **必需**): 新物料的名称。
- **响应:**
  - 一个指示操作成功或失败的 `InterReturn` 对象。

### 4. 批量添加物料

- **URL:** `/BaseMaterial/AddMaterialBatch`
- **HTTP 方法:** `POST`
- **描述:** 在单个批处理操作中向系统添加多个新物料。
- **请求体:**
  - 一个 `BaseMaterial` 对象列表。列表中的每个对象都需要：
    - `material_code` (String, **必需**)
    - `material_name` (String, **必需**)
    - `org_material_code` (String, **必需**)
- **响应:**
  - 一个指示批处理操作成功或失败的 `InterReturn` 对象。

### 5. 更新物料信息

- **URL:** `/BaseMaterial/UpdateMaterial`
- **HTTP 方法:** `POST`
- **描述:** 更新由其 `material_code` 标识的现有物料的信息。
- **请求体:**
  - `baseMaterial`: 一个包含更新字段的 `BaseMaterial` 对象。
    - `material_code` (String, **必需**): 要更新的物料代码。
    - 其他字段是可选的，如果提供将会被更新。
- **响应:**
  - 一个指示更新操作成功或失败的 `InterReturn` 对象。
