# StatisticController

`StatisticController` 是一个 Spring REST 控制器，提供用于库存分析的端点。与 `DataAnalysisController` 类似，它充当一个简单的外观（facade），将数据检索和处理的核心逻辑委托给 `StatisticService`。

## API 端点

- **基础 URL:** `/statistic`

### 1. 入库/出库物料排行榜图表

- **URL:** `/material/chart`
- **HTTP 方法:** `POST`
- **描述:** 根据物料的移动（入库或出库）检索其排名列表。
- **请求体:**
  - `param` (Map<String, Object>): 应包含一个 `type` 键（0 表示出库，1 表示入库）。
- **响应:**
  - 一个包含排名列表数据的 `InterReturn` 对象。

### 2. 每日入库/出库物料排名

- **URL:** `/day/chart`
- **HTTP 方法:** `POST`
- **描述:** 检索当天物料移动的排名列表。
- **请求体:**
  - `param` (Map<String, Object>): 用于参数的通用映射。
- **响应:**
  - 一个包含每日排名列表的 `InterReturn` 对象。

### 3. 入库/出库物料柱状图

- **URL:** `/library/statement`
- **HTTP 方法:** `POST`
- **描述:** 检索格式化为显示入库或出库物料量的柱状图数据。
- **请求体:**
  - `param` (Map<String, Object>): 应包含一个 `type` 键（0 表示出库，1 表示入库）。
- **响应:**
  - 一个包含柱状图数据的 `InterReturn` 对象。

### 4. 库存分析饼状图

- **URL:** `/pie/chart`
- **HTTP 方法:** `POST`
- **描述:** 检索格式化为用于常规库存分析的饼状图数据。
- **请求体:**
  - `param` (Map<String, Object>): 用于参数的通用映射。
- **响应:**
  - 一个包含饼状图数据的 `InterReturn` 对象。
