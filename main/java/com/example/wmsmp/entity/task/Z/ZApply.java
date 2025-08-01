package com.example.wmsmp.entity.task.Z;

import lombok.Data;

@Data
public class ZApply {
    //入库申请入参
    String uuid;
    String barcode;
    String start_dest;
    String end_dest;
    String task_type;
    String create_time;

    Integer lowHigh;//1为1-7层，2为8-18层
}
