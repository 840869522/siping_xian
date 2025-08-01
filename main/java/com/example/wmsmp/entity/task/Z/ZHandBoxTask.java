package com.example.wmsmp.entity.task.Z;

import lombok.Data;

@Data
public class ZHandBoxTask {
    //下发料箱二楼机械手拣选任务
    String task_id;//任务号
    String barcode;//料箱编号
    String dest;//格口位置号
    String all_mun;//抓取数量
    String cell_position;//宫格位置
    String cell_mun;//宫格数
}
