# FlowController

`FlowController` 是一个 Spring REST 控制器，它提供了一个查询操作流水的接口。操作流水充当了仓库系统中所有重要活动的审计追踪或日志。

## API 端点

### 1. 获取操作流水记录

- **URL:** `/Flow/GetFlowBy`
- **HTTP 方法:** `POST`
- **描述:** 根据多种过滤条件检索操作流水记录的分页列表。这允许用户搜索和审查操作历史。
- **请求体:**
  - `flow`: 一个用于过滤的 `Flow` 对象。
    - `flow_title` (String, 可选): 流水事件的标题 (支持部分匹配)。
    - `material_code` (String, 可选): 涉及的物料代码。
    - `material_name` (String, 可选): 物料名称 (支持部分匹配)。
    - `batch` (String, 可选): 批次号。
    - `pallet_code` (String, 可选): 托盘代码。
    - `order_id` (String, 可选): 相关的订单 ID。
    - `startTime` (String, 可选): 用于过滤的创建日期范围的开始时间。
    - `endTime` (String, 可选): 用于过滤的创建日期范围的结束时间。
  - `pageNo` (int): 要检索的页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `Flow` 对象分页列表的 `InterReturn` 对象。

## 内部方法

### AddFlow

- **注意:** 这是控制器内的一个 `public` 方法，但它**没有**作为 REST 端点暴露出来（其 `@PostMapping` 注解已被注释掉）。
- **描述:** 这个方法很可能由应用程序内的其他服务或切面（例如，通过 `FlowAnnotation`）在每次发生被追踪的操作时内部调用，以在操作流水日志中创建新条目。
