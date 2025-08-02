# BaseMaterialService

`BaseMaterialService` 为 `base_material` 表提供数据库操作，该表存储仓库中处理的所有物料的主数据。

## 实现

该服务是 MyBatis-Plus 框架中 `IService` 接口的标准实现。

- **`BaseMaterialService.java`**: 服务接口，继承自 `IService<BaseMaterial>`。
- **`BaseMaterialServiceImpl.java`**: 实现类，继承自 `ServiceImpl<BaseMaterialMapper, BaseMaterial>`。

## 业务逻辑

该服务**不**包含任何自定义业务逻辑。它完全依赖于 `MyBatis-Plus ServiceImpl` 类提供的通用方法。这些方法提供了一套全面的标准 CRUD（创建、读取、更新、删除）和批量操作，供 `BaseMaterialController` 使用。
