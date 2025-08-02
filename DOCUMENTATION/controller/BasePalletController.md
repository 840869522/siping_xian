# BasePalletController

`BasePalletController` 是一个 Spring REST 控制器，用于管理仓库内的托盘（或载具）。它提供了查询、创建和更新托盘信息的端点。

## API 端点

### 1. 获取托盘信息 (模糊搜索)

- **URL:** `/BasePallet/GetPalletByFuzzy`
- **HTTP 方法:** `POST`
- **描述:** 使用托盘代码的模糊匹配来检索托盘的分页列表。
- **请求体:**
  - `basePallet`: 一个用于过滤的 `BasePallet` 对象。
    - `pallet_code` (String, 可选): 要搜索的托盘代码 (模糊匹配)。
    - `status` (String, 可选): 用于过滤的托盘状态。
  - `pageNo` (int): 页码。
  - `pageSize` (int): 每页的项目数。
- **响应:**
  - 一个包含 `BasePallet` 对象分页列表的 `InterReturn` 对象。

### 2. 按范围添加托盘

- **URL:** `/BasePallet/AddPalletBetween`
- **HTTP 方法:** `POST`
- **描述:** 在指定范围内创建一系列具有顺序编号代码的新托盘。托盘代码以“P”为前缀，并用前导零填充到 6 位长度（例如，“P000001”）。
- **参数:**
  - `low` (int): 范围的起始数字。
  - `high` (int): 范围的结束数字。
- **响应:**
  - 一个 `InterReturn` 对象，指示序列中最后一个托盘创建的结果。

### 3. 添加单个托盘

- **URL:** `/BasePallet/AddPallet`
- **HTTP 方法:** `POST`
- **描述:** 向系统中添加一个新托盘。在创建之前，它会检查是否已存在具有相同代码的托盘。
- **请求体:**
  - `basePallet`: 一个 `BasePallet` 对象。
    - `pallet_code` (String, **必需**): 新托盘的唯一代码。
- **响应:**
  - 一个指示创建成功或失败的 `InterReturn` 对象。

### 4. 批量更新托盘打印状态

- **URL:** `/BasePallet/UpdatePalletIsPrintByCodes`
- **HTTP 方法:** `POST`
- **描述:** 在单个批处理操作中将多个托盘的打印状态更新为“是”。
- **请求体:**
  - 一个 `BasePallet` 对象列表。每个对象必须包含一个 `pallet_code`。
- **响应:**
  - 一个指示批量更新成功或失败的 `InterReturn` 对象。
