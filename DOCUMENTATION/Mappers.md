# 数据库映射器 (Mappers)

本文档描述了 `main/resources/mapper/` 目录中各种 MyBatis XML 映射文件的用途。虽然许多映射器被 `ServiceImpl` 用于标准的 CRUD 操作，但有几个包含了对应用程序业务逻辑至关重要的自定义 SQL 查询。

---

### `CheckDetailMapper.xml`

- **用途:** 管理盘点订单明细行的数据。
- **自定义查询:**
  - `update4Check` / `update2checkCount`: 自定义的 `UPDATE` 语句，允许对 `t_check_detail` 记录进行精细更新，例如增加 `check_count`（盘点数量）。
  - `queryCount`: 一个专门的 `SELECT` 查询，供 `CheckServiceImpl` 使用，用于计算给定盘点订单的未完成明细行数量，这是一个关键的验证步骤。
  - `queryMaterial`: 一个连接 `t_inventory` 和 `t_location_map` 的查询，用于获取物料的当前总库存，然后在创建新的盘点明细时填充 `inventoryCount` 字段。

---

### `StatisticMapper.xml`

- **用途:** 这是统计和分析仪表板的主要数据源。它包含了大量复杂的聚合查询。
- **自定义查询:**
  - `materialChartOut` / `materialChartIn`: 根据出库或入库订单明细的总数量，获取排名前 7 的物料。
  - `taskCountP`, `taskCountD`, `taskCountX`, `taskCountB`: 一组查询，用于根据类型和时间范围计算相应任务表（`t_task_p`, `t_task_d` 等）中的任务数量。
  - `queryCount`: 计算 `t_location_map` 中已使用、未使用或已禁用的库位数量。
  - `queryInventoryCount`: 按物料名称分组，计算每种物料的总库存量。
  - `pieSuccesP`, `pieSuccesD`, `pieSuccesX`, `pieSuccesB`: 一组按最终 `status` 分组任务的查询，用于为成功/失败率饼图生成数据。

---

### `SysRoleMapper.xml` & `SysUserMapper.xml`

- **用途:** 管理系统角色和用户。
- **自定义查询:**
  - 这两个映射器都包含一个 `searchBy` 查询。
  - 该查询的关键特性是它与 `sys_dictionary` 表执行 `INNER JOIN`。这使得它不仅能检索数字状态码（例如 `1`），还能检索人类可读的显示值（例如 "Active"），然后填充到 `SysUser` 或 `SysRole` 实体的 `status_display_value` 字段中。

---

### `FlowMapper.xml`

- **用途:** 支持操作审计追踪 (`Flow`) 系统。
- **自定义查询:**
  - `queryInventoryCount`: 一个从 `t_inventory` 表计算 `SUM(inventory_count)` 的查询。`FlowAspect` 使用此查询来记录操作前后物料的总库存，从而提供完整的审计日志。
