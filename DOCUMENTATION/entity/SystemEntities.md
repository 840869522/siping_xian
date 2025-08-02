# 系统实体

本文档描述了 `com.example.wmsmp.entity.system` 包中的实体，这些实体与系统管理、用户管理和 API 响应有关。

---

### `InterReturn` & `ResReturn`

- **描述:** 这些不是数据库实体，而是用于标准化 API 响应的数据传输对象 (DTO)。
- **`InterReturn`**: 这是所有 API 响应的主包装器。
  - `message` (String): 关于操作结果的人类可读消息。
  - `status` (boolean): `true` 表示成功，`false` 表示失败。
  - `timestamp` (Date): 响应生成的时间。
  - `result` (Object): 响应的实际有效载荷，通常是一个 `ResReturn` 对象。
- **`ResReturn`**: 此对象通常嵌套在 `InterReturn` 内部，用于携带分页数据。
  - `pageNo` (int): 当前页码。
  - `pageSize` (int): 每页的项目数。
  - `totalCount` (int): 可用的总记录数。
  - `totalPage` (int): 总页数。
  - `Anything` (Object): 当前页的数据记录列表。

---

### `SysDictionary`

- **表名:** `sys_dictionary`
- **描述:** 代表系统数据字典中的一个条目。数据字典用于通过一致、集中管理的值来填充 UI 元素，如下拉列表。
- **关键字段:**
  - `dic_name`: 此字典条目适用的表或上下文的名称。
  - `dic_type`: 字典条目的类型或类别。
  - `dic_display_name`: 一组字典条目的高级别名称，供前端用于请求特定下拉列表的所有值。
  - `dic_display_value`: 在 UI 中向用户显示的文本。
  - `dic_value`: 为所选选项存储在数据库中的实际整数值。

---

### `SysRole`

- **表名:** `sys_role`
- **描述:** 一个多用途实体，既代表用户角色，也代表该角色 UI 中的单个菜单项。数据以扁平列表形式存储，但由 `SysRoleController` 组装成树形结构。
- **关键字段:**
  - `role_id`: 条目的唯一 ID。该 ID 具有特定结构，其中前两位通常代表角色组。
  - `role_name`: 此菜单项所属角色的名称（例如，“P库管理员”）。
  - `title`: 菜单项的显示文本。
  - `component`: 要为此菜单项渲染的前端组件的名称。
  - `parent_id`: 父菜单项的 `role_id`，用于构建树形结构。`parent_id` 为 0 表示根级菜单。
  - `children` (List<SysRole>): 一个非数据库字段，用于在构建树后保存子菜单项。

---

### `SysUser`

- **表名:** `sys_users`
- **描述:** 代表一个系统用户账户。
- **关键字段:**
  - `login_name`: 用于登录的唯一用户名（主键）。
  - `user_name`: 用户的完整显示名称。
  - `password`: 用户的密码。它被标记为 `@TableField(select = false)` 以防止其被包含在常规查询结果中。
  - `role_name`: 分配给用户的角色名称。这可以是一个逗号分隔的列表。
  - `status`: 用户账户的状态（例如，1 表示活动，2 表示禁用）。
