# AgvLocationMapService

`AgvLocationMapService` 为 `t_agv_location_map` 表提供数据库操作，该表管理 AGV 可达位置与其中可能包含的托盘之间的关系。

## 实现

该服务是 MyBatis-Plus 框架中 `IService` 接口的标准实现。

- **`AgvLocationMapService.java`**: 服务接口，继承自 `IService<AgvLocationMap>`。
- **`AgvLocationMapServiceImpl.java`**: 实现类，继承自 `ServiceImpl<AgvLocationMapMapper, AgvLocationMap>`。

## 业务逻辑

该服务**不**包含任何自定义业务逻辑。它完全依赖于 `MyBatis-Plus ServiceImpl` 类提供的通用方法。这些方法提供了一套全面的标准 CRUD（创建、读取、更新、删除）和批量操作，供 `AgvLocationMapController` 使用。

例如，控制器从此服务中使用 `page()`、`update()` 和 `getOne()` 等方法，所有这些方法都继承自 `ServiceImpl`。
