package com.example.wmsmp.entity.task.Z;

import lombok.Data;

@Data
public class ZTaskReport {

    //上报任务状态接口入参
    String task_id;
    String barcode;
    String task_state;
    String task_state_desc;
    String task_type;
    String create_time;


}
