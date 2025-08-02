# BarrierGateController

`BarrierGateController` 是一个 Spring REST 控制器，旨在处理与道闸系统的交互，该系统很可能配备了车牌识别摄像头。

## API 端点

### 1. 处理车辆车牌

- **URL:** `/barrier/gate/car/num`
- **HTTP 方法:** `POST`
- **描述:** 此端点从道闸接收一个车牌号码。然后，它在入库和出库订单中搜索该车辆，以确定其指定的仓库。如果找到匹配的订单，控制器会向 LED 显示屏发送一个命令，以显示车辆的车牌及其目的地仓库。
- **请求体:**
  - 一个包含以下键的 JSON 对象：
    - `plateNo` (String, 必需): 车辆的车牌号码，由道闸的摄像头捕获。
- **响应:**
  - 一个包含以下字段的 JSON 对象：
    - `status` (boolean): 如果车辆被识别并且 LED 命令已发送，则为 `true`，否则为 `false`。
    - `msg` (String): 一个指示操作结果的消息。例如，“接受成功!” 或 “不属于WMS管理车辆!”。
