# 模块一：核心框架与流程审计模块

## 模块概述

该模块是整个WMS系统的技术基石和业务审计核心。它不处理具体的入库或出库业务，但为这些业务的正确、高效、可追溯执行提供了底层支持。其主要职责包括：

1.  **自动化业务流水记录**: 通过AOP（面向切面编程）和自定义注解，以非侵入的方式自动捕获关键业务操作（如库存变更），并生成详细的、可供审计的流水日志。
2.  **异步处理**: 利用高性能定时器（时间轮算法）将写日志等非核心I/O操作异步化，避免阻塞主业务线程，从而提升系统响应性能。
3.  **提供HTTP上下文**: 为AOP切面等非Web层组件提供访问当前HTTP请求上下文的能力。

该模块的设计优雅地实现了业务逻辑与横切关注点（如日志、审计）的分离，是系统健壮性和可维护性的关键。

---

## 文件深度分析

### 1. `annotation/FlowAnnotation.java`

*   **文件职责**: 自定义Java注解，作为“标记”使用，用于标识任何需要被记录业务流水的方法。
*   **实现分析**:
    *   `@Target({ElementType.PARAMETER, ElementType.METHOD})`: 指定该注解可以应用于方法或方法参数上。
    *   `@Retention(RetentionPolicy.RUNTIME)`: 确保该注解在运行时依然可用，以便AOP切面能够通过反射读取到它。
*   **关键属性**:
    *   `title()`: 字符串类型，用于定义流水所属的业务模块名称（如“库存盘点”），增强日志可读性。
    *   `businessType()`: `BusinessType`枚举类型，定义了具体的操作类型（增加、减少、移动），是后续进行库存计算和追溯的核心依据。
    *   `isSaveRequestData()`: 布尔开关，决定是否需要捕获并记录触发操作时的请求参数（如物料编码、数量等）。

### 2. `annotation/BusinessType.java`

*   **文件职责**: Java枚举类，用于定义和标准化系统中所有与库存变更相关的业务操作类型。
*   **关键枚举值**:
    *   `ADD(1, "增加")`: 代表任何导致库存增加的操作。
    *   `LOSS(2, "减少")`: 代表任何导致库存减少的操作。
    *   `ADD_AND_LOSS(3, "移动")`: 复合操作，特指库存的库内移动，逻辑上包含源头的“减少”和目标的“增加”。
*   **技术注解**:
    *   `@EnumValue`: MyBatis-Plus注解，指定在持久化到数据库时存储`code`字段的值（1, 2, 3）。
    *   `@JsonValue`: Jackson注解，指定在序列化为JSON时也使用`code`字段的值，确保API与数据库数据格式一致。

### 3. `annotation/FlowAspect.java`

*   **文件职责**: **本模块最核心的文件**。一个AOP切面，作为自动化的业务流水记录器，拦截所有被`@FlowAnnotation`标记的方法。
*   **关键方法分析**:
    *   `flowPointCut()`: 定义AOP的**切点**，通过`@annotation(...)`精确地指定了拦截目标。
    *   `doAfterReturning(JoinPoint joinPoint, Object jsonResult)`: 定义**后置通知**。在被拦截的方法成功执行并返回结果后运行，是记录成功操作流水的主要入口。
    *   `handleLog(...)`: **核心处理逻辑**。
        1.  **成功状态检查**: 解析方法返回的JSON结果，检查其`status`字段。只有当`status`为`true`时才继续，确保只记录成功的操作。
        2.  **创建与填充**: 创建`Flow`实体，从`ServletUtils`获取HTTP请求信息（操作人、URL）并填充。
        3.  **特殊逻辑处理**: 这是最关键的业务分发点。若业务类型为`ADD_AND_LOSS` (移动)，它会**克隆`Flow`对象，生成两条流水记录**：一条`LOSS`类型对应源位置，一条`ADD`类型对应目标位置，精确反映库存移动的本质。
    *   `setRequestValue(JoinPoint joinPoint, Flow flow)`: **请求数据解析器**。
        *   通过`argsArrayToString`将方法参数序列化为JSON，然后从中解析`material_code`, `pallet_code`, `inventory_count`等几十个预定义的关键业务数据，填充到`Flow`对象中。
        *   同时也会从`HttpServletRequest`的`parameterMap`中再次尝试解析，确保能覆盖`@RequestBody`和`@RequestParam`两种传参方式。
        *   最后调用`flowService.queryInventoryCount(flow)`查询操作**之后**的库存数，填充`after_count`字段，使流水信息更完整。
    *   `recordOper(final Flow flow)`: **异步持久化**。
        *   它不直接调用`flowService.save()`，而是通过`TimerUtils.instance().addTask(...)`将保存操作作为一个**异步任务**提交，避免I/O操作阻塞主业务线程。

### 4. `annotation/TimerUtils.java`

*   **文件职责**: 基于Netty `HashedWheelTimer`的高性能异步任务调度器。
*   **实现分析**:
    *   **Singleton模式**: 通过私有构造函数和内部静态类`SingletonHolder`确保全局只有一个实例。
    *   **HashedWheelTimer**: 使用Netty提供的时间轮定时器，添加任务的时间复杂度为O(1)，适合处理大量短时任务。
    *   `addTask(TimerTask task)`: `FlowAspect`实际调用的方法，将保存日志的任务添加到时间轮中，延迟为0，由后台线程在下一个时间刻度立即执行。

### 5. `annotation/ServletUtils.java`

*   **文件职责**: HTTP Servlet请求的工具类，用于在任何地方获取当前线程绑定的`HttpServletRequest`对象。
*   **实现分析**:
    *   `getRequest()`: 核心方法，通过`RequestContextHolder.getRequestAttributes()`从Spring上下文中获取当前请求对象。
*   **项目作用**: `FlowAspect`作为一个AOP切面，无法直接通过方法参数注入`HttpServletRequest`。该工具类解决了这个问题，使其能够访问到当前请求的URL、Header等信息。

### 6. `entity/Flow.java`

*   **文件职责**: 业务流水（Flow）的实体类，映射到数据库`t_flow`表。
*   **关键字段**:
    *   `flow_title`, `flow_type`: 业务标题和操作类型。
    *   `material_code`, `pallet_code`, `cmd_count`: 操作的物料、载具和数量。
    *   `after_count`: 操作**完成之后**，相关库存的最终数量，对于库存追溯至关重要。
    *   `location_code`: 一个用`@TableField(exist = false)`标记的**非持久化字段**。在处理“移动”操作时，被巧妙地用来在内存中临时存放“源头库位”的编码，保持了数据库表结构的简洁。

### 7. `service/FlowService.java` & `mapper/FlowMapper.java`

*   **文件职责**: 标准的Service和Mapper接口，负责`Flow`数据的持久化。
*   **实现分析**:
    *   均继承自MyBatis-Plus的`IService`和`BaseMapper`，自动获得了强大的、标准化的CRUD功能。
    *   `queryInventoryCount(Flow flow)`: 在接口中定义的自定义方法，其SQL实现在`FlowMapper.xml`中，用于查询特定托盘的当前库存总数。`FlowAspect`调用此方法来获取`after_count`，体现了Service层封装特定业务逻辑的能力。