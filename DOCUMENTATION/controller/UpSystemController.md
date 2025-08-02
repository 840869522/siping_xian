# UpSystemController

`UpSystemController` 是一个专用于与“上游系统”集成的 Spring REST 控制器，该系统很可能是 ERP、MRP 或其他高级计划系统。其目的是将仓库操作的完成情况报告回这个主系统。

## API 端点

### 1. 向上一级系统报告已完成的订单

- **URL:** `/InOrder/Back2UpSystem`
- **HTTP 方法:** `POST`
- **描述:** 此端点负责将已完成的入库或出库订单的最终详细信息发送回上游系统。
- **逻辑:**
  1.  它接收一个订单号 (`danjuhao`) 和一个类型 ("入库" 或 "出库")。
  2.  它从 WMS 数据库中检索指定订单的所有明细行。
  3.  它将这些数据格式化为上游系统所需的特定 `ReturnBack` 数据结构。
  4.  它通过 HTTP POST 请求将这些数据发送到一个预先配置的 URL（从 `application.properties` 读取）。
  5.  如果上游系统返回“成功”消息，此端点会将 WMS 订单的状态更新为“已回传”，以防止重复发送。
- **参数:**
  - `danjuhao` (String): 要报告的订单号。
  - `type` (String): 订单类型，"入库" 或 "出库"。
- **响应:**
  - 一个 `InterReturn` 对象，指示报告过程的成功或失败。
