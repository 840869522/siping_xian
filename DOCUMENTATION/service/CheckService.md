# CheckService

`CheckService` 为管理盘点订单（`CheckBean`）的头信息提供数据库操作和业务逻辑。

## 自定义业务逻辑

与本项目中的许多其他服务不同，`CheckServiceImpl` 包含了超越标准 `ServiceImpl` 方法的自定义业务逻辑。

### 1. `queryPage(CheckBean check, int pageNo, int pageSize)`

- **描述:** 此方法为 `CheckBean` 实体提供分页查询。它构建一个 `LambdaQueryWrapper` 以按 `checkCode` 和 `warehouseName` 进行过滤。虽然它使用了 `ServiceImpl` 的底层 `page()` 方法，但它封装了查询构建逻辑。

### 2. `checkUpdate(CheckBean check)`

- **描述:** 这是一个事务性方法，处理更新盘点订单状态的业务规则。
- **核心逻辑:**
  - 它在 `CheckBean` 对象上设置 `updateTime`。
  - **验证:** 如果新状态是“已完成”，它会执行一个关键的验证检查。它查询 `checkDetailMapper` 来计算该订单有多少明细行*尚未*完成。如果此计数大于零，它将拒绝更新并返回错误消息。
  - **状态级联:** 如果验证通过（或不需要），它会继续更新 `CheckBean` 头的状态。它还会更新所有关联的 `CheckDetailBean` 行的状态，以匹配新的头状态。
  - 整个操作被包裹在 `@Transactional` 注解中，以确保头表和明细表之间的数据一致性。
