# FlowService

`FlowService` 为 `t_flow` 表提供数据库操作，该表存储系统的操作审计追踪（“作业流水”）。

## 自定义业务逻辑

该服务包含一个不属于标准 `ServiceImpl` 的自定义方法。

### 1. `queryInventoryCount(Flow flow)`

- **描述:** 此方法是 `FlowMapper` 中一个自定义查询的直接包装器。
- **目的:** 它用于查询特定物料或位置的当前库存数量。此方法很可能在某些操作（如收货或发货）后由 `FlowAspect` 调用，以在 `t_flow` 表中记录操作前后的库存数量，从而创建详细的审计追踪。
