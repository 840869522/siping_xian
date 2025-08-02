# SysDictionaryController

`SysDictionaryController` 是一个 Spring REST 控制器，负责管理系统的数据字典。数据字典是一个键值存储，用于为整个应用程序的下拉列表和其他 UI 元素提供一致、标准化的值。

## API 端点

### 1. 获取所有字典条目

- **URL:** `/Dictionary/GetAllSysDictionary`
- **HTTP 方法:** `POST`
- **描述:** 通过分页检索数据字典中的所有条目。它包含一个基于 `dic_type` 移除重复条目的功能。
- **请求体:**
  - `resReturnIn`: 一个包含 `pageNo` 和 `pageSize` 的 `ResReturn` 对象。
  - `isDedup` (boolean): 如果为 `true`，结果将为每个唯一的 `dic_type` 只包含一个条目。
- **响应:**
  - 一个包含 `SysDictionary` 对象分页列表的 `InterReturn` 对象。

### 2. 按条件获取字典条目

- **URL:** `/Dictionary/GetAllSysDictionaryBy`
- **HTTP 方法:** `POST`
- **描述:** 使用各种过滤条件和分页查询数据字典。
- **请求体:**
  - `sysDictionary`: 一个用于过滤的 `SysDictionary` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `SysDictionary` 对象分页列表的 `InterReturn` 对象。

### 3. 添加字典条目

- **URL:** `/Dictionary/AddSysDictionary`
- **HTTP 方法:** `POST`
- **描述:** 向数据字典中添加一个新条目。
- **请求体:**
  - `sysDictionary`: 要添加的 `SysDictionary` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 4. 删除字典条目

- **URL:** `/Dictionary/DelSysDictionaryByDicId`
- **HTTP 方法:** `POST`
- **描述:** 按其唯一的 `dic_id` 删除一个字典条目。
- **请求体:**
  - `sysDictionary`: 一个包含要删除的 `dic_id` 的 `SysDictionary` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 更新字典条目

- **URL:** `/Dictionary/UpdateSysDictionaryByDicId`
- **HTTP 方法:** `POST`
- **描述:** 更新由其 `dic_id` 标识的现有字典条目。
- **请求体:**
  - `sysDictionary`: 一个包含 `dic_id` 和要更新字段的 `SysDictionary` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 获取下拉列表值

- **URL:** `/Dictionary/GetDropDownList`
- **HTTP 方法:** `POST`
- **描述:** 这是前端 UI 的一个关键端点。它检索给定字典类型（`dic_display_name`）的所有显示值（`dic_display_value`）。这用于动态填充下拉菜单。
- **参数:**
  - `dicDisplayName` (String): 要检索值的字典的名称/类型（例如，“OrderType”、“WarehouseName”）。
- **响应:**
  - 一个包含下拉选项字符串列表的 `InterReturn` 对象。
