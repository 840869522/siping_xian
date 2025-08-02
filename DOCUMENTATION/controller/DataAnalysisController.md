# DataAnalysisController

`DataAnalysisController` 是一个 Spring REST 控制器，作为数据分析仪表板（“看板数据分析”）的后端。它提供各种端点以检索用于可视化的统计数据。该控制器充当一个简单的外观（facade），将所有业务逻辑委托给 `StatisticService`。

## API 端点

- **基础 URL:** `/analysis`

所有端点都接受一个包含 `material_storage_area` 键的 JSON 对象，以按特定仓库过滤统计信息。

### 1. 入库/出库任务成功率

- **URL:** `/success/pie`
- **HTTP 方法:** `POST`
- **描述:** 检索适用于表示入库和出库任务成功率的饼图数据。
- **请求体:**
  - `param` (Map<String, Object>): 应包含 `material_storage_area`（仓库名称）。
- **响应:**
  - 一个包含饼图数据的 `InterReturn` 对象。

### 2. 库存物料排名

- **URL:** `/material/ranking`
- **HTTP 方法:** `POST`
- **描述:** 根据库存水平检索物料的排名列表。
- **请求体:**
  - `param` (Map<String, Object>): 应包含 `material_storage_area`。
- **响应:**
  - 一个包含排名列表的 `InterReturn` 对象。

### 3. 库存利用率

- **URL:** `/inventory/use`
- **HTTP 方法:** `POST`
- **描述:** 检索与库存利用率相关的数据。
- **请求体:**
  - `param` (Map<String, Object>): 应包含 `material_storage_area`。
- **响应:**
  - 一个包含利用率数据的 `InterReturn` 对象。

### 4. 周任务执行趋势

- **URL:** `/task/week`
- **HTTP 方法:** `POST`
- **描述:** 检索数据显示过去一周任务执行情况的趋势图。
- **请求体:**
  - `param` (Map<String, Object>): 应包含 `material_storage_area`。
- **响应:**
  - 一个包含周趋势数据的 `InterReturn` 对象。

### 5. 周任务类型分析

- **URL:** `/task/type`
- **HTTP 方法:** `POST`
- **描述:** 检索分析过去一周执行的不同类型任务的数据。
- **请求体:**
  - `param` (Map<String, Object>): 应包含 `material_storage_area`。
- **响应:**
  - 一个包含任务类型分析数据的 `InterReturn` 对象。
