# SysUserController

`SysUserController` 是一个 Spring REST 控制器，用于处理用户账户管理，包括关键的登录过程。

## API 端点

### 1. 测试后端可用性

- **URL:** `/User/AAATestHello`
- **HTTP 方法:** `POST`
- **描述:** 一个简单的测试端点，用于验证后端应用程序是否正在运行。它返回一个包含应用程序版本、数据库 URL 和当前服务器时间的字符串。
- **响应:** 一个包含系统信息的字符串。

### 2. 获取系统用户

- **URL:** `/User/GetAllSysUserBy`
- **HTTP 方法:** `POST`
- **描述:** 根据过滤条件检索系统用户的分页列表。
- **请求体:**
  - `sysUser`: 一个用于过滤的 `SysUser` 对象。
  - `pageNo` (int), `pageSize` (int)。
- **响应:**
  - 一个包含 `SysUser` 对象分页列表的 `InterReturn` 对象。

### 3. 用户登录

- **URL:** `/User/Login`
- **HTTP 方法:** `POST`
- **描述:** 验证用户的凭据。它会检查 `login_name` 和 `password`。对于非管理员用户，它还会验证用户分配的角色是否允许访问所选的仓库类型 (`storeClassifyValue`)。
- **请求体:**
  - `sysUser`: 一个包含 `login_name` 和 `password` 的 `SysUser` 对象。
  - `storeClassifyValue` (int): 一个代表所选仓库类型的字典值。
- **响应:**
  - 如果登录成功，则返回一个包含用户详细信息的 `InterReturn` 对象；如果失败，则返回错误消息。

### 4. 添加新用户

- **URL:** `/User/AddSysUser`
- **HTTP 方法:** `POST`
- **描述:** 在系统中创建一个新用户账户。
- **请求体:**
  - `sysUser`: 包含新用户所有详细信息的 `SysUser` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 5. 删除用户

- **URL:** `/User/DelSysUserByLoginName`
- **HTTP 方法:** `POST`
- **描述:** 根据登录名从系统中删除一个用户账户。
- **请求体:**
  - `sysUser`: 一个包含要删除的 `login_name` 的 `SysUser` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。

### 6. 更新用户

- **URL:** `/User/UpdateSysUserByLoginName`
- **HTTP 方法:** `POST`
- **描述:** 更新现有用户的信息，包括其显示名称、密码、分配的角色和状态。
- **请求体:**
  - `sysUser`: 一个包含 `login_name` 和要更新字段的 `SysUser` 对象。
- **响应:**
  - 一个指示结果的 `InterReturn` 对象。
