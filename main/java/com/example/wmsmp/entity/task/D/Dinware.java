package com.example.wmsmp.entity.task.D;

import lombok.Data;

@Data
public class Dinware {
    //用于1入库申请
    String applyTime;//申请时间
    String fromPort;//入库口编号 左侧靠近货架AO1 右侧远离货架AO2
    String barCode;//托盘码
    String cargoHeight;//货物高度
    String cargoWeight;//货物重量
}
