# AgvTaskService

`AgvTaskService` 为 `t_agv_task` 表提供数据库操作，该表存储分配给 AGV（自动导引车）系统的任务。

## 实现

该服务是 MyBatis-Plus 框架中 `IService` 接口的标准实现。

- **`AgvTaskService.java`**: 服务接口，继承自 `IService<AgvTask>`。
- **`AgvTaskServiceImpl.java`**: 实现类，继承自 `ServiceImpl<AgvTaskMapper, AgvTask>`。

## 业务逻辑

该服务**不**包含任何自定义业务逻辑。它完全依赖于 `MyBatis-Plus ServiceImpl` 类提供的通用方法。这些方法提供了一套全面的标准 CRUD（创建、读取、更新、删除）和批量操作，供 `TaskAgvController` 使用。

例如，控制器从此服务中使用 `page()`、`save()` 和 `update()` 等方法，所有这些方法都继承自 `ServiceImpl`。
