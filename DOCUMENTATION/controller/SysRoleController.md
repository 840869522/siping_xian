# SysRoleController

`SysRoleController` 是一个复杂的 Spring REST 控制器，用于管理系统角色和权限。它处理角色的创建、将菜单项（权限）分配给这些角色，以及为前端 UI 检索菜单结构。

## API 端点

### 1. 获取所有角色名称

- **URL:** `/Role/GetAllRoleName`
- **HTTP 方法:** `POST`
- **描述:** 检索系统中所有角色名称的去重列表。
- **响应:**
  - 一个 `InterReturn` 对象，其中包含一个 `SysRole` 对象列表，每个对象都有一个唯一的 `role_name`。

### 2. 按条件获取所有系统角色

- **URL:** `/Role/GetAllSysRoleBy`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件查询角色及其关联的菜单权限。从数据库获取的扁平列表在返回前会被转换为层次化的树形结构。
- **请求体:**
  - `sysRole`: 一个用于过滤的 `SysRole` 对象。
- **响应:**
  - 一个 `InterReturn` 对象，其中包含角色/权限树的根节点。

### 3. 按角色名称获取菜单 (用于登录)

- **URL:** `/Role/GetSysRoleByRoleName`
- **HTTP 方法:** `POST`
- **描述:** 用户登录后使用的一个关键端点。它根据用户的角色和所选的仓库类型，为用户获取适当的菜单结构。
- **请求体:**
  - `sysRole`: 一个包含 `role_name` 的 `SysRole` 对象。
  - `storeClassifyValue` (int): 一个代表所选仓库类型的字典值。
- **响应:**
  - 一个 `InterReturn` 对象，其中包含用于前端的层次化菜单结构。

### 4. 获取角色的可用仓库名称

- **URL:** `/Role/GetStoreName`
- **HTTP 方法:** `POST`
- **描述:** 根据用户分配的角色名称，确定用户有权访问哪些仓库类型。
- **请求体:**
  - `sysRole`: 一个包含用户 `role_name` 的 `SysRole` 对象。
- **响应:**
  - 一个 `InterReturn` 对象，其中包含可访问仓库的 `SysDictionary` 条目列表。

### 5. 更新角色/权限条目

- **URL:** `/Role/UpdateSysRole`
- **HTTP 方法:** `POST`
- **描述:** 更新单个角色/权限记录的属性（例如，更改菜单标题或图标）。
- **请求体:**
  - `sysRole`: 包含其 `role_id` 和要更新字段的 `SysRole` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 创建新角色和权限

- **URL:** `/Role/AddSysRoleInfo`
- **HTTP 方法:** `POST`
- **描述:** 一个用于创建新角色的复杂端点。它接收一个基本名称（例如，“操作员”）、一个仓库类型列表和一系列期望的菜单项（通过 `component_id`）。然后，它为每种仓库类型生成一整套角色和权限记录（例如，“P库操作员”，“B库操作员”）。
- **请求体:**
  - `checkedKeysAdd` (String[]): 要包含的菜单的 `component_id` 数组。
  - `role_name` (String): 新角色的基本名称。
  - `storage_name` (String[]): 仓库类型值的数组。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 7. 按角色名称获取原始菜单数据

- **URL:** `/Role/GetMenuBysysRoleName`
- **HTTP 方法:** `POST`
- **描述:** 检索与特定 `role_name` 关联的所有 `SysRole` 记录的扁平列表。
- **参数:**
  - `role_name` (String): 要查询的角色名称。
- **响应:**
  - 一个包含 `SysRole` 记录列表的 `InterReturn` 对象。

### 8. 修改角色权限

- **URL:** `/Role/ModifySysRoleInfo`
- **HTTP 方法:** `POST`
- **描述:** 编辑现有角色的权限。它智能地计算新旧权限集（组件ID）之间的差异，并执行必要的数据库插入和删除操作。
- **请求体:**
  - `checkedKeys` (String[][]): 一个二维数组，其中 `[0]` 是旧的 `component_id` 列表，`[1]` 是新的列表。
  - `role_name` (String): 正在修改的角色。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 9. 修改角色状态

- **URL:** `/Role/ModifySysRoleStatus`
- **HTTP 方法:** `POST`
- **描述:** 启用或禁用与给定 `role_name` 关联的所有菜单项。
- **参数:**
  - `role_name` (String): 要修改的角色。
  - `status` (int): 要设置的新状态。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。
