# InvenMaterialLocationmapController

`InvenMaterialLocationmapController` 是一个具有多种职责的 Spring REST 控制器。其主要声明的功能是“库存预警”，但它也包含通用的库存查询功能以及一个为“D库”（一个高密度自动化存储系统）设计的复杂的库位分配算法。

## API 端点

### 1. 获取库存预警状态

- **URL:** `/InvenMaterialLocationmap/GetInvenMaterialLocationmapBy`
- **HTTP 方法:** `POST`
- **描述:** 此端点检索库存记录，并为每个物料动态计算一个过期状态。该状态是通过将当前日期与物料的批次日期及其保质期进行比较来确定的。
- **计算出的状态:**
  - "已过期"
  - "临期"
  - "正常"
- **请求体:**
  - `invenMaterialLocationmap`: 一个用于过滤库存记录的对象。
  - `pageNo` (int), `pageSize` (int), `startTime` (String), `endTime` (String)。
- **响应:**
  - 一个 `InterReturn` 对象，其中包含带有计算状态的库存记录分页列表。

### 2. 通用库存查询

- **URL:** `/InvenMaterialLocationmap/GetBy`
- **HTTP 方法:** `POST`
- **描述:** 一个全面的查询，通过连接多个表（通过 `VMapInv` 视图）来检索详细的库存信息。
- **请求体:**
  - `item`: 一个用于过滤的 `VMapInv` 对象（按仓库、物料、批次、托盘、位置）。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `VMapInv` 记录分页列表的 `InterReturn` 对象。

### 3. 按仓库获取空托盘

- **URL:** `/InvenMaterialLocationmap/GetEmptyPalletByWarehouse`
- **HTTP 方法:** `POST`
- **描述:** 查找并返回指定仓库内所有可用空托盘的分页列表。
- **请求体:**
  - `item`: 一个包含 `warehouse` 名称的 `VMapInv` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含空托盘列表的 `InterReturn` 对象。

### 4. 在 D 仓库中分配存储位置

- **URL:** `/InvenMaterialLocationmap/GetLocationmap`
- **HTTP 方法:** `POST`
- **描述:** 此端点触发一个复杂的算法，为给定托盘在“D库”中找到一个最佳的存储位置。
- **逻辑概述:**
  1.  首先检查托盘是否已包含物料。
  2.  如果包含，它会尝试在具有相同物料和批次的其他托盘附近找到一个空位置（共置）。
  3.  如果托盘是空的或找不到合适的共置位置，则启动搜索新位置。
  4.  该搜索算法通过查询仓库每个级别的活动任务数（`TaskD`）并优先选择活动任务最少的级别来平衡工作负载。
  5.  然后，它在该级别内按照特定模式（例如，最深的库位优先）搜索可用位置。
- **参数:**
  - `pallet_code` (String): 需要位置的托盘代码。
- **响应:**
  - 如果找到合适的位置，则返回一个包含分配的 `LocationMap` 对象的 `InterReturn` 对象。
