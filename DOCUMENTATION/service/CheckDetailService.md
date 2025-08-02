# CheckDetailService

`CheckDetailService` 为管理盘点订单（`CheckDetailBean`）的明细行提供数据库操作和业务逻辑。

## 自定义业务逻辑

该服务包含几个超越标准 `ServiceImpl` 功能的自定义方法。

### 1. `queryPage(CheckDetailBean check, int pageNo, int pageSize)`

- **描述:** 为 `CheckDetailBean` 实体提供标准的分页查询，按 `checkCode` 进行过滤。

### 2. `queryCount(CheckBean check)`

- **描述:** 一个特定的查询，调用 `CheckDetailMapper` 中的自定义方法 (`queryCount`)。
- **目的:** 其主要作用是计算给定 `checkCode` 的盘点订单中**未**处于完成状态的明细行数量。这是 `CheckServiceImpl` 在将会盘点订单头标记为完成之前使用的一个关键验证步骤。

### 3. `queryMaterial(CheckDetailBean checkDetail)`

- **描述:** 该方法首先调用一个自定义的映射器查询 (`queryMaterial`) 来获取与盘点相关的物料列表。
- **逻辑:** 然后，它遍历此列表，并通过从 `BaseMaterial` 表中获取相应的 `material_price` 来丰富每个 `CheckDetailBean` 对象。

### 4. `update2checkCount(CheckDetailBean checkDetailBean)`

- **描述:** 该方法直接调用 `CheckDetailMapper` 中的自定义 `UPDATE` 语句 (`update2checkCount`)。
- **目的:** 它专门用于更新单个盘点明细行的 `checkCount`（实际盘点数量），这是 UI 中盘点过程的核心部分。
