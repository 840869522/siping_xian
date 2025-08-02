# 基础主数据实体

本文档描述了 `com.example.wmsmp.entity.base` 包中的实体，这些实体代表了仓库管理系统的核心主数据。

---

### `BaseETag`

- **表名:** `base_e_tag`
- **描述:** 代表电子标签（E-Tag）、工作站的物理位置以及当前在该位置的托盘/料箱之间的绑定关系。
- **关键字段:**
  - `tag_code`: 电子标签硬件的唯一标识符。
  - `tag_location`: 标签的物理位置代码（例如，一个拣货台编号）。
  - `pallet_code`: 当前放置在此位置的托盘或料箱的代码。

---

### `BaseEquipment`

- **表名:** `base_equipment`
- **描述:** 存储仓库中需要维护的设备的主数据。
- **关键字段:**
  - `equipment_code`: 设备的唯一标识符。
  - `equipment_name`: 设备的人类可读名称。
  - `period`: 设备的保养周期（例如，“30天”）。

---

### `BaseEquipmentDetail`

- **表名:** `base_equipment_detail`
- **描述:** 存储设备的保养和维修活动历史。它作为 `BaseEquipment` 实体的日志。
- **关键字段:**
  - `equipment_code`: 关联到 `BaseEquipment` 的外键。
  - `maintenance_date`: 执行保养的日期。
  - `maintenance_content`: 所做工作的描述。
  - `maintenance_person`: 执行工作的人员。

---

### `BaseMaterial`

- **表名:** `base_material`
- **描述:** 代表单个物料（或 SKU）的主数据。它包含一个产品的所有静态属性。
- **关键字段:**
  - `material_code`: 物料的唯一标识符。
  - `material_name`: 人类可读的名称。
  - `material_spec`: 物料的规格。
  - `supplier`: 物料的供应商。
  - `quality`: 物料的保质期（天）。
  - `min_stock`: 此物料的最小期望库存水平。

---

### `BasePallet`

- **表名:** `base_pallet`
- **描述:** 代表用于存放库存的托盘、料箱或其他类型的容器/载具的主数据。
- **关键字段:**
  - `pallet_code`: 托盘的唯一标识符。
  - `isprint`: 一个标志，指示是否已打印此托盘的标签。
  - `status`: 托盘的状态（例如，“可用”，“占用”）。
