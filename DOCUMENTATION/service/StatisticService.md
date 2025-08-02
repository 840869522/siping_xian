# StatisticService

`StatisticService` 为所有统计和数据分析端点提供业务逻辑。它包含许多查询和聚合来自各种映射器数据的方法，以生成适合图表和仪表板的结构化数据。

## 方法

### 物料和库位统计

- **`materialChart(Map<String, Object> param)`**: 根据物料的移动情况检索其排名列表。根据 `type` 参数（0 表示出库，1 表示入库），它会调用特定的映射器查询（`materialChartOut` 或 `materialChartIn`）。
- **`dayChart(Map<String, Object> param)`**: 通过调用 `StatisticMapper` 中的 `queryInventoryCount` 方法，检索当天物料移动的排名列表。
- **`libraryStatement(Map<String, Object> param)`**: 为显示给定仓库每月任务计数的条形图生成数据。它确定仓库（`P`、`D`、`X` 或 `B`），并为当年的每个月调用 `StatisticMapper` 中相应的 `taskCount` 方法。
- **`materialRanking(Map<String, Object> param)`**: 从库存中获取排名前 5 的物料，并为图表格式化数据。

### 库存和库位统计

- **`pieChart(Map<String, Object> param)`**: 为显示已占用与空置库位比例的饼图生成数据。它调用 `StatisticMapper` 中的 `queryCount` 方法。
- **`inventoryUse(Map<String, Object> param)`**: 创建一个图表数据，显示仓库库位的利用率，将其分为“已使用”、“未使用”和“禁用”三类。

### 任务统计

- **`successPie(Map<String, Object> param)`**: 收集特定仓库（`P`、`D`、`X` 或 `B`）任务成功和失败率的数据，以在饼图中显示。
- **`taskWeek(Map<String, Object> param)`**: 为周趋势图生成数据。它遍历过去 7 天，并从相应的任务映射器查询每天的总任务数。
- **`taskType(Map<String, Object> param)`**: 分析特定仓库在过去一周内各种任务类型的分布情况，为每种类型（例如，“入库”、“出库”、“移库”）提供计数。
