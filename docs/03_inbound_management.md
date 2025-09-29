# 模块三：入库管理模块

## 模块概述

入库管理模块负责处理货物从接收上游系统（如ERP）的入库指令，到最终完成收货、准备上架的全过程。它是一个流程驱动的模块，其设计清晰、严谨，并为操作员和外部系统提供了高效的数据处理能力。

主要职责包括：
1.  **入库单生命周期管理**：提供对入库单从“创建”到“审核”，再到“收货中”直至“已完成”的全生命周期状态管理。
2.  **多渠道任务创建**: 支持WMS操作员手动创建入库单，也支持通过API接口由上游系统批量下发，体现了良好的灵活性和集成性。
3.  **数据完整性校验**: 在流程的关键节点（如提交审核）进行业务规则校验，确保入库任务的完整性和有效性。
4.  **高效数据处理**: 提供了强大的Excel导入导出功能，极大地简化了用户创建和管理复杂入库单的工作量。

该模块是连接外部供应与内部库存的桥梁，是整个仓储作业流程的起点。

---

## 文件深度分析

### 1. `controller/InOrderController.java`

*   **文件职责**: 入库单（`InOrder`）的控制器，负责管理入库任务的创建、查询、状态推进和删除。
*   **关键方法分析**:
    *   `AddInOrder(@RequestBody InOrder inOrder)`: **手动创建入库单**。
        *   调用内部方法`GenerateInOrderNo()`自动生成一个唯一的、带日期和序列号的入库单号（如 "UO202309280001"）。
        *   将新创建的单据初始状态设置为“创建中”。
    *   `Adds(@RequestBody List<InOrder> inOrders)`: **批量创建入库单（上游系统集成）**。
        *   这是一个为外部系统设计的高性能接口，使用MyBatis的`ExecutorType.BATCH`模式进行批量插入，避免了逐条插入的低效。
        *   从上游系统下发的单据，其状态被直接设置为“已审核”，这表明系统信任上游数据，简化了WMS内部的审批流程。
    *   `SubmitInOrder(@RequestBody InOrder inOrder)`: **提交/审核入库单**。
        *   这是驱动入库流程向前的核心操作，用于将单据状态从“创建中”更新为“待审核”或“已审核”。
        *   **核心业务规则**: 在更新状态前，接口会通过`inOrderDetailService.list(...)`检查该入库单下是否已存在明细。如果没有任何明细，提交将被拒绝。这个校验确保了一个“空”的入库任务不会进入到后续的收货环节。
    *   `DelInOrder(@RequestBody InOrder inOrder)`: **删除入库单**。
        *   为了保证数据完整性，此操作是事务性的。它不仅会删除入库单头（`inOrderService.removeById(inOrder)`），还会一并删除其下所有关联的入库单明细（`inOrderDetailService.remove(wrapperIndetail)`），确保数据不会产生孤儿记录。

### 2. `controller/InOrderDetailController.java`

*   **文件职责**: 入库单明细（`InOrderDetail`）的控制器，负责管理一个入库任务中具体的物料项。
*   **关键方法分析**:
    *   `AddInOrderDetail(@RequestBody InOrderDetail inOrderDetail)`: **手动添加入库明细**。
        *   **核心业务规则**: 在添加前，接口会查询其所属的入库单头`InOrder`，并校验其状态必须为“创建中”。这可以防止用户向一个已经审核或完成的单据中添加新的物料。
    *   `Adds(@RequestBody List<InOrderDetail> inOrderDetails)`: **批量添加入库明细（上游系统集成）**。专为外部系统设计的高性能接口，使用MyBatis的`BATCH`模式批量创建明细行。
    *   `GetInOrderExcel(@RequestBody List<InOrder> inOrders)`: **导出为Excel**。
        *   接收一个入库单头列表，查询出这些单头下的所有明细。
        *   将头信息（如仓库名、订单类型）和明细信息（物料编码、数量）**合并到一个专门的`InOrderExcel` DTO（数据传输对象）中**。
        *   返回这个扁平化的列表，前端可以直接用这个列表生成结构清晰的Excel报表，是一种非常优雅的报表数据准备方式。
    *   `ImportInOrderExcel(@RequestBody List<InOrderExcel> inOrderExcels)`: **从Excel导入创建完整的入库单**。
        *   这是一个强大的批量创建功能，被包裹在一个**数据库事务**中（通过`ExecutorType.BATCH`和`sqlSession.commit()`/`rollback()`实现）。
        *   它首先从`inOrderExcels`列表中提取并**去重**所有入库单头信息，批量创建`InOrder`记录。
        *   然后，遍历整个列表，批量创建所有的`InOrderDetail`记录，确保了头和明细要么全部创建成功，要么全部失败。

### 3. `entity/InOrder.java`

*   **文件职责**: 入库单头实体，映射到`t_in_order`表。
*   **关键字段**:
    *   `in_order_id`: 业务主键，由程序生成的字符串ID。
    *   `status`: **核心流程状态字段**。其值的变化（创建中 -> 已审核 -> 收货中 -> 已完成）驱动了整个入库流程的正确流转。
    *   `order_type`: 订单类型，用于区分不同的入库业务场景（如“采购入库”、“退货入库”）。
    *   `org_order`: 原始单号，记录关联的外部系统单据号，是实现跨系统追溯的重要依据。

### 4. `entity/InOrderDetail.java`

*   **文件职责**: 入库单明细实体，映射到`t_in_order_detail`表，定义了入库任务的“物料清单”。
*   **关键字段**:
    *   `in_order_id`: 外键，将明细与主单关联。
    *   `order_count`: **订单数量/计划数量**。收货作业的目标。
    *   `actual_count`: **实际已收货数量**。这是入库流程中最重要的动态字段之一。在`InventoryController`的收货操作中被累加。
    *   **进度跟踪**: 通过比较`actual_count`和`order_count`，系统可以精确地知道每一项物料的收货进度，并支持分批收货等灵活业务。
    *   `status`: **明细行状态**。当`actual_count`达到`order_count`时，此状态会被更新为“已完成”，并成为触发主单状态自动完成的条件。