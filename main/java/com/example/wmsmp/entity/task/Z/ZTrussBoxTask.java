package com.example.wmsmp.entity.task.Z;

import lombok.Data;

@Data
public class ZTrussBoxTask {
    //下发桁架码垛任务
    String wave_no;//波次号
    String material_code;//物料编码
    String material_name;//物料名称
    String batch;//批次
    String box_count;//本波次总数
    String L4;//箱子信息1
    String H0;//箱子信息2
}
